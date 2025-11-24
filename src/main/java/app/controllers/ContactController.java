package app.controllers;

import app.entities.Customer;
import app.exceptions.DatabaseException;
import app.services.CustomerService;
import app.services.OrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.time.LocalDateTime;

public class ContactController
{

    private CustomerService customerService;
    private OrderService orderService;

    public ContactController(CustomerService customerService, OrderService orderService)
    {
        this.customerService = customerService;
        this.orderService = orderService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/contact", ctx -> showContactPage(ctx));

        app.post("/contact", ctx -> handleCreateCustomer(ctx));
    }

    private void handleCreateCustomer(Context ctx) throws DatabaseException
    {
        Customer customer = null;
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

            Integer carportId = ctx.sessionAttribute("carportId");
            if (carportId == null)
            {
                ctx.attribute("errorMessage","Ingen carport fundet - gå tilbage og indtast mål");
                customerService.deleteCustomer(customer.getCustomerId());
                ctx.render("contact.html");
            }

            boolean orderSucces = orderService.createOrder(carportId, customer.getCustomerId());

            if (!orderSucces)
            {
                customerService.deleteCustomer(customer.getCustomerId());
                ctx.attribute("errorMessage","Ordre kunne ikke oprettes");
                ctx.render("contact.html");
                return;
            }

            ctx.sessionAttribute("successMessage", "Kontakt info modtaget - du hører fra os snarest");
            ctx.redirect("/success");
        }
        catch (DatabaseException e)
        {
            if (customer != null)
            {
                customerService.deleteCustomer(customer.getCustomerId());
            }
            ctx.attribute("errorMessage", e.getMessage() + "fejl ved indlæsning af kunde info");
            ctx.render("contact.html");
        }
        catch (IllegalArgumentException e)
        {
            if (customer != null)
            {
                customerService.deleteCustomer(customer.getCustomerId());
            }
            ctx.attribute("errorMessage", e.getMessage());
            ctx.render("contact.html");
        }
        catch (Exception e)
        {
            if (customer != null)
            {
                customerService.deleteCustomer(customer.getCustomerId());
            }
            ctx.attribute("errorMessage", "Der opstod en uventet fejl");
            ctx.render("contact.html");
        }
    }

    private void showContactPage(Context ctx)
    {
        ctx.render("contact");
    }

}