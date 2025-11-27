package app.services;

import app.entities.Carport;

import java.util.HashMap;

public interface MaterialListService
{
    int calculatePosts(Carport carport);

    int calculateCeilingJoist(Carport carport);

    HashMap<Double, Integer> calculateTopPlate(Carport carport);

    HashMap<Double, Integer> calculateBlocking(Carport carport);

    HashMap<Double, Integer> calculateRoofPlates(Carport carport);

    int calculateRoofPlateScrews(Carport carport);

    int calculateBolts(int posts, HashMap<Double, Integer> topPlates);

    int calculateFittings(int rafters);

    int calculateScrewsNeeded(int fittings, int screws);

    int calculateScrewPacks(int packsize, int screws);
}
