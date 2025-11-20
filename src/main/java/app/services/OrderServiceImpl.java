package app.services;

import app.entities.BillOfMaterials;
import app.persistence.*;

public class OrderServiceImpl implements OrderService
{
    private OrderMapper orderMapper;
    private CarportMapper carportMapper;
    private BillOfMaterialsMapper billOfMaterialsMapper;
    private DrawingMapper drawingMapper;
    private CustomerMapper customerMapper;

    public OrderServiceImpl(OrderMapper orderMapper, CarportMapper carportMapper, BillOfMaterialsMapper billOfMaterialsMapper, DrawingMapper drawingMapper, CustomerMapper customerMapper)
    {
        this.orderMapper = orderMapper;
        this.carportMapper = carportMapper;
        this.billOfMaterialsMapper = billOfMaterialsMapper;
        this.drawingMapper = drawingMapper;
        this.customerMapper = customerMapper;
    }
}

