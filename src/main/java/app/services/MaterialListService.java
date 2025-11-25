package app.services;

import app.entities.Carport;

import java.util.HashMap;

public interface MaterialListService
{
    int calculatePosts(Carport carport);

    int calculateCeilingJoist(Carport carport);

    HashMap<Double, Integer> calculateTopPlate(Carport carport);

    int calculateRoofPlates(Carport carport);

    int calculateBolts(int posts);

    int calculateFittings(int rafters);

    int calculateFittingsScrews(int fittings);
}
