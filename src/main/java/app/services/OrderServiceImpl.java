package app.services;

import app.dto.OrderWithDetailsDTO;
import app.entities.*;
import app.exceptions.DatabaseException;
import app.persistence.*;
import app.util.Status;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class OrderServiceImpl implements OrderService
{
    private OrderMapper orderMapper;
    private CustomerMapper customerMapper;

    public OrderServiceImpl(OrderMapper orderMapper, CustomerMapper customerMapper)
    {
        this.orderMapper = orderMapper;
        this.customerMapper = customerMapper;

    }

 @Override
 public OrderWithDetailsDTO getOrderwithDetails(int orderId) throws DatabaseException
 {
     return orderMapper.getOrderWithDetailsByIdDTO(orderId);
 }

    @Override
    public List<Order> getAllOrders() throws DatabaseException
    {
        return orderMapper.getAllOrders();
    }

    @Override
    public int createOrder(int drawingId, int carportId, int customerId) throws DatabaseException
    {
        Order order = orderMapper.createOrder(drawingId, carportId, customerId);
        return order != null ? order.getOrderId() : -1;
    }

    @Override
    public boolean deleteOrder(int orderId) throws DatabaseException
    {
        return orderMapper.deleteOrder(orderId);
    }


    @Override
    public void updateOrderStatus(int orderId, Status status) throws DatabaseException
    {
        orderMapper.updateOrderStatus(orderId, status);
    }

    @Override
    public void updateOrderEmployee(int orderId, int employeeId) throws DatabaseException
    {
        if (employeeId != 0)
        {
            orderMapper.updateOrderEmployee(orderId, employeeId);
        }
        else
        {
            orderMapper.setOrderEmployeeNull(orderId);
        }
    }

    @Override
    public void updateOrderDeliveryDate(int orderId, LocalDateTime deliveryDate) throws DatabaseException
    {
        orderMapper.updateOrderDeliveryDate(orderId, deliveryDate);
    }

    @Override
    public void updateCustomerInfo(Customer customer) throws DatabaseException
    {
        customerMapper.updateCustomerInfo(customer);
    }

    @Override
    public void updateOrderTotalPrice(int orderId) throws DatabaseException
    {
        double totalPrice = getOrderwithDetails(orderId).getTotalPrice();
        orderMapper.updateOrderTotalPrice(orderId, totalPrice);
    }

    @Override
    public List<OrderWithDetailsDTO> getOrdersByStatusDTO(Status status) throws DatabaseException
    {
        return orderMapper.getAllOrdersByStatus(status);
    }
}

