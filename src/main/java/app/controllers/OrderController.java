package app.controllers;

import app.dto.OrderWithDetailsDTO;
import app.entities.Carport;
import app.entities.Customer;
import app.entities.MaterialsLine;
import app.exceptions.DatabaseException;
import app.services.OrderDetailsService;
import app.services.OrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderController
{
    private final OrderService orderService;
    private final OrderDetailsService orderDetailsService;

    public OrderController(OrderService orderService, OrderDetailsService orderDetailsService)
    {
        this.orderService = orderService;
        this.orderDetailsService = orderDetailsService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/orders", this::showOrders);
        app.get("/orders/details/{id}", this::showOrderDetails);

        app.post("/orders/delete/{id}", this::deleteOrder);

        app.post("/orders/details/{id}/update-order", this::updateOrderInfo);
        app.post("/orders/details/{id}/update-customer", this::updateCustomerInfo);
        app.post("/orders/details/{id}/update-carport", this::updateCarportInfo);
        app.post("/orders/details/{id}/update-prices", this::updateMaterialPrices);

        app.post("/orders/details/{id}/stykliste", this::generateMaterialList);
        app.post("/orders/details/{id}/regenerate-stykliste", this::regenerateMaterialList);
    }

    private void showOrderDetails(Context ctx)
    {
        String orderIdString = ctx.pathParam("id");
        String editSection = ctx.queryParam("edit");

        try
        {
            int orderId = Integer.parseInt(orderIdString);
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);
            if (order == null)
            {
                ctx.attribute("errorMessage", "Ordre ikke fundet");
                ctx.redirect("/orders");
                return;
            }
            List<MaterialsLine> materialsLines = order.getMaterialsLines();

            ctx.attribute("order", order);
            ctx.attribute("materialsLines", materialsLines);
            ctx.attribute("hasMaterialsList", materialsLines != null && !materialsLines.isEmpty());
            ctx.attribute("editSection", editSection);

            ctx.render("order-details.html");
        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Ugyldigt ordre ID");
            ctx.redirect("/orders");
        }
        catch (NullPointerException e)
        {
            ctx.attribute("errorMessage", "Ordre har ikke materiale liste");
            ctx.redirect("/orders");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("/orders");
        }
    }

    private void updateOrderInfo(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));
        try
        {
            String status = ctx.formParam("status");
            String deliveryDateString = ctx.formParam("deliveryDate");
            LocalDate deliveryDate = (deliveryDateString != null && !deliveryDateString.isEmpty())
                    ? LocalDate.parse(deliveryDateString) : null;

            orderService.updateOrderStatus(orderId, status);
            if (deliveryDate != null)
            {
                orderService.updateOrderDeliveryDate(orderId, deliveryDate.atStartOfDay().plusHours(12));
            }
            ctx.redirect("/orders/details/" + orderId + "?success=order");
        }
        catch (DatabaseException e)
        {
            ctx.redirect("/orders/details/" + orderId + "?error=" + e.getMessage());
        }
    }

    private void updateCustomerInfo(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);
            Customer customer = order.getCustomer();

            customer.setFirstName(ctx.formParam("firstName"));
            customer.setLastName(ctx.formParam("lastName"));
            customer.setEmail(ctx.formParam("email"));
            customer.setPhone(ctx.formParam("phone"));
            customer.setStreet(ctx.formParam("street"));
            customer.setHouseNumber(ctx.formParam("houseNumber"));
            customer.setZipcode(Integer.parseInt(ctx.formParam("zipcode")));
            customer.setCity(ctx.formParam("city"));

            orderService.updateCustomerInfo(customer);
            ctx.redirect("/orders/details/" + orderId + "?success=customer");

        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("/orders/details/" + orderId + "?error=" + e.getMessage());
        }
    }

    private void updateCarportInfo(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));
        try
        {
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);
            Carport carport = order.getCarport();

            double carportWidth = Double.parseDouble(ctx.formParam("width"));
            double carportLength = Double.parseDouble(ctx.formParam("length"));
            double carportHeight = Double.parseDouble(ctx.formParam("height"));
            boolean withShed = Boolean.parseBoolean(ctx.formParam("withShed"));

            Integer shedWidth = null;
            Integer shedLength = null;
            if (withShed)
            {
                String shedWidthString = ctx.formParam("shedWidth");
                String shedLengthString = ctx.formParam("shedLength");
                shedWidth = (shedWidthString != null && !shedWidthString.isEmpty())
                        ? Integer.parseInt(shedWidthString) : null;
                shedLength = (shedLengthString != null && !shedLengthString.isEmpty())
                        ? Integer.parseInt(shedLengthString) : null;
            }
            String customerWishes = ctx.formParam("customerWishes");

            carport.setWidth(carportWidth);
            carport.setLength(carportLength);
            carport.setHeight(carportHeight);
            carport.setWithShed(withShed);
            if (carport.isWithShed())
            {
                carport.setShedWidth(shedWidth);
                carport.setShedLength(shedLength);
            }
            carport.setCustomerWishes(customerWishes);

            orderService.updateCarport(carport);
            ctx.redirect("/orders/details/" + orderId + "?success=carport");
        }
        catch (DatabaseException e)
        {
            ctx.redirect("/orders/details/" + orderId + "?error=" + e.getMessage());
        }
    }

    private void updateMaterialPrices(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);
            List<MaterialsLine> lines = order.getMaterialsLines();

            double totalPrice = 0;

            for (int i = 0; i < lines.size(); i++)
            {
                String lineIdParam = ctx.formParam("lineIds[" + i + "]");
                String priceParam = ctx.formParam("prices[" + i + "]");

                if (lineIdParam != null && priceParam != null)
                {
                    int lineId = Integer.parseInt(lineIdParam);
                    double newPrice = Double.parseDouble(priceParam);

                    MaterialsLine line = lines.stream()
                            .filter(l -> l.getLineId() == lineId)
                            .findFirst()
                            .orElse(null);

                    if (line != null)
                    {
                        orderDetailsService.updateMaterialLinePrice(lineId, newPrice, line.getQuantity());
                        totalPrice += newPrice * line.getQuantity();
                    }
                }
            }

            orderService.updateOrderTotalPrice(orderId, totalPrice);
            ctx.redirect("/orders/details/" + orderId + "?success=prices");
        }
        catch (DatabaseException e)
        {
            ctx.redirect("/orders/details/" + orderId + "?error=" + e.getMessage());
        }
    }

    private void generateMaterialList(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            OrderWithDetailsDTO order = new OrderWithDetailsDTO();
            try
            {
                order = orderService.getOrderwithDetails(orderId);
            }
            catch (DatabaseException e)
            {
                ctx.attribute("errorMessage", "Ugyldigt ordre ID: " + orderId);
            }
            List<MaterialsLine> existingMaterialsLines = order.getMaterialsLines();
            if (existingMaterialsLines != null && !existingMaterialsLines.isEmpty())
            {
                ctx.attribute("errorMessage", "Materialer er allerede generet for denne ordre");
                ctx.redirect("/orders/details/" + orderId);
                return;
            }
            Carport carport = order.getCarport();
            orderDetailsService.addMaterialListToOrder(orderId, carport);
            //TODO: insert order total price so it is set on material list creation


            ctx.attribute("successMessage", "Materiale liste blev genereret");
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke oprette materiale liste: " + e.getMessage());
        }
    }

    private void regenerateMaterialList(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);

            orderDetailsService.regenerateMaterialList(orderId, order.getCarport());
            //TODO: insert order total price so it is set on material list creation (db is not updated yet)

            ctx.redirect("/orders/details/" + orderId + "?success=carport");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke regenerere materiale liste");
            ctx.redirect("/orders/details/" + orderId + "?error=" + e.getMessage());
        }
    }

    private void showOrders(Context ctx)
    {
        try
        {
            List<OrderWithDetailsDTO> newOrders = orderService.getOrdersByStatusDTO("NY ORDRE");
            List<OrderWithDetailsDTO> pendingOrders = orderService.getOrdersByStatusDTO("AFVENTER ACCEPT");
            List<OrderWithDetailsDTO> paidOrders = orderService.getOrdersByStatusDTO("BETALT");
            List<OrderWithDetailsDTO> inTransitOrders = orderService.getOrdersByStatusDTO("AFSENDT");
            List<OrderWithDetailsDTO> doneOrders = orderService.getOrdersByStatusDTO("AFSLUTTET");

            ctx.attribute("newOrders", newOrders);
            ctx.attribute("pendingOrders", pendingOrders);
            ctx.attribute("paidOrders", paidOrders);
            ctx.attribute("inTransitOrders", inTransitOrders);
            ctx.attribute("doneOrders", doneOrders);

            ctx.render("orders.html");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("orderErrorMessage", e.getMessage());
            ctx.attribute("paidOrders", new ArrayList<>());
            ctx.attribute("inTransitOrders", new ArrayList<>());
            ctx.attribute("doneOrders", new ArrayList<>());
            ctx.attribute("newOrders", new ArrayList<>());
            ctx.attribute("pendingOrders", new ArrayList<>());

            ctx.render("orders.html");
        }
    }

    private void deleteOrder(Context ctx) throws DatabaseException
    {
        String orderIdStr = ctx.pathParam("id");

        try
        {
            int orderId = Integer.parseInt(orderIdStr);
            if (orderService.deleteOrder(orderId))
            {
                ctx.redirect("/orders");
                ctx.attribute("successMessage", "Du har slette ordren med id " + orderId);
            }
            else
            {
                ctx.attribute("errorMessage", "Kunne ikke slette ordren");
                ctx.redirect("/orders");
            }
        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke parse tallet");
            ctx.redirect("/orders");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("/orders");
        }
    }
}