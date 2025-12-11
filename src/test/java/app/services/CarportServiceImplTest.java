package app.services;

import app.entities.Carport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarportServiceImplTest
{
    @DisplayName("Small carport 240x240 with small shed 120x120")
    @Test
    void testValidateShedTotalSize_SmallCarport_SmallShed()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);
        Carport carport = new Carport(1, 240, 240, 225, true, 120, 120, "");

        assertThrows(IllegalArgumentException.class,
                () -> service.validateShedTotalSize(carport.getLength(), carport.getWidth(), carport.getShedLength(), carport.getShedWidth()));
    }

    @DisplayName("Medium carport 330x330 with medium shed 210x210")
    @Test
    void testValidateShedTotalSize_MediumCarport_MediumShed()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);
        Carport carport = new Carport(1, 330, 330, 225, true, 210, 210, "");

        assertThrows(IllegalArgumentException.class,
                () -> service.validateShedTotalSize(carport.getLength(), carport.getWidth(), carport.getShedLength(), carport.getShedWidth()));
    }

    @DisplayName("Large carport 600x780 with shed 300x300 - space in front")
    @Test
    void testValidateShedTotalSize_LargeCarport_SpaceInFront()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);
        Carport carport = new Carport(1, 780, 600, 225, true, 300, 300, "");

        assertTrue(service.validateShedTotalSize(carport.getLength(), carport.getWidth(), carport.getShedLength(), carport.getShedWidth()));
    }

    @DisplayName("Carport 480x480 with max shed150x410")
    @Test
    void testValidateShedTotalSize_ExactMinimumSpaceBeside()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);
        Carport carport = new Carport(1, 480, 480, 225, true, 150, 410, "");

        assertTrue(service.validateShedTotalSize(carport.getLength(), carport.getWidth(), carport.getShedLength(), carport.getShedWidth()));
    }

    @DisplayName("Carport 540x540 with shed 300x300 - exactly 240cm in front")
    @Test
    void testValidateShedTotalSize_ExactMinimumSpaceFront()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);
        Carport carport = new Carport(1, 540, 540, 225, true, 300, 300, "");

        assertTrue(service.validateShedTotalSize(carport.getLength(), carport.getWidth(), carport.getShedLength(), carport.getShedWidth()));
    }

    @DisplayName("Carport 360x360 with shed too large - no 240x240 space")
    @Test
    void testValidateShedTotalSize_NoSpaceForCar()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);
        Carport carport = new Carport(1, 360, 360, 225, true, 300, 300, "");

        assertThrows(IllegalArgumentException.class,
                () -> service.validateShedTotalSize(carport.getLength(), carport.getWidth(), carport.getShedLength(), carport.getShedWidth()));
    }

    @DisplayName("Maximum carport 780x600 with maximum shed 510x540")
    @Test
    void testValidateShedTotalSize_MaximumDimensions()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);
        Carport carport = new Carport(1, 600, 780, 225, true, 510, 540, "");

        assertTrue(service.validateShedTotalSize(carport.getLength(), carport.getWidth(), carport.getShedLength(), carport.getShedWidth()));
    }

    @DisplayName("validate input, rounds down ")
    @Test
    void testValidateMeasurementInput()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);
        double actual = service.validateMeasurementInput(432, 240, 600);
        double expected = 420;

        assertEquals(expected, actual);

    }

    @DisplayName("validate input, rounds down from near max")
    @Test
    void testValidateMeasurementInputThree()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);
        double actual = service.validateMeasurementInput(439.9, 240, 600);
        double expected = 420;

        assertEquals(expected, actual);

    }
    @DisplayName("validaye input, input exceeds maximum, round to maximum")
    @Test
    void testValidateMeasurementInputOne()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);
        double actual = service.validateMeasurementInput(650, 240, 600);
        double expected = 600;

        assertEquals(expected, actual);

    }
    @DisplayName("validate input, Throws illegal argumant")
    @Test
    void testValidateMeasurementInputTwo()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);


        assertThrows(IllegalArgumentException.class,
                () -> service.validateMeasurementInput(200, 240, 600));

    }
}