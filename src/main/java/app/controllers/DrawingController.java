package app.controllers;

import app.services.SvgServiceImpl;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Locale;


public class DrawingController
{
    public void addRoutes(Javalin app)
    {
        app.get("/drawing", this::showDrawing);
    }

    private void showDrawing(Context ctx)
    {
        Locale.setDefault(new Locale("US"));

        SvgServiceImpl carportSvg = new SvgServiceImpl(0, 0, "0 0 855 690", "100%", "auto");

        String style = "stroke-width:5px; stroke: black; fill: white;";
        String dashedLine = "stroke-width:5px; stroke: black; stroke-dasharray:5 5;";
        String arrow = "marker-start: url(#beginArrow); marker-end: url(#endArrow);";

        carportSvg.addRectangle(0, 0, 600, 780, style);
        carportSvg.addLine(50,50,500,700,dashedLine);
        carportSvg.addArrow(20,20,600,700,style + arrow);

        ctx.attribute("svg", carportSvg.toString());
        ctx.render("drawing.html");
    }


}