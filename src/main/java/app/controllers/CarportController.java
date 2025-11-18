package app.controllers;

import app.exceptions.DatabaseException;
import app.services.CarportService;
import io.javalin.Javalin;
import io.javalin.http.Context;


public class CarportController
{
    private final CarportService carportservice;

    public CarportController(CarportService carportservice)
    {
        this.carportservice = carportservice;
    }

    public void addRoutes(Javalin app)
    {
        app.post("/carport/create-carport", this::createCarport);
    }

    private void createCarport(Context ctx)
    {
        try
        {
            double carportWidth = Double.parseDouble(ctx.formParam("carportWidth"));
            double carportLength = Double.parseDouble(ctx.formParam("carportLength"));
            double carportHeight = Double.parseDouble(ctx.formParam("carportHeight"));
            boolean withShed = ctx.formParam("withShed") != null; //returns true if checked else false
            String customerWishes = ctx.formParam("customerWishes");

            double shedWidth = Double.parseDouble(ctx.formParam("shedWidth"));
            double shedLength = Double.parseDouble(ctx.formParam("shedLength"));

            carportservice.createCarport(carportWidth, carportLength, carportHeight, withShed, shedWidth, shedLength, customerWishes);
            ctx.redirect("/contact");
        }
        catch (NullPointerException | NumberFormatException e)
        {
            ctx.sessionAttribute("carportErrorLabel", "Du skal udfylde alle nødvendige felter");
            ctx.redirect("/carport");
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("carportErrorLAbel", e.getMessage());
            ctx.redirect("/carport");
        }

    }
}

