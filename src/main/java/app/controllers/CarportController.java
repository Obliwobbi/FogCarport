package app.controllers;

import app.entities.Carport;
import app.exceptions.DatabaseException;
import app.services.CarportService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.HashMap;


public class CarportController
{
    private final CarportService carportservice;

    public CarportController(CarportService carportservice)
    {
        this.carportservice = carportservice;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/carport", this::showCarport);
        app.post("/carport/create-carport", this::createCarport);
    }

    private void showCarport(Context ctx)
    {
        ctx.render("carport.html");
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

            double shedWidth = 0.0;
            double shedLength = 0.0;

            if (withShed)
            {
                shedWidth = Double.parseDouble(ctx.formParam("shedWidth"));
                shedLength = Double.parseDouble(ctx.formParam("shedLength"));
                shedWidth = carportservice.validateShedMeasurement(carportWidth, shedWidth);
                shedLength = carportservice.validateShedMeasurement(carportLength, shedLength);
                carportservice.validateShedTotalSize(carportLength, carportWidth, shedLength, shedWidth);
            }

            Carport carport = carportservice.createCarport(carportWidth, carportLength, carportHeight, withShed, shedWidth, shedLength, customerWishes);

            ctx.sessionAttribute("carportErrorLabel", null);
            ctx.sessionAttribute("carportId", carport.getCarportId());
            ctx.sessionAttribute("carport", carport);
            ctx.redirect("/drawing");
        }
        catch (NullPointerException | NumberFormatException e)
        {
            ctx.sessionAttribute("carportErrorLabel", "Du skal udfylde alle nødvendige felter");
            ctx.redirect("/carport");
        }
        catch (DatabaseException | IllegalArgumentException e)
        {
            ctx.sessionAttribute("carportErrorLabel", e.getMessage());
            ctx.redirect("/carport");
        }

    }
}

