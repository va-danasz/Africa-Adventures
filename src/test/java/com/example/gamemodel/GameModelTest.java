package com.example.gamemodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

public class GameModelTest {

    private GameModel model;

    @BeforeEach
    public void setUp() {
        model = new GameModel();
    }

    @Test
    public void testAddAnimal() {
        Animal zebra = new Zebra(new Coordinate(100, 100), 1000, model);
        model.addAnimal(zebra);
        assertTrue(model.getAnimals().contains(zebra));
    }

    @Test
    public void testRemoveAnimal() {
        Animal lion = new Lion(new Coordinate(200, 200), 2000, model);
        model.addAnimal(lion);
        model.removeAnimal(lion);

        assertFalse(model.getAnimals().contains(lion));
        assertTrue(model.getAnimalsToRemove().contains(lion));
    }

    @Test
    public void testAddRanger() {
        Ranger ranger = new Ranger(new Coordinate(150, 150), 300.0, model, 1800, 900);
        model.addRanger(ranger);
        assertTrue(model.getRangers().contains(ranger));
    }

    @Test
    public void testRemoveDeadRanger() {
        Ranger ranger = new Ranger(new Coordinate(120, 120), 300.0, model, 1800, 900);
        model.addRanger(ranger);
        model.markRanger(ranger);
        model.removeDeadRangers();

        assertFalse(model.getRangers().contains(ranger));
        assertTrue(model.getRangersToRemove().contains(ranger));
    }

    @Test
    public void testGenerateObject() {
        int initialCount = model.getObjects().size();
        Buyable b = model.generateObject(new Random(), 1000, 1000);
        assertNotNull(b);
        assertTrue(model.getObjects().contains(b));
        assertTrue(model.getPlants().contains(b) || model.getPonds().contains(b));
        assertEquals(initialCount + 1, model.getObjects().size());
    }

    @Test
    public void testCreateAnimalOffspring() {
        Animal parent = new Zebra(new Coordinate(300, 300), 500, model);
        Animal baby = model.createAnimal(parent);

        assertNotNull(baby);
        assertEquals(parent.getClass(), baby.getClass());
        assertEquals(parent.getPrice(), baby.getPrice());
    }

    @Test
    public void testGetChipped() {
        Animal cheetah = new Cheetah(new Coordinate(100, 100), 500, model);
        model.getChipped(cheetah);

        assertTrue(cheetah.isChipped());
        assertEquals(3500.0, model.getCurrentMoney(), 0.01);
    }

    @Test
    public void testSellAnimal() {
        Animal lion = new Lion(new Coordinate(100, 100), 2000, model);
        model.addAnimal(lion);
        model.sellAnimal(lion);

        assertFalse(model.getAnimals().contains(lion));
        assertEquals(5000.0, model.getCurrentMoney(), 0.01);
    }

    @Test
    public void testWinConditionFalseInitially() {
        model.setDifficulty(Difficulty.EASY);
        assertFalse(model.win());
    }

    @Test
    public void testGameOverConditions() {
        model.setTimeCounter(2);
        assertEquals(1, model.gameOver());
    }

    @Test
    public void testAddVisitorCount() {
        model.addVisitorCount(3);
        model.setDifficulty(Difficulty.EASY);
        model.win();
    }

    @Test
    public void testCaptureAnimal() {
        Animal a = new Cheetah(new Coordinate(100, 100), 400, model);
        Poacher p = new Poacher(new Coordinate(110, 110), 100, 1, false, model, 1800, 900);
        model.addAnimal(a);
        model.captureAnimal(a, p);

        assertTrue(a.isCaptured());
        assertTrue(p.isEscaping());
        assertEquals(a, p.getCapturedAnimal());
    }
}
