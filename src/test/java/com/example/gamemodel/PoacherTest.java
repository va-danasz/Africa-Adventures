package com.example.gamemodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class PoacherTest {

    private GameModel gameModel;
    private Poacher poacher;
    private final double sceneWidth = 800;
    private final double sceneHeight = 600;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        poacher = new Poacher(new Coordinate(100, 100), 100, 1.5, false, gameModel, sceneWidth, sceneHeight);
    }

    @Test
    void testInitialValues() {
        assertEquals(100, poacher.getHp());
        assertFalse(poacher.getIsCaptured());
        assertNotNull(poacher.getCoord());
        assertFalse(poacher.isEscaping());
    }

    @Test
    void testHuntAddsVisibleAnimal() {
        Animal animal = new Animal(new Coordinate(120, 120), 200.0, gameModel, true);
        ArrayList<Animal> animals = new ArrayList<>();
        animals.add(animal);
        poacher.hunt(animals);
        assertTrue(poacher.containsTarget(animal));
    }

    @Test
    void testHuntDoesNotAddOutOfRangeAnimal() {
        Animal animal = new Animal(new Coordinate(1000, 1000), 200.0, gameModel, true);
        ArrayList<Animal> animals = new ArrayList<>();
        animals.add(animal);
        poacher.hunt(animals);
        assertFalse(poacher.containsTarget(animal));
    }

    @Test
    void testEscapeReturnsTrueNearEdge() {
        Poacher edgePoacher = new Poacher(new Coordinate(5, 5), 100, 1, false, gameModel, sceneWidth, sceneHeight);
        assertTrue(edgePoacher.escape());
    }

    @Test
    void testEscapeReturnsFalseOtherwise() {
        assertFalse(poacher.escape());
    }

    @Test
    void testSetAndGetCapturedAnimal() {
        Animal animal = new Animal(new Coordinate(200, 200), 200.0, gameModel, true);
        poacher.setCapturedAnimal(animal);
        assertEquals(animal, poacher.getCapturedAnimal());
    }

    @Test
    void testHpSetterBounds() {
        poacher.setHp(-50);
        assertEquals(0, poacher.getHp());
    }

    @Test
    void testIsVisibleForRangersTrue() {
        Ranger ranger = new Ranger(new Coordinate(105, 105), 1000, gameModel, sceneWidth, sceneHeight);
        ArrayList<Ranger> rangers = new ArrayList<>();
        rangers.add(ranger);
        assertTrue(poacher.isVisibleForRangers(rangers));
    }

    @Test
    void testIsVisibleForRangersFalseWhenCaptured() {
        poacher.setCaptured(true);
        Ranger ranger = new Ranger(new Coordinate(105, 105), 1000, gameModel, sceneWidth, sceneHeight);
        ArrayList<Ranger> rangers = new ArrayList<>();
        rangers.add(ranger);
        assertFalse(poacher.isVisibleForRangers(rangers));
    }

    @Test
    void testCaptureAnimalRemovesAnimalFromTargets() {
        Animal animal = new Animal(new Coordinate(110, 110), 200.0, gameModel, true);
        ArrayList<Animal> animals = new ArrayList<>();
        animals.add(animal);
        poacher.hunt(animals);
        assertTrue(poacher.containsTarget(animal));
        poacher.captureAnimal(animal);
        assertFalse(poacher.containsTarget(animal));
    }

    @Test
    void testCaptureAnimalNullTargetDoesNotThrow() {
        assertDoesNotThrow(() -> poacher.captureAnimal(null));
    }

}
