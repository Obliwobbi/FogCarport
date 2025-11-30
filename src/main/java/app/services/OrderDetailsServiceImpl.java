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
        double shortTopPlate = 480.0; // 480 is the shortest Top Plate in database
        double longTopPlate = 600.0; //600 is the of the longest Top Plate in database
        for (HashMap.Entry<Double, Integer> topPlate : topPlates.entrySet())
        {
            if (topPlate.getKey().equals(shortTopPlate))
            {
                materialList.add(insertMaterialLine(topPlate.getValue(), 9)); //MaterialId '9' is for short board (480)
            }
            else if (topPlate.getKey().equals(longTopPlate))
            {
                materialList.add(insertMaterialLine(topPlate.getValue(), 8)); //MaterialId '8' is for long board (600)
            }
        }

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
                             FASCIA BOARDS
         ####################################################### */
        HashMap<Double, Integer> fasciaBoardLength = calculatorService.calculateFasciaBoardLength(carport);
        //Needs 2 mapper calls, one with wide board (back) and one with narrower board (front)
        double shortFasciaBoard = 360.0;
        double longFasciaBoard = 540.0;
        for (HashMap.Entry<Double, Integer> fasciaBoard : fasciaBoardLength.entrySet())
        {
            if (fasciaBoard.getKey().equals(shortFasciaBoard))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 1)); //Sub-fascia board
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 3));
            }
            else if (fasciaBoard.getKey().equals(longFasciaBoard))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 2)); //Sub-fascia board
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 4));
            }
        }

        //only need half for the value for mapper call for 1 of the 2 calls
        HashMap<Double, Integer> fasciaBoardWidth = calculatorService.calculateFasciaBoardWidth(carport);
        for (HashMap.Entry<Double, Integer> fasciaBoard : fasciaBoardWidth.entrySet())
        {
            if (fasciaBoard.getKey().equals(shortFasciaBoard))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 1)); //Sub-fascia board
                materialList.add(insertMaterialLine(fasciaBoard.getValue() / 2, 3));
            }
            else if (fasciaBoard.getKey().equals(longFasciaBoard))
            {
                materialList.add(insertMaterialLine(fasciaBoard.getValue(), 2)); //Sub-fascia board
                materialList.add(insertMaterialLine(fasciaBoard.getValue() / 2, 4));
            }
        }

        //vandbrædt samme mængder og længder som fasciaboard metoderne til overside

        /* #######################################################
                          ROOF PLATES & SCREWS
         ####################################################### */
        HashMap<Double, Integer> roofPlates = calculatorService.calculateRoofPlates(carport);
        double shortRoofPlate = 360.0;
        double longRoofPlate = 600.0;
        for (HashMap.Entry<Double, Integer> roofPlate : roofPlates.entrySet())
        {
            if (roofPlate.getKey().equals(shortRoofPlate))
            {
                materialList.add(insertMaterialLine(roofPlate.getValue(), 16));
            }
            else if (roofPlate.getKey().equals(longRoofPlate))
            {
                materialList.add(insertMaterialLine(roofPlate.getValue(), 15));
            }
        }

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
            double shortBlocking = 240.0;
            double longBlocking = 270.0;
            for (HashMap.Entry<Double, Integer> blocking : blockings.entrySet())
            {
                if (blocking.getKey().equals(shortBlocking))
                {
                    materialList.add(insertMaterialLine(blocking.getValue(), 7));
                }
                else if (blocking.getKey().equals(longBlocking))
                {
                    materialList.add(insertMaterialLine(blocking.getValue(), 6));
                }
            }
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

    private ArrayList<MaterialsLine> insertTopPlates(HashMap<Double, Integer> topPlates, ArrayList<MaterialsLine> materialsList)
    {

        return null;
    }
}
