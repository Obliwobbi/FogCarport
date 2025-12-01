package app.controllers;

import app.entities.Carport;
import app.services.CarportTopViewSvg;
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

        Carport carport = ctx.sessionAttribute("carport");

        //TODO error Handling of potention nulls
        CarportTopViewSvg carportSvg = new CarportTopViewSvg(carport);

        ctx.attribute("svg", carportSvg.toString());
        ctx.render("drawing.html");
    }


}