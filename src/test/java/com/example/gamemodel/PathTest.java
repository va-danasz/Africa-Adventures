package com.example.gamemodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PathTest {

    private Path path;
    private Coordinate c1;
    private Coordinate c2;
    private Coordinate c3;

    @BeforeEach
    void setUp() {
        path = new Path(200);
        c1 = new Coordinate(0, 0);
        c2 = new Coordinate(1, 1);
        c3 = new Coordinate(2, 2);
    }

    @Test
    void testAddVertexAndHasVertex() {
        path.addVertex(c1);
        assertTrue(path.hasVertex(c1));
        assertEquals(1, path.getVertexCount());
    }

    @Test
    void testAddEdgeCreatesBothVertices() {
        path.addEdge(c1, c2);
        assertTrue(path.hasVertex(c1));
        assertTrue(path.hasVertex(c2));
        assertEquals(2, path.getVertexCount());
    }

    @Test
    void testIsConnectedDirectPath() {
        path.addEdge(c1, c2);
        path.addEdge(c2, c3);
        assertTrue(path.isConnected(c1, c3));
    }


    @Test
    void testInitAdjListAndPrintAllPaths() {
        path.addEdge(c1, c2);
        path.addEdge(c2, c3);
        path.initAdjList();

        int start = path.getIndex(c1);
        int end = path.getIndex(c3);

        Set<List<Integer>> allPaths = path.printAllPaths(start, end);
        assertFalse(allPaths.isEmpty());

        boolean hasExpectedPath = allPaths.stream()
                .anyMatch(p -> p.size() == 3 && p.getFirst() == start && p.get(2) == end);

        assertTrue(hasExpectedPath);
    }

    @Test
    void testCopyGraphFromAnotherPath() {
        Path other = new Path(100);
        other.addEdge(c1, c2);
        other.addEdge(c2, c3);
        other.initAdjList();

        path.copyGraphFrom(other);
        path.initAdjList();

        assertEquals(3, path.getVertexCount());
        assertTrue(path.isConnected(c1, c3));
    }

    @Test
    void testGetIndexReturnsCorrectIndex() {
        path.addVertex(c1);
        int index = path.getIndex(c1);
        assertTrue(index >= 0);
    }

    @Test
    void testGetEdgesCountPrintsCorrectly() {
        path.addEdge(c1, c2);
        path.addEdge(c2, c3);
    }

    @Test
    void testCopyGraphParamNullDontThrow() {
        assertDoesNotThrow(() -> path.copyGraphFrom(null));
    }
}
