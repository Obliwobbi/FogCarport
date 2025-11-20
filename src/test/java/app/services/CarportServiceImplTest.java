package app.services;

import app.entities.Carport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarportServiceImplTest
{

    @DisplayName("Carport Size Requirements fits")
    @Test
    void testvalidateShedTotalSize()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);

        Carport carport = new Carport(1,480,480,225,true,240,240,"");

        assertTrue(service.validateShedTotalSize(carport.getLength(), carport.getWidth(),carport.getShedLength(), carport.getShedWidth()));
    }

    @DisplayName("Carport Size Requirements Fails")
    @Test
    void testvalidateShedTotalSizeTwo()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);

        Carport carport = new Carport(1,240,240,225,true,240,240,"");

        assertThrows(IllegalArgumentException.class, () -> service.validateShedTotalSize(carport.getLength(), carport.getWidth(),carport.getShedLength(), carport.getShedWidth()));
    }

    @DisplayName("Carport Size Requirements Length under 240")
    @Test
    void testvalidateShedTotalSizeThree()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);

        Carport carport = new Carport(1,480,400,225,true,240,240,"");

        assertThrows(IllegalArgumentException.class, () -> service.validateShedTotalSize(carport.getLength(), carport.getWidth(),carport.getShedLength(), carport.getShedWidth()));
    }

    @DisplayName("Carport Size Requirements Width under 240")
    @Test
    void testvalidateShedTotalSizeFour()
    {
        CarportServiceImpl service = new CarportServiceImpl(null);

        Carport carport = new Carport(1,400,480,225,true,240,240,"");

        assertThrows(IllegalArgumentException.class, () -> service.validateShedTotalSize(carport.getLength(), carport.getWidth(),carport.getShedLength(), carport.getShedWidth()));
    }
}