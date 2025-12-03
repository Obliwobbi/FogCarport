package app.controllers;

import app.entities.Carport;
import app.services.*;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Locale;


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
        Locale.setDefault(new Locale("US"));

        Carport carport = ctx.sessionAttribute("carport");

        SvgService svgService = new SvgServiceImpl(0, 0, String.format("0 0 %.1f %.1f", (carport.getLength()), carport.getWidth()), "100%", "auto");

        CarportTopViewSvg carportSvg = new CarportTopViewSvg(carport, calculatorService, svgService);

        ctx.attribute("svg", carportSvg.createMeasuredCarportSvg());
        ctx.render("drawing.html");
    }


}