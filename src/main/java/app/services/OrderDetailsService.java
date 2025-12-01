package app.services;

import app.entities.Carport;
import app.entities.MaterialsLine;
import app.entities.Order;
import app.exceptions.DatabaseException;

import java.util.List;

public interface OrderDetailsService
{
    boolean addMaterialListToOrder (Order order, Carport carport) throws DatabaseException;

    List<MaterialsLine> createMaterialList(Carport carport) throws DatabaseException;
}
