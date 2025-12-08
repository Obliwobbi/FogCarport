package app.services;

import app.entities.Carport;

import java.util.HashMap;

public class CalculatorServiceImpl implements CalculatorService
{
    private double MAX_OVERHANG;

    @Override
    public int calculatePosts(Carport carport)
    {
        double carportWidth = carport.getWidth();
        double carportLength = carport.getLength();
        double shedWidth = carport.getShedWidth();
        double shedLength = carport.getShedLength();
        boolean withShed = carport.isWithShed();

        MAX_OVERHANG = (carportWidth >= 330) ? 70 : 35; //smaller overhang on smaller carports to give space for car

        boolean isFullWidth = shedWidth >= (carportWidth - MAX_OVERHANG);

        int result = 4; //Corner posts for carport

        if (withShed)
        {
            result += (isFullWidth || shedLength > Constants.MAX_LENGTH_BLOCKING ) ? 3 : 4; //door and corners of shed, partial width sheds need 1 more corner

            if (shedWidth > Constants.MAX_LENGTH_BLOCKING)
            {
                result += (shedWidth - Constants.MAX_LENGTH_BLOCKING > Constants.MAX_LENGTH_BLOCKING ? 4 : 2);

            }
            if (shedLength > Constants.MAX_LENGTH_BLOCKING)
            {
                result += (shedLength - Constants.MAX_LENGTH_BLOCKING > Constants.MAX_LENGTH_BLOCKING ? 6 : 2);

            }

            double remainingLength = (carportLength - Constants.POST_OFFSET_LONG) - shedLength; //Calculation of remaining length between shed and corner post due to max length of Top plate(Rem træ).

            if (remainingLength > Constants.MAX_LENGTH_BETWEEN_POST)
            {
                result += 2;
            }
        }
        else
        {
            if (carportLength > Constants.MAX_LENGTH_CARPORT_NO_SHED_FEWER_SUPPORTS)
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
            if (carportLength <= Constants.TOP_PLATE_SHORT)
            {
                result.put(Constants.TOP_PLATE_SHORT, 2);
            }
            else if (carportLength > Constants.TOP_PLATE_SHORT && carportLength <= Constants.TOP_PLATE_LONG)
            {
                result.put(Constants.TOP_PLATE_LONG, 2);
            }
            else
            {
                result.put(Constants.TOP_PLATE_SHORT, 4);
            }
        }
        else
        {
            boolean isFullWidth = shedWidth >= (carportWidth - Constants.MIN_OVERHANG);

            if (carportLength <= Constants.TOP_PLATE_SHORT)
            {
                result.put(Constants.TOP_PLATE_SHORT, 2);
            }
            else if (carportLength <= Constants.TOP_PLATE_LONG)
            {
                result.put(Constants.TOP_PLATE_LONG, 2);
            }
            else if (isFullWidth)
            {
                result.put(Constants.TOP_PLATE_LONG, 2);
                result.put(Constants.TOP_PLATE_SHORT, 1);
            }
            else
            {
                // Partial-width shed: asymmetrical placement
                result.put(Constants.TOP_PLATE_LONG, 1);
                result.put(Constants.TOP_PLATE_SHORT, 3);
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
        double ceilingJoistLength = (carportWidth <= Constants.CEILING_JOIST_SHORT) ? Constants.CEILING_JOIST_SHORT : Constants.CEILING_JOIST_LONG;

        count += (int) Math.ceil((carportLength - (Constants.CEILING_JOIST_WIDTH * 2)) / Constants.MAX_LENGTH_BTWN_CEILING_JOIST); //rounds up always to ensure there is no more than 60 cm between rafters
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

        if (carportLength <= Constants.FASCIA_BOARD_SHORT)
        {
            shortFasciaCount += 2;
        }
        else if (carportLength > Constants.FASCIA_BOARD_SHORT && carportLength <= Constants.FASCIA_BOARD_LONG)
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
            result.put(Constants.FASCIA_BOARD_SHORT, shortFasciaCount);
        }
        if (longFasciaCount != 0)
        {
            result.put(Constants.FASCIA_BOARD_LONG, longFasciaCount);
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

        if (carportWidth <= Constants.FASCIA_BOARD_SHORT)
        {
            shortFasciaCount += 2;
        }
        else if (carportWidth > Constants.FASCIA_BOARD_SHORT && carportWidth <= Constants.FASCIA_BOARD_LONG)
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
            result.put(Constants.FASCIA_BOARD_SHORT, shortFasciaCount);
        }
        if (longFasciaCount != 0)
        {
            result.put(Constants.FASCIA_BOARD_LONG, longFasciaCount);
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

        if (shedWidth > Constants.MAX_LENGTH_BLOCKING)
        {
            if (shedWidth / 2 > Constants.BLOCKING_SHORT)
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

        if (shedLength > Constants.MAX_LENGTH_BLOCKING)
        {
            if (shedLength / 2 > Constants.BLOCKING_SHORT)
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
            result.put(Constants.BLOCKING_SHORT, shortBlockingCount);
        }
        if (longBlockingCount != 0)
        {
            result.put(Constants.BLOCKING_LONG, longBlockingCount);
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

        if (carportLength <= (Constants.ROOF_PLATE_SHORT - Constants.BACKSIDE_OVERHANG))
        {
            result.put(Constants.ROOF_PLATE_SHORT, count);
        }

        if (carportLength > Constants.ROOF_PLATE_SHORT && carportLength <= (Constants.ROOF_PLATE_LONG - Constants.BACKSIDE_OVERHANG))
        {
            result.put(Constants.ROOF_PLATE_LONG, count);
        }

        if (carportLength > Constants.ROOF_PLATE_LONG && carportLength <= 700 - Constants.BACKSIDE_OVERHANG) // 700 is cut off for carport length, due to 20cm overlay from plates according to documentation provided by product owner
        {
            result.put(Constants.ROOF_PLATE_SHORT, (count * 2)); //needs double amount because of 2 rows and overlay
        }

        if (carportLength > 700)
        {
            result.put(Constants.ROOF_PLATE_LONG, count);
            result.put(Constants.ROOF_PLATE_SHORT, count);
        }

        return result;
    }

    @Override
    public int calculateRoofPlateScrews(Carport carport)
    {
        double carportWidth = carport.getWidth();
        double carportLength = carport.getLength();

        //Insert return in helper method to calculate packs
        return (int) Math.ceil((((carportWidth / 100) * (carportLength / 100)) * Constants.ROOF_PLATE_SCREWS_M2));
    }

    @Override
    public int calculateBolts(int posts, HashMap<Double, Integer> topPlates)
    {
        int result = posts * 2; //documentation says 2 bolts for each posts

        if (topPlates.containsKey(Constants.TOP_PLATE_SHORT) && topPlates.containsKey(Constants.TOP_PLATE_LONG)) // for joins need 2 additional bolt, if hastmap has both lengths there is a join
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
