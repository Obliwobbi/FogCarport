package app.controllers;

import app.dto.OrderWithDetailsDTO;
import app.entities.Carport;
import app.entities.Customer;
import app.entities.Employee;
import app.entities.MaterialsLine;
import app.exceptions.DatabaseException;
import app.services.EmailService;
import app.services.EmployeeService;
import app.services.OrderDetailsService;
import app.services.OrderService;
import app.util.Status;
import io.javalin.Javalin;
import io.javalin.http.Context;
import jakarta.mail.MessagingException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderController
{
    private final OrderService orderService;
    private final OrderDetailsService orderDetailsService;
    private final EmailService emailService;
    private final EmployeeService employeeService;

    public OrderController(OrderService orderService, OrderDetailsService orderDetailsService, EmailService emailService, EmployeeService employeeService)
    {
        this.orderService = orderService;
        this.orderDetailsService = orderDetailsService;
        this.emailService = emailService;
        this.employeeService = employeeService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/login", this::showLogin);
        app.get("/orders", this::showOrders);
        app.get("/orders/details/{id}", this::showOrderDetails);

        app.post("/login", this::authenticateLogin);

        app.post("/orders/delete/{id}", this::deleteOrder);

        app.post("/orders/details/{id}/update-order", this::updateOrderInfo);
        app.post("/orders/details/{id}/update-customer", this::updateCustomerInfo);
        app.post("/orders/details/{id}/update-carport", this::updateCarportInfo);
        app.post("/orders/details/{id}/update-prices", this::updateMaterialPrices);

        app.post("/orders/details/{id}/stykliste", this::generateMaterialList);
        app.post("/orders/details/{id}/regenerate-stykliste", this::regenerateMaterialList);

        app.post("/orders/send-offer/{id}", this::sendOffer);
    }

    private void showOrders(Context ctx)
    {
        try
        {
            String statusFilter = ctx.queryParam("status");

            List<OrderWithDetailsDTO> newOrders = orderService.getOrdersByStatusDTO(Status.NEW);

            List<OrderWithDetailsDTO> filteredOrders;
            if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("ALL"))
            {
                filteredOrders = orderService.getOrdersByStatusDTO(Status.valueOf(statusFilter));
            }
            else
            {
                filteredOrders = new ArrayList<>();
                filteredOrders.addAll(orderService.getOrdersByStatusDTO(Status.PENDING));
                filteredOrders.addAll(orderService.getOrdersByStatusDTO(Status.PAID));
                filteredOrders.addAll(orderService.getOrdersByStatusDTO(Status.IN_TRANSIT));
                filteredOrders.addAll(orderService.getOrdersByStatusDTO(Status.DONE));

            }
            ctx.attribute("newOrders", newOrders);
            ctx.attribute("filteredOrders", filteredOrders);
            ctx.attribute("selectedStatus", statusFilter != null ? statusFilter : "ALL");
            ctx.attribute("allStatuses", Status.values());

            consumeFlash(ctx);
            ctx.render("orders.html");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", "Der opstod en fejl ved hentning af ordrer");
            ctx.attribute("paidOrders", new ArrayList<>());
            ctx.attribute("inTransitOrders", new ArrayList<>());
            ctx.attribute("doneOrders", new ArrayList<>());
            ctx.attribute("newOrders", new ArrayList<>());
            ctx.attribute("pendingOrders", new ArrayList<>());

            consumeFlash(ctx);
            ctx.render("orders.html");
        }
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
                flashError(ctx, "Ordren blev ikke fundet");
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

            // Transfer session messages to context attributes and clear them
            consumeFlash(ctx);
            ctx.render("order-details.html");
        }
        catch (NumberFormatException e)
        {
            flashError(ctx, "Ugyldigt ordre ID");
            ctx.redirect("/orders");
        }
        catch (NullPointerException e)
        {
            flashError(ctx, "Ordren har ikke en materiale liste");
            ctx.redirect("/orders");
        }
        catch (DatabaseException e)
        {
            flashError(ctx, "Der opstod en fejl ved hentning af ordren");
            ctx.redirect("/orders");
        }
    }

    private void deleteOrder(Context ctx)
    {
        String orderIdStr = ctx.pathParam("id");

        try
        {
            int orderId = Integer.parseInt(orderIdStr);
            if (orderService.deleteOrder(orderId))
            {
                flashSuccess(ctx, "Ordren blev slettet");
                ctx.redirect("/orders");
            }
            else
            {
                flashError(ctx, "Kunne ikke slette ordren. Prøv igen senere.");
                ctx.redirect("/orders");
            }
        }
        catch (NumberFormatException e)
        {
            flashError(ctx, "Ugyldigt ordre ID");
            ctx.redirect("/orders");
        }
        catch (DatabaseException e)
        {
            flashError(ctx, "Kunne ikke slette ordren. Prøv igen senere.");
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


            orderService.updateOrderStatus(orderId, Status.valueOf(status));
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

            flashSuccess(ctx, "Ordre information blev opdateret");
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (DatabaseException e)
        {
            flashError(ctx, "Kunne ikke opdatere ordre information. Prøv igen senere.");
            ctx.redirect("/orders/details/" + orderId);
        }
    }

    //TODO move service actions to service layer and validate input
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
            flashSuccess(ctx, "Kunde information blev opdateret");
            ctx.redirect("/orders/details/" + orderId);

        }
        catch (DatabaseException e)
        {
            flashError(ctx, "Kunne ikke opdatere kunde information. Prøv igen senere.");
            ctx.redirect("/orders/details/" + orderId);
        }
    }

    //TODO move service actions to service layer and validate input
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
            flashSuccess(ctx, "Carport information blev opdateret");
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (DatabaseException e)
        {
            flashError(ctx, "Kunne ikke opdatere carport information. Prøv igen senere.");
            ctx.redirect("/orders/details/" + orderId);
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
            orderService.updateOrderTotalPrice(orderId);
            flashSuccess(ctx, "Priser blev opdateret");
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (DatabaseException e)
        {
            flashError(ctx, "Kunne ikke opdatere priser. Prøv igen senere.");
            ctx.redirect("/orders/details/" + orderId);
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
                flashSuccess(ctx, "Materiale listen blev genereret");
                ctx.redirect("/orders/details/" + orderId);
            }
            else
            {
                flashError(ctx, "Materiale listen er allerede genereret for denne ordre");
                ctx.redirect("/orders/details/" + orderId);
            }
        }
        catch (DatabaseException e)
        {
            flashError(ctx, "Kunne ikke oprette materiale liste. Prøv igen senere.");
            ctx.redirect("/orders/details/" + orderId);
        }
    }

    private void regenerateMaterialList(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);

            orderDetailsService.regenerateMaterialList(orderId, order.getCarport());

            flashSuccess(ctx, "Materiale listen blev opdateret");
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (DatabaseException e)
        {
            flashError(ctx, "Kunne ikke opdatere materiale listen. Prøv igen senere.");
            ctx.redirect("/orders/details/" + orderId);
        }
    }

    private void sendOffer(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);
            emailService.sendCarportOffer(order);
            flashSuccess(ctx, "Tilbuddet blev sendt til kunden!");
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (DatabaseException | MessagingException e)
        {
            flashError(ctx, "Kunne ikke sende email til kunden. Prøv igen senere.");
            ctx.redirect("/orders/details/" + orderId);
        }
    }

    private void flashSuccess(Context ctx, String msg)
    {
        ctx.sessionAttribute("successMessage", msg);
        ctx.sessionAttribute("errorMessage", null);
    }

    private void flashError(Context ctx, String msg)
    {
        ctx.sessionAttribute("errorMessage", msg);
        ctx.sessionAttribute("successMessage", null);
    }

    private void consumeFlash(Context ctx)
    {
        String sessionSuccessMessage = ctx.sessionAttribute("successMessage");
        if (sessionSuccessMessage != null)
        {
            ctx.attribute("successMessage", sessionSuccessMessage);
            ctx.sessionAttribute("successMessage", null);
        }

        String sessionErrorMessage = ctx.sessionAttribute("errorMessage");
        if (sessionErrorMessage != null)
        {
            ctx.attribute("errorMessage", sessionErrorMessage);
            ctx.sessionAttribute("errorMessage", null);
        }
    }

}