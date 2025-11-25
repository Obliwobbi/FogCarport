package app.services;

import app.entities.Carport;

import java.util.HashMap;

public interface MaterialListService
{
    int calculatePosts(Carport carport);

    int calculateCeilingJoist(Carport carport);

    HashMap<Double, Integer> calculateTopPlate(Carport carport);

    int calculateBlocking(Carport carport);

    HashMap<Double, Integer> calculateRoofPlates(Carport carport);

    int calculateBolts(int posts);

    int calculateFittings(int rafters);

    int calculateFittingsScrewsNeeded(int fittings, int screws);

    int calculateScrewPacks(int packsize, int screws);
}
