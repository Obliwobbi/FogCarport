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
    public List<MaterialsLine> createMaterialList(Carport carport) throws DatabaseException
    {
        List<MaterialsLine> materialList = new ArrayList<>();

        /* #######################################################
                               POSTS
         ####################################################### */
        int posts = calculatorService.calculatePosts(carport);
        materialList.add(insertMaterialLine(posts, 11)); //MaterialID '11' is taken from database to match with post material

        /* #######################################################
                             TOP PLATES
         ####################################################### */
        HashMap<Double, Integer> topPlates = calculatorService.calculateTopPlate(carport);
        materialList = forEachMaterial(topPlates,materialList,480.0,600.0,9,8);

        /* #######################################################
                          BOLTS & WASHERS
         ####################################################### */
        int bolts = calculatorService.calculateBolts(posts, topPlates);
        materialList.add(insertMaterialLine(bolts, 21)); //MaterialId '21' is for bolts 10x120mm
        materialList.add(insertMaterialLine(bolts, 23)); //same amount of washers as bolts

        /* #######################################################
                  CEILING JOISTS & FITTINGS + SCREWS
         ####################################################### */
        int ceilingJoists = calculatorService.calculateCeilingJoist(carport);
        materialList.add(insertMaterialLine(ceilingJoists, 10)); //MaterialId '10' is for long ceiling joist (600)
        int ceilingJoistFittings = ceilingJoists * 2; //Needs double amount, 1 right and 1 left fitting for each ceiling joist
        materialList.add(insertMaterialLine(ceilingJoists, 19)); //MaterialId '19' is for right fitting
        materialList.add(insertMaterialLine(ceilingJoists, 20)); //MaterialId '20' is for left fitting

        int universalScrews = calculatorService.calculateScrewsNeeded(ceilingJoistFittings, 9); //according to documentation, will add to packs needed later

        /* #######################################################
                            PERFORATED STRIP
         ####################################################### */
        int perforatedStrip = calculatorService.calculatePerforatedStrip(carport);
        materialList.add(insertMaterialLine(perforatedStrip, 18));

        /* #######################################################
                      FASCIA BOARDS + WEATHERBOARD/SIDING
         ####################################################### */
        HashMap<Double, Integer> fasciaBoardLength = calculatorService.calculateFasciaBoardLength(carport);
        //Needs 2 mapper calls, one with wide board (back) and one with narrower board (front)
        //Weatherboard, needs same quantity and lengths as fascia board
        double shortFasciaBoard = 360.0;
        double longFasciaBoard = 540.0;
        for (HashMap.Entry<Double, Integer> fasciaBoard : fasciaBoardLength.entrySet())
        {
            if (fasciaBoard.getKey().equals(shortFasciaBoard))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 1)); //Sub-fascia board
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 3)); //Fascia
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 14)); //Weatherboard
            }
            else if (fasciaBoard.getKey().equals(longFasciaBoard))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 2)); //Sub-fascia board
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 4)); //Fascia
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 13)); //Weatherboard
            }
        }

        //only need half for the value for mapper call for 1 of the 2 calls
        HashMap<Double, Integer> fasciaBoardWidth = calculatorService.calculateFasciaBoardWidth(carport);
        for (HashMap.Entry<Double, Integer> fasciaBoard : fasciaBoardWidth.entrySet())
        {
            if (fasciaBoard.getKey().equals(shortFasciaBoard))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 1)); //Sub-fascia board
                materialList.add(insertMaterialLine(fasciaBoard.getValue() / 2, 3)); //Fascia
                materialList.add(insertMaterialLine(fasciaBoard.getValue() / 2, 14)); //Weatherboard
            }
            else if (fasciaBoard.getKey().equals(longFasciaBoard))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 2)); //Sub-fascia board
                materialList.add(insertMaterialLine(fasciaBoard.getValue() / 2, 4)); //Fascia
                materialList.add(insertMaterialLine(fasciaBoard.getValue() / 2, 13)); //Weatherboard
            }
        }

        /* #######################################################
                          ROOF PLATES & SCREWS
         ####################################################### */
        HashMap<Double, Integer> roofPlates = calculatorService.calculateRoofPlates(carport);
        materialList = forEachMaterial(roofPlates,materialList,360.0,600.0,16,15);

        int roofPlateScrews = calculatorService.calculateRoofPlateScrews(carport);
        int packSizeRoofPlateScrew = 200; //According to documentation
        int roofPlateScrewPacks = calculatorService.calculateScrewPacks(packSizeRoofPlateScrew, roofPlateScrews);
        materialList.add(insertMaterialLine(roofPlateScrewPacks, 17));

        /* #######################################################
                            IF IS WITH SHED
         ####################################################### */
        if (carport.isWithShed())
        {
            HashMap<Double, Integer> blockings = calculatorService.calculateBlocking(carport);
            materialList = forEachMaterial(blockings,materialList,240.0,270.0,7,6);

            int blockingFittingsCount = blockings.values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();
            int blockingFittings = blockingFittingsCount * 2; //Need two pr blocking (according to delivered material, 16 blockings and 32 fittings)
            materialList.add(insertMaterialLine(blockingFittings, 29));

            int blockingFittingsScrews = calculatorService.calculateScrewsNeeded(blockingFittings, 4); //according to documentation
            universalScrews += blockingFittingsScrews; //Added later to materialslines (same screws as to ceiling joists)

            int sideBoards = calculatorService.calculateSidingBoard(carport);
            materialList.add(insertMaterialLine(sideBoards, 12));

            int screwsSideBoardsInnerLayer = calculatorService.calculateScrewsNeeded(sideBoards, 3); //according to documentation
            int packSizeShortScrew = 300; //according to documentation
            int sideBoardsInnerScrewPack = calculatorService.calculateScrewPacks(packSizeShortScrew, screwsSideBoardsInnerLayer);
            materialList.add(insertMaterialLine(sideBoardsInnerScrewPack, 24));

            int screwsSideBoardsOuterLayer = calculatorService.calculateScrewsNeeded(sideBoards, 6); //according to documentation
            int packSizeLongScrew = 400; //according to documentation
            int sideBoardsOuterScrewPack = calculatorService.calculateScrewPacks(packSizeLongScrew, screwsSideBoardsOuterLayer);
            materialList.add(insertMaterialLine(sideBoardsOuterScrewPack, 26));

            //These are always added if with shed, these are for the door, so no calculations needed.
            materialList.add(insertMaterialLine(1, 5)); //Z strip to behind shed door
            materialList.add(insertMaterialLine(1,27)); //Barndoor grip
            materialList.add(insertMaterialLine(2,28)); //T-Hinge
        }

        /* #######################################################
              UNIVERSAL SCREWS (CEILING JOISTS AND BLOCKING)
         ####################################################### */
        int packSizeFittingScrew = 250; //according to documentation
        int fittingScrewPacks = calculatorService.calculateScrewPacks(packSizeFittingScrew, universalScrews);
        materialList.add(insertMaterialLine(fittingScrewPacks, 23));

        //Get all materials in a list
        //Loop through
        //when match take calculator info
        //create MaterialLine with quantity and material object


        return materialList;
    }

    private MaterialsLine insertMaterialLine(int quantity, int materialId) throws DatabaseException
    {
        Material material = materialMapper.getMaterialById(materialId);
        return new MaterialsLine(quantity, material.getPrice() * quantity, material);
    }

    private List<MaterialsLine> forEachMaterial(HashMap<Double, Integer> boards, List<MaterialsLine> currentMaterialList, double shortBoard, double longBoard, int shortMaterialId, int longMaterialId) throws DatabaseException
    {
        List<MaterialsLine> newMaterialList = currentMaterialList;

        for (HashMap.Entry<Double, Integer> material : boards.entrySet())
        {
            if (material.getKey().equals(shortBoard))
            {
                newMaterialList.add(insertMaterialLine(material.getValue(), shortMaterialId));
            }
            else if (material.getKey().equals(longBoard))
            {
                newMaterialList.add(insertMaterialLine(material.getValue(), longMaterialId));
            }
        }

        return newMaterialList;
    }
}
