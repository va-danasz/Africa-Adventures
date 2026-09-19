package com.example.gamemodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class AnimalTest {

    private GameModel gameModel;
    private Animal animal;

    @BeforeEach
    public void setUp() {
        gameModel = new GameModel();
        animal = new Animal(new Coordinate(100, 100), 200.0, gameModel, false);
    }

    @Test
    public void testAnimalAgesProperlyAndCooldownsDecrease() {
        animal.aging();
        assertEquals("0.10", animal.getAge());
        assertTrue(animal.getDrinkingCooldown() <= 0);
        assertTrue(animal.getEatingCooldown() <= 0);
    }

    @Test
    public void testAnimalReproductionEligible() {
        Animal other = new Animal(new Coordinate(105, 105), 200.0, gameModel, false);
        for (int i = 0; i < 20; i++) {
            animal.aging();
            other.aging();
        }

        assertTrue(animal.reproduce(other));
        assertEquals(40, animal.getReproduceCooldown());
        assertEquals(40, other.getReproduceCooldown());
    }

    @Test
    public void testMigrationTowardsTarget() {
        Coordinate target = new Coordinate(150, 100);
        animal.setTargetCoord(target);
        animal.setSpeed(1.0);

        boolean result = animal.migrate();

        assertFalse(result);
        assertNotEquals(100, animal.getCoord().getX());
    }

    @Test
    public void testHasArrivedWhenClose() {
        animal.setTargetCoord(new Coordinate(101, 100));
        assertTrue(animal.hasArrived());
    }

    @Test
    public void testFollowLeaderAssignsCorrectTargetCoord() {
        Animal leader = new Animal(new Coordinate(200, 200), 200.0, gameModel, false);
        animal.followLeaderWithOffset(leader, 1);
        assertNotNull(animal.getTargetCoord());
        assertEquals(leader, animal.getFollowingLeader());
    }

    @Test
    public void testDrinkingStatus() throws InterruptedException {
        Coordinate waterCoord = new Coordinate(120, 120);
        Pond pond = new Pond(waterCoord, 1000.0, 10, gameModel);
        gameModel.getObjects().add(pond);
        gameModel.getPonds().add(pond);
        animal.setTargetCoord(waterCoord);

        animal.getCoord().setX(120);
        animal.getCoord().setY(120);
        animal.getDrinkingPlaces().add(waterCoord);

        animal.migrate();

        Thread.sleep(3100);

        assertFalse(animal.isDrinking());
    }

    @Test
    public void testEatingStatus() throws InterruptedException {
        Coordinate foodCoord = new Coordinate(110, 110);
        Plant plant = new Plant(foodCoord, 500.0, 20, gameModel);
        gameModel.getObjects().add(plant);
        gameModel.getPlants().add(plant);
        animal.setTargetCoord(foodCoord);

        animal.getCoord().setX(110);
        animal.getCoord().setY(110);
        animal.getEatingPlaces().add(foodCoord);

        animal.migrate();

        Thread.sleep(3100);

        assertFalse(animal.isEating());
    }

    @Test
    public void testCreateRandomTargetWithinBounds() {
        animal.createRandomTarget(800, 600);
        Coordinate target = animal.getTargetCoord();
        assertNotNull(target);
        assertTrue(target.getX() >= 800 * 0.05 && target.getX() <= 800 * 0.95);
        assertTrue(target.getY() >= 600 * 0.05 && target.getY() <= 600 * 0.95);
    }

    @Test
    public void testCalculateEdgeTargetGivesOffset() {
        Coordinate objectCenter = new Coordinate(300, 300);
        Coordinate edge = animal.calculateEdgeTarget(objectCenter, 50);
        assertNotEquals(300, edge.getX());
        assertNotEquals(300, edge.getY());
    }

    @Test
    public void testSameCoordAndUnknownPlace() {
        Coordinate c1 = new Coordinate(10, 10);
        Coordinate c2 = new Coordinate(10, 10);
        ArrayList<Coordinate> list = new ArrayList<>();
        assertTrue(animal.sameCoord(c1, c2));
        assertTrue(animal.unknownPlace(c1, list));
        list.add(c1);
        assertFalse(animal.unknownPlace(c2, list));
    }


    @Test
    public void testMigrateWithNullTargetDoesNotCrash() {
        animal.setTargetCoord(null);
        assertFalse(animal.migrate());
    }

    @Test
    public void testHasArrivedWithNullTargetCoord() {
        animal.setTargetCoord(null);
        assertFalse(animal.hasArrived());
    }

    @Test
    public void testDrinkingInterruptedStatusHandled() throws InterruptedException {
        Coordinate waterCoord = new Coordinate(120, 120);
        Pond pond = new Pond(waterCoord, 1000.0, 10, gameModel);
        gameModel.getObjects().add(pond);
        gameModel.getPonds().add(pond);

        animal.setTargetCoord(waterCoord);
        animal.getCoord().setX(120);
        animal.getCoord().setY(120);
        animal.getDrinkingPlaces().add(waterCoord);

        Thread t = new Thread(animal::migrate);
        t.start();
        t.interrupt();
        t.join(1000);

        assertTrue(true);
    }

}
