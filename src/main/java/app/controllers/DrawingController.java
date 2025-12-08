package app.controllers;

import app.dto.OrderWithDetailsDTO;
import app.entities.Carport;
import app.entities.Drawing;
import app.exceptions.DatabaseException;
import app.services.*;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class DrawingController
{
    private final DrawingService drawingService;
    private final CalculatorService calculatorService;
    private final OrderService orderService;

    public DrawingController(DrawingService drawingService, CalculatorService calculatorService, OrderService orderService)
    {
        this.drawingService = drawingService;
        this.calculatorService = calculatorService;
        this.orderService = orderService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/drawing", this::showDrawing);
        app.get("/orders/details/{id}/drawing", this::showOrderDrawing);
        app.post("/orders/details/{id}/regenerate-drawing", this::regenerateDrawing);
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