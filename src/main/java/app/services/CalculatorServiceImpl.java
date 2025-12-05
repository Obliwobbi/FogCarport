package app.services;

import app.entities.Carport;

import java.util.HashMap;

public class CalculatorServiceImpl implements CalculatorService
{
    int MAX_LENGTH_BLOCKING = 270; //270 is max length between allowed for Blockings for Sideboards, src: documentation provided by product owner
    int MAX_LENGTH_BETWEEN_POST = 310; // 310 is max length allowed between posts under Top Plate, src: documentation provided by product owner

    double TOP_PLATE_SHORT = 480.0; // 480 is the shortest Top Plate in database
    double TOP_PLATE_LONG = 600.0; //600 is the of the longest Top Plate in database
    int MIN_OVERHANG = 30; //the 30cm is to account for necessary overhang on sides of the shed, from documentation provided by product owner

    double CEILING_JOIST_WIDTH = 4.5;
    double CEILING_JOIST_SHORT = 480.0;
    double CEILING_JOIST_LONG = 600.0;

    int MAX_LENGTH_BTWN_CEILING_JOIST = 60; // src: documentation provided by product owner

    double FASCIA_BOARD_SHORT = 360.0; //shortest FasciaBoard in database
    double FASCIA_BOARD_LONG = 540.0; //longest FasciaBoard in database

    double BLOCKING_SHORT = 240.0;
    double BLOCKING_LONG = 270.0;

    double ROOF_PLATE_SHORT = 360.0; // 360 is the shortest roof Plate in database
    double ROOF_PLATE_LONG = 600.0; // 600 is the longest Top Plate in database
    int BACKSIDE_OVERHANG = 5; //5 cm overhang on backside of carport for running of water, src: documentation provided by product owner
    double MAX_OVERHANG;

    double POST_OFFSET_LONG = 100;
    double MAX_LENGTH_CARPORT_NO_SHED_FEWER_SUPPORTS = 510; // for no shed, corner posts  can start 1 meter in from both ends. needs 1 in middle of each side to account for no more than 310 between posts


    @Override
    public int calculatePosts(Carport carport)
    {
        double carportWidth = carport.getWidth();
        double carportLength = carport.getLength();
        double shedWidth = carport.getShedWidth();
        double shedLength = carport.getShedLength();
        boolean withShed = carport.isWithShed();

        MAX_OVERHANG = (carportWidth >= 330) ? 70 : 35;

        boolean isFullWidth = shedWidth >= (carportWidth - MAX_OVERHANG);

        int result = 4; //Corner posts for carport

        if (withShed)
        {
            result += (isFullWidth) ? 3 : 4; //door and corners of shed, partial width sheds need 1 more corner

            if (shedWidth > MAX_LENGTH_BLOCKING)
            {
                result += (shedWidth - MAX_LENGTH_BLOCKING > MAX_LENGTH_BLOCKING ? 4 : 2);

            }
            if (shedLength > MAX_LENGTH_BLOCKING)
            {
                result += (shedLength - MAX_LENGTH_BLOCKING > MAX_LENGTH_BLOCKING ? 6 : 2);

            }

            double remainingLength = (carportLength - POST_OFFSET_LONG) - shedLength; //Calculation of remaining length between shed and corner post due to max length of Top plate(Rem træ).

            if (remainingLength > MAX_LENGTH_BETWEEN_POST)
            {
                result += 2;
            }
        }
        else
        {
            if (carportLength > MAX_LENGTH_CARPORT_NO_SHED_FEWER_SUPPORTS)
            {
                result += 2;
            }
        }
        return result;
    }

    @Override
    public HashMap<Double, Integer> calculateTopPlate(Carport carport)
    {
        double carportWidth = carport.getWidth();
        double carportLength = carport.getLength();
        double shedWidth = carport.getShedWidth();
        boolean withShed = carport.isWithShed();

        HashMap<Double, Integer> result = new HashMap<>();

        if (!withShed)
        {
            if (carportLength <= TOP_PLATE_SHORT)
            {
                result.put(TOP_PLATE_SHORT, 2);
            }
            else if (carportLength > TOP_PLATE_SHORT && carportLength <= TOP_PLATE_LONG)
            {
                result.put(TOP_PLATE_LONG, 2);
            }
            else
            {
                result.put(TOP_PLATE_SHORT, 4);
            }
        }
        else
        {
            boolean isFullWidth = shedWidth >= (carportWidth - MIN_OVERHANG);

            if (carportLength <= TOP_PLATE_SHORT)
            {
                result.put(TOP_PLATE_SHORT, 2);
            }
            else if (carportLength <= TOP_PLATE_LONG)
            {
                result.put(TOP_PLATE_LONG, 2);
            }
            else if (isFullWidth)
            {
                result.put(TOP_PLATE_LONG, 2);
                result.put(TOP_PLATE_SHORT, 1);
            }
            else
            {
                // Partial-width shed: asymmetrical placement
                result.put(TOP_PLATE_LONG, 1);
                result.put(TOP_PLATE_SHORT, 3);
            }
        }
        return result;
    }

    @Override
    public HashMap<Double, Integer> calculateCeilingJoist(Carport carport)
    {
        double carportWidth = carport.getWidth();
        double carportLength = carport.getLength();

        HashMap<Double, Integer> result = new HashMap<>();
        int count = 2; //one for each for the ends.
        double ceilingJoistLength = (carportWidth <= CEILING_JOIST_SHORT) ? CEILING_JOIST_SHORT : CEILING_JOIST_LONG;

        count += (int) Math.ceil((carportLength - (CEILING_JOIST_WIDTH * 2)) / MAX_LENGTH_BTWN_CEILING_JOIST); //rounds up always to ensure there is no more than 60 cm between rafters
        result.put(ceilingJoistLength, count);

        return result;
    }

    @Override
    public HashMap<Double, Integer> calculateFasciaBoardLength(Carport carport)
    {
        double carportLength = carport.getLength();

        HashMap<Double, Integer> result = new HashMap<>();
        int shortFasciaCount = 0;
        int longFasciaCount = 0;

        if (carportLength <= FASCIA_BOARD_SHORT)
        {
            shortFasciaCount += 2;
        }
        else if (carportLength > FASCIA_BOARD_SHORT && carportLength <= FASCIA_BOARD_LONG)
        {
            longFasciaCount += 2;
        }
        else if (carportLength <= 690) // max length for carport to account of join
        {
            shortFasciaCount += 4;
        }
        else
        {
            longFasciaCount += 2;
            shortFasciaCount += 2;
        }

        if (shortFasciaCount != 0)
        {
            result.put(FASCIA_BOARD_SHORT, shortFasciaCount);
        }
        if (longFasciaCount != 0)
        {
            result.put(FASCIA_BOARD_LONG, longFasciaCount);
        }

        return result;
    }

    @Override
    public HashMap<Double, Integer> calculateFasciaBoardWidth(Carport carport)
    {
        double carportWidth = carport.getWidth();

        HashMap<Double, Integer> result = new HashMap<>();
        int shortFasciaCount = 0;
        int longFasciaCount = 0;

        if (carportWidth <= FASCIA_BOARD_SHORT)
        {
            shortFasciaCount += 2;
        }
        else if (carportWidth > FASCIA_BOARD_SHORT && carportWidth <= FASCIA_BOARD_LONG)
        {
            longFasciaCount += 2;
        }
        else if (carportWidth <= 690) // max length for carport to account of join
        {
            shortFasciaCount += 4;
        }
        else
        {
            longFasciaCount += 2;
            shortFasciaCount += 2;
        }

        if (shortFasciaCount != 0)
        {
            result.put(FASCIA_BOARD_SHORT, shortFasciaCount);
        }
        if (longFasciaCount != 0)
        {
            result.put(FASCIA_BOARD_LONG, longFasciaCount);
        }

        return result;
    }

    @Override
    public HashMap<Double, Integer> calculateBlocking(Carport carport)
    {
        double shedWidth = carport.getShedWidth();
        double shedLength = carport.getShedLength();

        HashMap<Double, Integer> result = new HashMap<>();
        int shortBlockingCount = 0;
        int longBlockingCount = 0;

        if (shedWidth > MAX_LENGTH_BLOCKING)
        {
            if (shedWidth / 2 > BLOCKING_SHORT)
            {
                //If length between posts on shed width is larger than the short board, we use the long one
                longBlockingCount += 12; //according to documentation provided, there needs to be 3 blockings between each post if over 240
            }
            else
            {
                shortBlockingCount += 8; //according to documentation provided there needs to be 2 blockings between each post, if under 240
            }
        }
        else
        {
            shortBlockingCount += 4;
        }

        if (shedLength > MAX_LENGTH_BLOCKING)
        {
            if (shedLength / 2 > BLOCKING_SHORT)
            {
                longBlockingCount += 12;
            }
            else
            {
                shortBlockingCount += 8;
            }
        }
        else
        {
            shortBlockingCount += 4;
        }

        if (shortBlockingCount != 0)
        {
            result.put(BLOCKING_SHORT, shortBlockingCount);
        }
        if (longBlockingCount != 0)
        {
            result.put(BLOCKING_LONG, longBlockingCount);
        }

        return result;
    }

    @Override
    public int calculateSidingBoard(Carport carport)
    {
        double shedWidth = carport.getShedWidth();
        double shedLength = carport.getShedLength();

        double shedTotalLength = (shedLength * 2) + (shedWidth * 2);

        //Math is done by extrapolating from documentation provided by product owner:
        //our shed in delivered material is 530x210, and they deliver 200 pieces of lumber for siding.
        //so we take total length of shed / 200, and get a coverage pr board of 7.4cm.
        return (int) Math.ceil(shedTotalLength / 7.4);
    }

    @Override
    public HashMap<Double, Integer> calculateRoofPlates(Carport carport)
    {
        double carportWidth = carport.getWidth();
        double carportLength = carport.getLength();

        HashMap<Double, Integer> result = new HashMap<>();
        int count = (int) Math.ceil(carportWidth / 100); //each plate is 109, and need overlay, so accounting for waste rounds up, src: documentation provided by product owner

        if (carportLength <= (ROOF_PLATE_SHORT - BACKSIDE_OVERHANG))
        {
            result.put(ROOF_PLATE_SHORT, count);
        }

        if (carportLength > ROOF_PLATE_SHORT && carportLength <= (ROOF_PLATE_LONG - BACKSIDE_OVERHANG))
        {
            result.put(ROOF_PLATE_LONG, count);
        }

        if (carportLength > ROOF_PLATE_LONG && carportLength <= 700 - BACKSIDE_OVERHANG) // 700 is cut off for carport length, due to 20cm overlay from plates according to documentation provided by product owner
        {
            result.put(ROOF_PLATE_SHORT, (count * 2)); //needs double amount because of 2 rows and overlay
        }

        if (carportLength > 700)
        {
            result.put(ROOF_PLATE_LONG, count);
            result.put(ROOF_PLATE_SHORT, count);
        }

        return result;
    }

    @Override
    public int calculateRoofPlateScrews(Carport carport)
    {
        double carportWidth = carport.getWidth();
        double carportLength = carport.getLength();

        //Insert return in helper method to calculate packs
        return (int) Math.ceil((((carportWidth / 100) * (carportLength / 100)) * 12)); //documentation says 12 screws pr squaremeter of roofplate
    }

    @Override
    public int calculateBolts(int posts, HashMap<Double, Integer> topPlates)
    {
        int result = posts * 2; //documentation says 2 bolts for each posts

        if (topPlates.containsKey(TOP_PLATE_SHORT) && topPlates.containsKey(TOP_PLATE_LONG)) // for joins need 2 additional bolt, if hastmap has both lengths there is a join
        {
            result += 4;
        }
        else if (topPlates.containsValue(4)) //if 4 there is also a join somewhere, need 2 extra bolts
        {
            result += 4;
        }
        return result;
    }

    @Override
    public int calculatePerforatedStrip(Carport carport)
    {
        double carportLength = carport.getLength();
        double carportWidth = carport.getWidth();

        double a = carportLength / 100; //converts to M
        double b = carportWidth / 100; //converts to M

        double c = Math.hypot(a, b); //Pythagoras
        if (c > 5)
        {
            return 2;
        }
        else
        {
            return 1;
        }
    }

    @Override
    public int calculateScrewsNeeded(int material, int screws)
    {
        return material * screws;
    }

    @Override
    public int calculateScrewPacks(int packsize, int screws)
    {
        double result = Math.ceil((double) screws / packsize);
        return (int) result;
    }

    @Override
    public int sumHashMapValues(HashMap<?, Integer> map)
    {
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }
}
