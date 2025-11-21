package app.controllers;

import app.entities.Customer;
import app.exceptions.DatabaseException;
import app.services.CustomerService;
import app.services.OrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.time.LocalDateTime;

public class ContactController {

    private CustomerService customerService;
    private OrderService orderService;

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
            Customer customer = customerService.registerNewCustomer(
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
            if(carportId == null)
            {
                throw new IllegalArgumentException("Ingen carport fundet - gå tilbage og indtast mål");
            }

            orderService.createOrder(carportId, customer.getCustomerId());

            ctx.sessionAttribute("carportId", null);
            ctx.sessionAttribute("carport", null);

            ctx.sessionAttribute("successMessage", "Kontakt info modtaget - du hører fra os snarest");
            ctx.redirect("/success");
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