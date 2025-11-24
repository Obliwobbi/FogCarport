package app.services;

import app.entities.Carport;

public interface MaterialListService
{
    int calculatePosts(Carport carport);

    int calculateRafters(Carport carport);

    int calculateRoofPlates(Carport carport);

    int calculateBolts(int posts);

    int calculateFittings(int rafters);

    int calculateFittingsScrews(int fittings);
}
