package app.services;

import app.entities.Carport;

import java.util.HashMap;

public interface CalculatorService
{
    int calculatePosts(Carport carport);

    HashMap<Double, Integer> calculateCeilingJoist(Carport carport);

    HashMap<Double, Integer> calculateTopPlate(Carport carport);

    HashMap<Double, Integer> calculateBlocking(Carport carport);

    HashMap<Double, Integer> calculateRoofPlates(Carport carport);

    int calculateRoofPlateScrews(Carport carport);

    int calculateBolts(int posts, HashMap<Double, Integer> topPlates);

    int calculateScrewsNeeded(int material, int screws);

    int calculateScrewPacks(int packSize, int screws);

    int calculateSidingBoard(Carport carport);

    HashMap<Double, Integer> calculateFasciaBoardLength(Carport carport);

    HashMap<Double, Integer> calculateFasciaBoardWidth(Carport carport);

    int calculatePerforatedStrip(Carport carport);

    int sumHashMapValues(HashMap<?, Integer> map);
}