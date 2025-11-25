package app.services;

import app.entities.Carport;

import java.util.HashMap;

public class MaterialListServiceImpl implements MaterialListService
{

    public int calculatePosts(Carport carport)
    {
        int result = 4;

        if (carport.isWithShed())
        {
            result += 3; //door

            if (carport.getShedWidth() > 270)
            {
                result += 2;
            }
            if (carport.getShedLength() > 270)
            {
                result += 2;
            }
            //Calculation of remaining length between shed and corner post due to max length of Wall Plate(Rem træ).
            double remainingLength = (carport.getLength() - 100.0) - carport.getShedLength();
            if (remainingLength > 310)
            {
                result += 2;
            }
        } else
        {
            if (carport.getLength() > 510)
            {
                result += 2;
            }
        }
        return result;
    }

    public int calculateCeilingJoist(Carport carport)
    {
        int result = 2; //one for each for the ends
        double carportLength = carport.getLength();

        result += (int) Math.ceil((carportLength - 9) / 60); //rounds up always to ensure there is no more than 60 cm between rafters

        return result;
    }

    public HashMap<Double, Integer> calculateTopPlate(Carport carport)
    {
        HashMap<Double, Integer> result = new HashMap<>();

        if (!carport.isWithShed())
        {
            if (carport.getLength() <= 480)
            {
                result.put(480.0, 2);
            }
            else if (carport.getLength() > 480 && carport.getLength() <= 600)
            {
                result.put(600.0, 2);
            }
            else
            {
                result.put(480.0, 4);
            }
        }
        else
        {
            if (carport.getLength() > 600 && carport.getShedWidth() == (carport.getWidth() - 30))
            {
                result.put(600.0, 2);
                result.put(480.0, 1);
            }
            else if (carport.getLength() > 480 && carport.getLength() <= 600)
            {
                result.put(600.0, 2);
            }
            else if (carport.getLength() <= 480 && carport.getShedWidth() <= (carport.getWidth() - 30))
            {
                result.put(480.0, 2);
            }
            else
            {
                result.put(600.0, 1);
                result.put(480.0, 3);
            }
        }
        return result;
    }

    public int calculateBlocking(Carport carport)
    {
        int result = 0;

        return result;
    }

    public int calculateSidingBoard(Carport carport)
    {
        int result = 0;

        return result;
    }

    public HashMap<Double, Integer> calculateRoofPlates(Carport carport)
    {
        //tagplader :
        //Start med at lægge pladerne løst op, så de når ud til sternbrædderne i sider og front, bagerst skal
        //pladerne række ca. 5.cm ud over sternbrættet, med henblik på afvanding.

        HashMap<Double, Integer> result = new HashMap<>();
        int count = (int) Math.ceil(carport.getWidth()/100);

        if(carport.getLength() <= 355)
        {
            result.put(360.0,count);
        }

        if(carport.getLength() > 360 && carport.getLength() <=595)
        {
            result.put(600.0,count);
        }

        if(carport.getLength() > 600 && carport.getLength() <=695)
        {
            result.put(360.0,(count*2));
        }

        if(carport.getLength() > 700)
        {
            result.put(600.0,count);
            result.put(360.0,count);
        }

        return result;
    }

    public int calculateRoofPlateScrews(Carport carport)
    {
        //Insert return in helper method to calculate packs
        return (int) Math.ceil( ((carport.getWidth()* carport.getLength())/100) * 15);
    }

    public int calculateBolts(int posts, HashMap<Double, Integer> topPlates)
    {
        int result = posts * 2;

        if (topPlates.containsKey(480.0) && topPlates.containsKey(600.0))
        {
            result += 4;
        }
        else if (topPlates.containsValue(4))
        {
            result += 4;
        }
            return result;
    }

    public int calculateFittings(int ceilingJoist)
    {
        int result = 0;
        //Uses ceiling joist count for getting x right and x left fittings.
        return result;
    }

    public int calculateFittingsScrewsNeeded(int fittings, int screws)
    {
        return fittings * screws;
    }

    public int calculateScrewPacks(int packsize, int screws)
    {
        return (int) Math.ceil(screws/packsize);
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