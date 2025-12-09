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
    private EmployeeMapper employeeMapper;
    private MaterialsLinesMapper materialsLinesMapper;

    public OrderServiceImpl(OrderMapper orderMapper, CarportMapper carportMapper, DrawingMapper drawingMapper, CustomerMapper customerMapper, EmployeeMapper employeeMapper, MaterialsLinesMapper materialsLinesMapper)
    {
        this.orderMapper = orderMapper;
        this.carportMapper = carportMapper;
        this.drawingMapper = drawingMapper;
        this.customerMapper = customerMapper;
        this.employeeMapper = employeeMapper;
        this.materialsLinesMapper = materialsLinesMapper;
    }

    @Override
    public OrderWithDetailsDTO getOrderwithDetails(int orderId) throws DatabaseException
    {
        Order order = orderMapper.getOrderById(orderId);
        Drawing drawing = drawingMapper.getDrawingById(order.getDrawingId());
        Carport carport = carportMapper.getCarportById(order.getCarportId());
        Customer customer = customerMapper.getCustomerByID(order.getCustomerId());
        Employee employee = employeeMapper.getEmployeeById(order.getEmployeeId());
        List<MaterialsLine> materialsLines = materialsLinesMapper.getMaterialLinesByOrderId(orderId);

        return new OrderWithDetailsDTO(orderId,
                order.getOrderDate(),
                order.getStatus(),
                order.getDeliveryDate(),
                drawing,
                materialsLines,
                carport,
                customer,
                employee);
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
    public List<Employee> getAllEmployees() throws DatabaseException
    {
        return employeeMapper.getAllEmployees();
    }

    @Override
    public void updateOrderStatus(int orderId, String status) throws DatabaseException
    {
        orderMapper.updateOrderStatus(orderId, status);
    }

    @Override
    public void updateOrderEmployee(int orderId, int employeeId) throws DatabaseException
    {
        if(employeeId != 0)
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
    public void updateCarport(Carport carport) throws DatabaseException
    {
        carportMapper.updateCarport(carport);
    }

    @Override
    public void updateOrderTotalPrice(int orderId) throws DatabaseException
    {
        double totalPrice = getOrderwithDetails(orderId).getTotalPrice();
        orderMapper.updateOrderTotalPrice(orderId, totalPrice);
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

