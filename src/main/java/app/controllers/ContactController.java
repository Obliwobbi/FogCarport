package app.controllers;

import app.entities.Carport;
import app.entities.Customer;
import app.entities.Drawing;
import app.exceptions.DatabaseException;
import app.services.CarportService;
import app.services.CustomerService;
import app.services.DrawingService;
import app.services.OrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class ContactController
{

    private final CustomerService customerService;
    private final OrderService orderService;
    private final DrawingService drawingService;
    private final CarportService carportService;

    public ContactController(CustomerService customerService, OrderService orderService, DrawingService drawingService, CarportService carportService)
    {
        this.customerService = customerService;
        this.orderService = orderService;
        this.drawingService = drawingService;
        this.carportService = carportService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/contact", this::showContactPage);

        app.post("/contact/create-order", this::handleCreateOrder);

        app.get("/success", this::showSuccessPage);
    }

    private void handleCreateOrder(Context ctx) throws DatabaseException
    {
        Customer customer = null;
        Drawing drawing = null;
        Carport carport = null;

        try
        {
            customer = customerService.registerNewCustomer(
                    ctx.formParam("firstname"),
                    ctx.formParam("lastname"),
                    ctx.formParam("email"),
                    ctx.formParam("phonenumber"),
                    ctx.formParam("street"),
                    ctx.formParam("housenumber"),
                    Integer.parseInt(ctx.formParam("zipcode")),
                    ctx.formParam("city")
            );

            drawing = drawingService.createDrawing(ctx.sessionAttribute("drawing"));
            carport = carportService.createCarport(ctx.sessionAttribute("carport"));

            if (carport == null || drawing == null)
            {
                orderFailure(drawing, carport, customer);
                ctx.attribute("errorMessage", "Ingen carport fundet - gå tilbage og indtast mål");
                ctx.render("contact.html");
                return;
            }

            int carportId = carport.getCarportId();
            int drawingId = drawing.getDrawingId();
            int customerId = customer.getCustomerId();

            int orderId = orderService.createOrder(drawingId, carportId, customerId);

            if (orderId == -1)
            {
                orderFailure(drawing, carport, customer);
                ctx.attribute("errorMessage", "Ordre kunne ikke oprettes");
                ctx.render("contact.html");
                return;
            }

            ctx.sessionAttribute("successOrderId", orderId);
            ctx.sessionAttribute("successCustomer", customer);
            ctx.sessionAttribute("successCarport", carport);

            clearCustomerSession(ctx);

            ctx.redirect("/success");
        }
        catch (DatabaseException e)
        {
            orderFailure(drawing, carport, customer);
            clearCustomerSession(ctx);
            ctx.attribute("errorMessage", "Der opstod en uventet fejl, prøv igen");
            ctx.render("contact.html");
        }
        catch (NumberFormatException e)
        {
            orderFailure(drawing, carport, customer);
            ctx.attribute("errorMessage", "Postnummer skal være et gyldigt tal");
            ctx.render("contact.html");
        }
        catch (IllegalArgumentException e)
        {
            orderFailure(drawing, carport, customer);
            ctx.attribute("errorMessage", e.getMessage());
            ctx.render("contact.html");
        }
        catch (NullPointerException e)
        {
            orderFailure(drawing, carport, customer);
            ctx.attribute("errorMessage", "Udfyld venligtst alle felterne");
            ctx.render("contact.html");
        }
        catch (Exception e)
        {
            orderFailure(drawing, carport, customer);
            clearCustomerSession(ctx);
            ctx.attribute("errorMessage", "Der opstod en uventet fejl");
            ctx.render("contact.html");
        }
    }

    private void showContactPage(Context ctx)
    {
        ctx.render("contact.html");
    }

    private void showSuccessPage(Context ctx)
    {
        Integer orderId = ctx.sessionAttribute("successOrderId");
        Customer customer = ctx.sessionAttribute("successCustomer");
        Carport carport = ctx.sessionAttribute("successCarport");

        if (orderId == null || customer == null || carport == null)
        {
            ctx.redirect("/");
            return;
        }

        ctx.attribute("orderId", orderId);
        ctx.attribute("customer", customer);
        ctx.attribute("carport", carport);
        ctx.render("success.html");
    }

    private void orderFailure(Drawing drawing, Carport carport, Customer customer) throws DatabaseException
    {
        try
        {
            if (drawing != null)
            {
                drawingService.deleteDrawing(drawing.getDrawingId());
            }
            if (carport != null)
            {
                carportService.deleteCarport(carport.getCarportId());
            }
            if (customer != null)
            {
                customerService.deleteCustomer(customer.getCustomerId());
            }
        }
        catch (DatabaseException e)
        {
            //TODO Should probably be logged
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }

    private void clearCustomerSession(Context ctx)
    {
        ctx.sessionAttribute("drawing", null);
        ctx.sessionAttribute("carport", null);
    }
}