package app.controllers;

import app.dto.OrderWithDetailsDTO;
import app.entities.Carport;
import app.entities.Customer;
import app.entities.Employee;
import app.entities.MaterialsLine;
import app.exceptions.DatabaseException;
import app.exceptions.EmailException;
import app.services.*;
import app.util.Status;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderController
{
    private final OrderService orderService;
    private final OrderDetailsService orderDetailsService;
    private final EmailService emailService;
    private final EmployeeService employeeService;
    private final CarportService carportService;
    private final CustomerService customerService;

    public OrderController(OrderService orderService, OrderDetailsService orderDetailsService, EmailService emailService, EmployeeService employeeService, CarportService carportService, CustomerService customerService)
    {
        this.orderService = orderService;
        this.orderDetailsService = orderDetailsService;
        this.emailService = emailService;
        this.employeeService = employeeService;
        this.carportService = carportService;
        this.customerService = customerService;
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

    private void showLogin(Context ctx)
    {
        ctx.render("login");
    }

    private void authenticateLogin(Context ctx)
    {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        try
        {
            Employee employee = employeeService.authenticateEmployee(email, password);
            ctx.sessionAttribute("currentEmployee", employee);
            ctx.redirect("/orders");
        }
        catch (DatabaseException e)
        {
            flashError(ctx, "Forkert email eller password");
            ctx.render("login");
        }
    }

    private void showOrders(Context ctx)
    {
        if (!orderService.requireEmployee(ctx)) return;

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
        if (!orderService.requireEmployee(ctx)) return;

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
            List<Employee> employees = employeeService.getAllEmployees();

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
        if (!orderService.requireEmployee(ctx)) return;

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
        if (!orderService.requireEmployee(ctx)) return;

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

    private void updateCustomerInfo(Context ctx)
    {
        if (!orderService.requireEmployee(ctx)) return;

        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);

            Customer validatedCustomer = customerService.validateCustomer(
                    order.getCustomer(),
                    ctx.formParam("firstName"),
                    ctx.formParam("lastName"),
                    ctx.formParam("email"),
                    ctx.formParam("phone"),
                    ctx.formParam("street"),
                    ctx.formParam("houseNumber"),
                    Integer.parseInt(ctx.formParam("zipcode")),
                    ctx.formParam("city")
            );

            customerService.updateCustomerInfo(validatedCustomer);
            flashSuccess(ctx, "Kunde information blev opdateret");
            ctx.redirect("/orders/details/" + orderId);

        }
        catch (DatabaseException e)
        {
            flashError(ctx, "Kunne ikke opdatere kunde information. Prøv igen senere.");
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (IllegalArgumentException e)
        {
            flashError(ctx, e.getMessage());
            ctx.redirect("/orders/details/" + orderId);
        }
    }

    private void updateCarportInfo(Context ctx)
    {
        if (!orderService.requireEmployee(ctx)) return;

        int orderId = Integer.parseInt(ctx.pathParam("id"));
        try
        {
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);

            Carport validatedCarport = carportService.validateAndBuildCarport(
                    order.getCarport(),
                    carportService.parseOptionalDouble(ctx.formParam("width")),
                    carportService.parseOptionalDouble(ctx.formParam("length")),
                    carportService.parseOptionalDouble(ctx.formParam("height")),
                    Boolean.parseBoolean(ctx.formParam("withShed")),
                    carportService.parseOptionalDouble(ctx.formParam("shedWidth")),
                    carportService.parseOptionalDouble(ctx.formParam("shedLength")),
                    ctx.formParam("customerWishes")
            );

            carportService.updateCarport(validatedCarport);
            flashSuccess(ctx, "Carport information blev opdateret");
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (DatabaseException e)
        {
            flashError(ctx, "Kunne ikke opdatere carport information. Prøv igen senere.");
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (IllegalArgumentException e)
        {
            flashError(ctx, e.getMessage());
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (NullPointerException e)
        {
            flashError(ctx, "Alle felter skal være udfyldt.");
            ctx.redirect("/orders/details/" + orderId);
        }
    }


    private void updateMaterialPrices(Context ctx)
    {
        if (!orderService.requireEmployee(ctx)) return;

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
        if (!orderService.requireEmployee(ctx)) return;

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
        if (!orderService.requireEmployee(ctx)) return;

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

    //TODO: sendOffer ved aflevering
//
//    private void sendOffer(Context ctx)
//    {
//        if (!orderService.requireEmployee(ctx)) return;
//
//        int orderId = Integer.parseInt(ctx.pathParam("id"));
//
//        try
//        {
//            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);
//            emailService.sendCarportOffer(order);
//            flashSuccess(ctx, "Tilbuddet blev sendt til kunden!");
//            ctx.redirect("/orders/details/" + orderId);
//        }
//        catch (DatabaseException | MessagingException | UnsupportedEncodingException e)
//        {
//            flashError(ctx, "Kunne ikke sende email til kunden. Prøv igen senere.");
//            ctx.redirect("/orders/details/" + orderId);
//        }
//    }

    private void sendOffer(Context ctx)
    {
        if (!orderService.requireEmployee(ctx)) return;

        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);
            emailService.sendCarportOffer(order);

            flashSuccess(ctx, "Tilbuddet blev sendt til kunden!");
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (DatabaseException e)
        {
            //logger.severe("Database fejl ved hentning af ordre #" + orderId + ": " + e.getMessage());
            flashError(ctx, "Systemfejl - kontakt IT support");
            ctx.redirect("/orders/details/" + orderId);
        }
        catch (EmailException e)
        {
            //logger.warning("Email fejl (type: " + e.getErrorType() + "): " + e.getMessage());
            flashError(ctx, e.getErrorType().getUserMessage());
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