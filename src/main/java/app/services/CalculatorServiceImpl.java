package app.services;

import app.entities.Carport;

import java.util.HashMap;

public class CalculatorServiceImpl implements CalculatorService
{

    @Override
    public int calculatePosts(Carport carport)
    {
        int result = 4; //Corner posts for carport
        int blockingMaxLength = 270; //270 is max length between allowed for Blockings for Sideboards, src: documentation provided by product owner
        int maxLengthBetweenPosts = 310; // 310 is max length allowed between posts under Top Plate, src: documentation provided by product owner

        if (carport.isWithShed())
        {
            result += 3; //door and corners of shed

            if (carport.getShedWidth() > blockingMaxLength)
            {
                result += 2;
            }
            if (carport.getShedLength() > blockingMaxLength)
            {
                result += 2;
            }

            double remainingLength = (carport.getLength() - 100.0) - carport.getShedLength(); //Calculation of remaining length between shed and corner post due to max length of Top plate(Rem træ).

            if (remainingLength > maxLengthBetweenPosts)
            {
                result += 2;
            }
        }
        else
        {
            if (carport.getLength() > 510) // for no shed, corner posts  can start 1 meter in from both ends. needs 1 in middle of each side to account for no more than 310 between posts
            {
                result += 2;
            }
        }
        return result;
    }

    @Override
    public HashMap<Double, Integer> calculateTopPlate(Carport carport)
    {
        HashMap<Double, Integer> result = new HashMap<>();
        //Only 2 lengths of Top Plates in database, 480 and 600.
        double shortTopPlate = 480.0; // 480 is the shortest Top Plate in database
        double longTopPlate = 600.0; //600 is the of the longest Top Plate in database
        int minOverhang = 30; //the 30cm is to account for necessary overhang on sides of the shed, from documentation provided by product owner

        if (!carport.isWithShed())
        {
            if (carport.getLength() <= shortTopPlate)
            {
                result.put(shortTopPlate, 2);
            }
            else if (carport.getLength() > shortTopPlate && carport.getLength() <= longTopPlate)
            {
                result.put(longTopPlate, 2);
            }
            else
            {
                result.put(shortTopPlate, 4);
            }
        }
        else
        {
            boolean isFullWidth = carport.getShedWidth() >= (carport.getWidth() - minOverhang);

            if (carport.getLength() <= shortTopPlate)
            {
                result.put(shortTopPlate, 2);
            }
            else if (carport.getLength() <= longTopPlate)
            {
                result.put(longTopPlate, 2);
            }
            else if (isFullWidth)
            {
                result.put(longTopPlate, 2);
                result.put(shortTopPlate, 1);
            }
            else
            {
                // Partial-width shed: asymmetrical placement
                result.put(longTopPlate, 1);
                result.put(shortTopPlate, 3);
            }
        }
        return result;
    }

    @Override
    public HashMap<Double, Integer> calculateCeilingJoist(Carport carport)
    {
        HashMap<Double, Integer> result = new HashMap<>();

        double ceilingJoistWidth = 4.5;
        double shortCeilingJoist = 480.0;
        double longCeilingJoist = 600.0;

        int maxLengthBetweenCeilingJoist = 60; // src: documentation provided by product owner
        int count = 2; //one for each for the ends.
        double ceilingJoistLength = 0;
        if (carport.getWidth() <= shortCeilingJoist)
        {
            ceilingJoistLength = shortCeilingJoist;
        }
        else if (carport.getWidth() > shortCeilingJoist)
        {
            ceilingJoistLength = longCeilingJoist;
        }
        count += (int) Math.ceil((carport.getLength() - (ceilingJoistWidth * 2)) / maxLengthBetweenCeilingJoist); //rounds up always to ensure there is no more than 60 cm between rafters
        result.put(ceilingJoistLength, count);

        return result;
    }

    @Override
    public HashMap<Double, Integer> calculateFasciaBoardLength(Carport carport)
    {
        HashMap<Double, Integer> result = new HashMap<>();
        double shortFasciaBoard = 360.0; //shortest FasciaBoard in database
        double longFasciaBoard = 540.0; //longest FasciaBoard in database

        int shortFasciaCount = 0;
        int longFasciaCount = 0;

        if (carport.getLength() <= shortFasciaBoard)
        {
            shortFasciaCount += 2;
        }
        else if (carport.getLength() > shortFasciaBoard && carport.getLength() <= longFasciaBoard)
        {
            longFasciaCount += 2;
        }
        else if (carport.getLength() <= 690) // max length for carport to account of join
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
            result.put(shortFasciaBoard, shortFasciaCount);
        }
        if (longFasciaCount != 0)
        {
            result.put(longFasciaBoard, longFasciaCount);
        }

        return result;
    }

    @Override
    public HashMap<Double, Integer> calculateFasciaBoardWidth(Carport carport)
    {
        HashMap<Double, Integer> result = new HashMap<>();
        double shortFasciaBoard = 360.0; //shortest FasciaBoard in database
        double longFasciaBoard = 540.0; //longest FasciaBoard in database

        int shortFasciaCount = 0;
        int longFasciaCount = 0;

        if (carport.getWidth() <= shortFasciaBoard)
        {
            shortFasciaCount += 2;
        }
        else if (carport.getWidth() > shortFasciaBoard && carport.getWidth() <= longFasciaBoard)
        {
            longFasciaCount += 2;
        }
        else if (carport.getWidth() <= 690) // max length for carport to account of join
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
            result.put(shortFasciaBoard, shortFasciaCount);
        }
        if (longFasciaCount != 0)
        {
            result.put(longFasciaBoard, longFasciaCount);
        }

        return result;
    }

    @Override
    public HashMap<Double, Integer> calculateBlocking(Carport carport)
    {
        HashMap<Double, Integer> result = new HashMap<>();
        int blockingMaxLength = 270; //Length between posts on shed cant be larger than 270, if so, a posts it put in the middle

        double shortBlockingLength = 240.0;
        double longBlockingLength = 270.0;

        int shortBlockingCount = 0;
        int longBlockingCount = 0;

        if (carport.getShedWidth() > blockingMaxLength)
        {
            if (carport.getShedWidth() / 2 > shortBlockingLength)
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

        if (carport.getShedLength() > blockingMaxLength)
        {
            if (carport.getShedLength() / 2 > shortBlockingLength)
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
            result.put(shortBlockingLength, shortBlockingCount);
        }
        if (longBlockingCount != 0)
        {
            result.put(longBlockingLength, longBlockingCount);
        }

        return result;
    }

    @Override
    public int calculateSidingBoard(Carport carport)
    {
        double shedTotalLength = (carport.getShedLength() * 2) + (carport.getShedWidth() * 2);

        //Math is done by extrapolating from documentation provided by product owner:
        //our shed in delivered material is 530x210, and they deliver 200 pieces of lumber for siding.
        //so we take total length of shed / 200, and get a coverage pr board of 7.4cm.
        return (int) Math.ceil(shedTotalLength / 7.4);
    }

    @Override
    public HashMap<Double, Integer> calculateRoofPlates(Carport carport)
    {
        //2 lengths of Roof Plates in database, 360 and 600.
        double shortRoofPlate = 360.0; // 360 is the shortest roof Plate in database
        double longRoofPlate = 600.0; // 600 is the longest Top Plate in database
        int overhang = 5; //5 cm overhang on backside of carport for running of water, src: documentation provided by product owner

        HashMap<Double, Integer> result = new HashMap<>();
        int count = (int) Math.ceil(carport.getWidth() / 100); //each plate is 109, and need overlay, so accounting for waste rounds up, src: documentation provided by product owner

        if (carport.getLength() <= (shortRoofPlate - overhang))
        {
            result.put(shortRoofPlate, count);
        }

        if (carport.getLength() > shortRoofPlate && carport.getLength() <= (longRoofPlate - overhang))
        {
            result.put(longRoofPlate, count);
        }

        if (carport.getLength() > longRoofPlate && carport.getLength() <= 700 - overhang) // 700 is cut off for carport length, due to 20cm overlay from plates according to documentation provided by product owner
        {
            result.put(shortRoofPlate, (count * 2)); //needs double amount because of 2 rows and overlay
        }

        if (carport.getLength() > 700)
        {
            result.put(longRoofPlate, count);
            result.put(shortRoofPlate, count);
        }

        return result;
    }

    @Override
    public int calculateRoofPlateScrews(Carport carport)
    {
        //Insert return in helper method to calculate packs
        return (int) Math.ceil((((carport.getWidth() / 100) * (carport.getLength() / 100)) * 12)); //documentation says 12 screws pr squaremeter of roofplate
    }

    @Override
    public int calculateBolts(int posts, HashMap<Double, Integer> topPlates)
    {
        int result = posts * 2; //documentation says 2 bolts for each posts
        double shortTopPlate = 480.0; // 480 is the shortest Top Plate in database
        double longTopPlate = 600.0; //600 is the of the longest Top Plate in database

        if (topPlates.containsKey(shortTopPlate) && topPlates.containsKey(longTopPlate)) // for joins need 2 additional bolt, if hastmap has both lengths there is a join
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
    public int calculateFittings(int ceilingJoist)
    {
        int result = 0;
        //Uses ceiling joist count for getting x right and x left fittings.
        return result;
    }

    @Override
    public int calculatePerforatedStrip(Carport carport)
    {
        double a = carport.getLength() / 100; //converts to M
        double b = carport.getWidth() / 100; //converts to M

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
        return (int) Math.ceil(screws / packsize);
    }

    @Override
    public int sumHashMapValues(HashMap<?, Integer> map)
    {
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }
}


/*
Materialer forbrug :
hvis skur er mere end 270 bred skal der ekstra stolpe til montering af løsholte(tværstiver)

Læg remmen i udskæringen på toppen af stolperne (fasthold evt. med skruetvinger under
monteringen) og bor hul til bræddebolte, 2 stk. pr. stolpe,

Bemærk at remmen samles af 2 stykker, over den stolpe der er mellem skur og carport,
Samlingen centreres over stolpen og der anvendes i alt 4 bolte til denne samling.

Montér universalbeslagene med vinklen mod bagsiden af spær/ovenpå rem, og fladen på
indvendigt side af rem.(se tegning) Alle beslag monteres med 3 beslags skruer pr. flade i
beslaget. Vær opmærksom på at der er højre og venstre beslag. Ved det bagerste spær monteres
beslaget på spærets forside af hensyn til stern eller beklædning.

minimum 200 mm overlap og altid midt over lægte.

mmax 700 mm melen monterede bræder


 */