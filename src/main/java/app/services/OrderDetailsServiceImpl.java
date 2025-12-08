package app.services;

import app.entities.Carport;
import app.entities.Material;
import app.entities.MaterialsLine;
import app.exceptions.DatabaseException;
import app.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
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

    @Override
    public boolean addMaterialListToOrder(int orderId, Carport carport) throws DatabaseException
    {
        List<MaterialsLine> materialsLineList = createMaterialList(carport);
        for (MaterialsLine materialsLine : materialsLineList)
        {
            try
            {
                materialsLinesMapper.createMaterialLine(orderId, materialsLine);
            }
            catch (DatabaseException e)
            {
                throw new DatabaseException("Kunne ikke oprette MaterialLine med id og navn: " + materialsLine.getMaterial().getId() + ", " + materialsLine.getMaterial().getName() + " på ordre id " + orderId + ": " + e.getMessage());
            }
        }
        return true;
    }

    @Override
    public void updateMaterialLinePrice(int lineId, double newPrice, int quantity) throws DatabaseException
    {
        materialsLinesMapper.updateMaterialLinePrice(lineId, newPrice, quantity);
    }

    @Override
    public void regenerateMaterialList(int orderId, Carport carport) throws DatabaseException
    {
        boolean materialsDeleted = materialsLinesMapper.deleteAllMaterialLinesByOrderId(orderId);
        if (materialsDeleted)
        {
            addMaterialListToOrder(orderId, carport);
        }
    }

    @Override
    public List<MaterialsLine> createMaterialList(Carport carport) throws DatabaseException
    {
        List<MaterialsLine> materialList = new ArrayList<>();

        /* #######################################################
                               POSTS
         ####################################################### */
        int posts = calculatorService.calculatePosts(carport);
        materialList.add(insertMaterialLine(posts, Constants.POST_MATERIAL_ID));

        /* #######################################################
                             TOP PLATES
         ####################################################### */
        HashMap<Double, Integer> topPlates = calculatorService.calculateTopPlate(carport);
        forEachMaterial(topPlates, materialList, Constants.TOP_PLATE_SHORT, Constants.TOP_PLATE_LONG, Constants.TOP_PLATE_SHORT_MATERIAL_ID, Constants.TOP_PLATE_LONG_MATERIAL_ID);

        /* #######################################################
                          BOLTS & WASHERS
         ####################################################### */
        int bolts = calculatorService.calculateBolts(posts, topPlates);
        materialList.add(insertMaterialLine(bolts, Constants.BOLT_MATERIAL_ID));
        materialList.add(insertMaterialLine(bolts, Constants.WASHER_MATERIAL_ID)); //same amount of washers as bolts

        /* #######################################################
                  CEILING JOISTS & FITTINGS + SCREWS
         ####################################################### */
        HashMap<Double, Integer> ceilingJoists = calculatorService.calculateCeilingJoist(carport);
        forEachMaterial(ceilingJoists, materialList, Constants.CEILING_JOIST_SHORT, Constants.CEILING_JOIST_LONG, Constants.CEILING_JOIST_SHORT_MATERIAL_ID, Constants.CEILING_JOIST_LONG_MATERIAL_ID);

        int ceilingJoistFittings = calculatorService.sumHashMapValues(ceilingJoists); //Needs double amount, 1 right and 1 left fitting for each ceiling joist
        materialList.add(insertMaterialLine(ceilingJoistFittings, Constants.CEILING_JOIST_FITTING_RIGHT_MATERIAL_ID));
        materialList.add(insertMaterialLine(ceilingJoistFittings, Constants.CEILING_JOIST_FITTING_LEFT_MATERIAL_ID));

        int universalScrews = calculatorService.calculateScrewsNeeded(ceilingJoistFittings, Constants.SCREWS_PER_CEILING_JOIST_FITTING);

        /* #######################################################
                            PERFORATED STRIP
         ####################################################### */
        int perforatedStrip = calculatorService.calculatePerforatedStrip(carport);
        materialList.add(insertMaterialLine(perforatedStrip, Constants.PERFORATED_STRIP_MATERIAL_ID));

        /* #######################################################
                      FASCIA BOARDS + WEATHERBOARD/SIDING
         ####################################################### */
        HashMap<Double, Integer> fasciaBoardLength = calculatorService.calculateFasciaBoardLength(carport);
        //Needs 2 mapper calls, one with wide board (back) and one with narrower board (front)
        //Weatherboard, needs same quantity and lengths as fascia board
        addFasciaMaterials(fasciaBoardLength, materialList, false);

        //only need half for the value for mapper call for 1 of the 2 calls
        HashMap<Double, Integer> fasciaBoardWidth = calculatorService.calculateFasciaBoardWidth(carport);
        addFasciaMaterials(fasciaBoardWidth, materialList, true);

        /* #######################################################
                          ROOF PLATES & SCREWS
         ####################################################### */
        HashMap<Double, Integer> roofPlates = calculatorService.calculateRoofPlates(carport);
        forEachMaterial(roofPlates, materialList, Constants.ROOF_PLATE_SHORT, Constants.ROOF_PLATE_LONG, Constants.ROOF_PLATE_SHORT_MATERIAL_ID, Constants.ROOF_PLATE_LONG_MATERIAL_ID);

        int roofPlateScrews = calculatorService.calculateRoofPlateScrews(carport);
        int roofPlateScrewPacks = calculatorService.calculateScrewPacks(Constants.PACK_SIZE_ROOF_PLATE_SCREW, roofPlateScrews);
        materialList.add(insertMaterialLine(roofPlateScrewPacks, Constants.ROOF_PLATE_SCREW_MATERIAL_ID));

        /* #######################################################
                            IF IS WITH SHED
         ####################################################### */
        if (carport.isWithShed())
        {
            HashMap<Double, Integer> blockings = calculatorService.calculateBlocking(carport);
            forEachMaterial(blockings, materialList, Constants.BLOCKING_SHORT, Constants.BLOCKING_LONG, Constants.BLOCKING_SHORT_MATERIAL_ID, Constants.BLOCKING_LONG_MATERIAL_ID);

            int blockingFittings = calculatorService.sumHashMapValues(blockings) * 2; //Need two pr blocking (according to delivered material, 16 blockings and 32 fittings)
            materialList.add(insertMaterialLine(blockingFittings, Constants.BLOCKING_FITTING_MATERIAL_ID));

            int blockingFittingsScrews = calculatorService.calculateScrewsNeeded(blockingFittings, Constants.SCREWS_PER_BLOCKING_FITTING);
            universalScrews += blockingFittingsScrews; //Added later to materialslines (same screws as to ceiling joists)

            int sideBoards = calculatorService.calculateSidingBoard(carport);
            materialList.add(insertMaterialLine(sideBoards, Constants.SIDE_BOARD_MATERIAL_ID));

            int screwsSideBoardsInnerLayer = calculatorService.calculateScrewsNeeded(sideBoards, Constants.SHORT_SCREWS_NEEDED_PER_BOARD);
            int sideBoardsInnerScrewPack = calculatorService.calculateScrewPacks(Constants.PACK_SIZE_SHORT_SCREW, screwsSideBoardsInnerLayer);
            materialList.add(insertMaterialLine(sideBoardsInnerScrewPack, Constants.SCREW_SHORT_MATERIAL_ID));

            int screwsSideBoardsOuterLayer = calculatorService.calculateScrewsNeeded(sideBoards, Constants.LONG_SCREWS_NEEDED_PER_BOARD);
            int sideBoardsOuterScrewPack = calculatorService.calculateScrewPacks(Constants.PACK_SIZE_LONG_SCREW, screwsSideBoardsOuterLayer);
            materialList.add(insertMaterialLine(sideBoardsOuterScrewPack, Constants.SCREW_LONG_MATERIAL_ID));

            //These are always added if with shed, these are for the door, so no calculations needed.
            materialList.add(insertMaterialLine(1, Constants.SHED_DOOR_STRIP_MATERIAL_ID));
            materialList.add(insertMaterialLine(1, Constants.SHED_DOOR_GRIP_MATERIAL_ID));
            materialList.add(insertMaterialLine(2, Constants.SHED_DOOR_HINGE_MATERIAL_ID));
        }

        /* #######################################################
              UNIVERSAL SCREWS (CEILING JOISTS AND BLOCKING)
         ####################################################### */
        int fittingScrewPacks = calculatorService.calculateScrewPacks(Constants.PACK_SIZE_FITTING_SCREW, universalScrews);
        materialList.add(insertMaterialLine(fittingScrewPacks, Constants.SCREW_UNIVERSAL_MATERIAL_ID));

        return materialList;
    }

    private MaterialsLine insertMaterialLine(int quantity, int materialId) throws DatabaseException
    {
        Material material = materialMapper.getMaterialById(materialId);
        return new MaterialsLine(quantity, material.getPrice() * quantity, material);
    }

    private void forEachMaterial(HashMap<Double, Integer> boards, List<MaterialsLine> currentMaterialList, double shortBoard, double longBoard, int shortMaterialId, int longMaterialId) throws DatabaseException
    {
        for (HashMap.Entry<Double, Integer> material : boards.entrySet())
        {
            if (material.getKey().equals(shortBoard))
            {
                currentMaterialList.add(insertMaterialLine(material.getValue(), shortMaterialId));
            }
            else if (material.getKey().equals(longBoard))
            {
                currentMaterialList.add(insertMaterialLine(material.getValue(), longMaterialId));
            }
        }
    }

    private void addFasciaMaterials(HashMap<Double, Integer> fasciaBoards, List<MaterialsLine> materialList, boolean isWidth) throws DatabaseException
    {
        for (HashMap.Entry<Double, Integer> fasciaBoard : fasciaBoards.entrySet())
        {
            if (fasciaBoard.getKey().equals(Constants.FASCIA_BOARD_SHORT))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), Constants.SUB_FASCIA_BOARD_SHORT_MATERIAL_ID));
                int quantity = isWidth ? fasciaBoard.getValue() / 2 : fasciaBoard.getValue();
                materialList.add(insertMaterialLine(quantity, Constants.FASCIA_BOARD_SHORT_MATERIAL_ID));
                materialList.add(insertMaterialLine(quantity, Constants.WEATHER_BOARD_SHORT_MATERIAL_ID));
            }
            else if (fasciaBoard.getKey().equals(Constants.FASCIA_BOARD_LONG))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), Constants.SUB_FASCIA_BOARD_LONG_MATERIAL_ID));
                int quantity = isWidth ? fasciaBoard.getValue() / 2 : fasciaBoard.getValue();
                materialList.add(insertMaterialLine(quantity, Constants.FASCIA_BOARD_LONG_MATERIAL_ID));
                materialList.add(insertMaterialLine(quantity, Constants.WEATHER_BOARD_LONG_MATERIAL_ID));
            }
        }
    }

}
