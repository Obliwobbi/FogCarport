package app.controllers;

import app.entities.Carport;
import app.services.CarportService;
import app.util.Constants;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class CarportController
{
    private final CarportService carportService;

    public CarportController(CarportService carportService)
    {
        this.carportService = carportService;
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
            double carportWidth = carportService.validateMeasurementInput(Double.parseDouble(ctx.formParam("carportWidth")), Constants.MIN_CARPORT_WIDTH, Constants.MAX_CARPORT_WIDTH);
            double carportLength = carportService.validateMeasurementInput(Double.parseDouble(ctx.formParam("carportLength")), Constants.MIN_CARPORT_LENGTH, Constants.MAX_CARPORT_LENGTH);
            double carportHeight = Double.parseDouble(ctx.formParam("carportHeight"));
            boolean withShed = ctx.formParam("withShed") != null; //returns true if checked else false


            String customerWishes = carportService.validateStringInput(ctx.formParam("customerWishes"));

            double shedWidth = 0.0;
            double shedLength = 0.0;

            if (withShed)
            {
                shedWidth = carportService.validateMeasurementInput(Double.parseDouble(ctx.formParam("shedWidth")),Constants.MIN_SHED_WIDTH, Constants.MAX_SHED_WIDTH);
                shedLength = carportService.validateMeasurementInput(Double.parseDouble(ctx.formParam("shedLength")), Constants.MIN_SHED_LENGTH, Constants.MAX_SHED_LENGTH);
                shedWidth = carportService.validateShedMeasurement(carportWidth, shedWidth);
                shedLength = carportService.validateShedMeasurement(carportLength, shedLength);
                carportService.validateShedTotalSize(carportLength, carportWidth, shedLength, shedWidth);
            }

            Carport tmpCarport = new Carport(carportWidth, carportLength, carportHeight, withShed, shedWidth, shedLength, customerWishes);

            ctx.sessionAttribute("carportErrorLabel", null);
            ctx.sessionAttribute("carport", tmpCarport);
            ctx.redirect("/drawing");
        }
        catch (NullPointerException | NumberFormatException e)
        {
            ctx.sessionAttribute("carportErrorLabel", "Du skal udfylde alle nødvendige felter");
            ctx.redirect("/carport");
        }
        catch (IllegalArgumentException e)
        {
            ctx.sessionAttribute("carportErrorLabel", e.getMessage());
            ctx.redirect("/carport");
        }
    }
}