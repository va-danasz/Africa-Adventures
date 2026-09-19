package com.example.gamemodel;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RangerTest {
    private GameModel gameModel;
    private Ranger ranger;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        double sceneHeight = 600;
        double sceneWidth = 800;
        ranger = new Ranger(new Coordinate(100, 100), 1000, gameModel, sceneWidth, sceneHeight);
    }

    @Test
    void testInitialValues() {
        assertEquals(100, ranger.getHp());
        assertEquals(1000, ranger.getPrice());
        assertEquals(CanBuy.RANGER, ranger.getType());
        assertEquals(200.0, ranger.getVision());
        assertFalse(ranger.isInTargetingMode());
    }

    @Test
    void testPutAnimalNotAlreadyHunting() {
        Animal animal = new Animal(new Coordinate(150, 150), 200.0, gameModel, true);
        ranger.putAnimalNotAlreadyHunting(animal);
        List<Animal> targets = ranger.getTargets();
        assertEquals(1, targets.size());
        assertTrue(targets.contains(animal));
    }


    @Test
    void testSetTargetingMode() {
        ranger.setTargetingMode(true);
        assertTrue(ranger.isInTargetingMode());
    }


    @Test
    void testPoacherBattle() {
        Poacher poacher = new Poacher(new Coordinate(100, 100), 100, 1.5, false, gameModel, 1800, 900);
        gameModel.getPoachers().add(poacher);
        ranger.hunt();
        assertTrue(ranger.getHp() < 100 || poacher.getHp() < 100);
    }

    @Test
    void testWandering() {
        ranger.hunt();
        assertNotNull(ranger.getTargetCoord());
    }

    @Test
    void testPutAnimalAlreadyTargetedDoesNotDuplicate() {
        Animal animal = new Animal(new Coordinate(150, 150), 200.0, gameModel, true);
        ranger.putAnimalNotAlreadyHunting(animal);
        ranger.putAnimalNotAlreadyHunting(animal); // try again
        assertEquals(1, ranger.getTargets().size());
    }

    @Test
    void testHuntWithNoPoachersDoesNotThrow() {
        assertDoesNotThrow(() -> ranger.hunt());
    }

    @Test
    void testWanderingWithNullTargetDoesNotThrow() {
        assertDoesNotThrow(() -> ranger.hunt());
    }
}
