package app.persistence;

public class OrderMapper
{
    private final ConnectionPool connectionPool;

    public OrderMapper(ConnectionPool connectionPool)
    {

        this.connectionPool = connectionPool;
    }
}

