package com.example.gamemodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class JeepTest {

    private Jeep jeep;
    private GameModel gameModel;
    private TouristPipeline pipeline;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        pipeline = new TouristPipeline();
        jeep = new Jeep(new Coordinate(0, 0), 500, pipeline, gameModel);

        gameModel.setSpeedModifier(1.0);
        gameModel.setExit(new Coordinate(100, 100));

        Path path = new Path(250);
        List<Integer> examplePath = List.of(0, 1, 2, 3);
        Set<List<Integer>> allPaths = new HashSet<>();
        allPaths.add(examplePath);
        path.setAllPaths(allPaths);

        Map<Integer, Coordinate> indexToCoord = new HashMap<>();
        indexToCoord.put(0, new Coordinate(0, 0));
        indexToCoord.put(1, new Coordinate(10, 10));
        indexToCoord.put(2, new Coordinate(50, 50));
        indexToCoord.put(3, new Coordinate(100, 100));
        path.setReverseIndexMap(indexToCoord);

        gameModel.setNewPath(path);
    }

    @Test
    void testCalcSatisfaction_WithDifferentAnimals() {
        Lion lion = new Lion(new Coordinate(1, 1), 500.0, gameModel);
        Antilope antilope = new Antilope(new Coordinate(2, 2), 700.0, gameModel);
        gameModel.setAnimals(new ArrayList<>(List.of(lion, antilope)));

        jeep.setCoord(new Coordinate(0, 0));
        jeep.checkForAnimals();
        jeep.calcSatisfaction();

        double expectedSatisfaction = (2 * 0.5) + (2 * 0.3) - (500 * 0.0002);
        assertEquals(expectedSatisfaction, gameModel.getSatisfactionMultiplier(), 0.01);
    }

    @Test
    void testMoveTo_ReachesDestination() {
        Coordinate target = new Coordinate(10, 0);
        jeep.setSpeed(2);
        gameModel.setSpeedModifier(1.0);

        jeep.moveTo(target);
        assertEquals(target.getX(), jeep.getCoord().getX(), 0.1);
        assertEquals(target.getY(), jeep.getCoord().getY(), 0.1);
    }

    @Test
    void testCheckForAnimals_WithinVision() {
        Animal elephant = new Animal(new Coordinate(20, 20), 1000.0, gameModel, false);
        gameModel.setAnimals(new ArrayList<>(List.of(elephant)));

        jeep.setCoord(new Coordinate(0, 0));
        jeep.checkForAnimals();
        jeep.calcSatisfaction();

        assertTrue(gameModel.getAnimals().contains(elephant));
        assertTrue(gameModel.getSatisfactionMultiplier() > 0);
    }

    @Test
    void testDrive_PathExecution() {
        Animal zebra = new Animal(new Coordinate(25, 25), 900.0, gameModel, false);
        gameModel.setAnimals(new ArrayList<>(List.of(zebra)));
        pipeline.submit(3);

        jeep.drive();

        assertEquals(7000.0, gameModel.getCurrentMoney());
        assertEquals(3, jeep.getPassengerNum());


        assertTrue(gameModel.getSatisfactionMultiplier() > 0);
    }

    @Test
    void testDrive_HandlesNoTouristsGracefully() {
        pipeline.submit(0);
        jeep.drive();

        assertEquals(4000, gameModel.getCurrentMoney());
        assertEquals(0, jeep.getPassengerNum());
    }

    @Test
    void testMoveTo_nullCoordinateDoesNotThrow() {
        assertDoesNotThrow(() -> jeep.moveTo(null));
    }

    @Test
    void testFindRandomPath_WithNullTargetDoesNotThrow() {
        assertDoesNotThrow(() -> jeep.findRandomPath(null));
    }
}
