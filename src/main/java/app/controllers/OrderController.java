package app.controllers;

import app.dto.OrderWithDetailsDTO;
import app.entities.Carport;
import app.entities.Customer;
import app.entities.Employee;
import app.entities.MaterialsLine;
import app.exceptions.DatabaseException;
import app.services.EmailService;
import app.services.OrderDetailsService;
import app.services.OrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import jakarta.mail.MessagingException;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderController
{
    private final OrderService orderService;
    private final OrderDetailsService orderDetailsService;
    private final EmailService emailService;

    public OrderController(OrderService orderService, OrderDetailsService orderDetailsService, EmailService emailService)
    {
        this.orderService = orderService;
        this.orderDetailsService = orderDetailsService;
        this.emailService = emailService;
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
        app.post("/orders/details/{id}/update-total-price", this::setTotalOrderPrice);

        app.post("/orders/details/{id}/stykliste", this::generateMaterialList);
        app.post("/orders/details/{id}/regenerate-stykliste", this::regenerateMaterialList);

        app.post("/orders/send-offer/{id}", this::sendOffer);
    }

    private void sendOffer(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);
            emailService.sendCarportOffer(order);
            ctx.redirect("/orders/details/" + orderId + "?success=email");
        }
        catch (DatabaseException | MessagingException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("/orders/details/" + orderId + "?error=" + e.getMessage());
        }

    }

    private void showOrderDetails(Context ctx)
    {
        String orderIdString = ctx.pathParam("id");
        String editSection = ctx.queryParam("edit");
        String success = ctx.queryParam("success");
        String error = ctx.queryParam("error");

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
            List<Employee> employees = orderService.getAllEmployees();

            ctx.attribute("order", order);
            ctx.attribute("employees", employees);
            ctx.attribute("materialsLines", materialsLines);
            ctx.attribute("hasMaterialsList", materialsLines != null && !materialsLines.isEmpty());
            ctx.attribute("editSection", editSection);

            if ("email".equals(success))
            {
                ctx.attribute("successMessage", "Tilbuddet blev sendt til kunden!");
            }
            else if ("order".equals(success))
            {
                ctx.attribute("successMessage", "Ordre information opdateret");
            }
            else if ("customer".equals(success))
            {
                ctx.attribute("successMessage", "Kunde information opdateret");
            }
            else if ("carport".equals(success))
            {
                ctx.attribute("successMessage", "Carport information opdateret");
            }
            else if ("prices".equals(success))
            {
                ctx.attribute("successMessage", "Priser opdateret");
            }
            else if ("totalPrice".equals(success))
            {
                ctx.attribute("successMessage", "Total pris blev opdateret");
            }

            if ("email".equals(error))
            {
                ctx.attribute("errorMessage", "Kunne ikke sende email. Prøv igen.");
            }

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
            String employeeIdString = ctx.formParam("employeeId");


            orderService.updateOrderStatus(orderId, status);
            if (deliveryDate != null)
            {
                orderService.updateOrderDeliveryDate(orderId, deliveryDate.atStartOfDay().plusHours(12));
            }
            if (employeeIdString != null && !employeeIdString.isEmpty())
            {
                int employeeId = Integer.parseInt(employeeIdString);
                orderService.updateOrderEmployee(orderId, employeeId);
            }
            else
            {
                orderService.updateOrderEmployee(orderId, 0);
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
            List<MaterialsLine> lines = orderService.getOrderwithDetails(orderId).getMaterialsLines();

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
                    }
                }
            }
            orderService.setOrderTotalPrice(orderId);
            ctx.redirect("/orders/details/" + orderId + "?success=prices");
        }
        catch (DatabaseException e)
        {
            ctx.redirect("/orders/details/" + orderId + "?error=" + e.getMessage());
        }
    }

    private void setTotalOrderPrice(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            String totalPriceString = ctx.formParam("totalPrice");
            if (totalPriceString == null || totalPriceString.isEmpty())
            {
                ctx.redirect("/orders/details/" + orderId + "?error=Ingen pris angivet");
                return;
            }

            double totalPrice = Double.parseDouble(totalPriceString);
            orderService.setOrderTotalPrice(orderId, totalPrice);
            ctx.redirect("/orders/details/" + orderId + "?success=totalPrice");

        }
        catch (NumberFormatException e)
        {
            ctx.redirect("/orders/details/" + orderId + "?error=Ugyldig pris format");
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
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);
            Carport carport = order.getCarport();

            if (orderDetailsService.addMaterialListToOrder(orderId, carport))
            {
                orderService.updateOrderTotalPrice(orderId);
                ctx.attribute("successMessage", "Materiale liste blev genereret");
                ctx.redirect("/orders/details/" + orderId);
            }
            else
            {
                ctx.attribute("errorMessage", "Materialer er allerede generet for denne ordre");
                ctx.redirect("/orders/details/" + orderId);
            }
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