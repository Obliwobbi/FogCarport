package app.services;

import app.entities.Carport;
import app.entities.Material;
import app.entities.MaterialsLine;
import app.entities.Order;
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

    int postMaterialId = 12; //MaterialID '11' is taken from database to match with post material

    double shortTopPlateBoard = 480.0;
    int shortTopPlateBoardMaterialId = 9;
    double longTopPlateBoard = 600.0;
    int longTopPlateBoardMaterialId = 8;

    int boltMaterialId = 22; //MaterialId '22' is for bolts 10x120mm
    int washerMaterialId = 23;

    double shortCeilingJoist = 480.0;
    int shortCeilingJoistMaterialId = 10; //MaterialId '10' is for long ceiling joist (600)
    double longCeilingJoist = 600.0;
    int longCeilingJoistMaterialId = 11; //MaterialId '10' is for long ceiling joist (600)
    int rightCeilingJoistFittingMaterialId = 20; //MaterialId '19' is for right fitting
    int leftCeilingJoistFittingMaterialId = 21; //MaterialId '20' is for left fitting
    int screwsPerCeilingJoistFitting = 9;  //according to documentation, 9 screws per ceiling joist fitting

    int perforatedStripMaterialId = 19;

    double shortFasciaBoard = 360.0;
    int shortFasciaBoardMaterialId = 3;
    int shortSubFasciaBoardMaterialId = 1;
    int shortWeatherBoardMaterialId = 15;

    double longFasciaBoard = 540.0;
    int longFasciaBoardMaterialId = 4;
    int longSubFasciaBoardMaterialId = 2;
    int longWeatherBoardMaterialId = 14;

    double shortRoofPlate = 360.0;
    int shortRoofPlateMaterialId = 17;
    double longRoofPlate = 600.0;
    int longRoofPlateMaterialId = 16;

    int packSizeRoofPlateScrew = 200; //According to documentation
    int roofPlateScrewMaterialId = 18;

    double shortBlocking = 240.0;
    int shortBlockingMaterialId = 7;
    double longBlocking = 270.0;
    int longBlockingMaterialId = 6;

    int blockingFittingMaterialId = 30;
    int screwsPerBlockingFitting = 4;  //according to documentation

    int sideBoardMaterialId = 13;

    int packSizeShortScrew = 300; //according to documentation
    int shortScrewsNeededPerBoard = 3; //according to documentation
    int shortScrewMaterialId = 25;

    int packSizeLongScrew = 400; //according to documentation
    int longScrewsNeededPerBoard = 6; //according to documentation
    int longScrewMaterialId = 27;

    int shedDoorStripMaterialId = 5;
    int shedDoorGripMaterialId = 28;
    int shedDoorTHingeMaterialId = 29;

    int packSizeFittingScrew = 250; //according to documentation
    int universalScrewsMaterialId = 24;

    public OrderDetailsServiceImpl(CalculatorService calculatorService, MaterialsLinesMapper materialsLinesMapper, MaterialMapper materialMapper)
    {
        this.calculatorService = calculatorService;
        this.materialsLinesMapper = materialsLinesMapper;
        this.materialMapper = materialMapper;
    }

    @Override
    public boolean addMaterialListToOrder (int orderId, Carport carport) throws DatabaseException
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
                    throw new DatabaseException("Kunne ikke oprette MaterialLine med id og navn: "+ materialsLine.getMaterial().getId() + ", " + materialsLine.getMaterial().getName() + " på ordre id " + orderId +": " + e.getMessage());
                }
        }
        return true;
    }

    @Override
    public List<MaterialsLine> createMaterialList(Carport carport) throws DatabaseException
    {
        List<MaterialsLine> materialList = new ArrayList<>();

        /* #######################################################
                               POSTS
         ####################################################### */
        int posts = calculatorService.calculatePosts(carport);
        materialList.add(insertMaterialLine(posts, postMaterialId));

        /* #######################################################
                             TOP PLATES
         ####################################################### */
        HashMap<Double, Integer> topPlates = calculatorService.calculateTopPlate(carport);
        forEachMaterial(topPlates, materialList, shortTopPlateBoard, longTopPlateBoard, shortTopPlateBoardMaterialId, longTopPlateBoardMaterialId);

        /* #######################################################
                          BOLTS & WASHERS
         ####################################################### */
        int bolts = calculatorService.calculateBolts(posts, topPlates);
        materialList.add(insertMaterialLine(bolts, boltMaterialId));
        materialList.add(insertMaterialLine(bolts, washerMaterialId)); //same amount of washers as bolts

        /* #######################################################
                  CEILING JOISTS & FITTINGS + SCREWS
         ####################################################### */
        HashMap<Double, Integer> ceilingJoists = calculatorService.calculateCeilingJoist(carport);
        forEachMaterial(ceilingJoists, materialList, shortCeilingJoist, longCeilingJoist, shortCeilingJoistMaterialId, longCeilingJoistMaterialId);

        int ceilingJoistFittings = calculatorService.sumHashMapValues(ceilingJoists); //Needs double amount, 1 right and 1 left fitting for each ceiling joist
        materialList.add(insertMaterialLine(ceilingJoistFittings, rightCeilingJoistFittingMaterialId));
        materialList.add(insertMaterialLine(ceilingJoistFittings, leftCeilingJoistFittingMaterialId));

        int universalScrews = calculatorService.calculateScrewsNeeded(ceilingJoistFittings, screwsPerCeilingJoistFitting);

        /* #######################################################
                            PERFORATED STRIP
         ####################################################### */
        int perforatedStrip = calculatorService.calculatePerforatedStrip(carport);
        materialList.add(insertMaterialLine(perforatedStrip, perforatedStripMaterialId));

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
        forEachMaterial(roofPlates, materialList, shortRoofPlate, longRoofPlate, shortRoofPlateMaterialId, longRoofPlateMaterialId);

        int roofPlateScrews = calculatorService.calculateRoofPlateScrews(carport);
        int roofPlateScrewPacks = calculatorService.calculateScrewPacks(packSizeRoofPlateScrew, roofPlateScrews);
        materialList.add(insertMaterialLine(roofPlateScrewPacks, roofPlateScrewMaterialId));

        /* #######################################################
                            IF IS WITH SHED
         ####################################################### */
        if (carport.isWithShed())
        {
            HashMap<Double, Integer> blockings = calculatorService.calculateBlocking(carport);
            forEachMaterial(blockings, materialList, shortBlocking, longBlocking, shortBlockingMaterialId, longBlockingMaterialId);

            int blockingFittings = calculatorService.sumHashMapValues(blockings) * 2; //Need two pr blocking (according to delivered material, 16 blockings and 32 fittings)
            materialList.add(insertMaterialLine(blockingFittings, blockingFittingMaterialId));

            int blockingFittingsScrews = calculatorService.calculateScrewsNeeded(blockingFittings, screwsPerBlockingFitting);
            universalScrews += blockingFittingsScrews; //Added later to materialslines (same screws as to ceiling joists)

            int sideBoards = calculatorService.calculateSidingBoard(carport);
            materialList.add(insertMaterialLine(sideBoards, sideBoardMaterialId));

            int screwsSideBoardsInnerLayer = calculatorService.calculateScrewsNeeded(sideBoards, shortScrewsNeededPerBoard);
            int sideBoardsInnerScrewPack = calculatorService.calculateScrewPacks(packSizeShortScrew, screwsSideBoardsInnerLayer);
            materialList.add(insertMaterialLine(sideBoardsInnerScrewPack, shortScrewMaterialId));

            int screwsSideBoardsOuterLayer = calculatorService.calculateScrewsNeeded(sideBoards, longScrewsNeededPerBoard);
            int sideBoardsOuterScrewPack = calculatorService.calculateScrewPacks(packSizeLongScrew, screwsSideBoardsOuterLayer);
            materialList.add(insertMaterialLine(sideBoardsOuterScrewPack, longScrewMaterialId));

            //These are always added if with shed, these are for the door, so no calculations needed.
            materialList.add(insertMaterialLine(1, shedDoorStripMaterialId));
            materialList.add(insertMaterialLine(1, shedDoorGripMaterialId));
            materialList.add(insertMaterialLine(2, shedDoorTHingeMaterialId));
        }

        /* #######################################################
              UNIVERSAL SCREWS (CEILING JOISTS AND BLOCKING)
         ####################################################### */
        int fittingScrewPacks = calculatorService.calculateScrewPacks(packSizeFittingScrew, universalScrews);
        materialList.add(insertMaterialLine(fittingScrewPacks, universalScrewsMaterialId));

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
            if (fasciaBoard.getKey().equals(shortFasciaBoard))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), shortSubFasciaBoardMaterialId));
                int quantity = isWidth ? fasciaBoard.getValue() / 2 : fasciaBoard.getValue();
                materialList.add(insertMaterialLine(quantity, shortFasciaBoardMaterialId));
                materialList.add(insertMaterialLine(quantity, shortWeatherBoardMaterialId));
            }
            else if (fasciaBoard.getKey().equals(longFasciaBoard))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), longSubFasciaBoardMaterialId));
                int quantity = isWidth ? fasciaBoard.getValue() / 2 : fasciaBoard.getValue();
                materialList.add(insertMaterialLine(quantity, longFasciaBoardMaterialId));
                materialList.add(insertMaterialLine(quantity, longWeatherBoardMaterialId));
            }
        }
    }

}
