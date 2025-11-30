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

        int ceilingJoists = calculatorService.calculateCeilingJoist(carport);
        int ceilingJoistFittings = calculatorService.calculateFittings(ceilingJoists);
        int universalScrews = calculatorService.calculateScrewsNeeded(ceilingJoistFittings, 9); //according to documentation

        //hulbånd calculation
        int perforatedStrip = calculatorService.calculatePerforatedStrip(carport);

        HashMap<Double, Integer> fasciaBoardLength = calculatorService.calculateFasciaBoardLength(carport);
        HashMap<Double, Integer> fasciaBoardWidth = calculatorService.calculateFasciaBoardWidth(carport);
        //only need half for the value for mapper call for 1 of the 4 calls

        //vandbrædt samme mængder og længder som fasciaboard metoderne til overside

        HashMap<Double,Integer> roofPlates = calculatorService.calculateRoofPlates(carport);

        int roofPlateScrews = calculatorService.calculateRoofPlateScrews(carport);
        int packSizeRoofPlateScrew = 200; //According to documentation
        int roofPlateScrewPacks = calculatorService.calculateScrewPacks(packSizeRoofPlateScrew,roofPlateScrews);

        if(carport.isWithShed())
        {
            HashMap<Double, Integer> blockings = calculatorService.calculateBlocking(carport);
            int blockingFittingsCount = blockings.values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();
            int blockingFittings = calculatorService.calculateFittings(blockingFittingsCount);
            int blockingFittingsScrews = calculatorService.calculateScrewsNeeded(blockingFittings, 4); //according to documentation

            universalScrews += blockingFittingsScrews;

            int sideBoards = calculatorService.calculateSidingBoard(carport);

            int screwsSideBoardsInnerLayer = calculatorService.calculateScrewsNeeded(sideBoards,3); //according to documentation
            int screwsSideBoardsOuterLayer = calculatorService.calculateScrewsNeeded(sideBoards,6); //according to documentation

            int packSizeShortScrew = 300; //according to documentation
            int packSizeLongScrew = 400; //according to documentation

            int sideBoardsInnerScrewPack = calculatorService.calculateScrewPacks(packSizeShortScrew,screwsSideBoardsInnerLayer);
            int sideBoardsOuterScrewPack = calculatorService.calculateScrewPacks(packSizeLongScrew,screwsSideBoardsOuterLayer);

            //tilføj beslag, greb, lægte til Z bag dør
        }

        int packSizeFittingScrew = 250; //according to documentation
        int fittingScrewPacks = calculatorService.calculateScrewPacks(packSizeFittingScrew, universalScrews);


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
