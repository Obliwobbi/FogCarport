package app.services;

import app.dto.OrderWithDetailsDTO;
import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class OrderServiceImpl implements OrderService
{
    private OrderMapper orderMapper;
    private CarportMapper carportMapper;
    private DrawingMapper drawingMapper;
    private CustomerMapper customerMapper;
    private MaterialsLinesMapper materialsLinesMapper;

    public OrderServiceImpl(OrderMapper orderMapper, CarportMapper carportMapper, DrawingMapper drawingMapper, CustomerMapper customerMapper)
    {
        this.orderMapper = orderMapper;
        this.carportMapper = carportMapper;
        this.drawingMapper = drawingMapper;
        this.customerMapper = customerMapper;
    }

    @Override
    public OrderWithDetailsDTO getOrderwithDetails(int orderId) throws DatabaseException
    {
        Order order = orderMapper.getOrderById(orderId);
        Drawing drawing = drawingMapper.getDrawingById(order.getDrawingId());
        Carport carport = carportMapper.getCarportById(order.getCarportId());
        Customer customer = customerMapper.getCustomerByID(order.getCustomerId());

        return new OrderWithDetailsDTO(orderId,
                order.getOrderDate(),
                order.getStatus(),
                order.getDeliveryDate(),
                drawing,
                order.getMaterialLines(),
                carport,
                customer);
    }

    @Override
    public List<Order> getAllOrders() throws DatabaseException
    {
        return orderMapper.getAllOrders();
    }

    @Override
    public Order createOrder(LocalDateTime orderDate, String status, LocalDateTime deliveryDate, Integer drawingId, int carportId, int customerId) throws DatabaseException
    {
        return orderMapper.createOrder(orderDate, status, deliveryDate, drawingId, carportId, customerId);
    }

    @Override
    public boolean createOrder(int carportId, int customerId) throws DatabaseException
    {
        LocalDateTime orderDate = LocalDateTime.now();
        String status = "NY ORDRE";
        LocalDateTime deliveryDate = LocalDateTime.now().plusYears(1);
        Integer drawingId = null;

        orderMapper.createOrder(orderDate, status, deliveryDate, drawingId, carportId, customerId);
        return true;
    }

    @Override
    public boolean deleteOrder(int orderId) throws DatabaseException
    {
        return orderMapper.deleteOrder(orderId);
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
    public List<OrderWithDetailsDTO> getOrdersByStatusDTO(String status) throws DatabaseException
    {
        return orderMapper.getAllOrdersByStatus(status);
    }
}

