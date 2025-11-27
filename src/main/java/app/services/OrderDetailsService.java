package app.services;

import app.entities.Carport;
import app.entities.MaterialsLine;

import java.util.List;

public interface OrderDetailsService
{
    List<MaterialsLine> createMaterialList(Carport carport);
}
