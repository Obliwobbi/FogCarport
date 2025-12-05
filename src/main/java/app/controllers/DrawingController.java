package app.controllers;

import app.entities.Carport;
import app.entities.Drawing;
import app.services.*;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class DrawingController
{
    private final CalculatorService calculatorService;
    private final DrawingService drawingService;

    public DrawingController(DrawingService drawingService, CalculatorService calculatorService)
    {
        this.calculatorService = calculatorService;
        this.drawingService = drawingService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/drawing", this::showDrawing);
    }

    private void showDrawing(Context ctx)
    {
        try
        {
            Carport carport = ctx.sessionAttribute("carport");

            String carportTopView = drawingService.showDrawing(carport, calculatorService);

            Drawing tmpDrawing = new Drawing(carportTopView);

            ctx.attribute("svg", carportTopView);
            ctx.sessionAttribute("drawing", tmpDrawing);

            ctx.render("drawing.html");
        }
        catch (NullPointerException e)
        {
            ctx.redirect("/carport");
        }
    }
}