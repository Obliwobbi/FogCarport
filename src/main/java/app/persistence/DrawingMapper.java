package app.persistence;

public class DrawingMapper {
    private ConnectionPool connectionPool;
    public DrawingMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }
}

