package app.services;

import app.entities.Carport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MaterialListServiceImplTest
{
    MaterialListServiceImpl materialListService = new MaterialListServiceImpl();

    @BeforeEach
    void setUp()
    {

    }

    @DisplayName("Delivered Material: Carport size (6m x 7.8m) + shed (5.3m x 2.1m)")
    @Test
    void calculatePosts()
    {
        Carport carport = new Carport(1, 600,780,225,true,530,210,"");

        int actual = materialListService.calculatePosts(carport);
        int expected = 11; //From delivered material
        assertEquals(expected,actual);
    }

    @DisplayName("Max carport size (6m x 7.8m) + max shed (5.3m x 5.1m)")
    @Test
    void calculatePostsMaxCarportMaxShedSize()
    {
        Carport carport = new Carport(1, 600,780,225,true,530,510,"");

        int actual = materialListService.calculatePosts(carport);
        int expected = 11; //From delivered material
        assertEquals(expected,actual);
    }

    @DisplayName("Max size carport, no shed")
    @Test
    void calculatePostsMaxSize()
    {
        Carport carport = new Carport(1, 600,780,225,false,"");

        int actual = materialListService.calculatePosts(carport);
        int expected = 6;

        assertEquals(expected,actual);
    }

    @DisplayName("Carport over 510, no shed")
    @Test
    void calculatePostsOver510NoShed()
    {
        Carport carport = new Carport(1, 600,520,225,false,"");

        int actual = materialListService.calculatePosts(carport);
        int expected = 6;

        assertEquals(expected,actual);
    }


    @DisplayName("Carport under 510 with no shed")
    @Test
    void calculatePostUnder510NoShed()
    {
        Carport carport = new Carport(1, 600,500,225,false,"");

        int actual = materialListService.calculatePosts(carport);
        int expected = 4;

        assertEquals(expected,actual);
    }


    @DisplayName("Max size carport with small shed")
    @Test
    void calculatePostMaxSizeSmallShed()
    {
        Carport carport = new Carport(1, 600,780,225,true,120,120,"");

        int actual = materialListService.calculatePosts(carport);
        int expected = 9;

        assertEquals(expected,actual);
    }


}