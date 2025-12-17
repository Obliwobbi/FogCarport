package app.services;

import app.entities.Carport;
import app.entities.MaterialsLine;
import app.exceptions.DatabaseException;

import java.util.List;

public interface OrderDetailsService
{
    boolean addMaterialListToOrder(int orderId, Carport carport) throws DatabaseException;

    void regenerateMaterialList(int orderId, Carport carport) throws DatabaseException;

    void updateMaterialLinePrice(int lineId, double newPrice, int quantity) throws DatabaseException;

    List<MaterialsLine> createMaterialList(Carport carport) throws DatabaseException;
}