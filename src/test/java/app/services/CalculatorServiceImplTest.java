package app.services;

import app.entities.Carport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceImplTest
{
    CalculatorServiceImpl materialListService = new CalculatorServiceImpl();

    @BeforeEach
    void setUp()
    {

    }

    // ************************ TESTING OF: POSTS ************************

    @DisplayName("Delivered Material: Carport size (6m x 7.8m) + shed (5.3m x 2.1m)")
    @Test
    void calculatePosts()
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 530, 210, "");

        int actual = materialListService.calculatePosts(carport);
        int expected = 11; //From delivered material
        assertEquals(expected, actual);
    }

    @DisplayName("Max carport size (6m x 7.8m) + max shed (5.3m x 5.1m)")
    @Test
    void calculatePostsMaxCarportMaxShedSize()
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 530, 510, "");

        int actual = materialListService.calculatePosts(carport);
        int expected = 11; //From delivered material
        assertEquals(expected, actual);
    }

    @DisplayName("Max size carport, no shed")
    @Test
    void calculatePostsMaxSize()
    {
        Carport carport = new Carport(1, 600, 780, 225, false, "");

        int actual = materialListService.calculatePosts(carport);
        int expected = 6;

        assertEquals(expected, actual);
    }

    @DisplayName("Carport over 510, no shed")
    @Test
    void calculatePostsOver510NoShed()
    {
        Carport carport = new Carport(1, 600, 520, 225, false, "");

        int actual = materialListService.calculatePosts(carport);
        int expected = 6;

        assertEquals(expected, actual);
    }


    @DisplayName("Carport under 510 with no shed")
    @Test
    void calculatePostUnder510NoShed()
    {
        Carport carport = new Carport(1, 600, 500, 225, false, "");

        int actual = materialListService.calculatePosts(carport);
        int expected = 4;

        assertEquals(expected, actual);
    }


    @DisplayName("Max size carport with small shed")
    @Test
    void calculatePostMaxSizeSmallShed()
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 120, 120, "");

        int actual = materialListService.calculatePosts(carport);
        int expected = 9;

        assertEquals(expected, actual);
    }

    // ************************ TESTING OF: CEILING JOISTS ************************

    @DisplayName("Calculate ceiling Joist, Delivered Material: Carport size (6m x 7.8m) + shed (5.3m x 2.1m)")
    @Test
    void calculateCeilingJoist()
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 530, 210, "");

        int actual = materialListService.calculateCeilingJoist(carport);
        int expected = 15;

        assertEquals(expected, actual);
    }

    @DisplayName("Calculate ceiling Joist, where carport.length == 510, so before rounding up should be 8.35 + 2 joists needed")
    @Test
    void calculateCeilingJoistTwo()
    {
        Carport carport = new Carport(1, 600, 510, 225, true, 530, 210, "");

        // 510 - 9 (to account for a joist in both ends. then 501/60(max length between joints) = 8.35. method rounds to 9 and add the 2 for the ends.

        int actual = materialListService.calculateCeilingJoist(carport);
        int expected = 11;

        assertEquals(expected, actual);
    }

    // ************************ TESTING OF: TOP PLATES ************************

    @DisplayName("Top plate: no shed, short carport")
    @Test
    public void calculateTopPlate_Short()
    {
        Carport carport = new Carport(1, 300, 480, 225, false, 0, 0, "");
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(2, result.get(480.0));
        assertNull(result.get(600.0));
    }

    @DisplayName("Top plate: no shed, medium carport")
    @Test
    public void calculateTopPlate_Medium()
    {
        Carport carport = new Carport(1, 300, 600, 225, false, 0, 0, "");
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(2, result.get(600.0));
        assertNull(result.get(480.0));
    }

    @DisplayName("Top plate: no shed, long carport")
    @Test
    public void calculateTopPlate_Long()
    {
        Carport carport = new Carport(1, 300, 780, 225, false, 0, 0, "");
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(4, result.get(480.0));
        assertNull(result.get(600.0));
    }

    @DisplayName("Top plate: Delivered Material: Carport size (6m x 7.8m) + shed (5.3m x 2.1m)")
    @Test
    public void calculateTopPlate_LongFullWidth()
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 570, 210, ""); // shedWidth = 600-30 = 570
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(2, result.get(600.0));
        assertEquals(1, result.get(480.0));
    }

    @DisplayName("Top plate: full width shed, short carport")
    @Test
    public void calculateTopPlate_ShortFullWidth()
    {
        Carport carport = new Carport(1, 300, 480, 225, true, 270, 210, ""); // shedWidth = 300-30 = 270
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(2, result.get(480.0));
        assertNull(result.get(600.0));
    }

    @DisplayName("Top plate: full width shed, medium carport")
    @Test
    public void calculateTopPlate_MediumFullWidth()
    {
        Carport carport = new Carport(1, 450, 580, 225, true, 420, 300, ""); // shedWidth = 450-30 = 420
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(2, result.get(600.0));
        assertNull(result.get(480.0));
    }

    @DisplayName("Top plate: partial width shed, long carport")
    @Test
    public void calculateTopPlate_LongPartialWidth()
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 210, 210, ""); // shedWidth = 210, much less than 570
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(1, result.get(600.0));
        assertEquals(3, result.get(480.0));
    }

    @DisplayName("Top plate: partial width shed, medium carport")
    @Test
    public void calculateTopPlate_ShortPartialWidth()
    {
        Carport carport = new Carport(1, 600, 480, 225, true, 240, 210, ""); // shedWidth = 240, less than 570
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(2, result.get(480.0));
        assertNull(result.get(600.0));
    }

    @DisplayName("Top plate: partial width shed, medium carport")
    @Test
    public void calculateTopPlate_MediumPartialWidth()
    {
        Carport carport = new Carport(1, 450, 540, 225, true, 180, 300, ""); // shedWidth = 180, less than 420
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(2, result.get(600.0));
        assertNull(result.get(480.0));
    }

    // Edge cases
    @DisplayName("Top plate: partial width shed, exactly 600cm carport")
    @Test
    public void calculateTopPlate_Exactly600_FullWidth()
    {
        Carport carport = new Carport(1, 240, 600, 225, true, 210, 300, ""); // shedWidth = 240-30 = 210
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(2, result.get(600.0));
        assertNull(result.get(480.0));
    }

    @DisplayName("Top plate: full width shed, 630cm carport")
    @Test
    public void calculateTopPlate_FullWidth()
    {
        Carport carport = new Carport(1, 300, 630, 225, true, 270, 300, ""); // shedWidth = 300-30 = 270
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(2, result.get(600.0));
        assertEquals(1, result.get(480.0));
    }

    @DisplayName("Top plate: partial width shed, 630cm carport")
    @Test
    public void calculateTopPlate_PartialWidth()
    {
        Carport carport = new Carport(1, 600, 630, 225, true, 210, 300, ""); // shedWidth = 210, much less than 570
        HashMap<Double, Integer> result = materialListService.calculateTopPlate(carport);

        assertEquals(1, result.get(600.0));
        assertEquals(3, result.get(480.0));
    }

    // ************************ TESTING OF: SHED BLOCKING ************************

    @DisplayName("Shed Blocking: Delivered Material: Length: 780cm, Width: 600 carport, Length: 530cm, Width: 210cm shed.")
    @Test
    public void calculateShedBlockingDeliveredMaterial()
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 530,210,"");
        HashMap<Double,Integer> result = materialListService.calculateBlocking(carport);

        assertEquals(4,result.get(240.0));
        assertEquals(12,result.get(270.0));
    }

    @DisplayName("Shed Blocking: Length: 780cm, Width: 600 carport, Length: 530cm, Width: 210cm shed.")
    @Test
    public void calculateShedBlockingShortShed()
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 210,210,"");
        HashMap<Double,Integer> result = materialListService.calculateBlocking(carport);

        assertEquals(8, result.get(240.0));
        assertNull(result.get(270.0));
    }

    @DisplayName("Shed Blocking: Length: 780cm, Width: 600 carport, Length: 530cm, Width: 210cm shed.")
    @Test
    public void calculateShedBlockingLongShed()
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 780,780,"");
        HashMap<Double,Integer> result = materialListService.calculateBlocking(carport);

        assertEquals(24, result.get(270.0));
        assertNull(result.get(240.0));
    }


    // ************************ TESTING OF: SHED SIDING ************************

    @DisplayName("Shed Siding: Deliverd Material: Length: 780cm, Width: 600 carport, Length: 530cm, Width: 210cm shed.")
    @Test
    public void calculateShedSidingDeliverdMaterial()
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 530,210,"");
        int shedSidingCount = materialListService.calculateSidingBoard(carport);

        assertEquals(200, shedSidingCount);
    }

    @DisplayName("Shed Siding: Deliverd Material: Length: 780cm, Width: 600 carport, Length: 530cm, Width: 210cm shed.")
    @Test
    public void calculateShedSidingBigShed()
    {
        Carport carport = new Carport(1, 600, 780, 225, true, 530,510,"");
        int shedSidingCount = materialListService.calculateSidingBoard(carport);

        assertEquals(282, shedSidingCount);
    }

    // ************************ TESTING OF: FASCIA BOARDS ************************

    @DisplayName("Fascia Board Length: Delivered material: Length: 780cm, Width: 600 carport")
    @Test
    public void calculateFasciaBoardsFullSizeLength()
    {
        Carport carport = new Carport(1, 600, 780, 225, false,"");
        HashMap<Double, Integer> result = materialListService.calculateFasciaBoardLength(carport);

        assertEquals(2, result.get(540.0));
        assertEquals(2, result.get(360.0));
    }

    @DisplayName("Fascia Board Width: Delivered material: Length: 780cm, Width: 600 carport")
    @Test
    public void calculateFasciaBoardsFullSizeWidth()
    {
        Carport carport = new Carport(1, 600, 780, 225, false,"");
        HashMap<Double, Integer> result = materialListService.calculateFasciaBoardWidth(carport);

        assertNull(result.get(540.0));
        assertEquals(4, result.get(360.0));
    }

    // ************************ TESTING OF: ROOF PLATES ************************

    @DisplayName("Roof plate: Delivered material: Length: 780cm, Width: 600 carport")
    @Test
    public void calculateRoofPlatesFullSize()
    {
        Carport carport = new Carport(1, 600, 780, 225, false,"");
        HashMap<Double, Integer> result = materialListService.calculateRoofPlates(carport);

        assertEquals(6, result.get(600.0));
        assertEquals(6, result.get(360.0));
    }

    @DisplayName("Roof plate: Length: 595cm, Width: 530 carport")
    @Test
    public void calculateRoofPlatesShorterWidth()
    {
        Carport carport = new Carport(1, 530, 595, 225, false,"");
        HashMap<Double, Integer> result = materialListService.calculateRoofPlates(carport);

        assertEquals(6, result.get(600.0));
        assertNull(result.get(360.0));
    }

    @DisplayName("Roof plate: Length: 695 cm, Width: 410 carport")
    @Test
    public void calculateRoofPlatesOnlyShortPlates()
    {
        Carport carport = new Carport(1, 410, 695, 225, false,"");
        HashMap<Double, Integer> result = materialListService.calculateRoofPlates(carport);

        assertNull(result.get(600.0));
        assertEquals(10, result.get(360.0));
    }

    @DisplayName("Roof plate: Length: 240cm, Width: 480 carport")
    @Test
    public void calculateRoofPlatesShortLength()
    {
        Carport carport = new Carport(1, 480, 240, 225, false,"");
        HashMap<Double, Integer> result = materialListService.calculateRoofPlates(carport);

        assertNull(result.get(600.0));
        assertEquals(5, result.get(360.0));
    }
}
