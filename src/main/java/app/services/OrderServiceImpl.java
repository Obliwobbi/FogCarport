package app.services;

import app.persistence.OrderMapper;

public class OrderServiceImpl implements OrderService
{
    private OrderMapper orderMapper;

    public OrderServiceImpl(OrderMapper orderMapper)
    {
        this.orderMapper = orderMapper;
    }
}

