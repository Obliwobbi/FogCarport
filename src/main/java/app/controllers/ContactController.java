package app.controllers;

import app.exceptions.DatabaseException;
import app.services.CustomerService;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class ContactController {

    private CustomerService customerService;

    public ContactController(CustomerService customerService)
    {
        this.customerService = customerService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/contact", ctx -> showContactPage(ctx));
        app.post("/contact", ctx -> handleCreateCustomer(ctx));
    }

    private void handleCreateCustomer(Context ctx)
    {
        try
        {
            customerService.registerNewCustomer(
                    ctx.formParam("firstname"),
                    ctx.formParam("lastname"),
                    ctx.formParam("email"),
                    ctx.formParam("phonenumber"),
                    ctx.formParam("street"),
                    ctx.formParam("housenumber"),
                    Integer.parseInt(ctx.formParam("zipcode")),
                    ctx.formParam("city")
            );
        }
        catch (DatabaseException e)
            {
                ctx.attribute("errorMessage", e.getMessage() + "fejl ved indlæsning af kunde info");
                ctx.render("contact.html");
            }
        catch (IllegalArgumentException e)
            {
                ctx.attribute("errorMessage", e.getMessage());
                ctx.render("contact.html");
            }
    }

    private void showContactPage (Context ctx)
    {
        ctx.render("contact");
    }

}