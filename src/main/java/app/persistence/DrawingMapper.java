package app.persistence;

import app.entities.Drawing;

public class DrawingMapper {
    private ConnectionPool connectionPool;
    public DrawingMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Drawing getDrawingById(int drawingId)
    {
        return null;
    }
}

