package app.services;

import app.entities.Carport;
import app.entities.MaterialsLine;
import app.persistence.*;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailsServiceImpl implements OrderDetailsService
{
    private final CalculatorService calculatorService;
    private final MaterialsLinesMapper materialsLinesMapper;
    private final MaterialMapper materialMapper;

    public OrderDetailsServiceImpl(CalculatorService calculatorService, MaterialsLinesMapper materialsLinesMapper, MaterialMapper materialMapper)
    {
        this.calculatorService = calculatorService;
        this.materialsLinesMapper = materialsLinesMapper;
        this.materialMapper = materialMapper;
    }

    public List<MaterialsLine> createMaterialList(Carport carport)
    {
        List<MaterialsLine> materialList = new ArrayList<>();

        //Get all materials in a list
        //





        return materialList;
    }

}
