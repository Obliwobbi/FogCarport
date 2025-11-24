package app.services;

import app.entities.Carport;

public class MaterialListServiceImpl implements MaterialListService
{

    public int calculatePosts(Carport carport)
    {
        int result = 4;

        if (carport.isWithShed())
        {
            result += 1; //door
            
            if (carport.getShedWidth() > 270)
            {
                result += 2;
            }
            if (carport.getShedLength() > 270)
            {
                result += 2;
            }
            double remainingLength = (carport.getLength()-1)-carport.getShedLength();
            if(remainingLength > 310)
            {
                result += 2;
            }
        }
        else
        {
            if(carport.getLength() > 600)
            {
                result += 2;
            }

        }
        return result;
    }
}


/*
Materialer forbrug :
max 310 mellem stolper, første stolpe fra forside 1 meter inde.

hvis skur er mere end 270 bred skal der ekstra stolpe til montering af løsholte(tværstiver)

Læg remmen i udskæringen på toppen af stolperne (fasthold evt. med skruetvinger under
monteringen) og bor hul til bræddebolte, 2 stk. pr. stolpe,

Bemærk at remmen samles af 2 stykker, over den stole der er mellem skur og carport,
Samlingen centreres over stolpen og der anvendes i alt 4 bolte til denne samling.

Afstanden mellem spærene skal være ens max 60.cm

Montér universalbeslagene med vinklen mod bagsiden af spær/ovenpå rem, og fladen på
indvendigt side af rem.(se tegning) Alle beslag monteres med 3 beslags skruer pr. flade i
beslaget. Vær opmærksom på at der er højre og venstre beslag. Ved det bagerste spær monteres
beslaget på spærets forside af hensyn til stern eller beklædning.

OBS. Rem max 600 (også dem der bruges til spær)
Mindste i materialet udleveret er 480 cm

hulbånd monteres med 2 beslagskruer

tagplader :
Start med at lægge pladerne løst op, så de når ud til sternbrædderne i sider og front, bagerst skal
pladerne række ca. 5.cm ud over sternbrættet, med henblik på afvanding.

minimum 200 mm overlap og altid midt over lægte.

mmax 700 mm melen monterede bræder


 */