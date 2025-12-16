package app.controllers;

import app.entities.Carport;
import app.services.CarportService;
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
            Carport validatedCarport = carportService.validateAndBuildCarport(
                    null, // Creating new Carport
                    Double.parseDouble(ctx.formParam("carportWidth")),
                    Double.parseDouble(ctx.formParam("carportLength")),
                    Double.parseDouble(ctx.formParam("carportHeight")),
                    ctx.formParam("withShed") != null,
                    carportService.parseDouble(ctx.formParam("shedWidth")),
                    carportService.parseDouble(ctx.formParam("shedLength")),
                    ctx.formParam("customerWishes")
            );

            ctx.sessionAttribute("carportErrorLabel", null);
            ctx.sessionAttribute("carport", validatedCarport);
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
