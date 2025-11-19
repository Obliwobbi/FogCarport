package app.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class HomeController
{
    public void addRoutes(Javalin app)
    {
        app.get("/", this::showIndex);
    }

    private void showIndex(Context ctx)
    {
        ctx.render("index.html");
    }
}
