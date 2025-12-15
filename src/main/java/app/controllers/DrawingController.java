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

    private void showOrderDrawing(Context ctx)
    {
        try
        {
            int orderId = Integer.parseInt(ctx.pathParam("id"));
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);

            String svgData;
            if (order.getDrawing() == null)
            {
                svgData = drawingService.showDrawing(order.getCarport(), calculatorService);
            }
            else
            {
                svgData = order.getDrawing().getDrawingData();
            }

            ctx.attribute("svg", svgData);
            ctx.attribute("orderId", orderId);
            ctx.attribute("isOrderView", true);
            ctx.render("drawing.html");
        }
        catch (DatabaseException e)
        {
            ctx.redirect("/orders?error=" + e.getMessage());
        }
    }

    private void regenerateDrawing(Context ctx)
    {
        int orderId = Integer.parseInt(ctx.pathParam("id"));

        try
        {
            OrderWithDetailsDTO order = orderService.getOrderwithDetails(orderId);

            String newSvg = drawingService.showDrawing(order.getCarport(), calculatorService);
            Drawing updatedDrawing = new Drawing(order.getDrawing().getDrawingId(), newSvg);

            drawingService.updateDrawing(updatedDrawing);

            ctx.sessionAttribute("successMessage", null);
            ctx.sessionAttribute("successMessage", "Tegning opdateret");
            ctx.redirect("/orders/details/" + orderId +  "/drawing");
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", "Tegning blev ikke odpateret");
            ctx.redirect("/orders/details/" + orderId);
        }
    }
}