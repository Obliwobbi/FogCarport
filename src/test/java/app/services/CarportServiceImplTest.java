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

        assertThrows(IllegalArgumentException.class,
                () -> service.validateAndBuildCarport(null, 240, 240, 225, true, 120.0, 120.0, ""));
    }

    @DisplayName("Medium carport 330x330 with medium shed 210x210")
    @Test
    void testValidateShedTotalSize_MediumCarport_MediumShed()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.validateAndBuildCarport(null, 330, 330, 225, true, 210.0, 210.0, ""));
    }

    @DisplayName("Large carport 600x780 with shed 300x300 - space in front")
    @Test
    void testValidateShedTotalSize_LargeCarport_SpaceInFront()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);

        Carport result = service.validateAndBuildCarport(null,  600,780, 225, true, 300.0, 300.0, "");
        assertNotNull(result);
        assertEquals(780, result.getLength());
        assertEquals(600, result.getWidth());
    }

    @DisplayName("Carport 480x480 with max shed150x410")
    @Test
    void testValidateShedTotalSize_ExactMinimumSpaceBeside()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);

        Carport result = service.validateAndBuildCarport(null, 480, 480, 225, true, 150.0, 410.0, "");
        assertNotNull(result);
        assertEquals(480, result.getLength());
        assertEquals(480, result.getWidth());
    }

    @DisplayName("Carport 540x540 with shed 300x300 - exactly 240cm in front")
    @Test
    void testValidateShedTotalSize_ExactMinimumSpaceFront()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);

        Carport result = service.validateAndBuildCarport(null, 540, 540, 225, true, 300.0, 300.0, "");
        assertNotNull(result);
        assertEquals(540, result.getLength());
        assertEquals(540, result.getWidth());
    }

    @DisplayName("Carport 360x360 with shed too large - no 240x240 space")
    @Test
    void testValidateShedTotalSize_NoSpaceForCar()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.validateAndBuildCarport(null, 360, 360, 225, true, 300.0, 300.0, ""));
    }

    @DisplayName("Maximum carport 780x600 with maximum shed 510x540")
    @Test
    void testValidateShedTotalSize_MaximumDimensions()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);

        Carport result = service.validateAndBuildCarport(null, 600,780,  225, true, 510.0, 540.0, "");
        assertNotNull(result);
        assertEquals(780, result.getLength());
        assertEquals(600, result.getWidth());
    }

    @DisplayName("validate input, Throws illegal argument")
    @Test
    void testValidateMeasurementInputTwo()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.validateAndBuildCarport(null, 200, 200, 225, false, null, null, ""));
    }
}
