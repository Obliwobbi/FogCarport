package app.services;

import app.dto.OrderWithDetailsDTO;
import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public OrderWithDetailsDTO getOrderwithDetails(int orderId) throws DatabaseException
    {
        Order order = orderMapper.getOrderById(orderId);
        Drawing drawing = drawingMapper.getDrawingById(order.getDrawingId());
        Carport carport = carportMapper.getCarportById(order.getCarportId());
        BillOfMaterials bOM = billOfMaterialsMapper.getBillOfMaterialsById(order.getBillOfMaterialsId());
        Customer customer = customerMapper.getCustomerByID(order.getCustomerId());

        return new OrderWithDetailsDTO(orderId,
                order.getOrderDate(),
                order.getStatus(),
                order.getDeliveryDate(),
                drawing,
                carport,
                bOM,
                customer);
    }

    @Override
    public List<Order> getAllOrders() throws DatabaseException
    {
        return orderMapper.getAllOrders();
    }

    @Override
    public Order createOrder(LocalDateTime orderDate, String status, LocalDateTime deliveryDate, Integer drawingId, int carportId, Integer billOfMaterialsId, int customerId) throws DatabaseException
    {
        return orderMapper.createOrder(orderDate, status, deliveryDate, drawingId, carportId, billOfMaterialsId, customerId);
    }

    @Override
    public void deleteOrder(int orderId) throws DatabaseException
    {
        orderMapper.deleteOrder(orderId);
    }

    @Override
    public void updateOrderStatus(int orderId, String status) throws DatabaseException
    {
        orderMapper.updateOrderStatus(orderId, status);
    }

    @Override
    public void updateOrderDeliveryDate(int orderId, LocalDateTime deliveryDate) throws DatabaseException
    {
        orderMapper.updateOrderDeliveryDate(orderId, deliveryDate);
    }

    @Override
    public void updateOrderBillOfMaterials(int orderId, int billOfMaterialsId) throws DatabaseException
    {
        orderMapper.updateOrderBillOfMaterials(orderId, billOfMaterialsId);
    }

    @Override
    public void updateCarport(Carport carport) throws DatabaseException
    {
        carportMapper.updateCarport(carport);
    }

    @Override
    public List<Order> getOrdersByStatus(String status) throws DatabaseException
    {
        return orderMapper.getAllOrders().stream()
                .filter(order -> order.getStatus().equals(status))
                .sorted(Comparator.comparing(Order::getOrderDate))
                .collect(Collectors.toList());
    }
}

