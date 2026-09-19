package com.example.gamemodel;

import java.io.Serializable;
import java.util.*;

public class Path extends AbstractBuyable implements Serializable {
    private final ArrayList<RoadPiece> path;
    private final CanBuy type;
    private Map<Coordinate, List<Coordinate>> map = new HashMap<>();
    private ArrayList<Integer>[] adjList;
    private Map<Coordinate, Integer> indexMap = new HashMap<>();
    private int indexCounter = 0;
    private Set<List<Integer>> allPaths = new HashSet<>();
    private Map<Integer, Coordinate> reverseIndexMap = new HashMap<>();
    private final double price;

    public Path(double price) {
        this.type = CanBuy.PATH;
        this.price = price;
        this.path = new ArrayList<>();
    }

    /**
     * Creating an adjacency list to keep track of connections in the graph
     */
    public void initAdjList() {
        int v = getVertexCount();
        adjList = new ArrayList[v];
        for (int i = 0; i < v; i++) adjList[i] = new ArrayList<>();
        for (Map.Entry<Coordinate, List<Coordinate>> entry : map.entrySet()) {
            int srcIndex = indexMap.get(entry.getKey());
            for (Coordinate neighbor : entry.getValue()) {
                int destIndex = indexMap.get(neighbor);
                adjList[srcIndex].add(destIndex);
            }
        }
    }

    /**
     * This method ensures when we build up the graph froam road pieces in
     * multiple rounds, we are working with the same instance of graph.
     * @param other
     */
    public void copyGraphFrom(Path other) {
        if (other == null) {
            return;
        }

        this.map = new HashMap<>();
        for (Map.Entry<Coordinate, List<Coordinate>> entry : other.map.entrySet()) {
            this.map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        this.indexMap = new HashMap<>(other.indexMap);
        this.indexCounter = other.indexCounter;
        this.reverseIndexMap = new HashMap<>(other.reverseIndexMap);

        if (other.adjList != null) {
            this.adjList = new ArrayList[other.adjList.length];
            for (int i = 0; i < other.adjList.length; i++) {
                this.adjList[i] = new ArrayList<>(other.adjList[i]);
            }
        } else {
            this.adjList = null;
        }
    }

    /**
     * returns all paths, and their component roadpieces.
     * @param s
     * @param d
     * @return
     */
    public Set<List<Integer>> printAllPaths(int s, int d) {
        boolean[] isVisited = new boolean[adjList.length];
        List<Integer> pathList = new ArrayList<>();
        pathList.add(s);
        printAllPathsUtil(s, d, isVisited, pathList, allPaths);
        return allPaths;
    }

    /**
     * Recursive function to print out all possible paths between two nodes, using our adjacency list.
     *
     * @param u
     * @param d
     * @param isVisited
     * @param localPathList
     * @param allPaths
     */
    private void printAllPathsUtil(Integer u, Integer d, boolean[] isVisited, List<Integer> localPathList, Set<List<Integer>> allPaths) {
        if (u.equals(d)) {
            allPaths.add(new ArrayList<>(localPathList));
            return;
        }
        isVisited[u] = true;
        for (Integer i : adjList[u]) {
            if (!isVisited[i]) {
                localPathList.add(i);
                printAllPathsUtil(i, d, isVisited, localPathList, allPaths);
                localPathList.remove(localPathList.size() - 1);
            }
        }
        isVisited[u] = false;
    }

    /**
     * Adds a new vertex to the path
     * @param coord the new vertex's coordinate
     */
    public void addVertex(Coordinate coord) {
        if (!map.containsKey(coord)) {
            map.put(coord, new LinkedList<>());
            indexMap.put(coord, indexCounter);
            reverseIndexMap.put(indexCounter, coord);
            indexCounter++;
        }
    }

    public void addEdge(Coordinate source, Coordinate destination) {
        if (!map.containsKey(source)) addVertex(source);
        if (!map.containsKey(destination)) addVertex(destination);
        map.get(source).add(destination);
        map.get(destination).add(source);
    }

    /**
     * Checking if there's at least one correct path from entry to exit
     * using BFS algorithm.
     */
    public boolean isConnected(Coordinate start, Coordinate end) {
        boolean hasBothEnd = hasVertex(start) && hasVertex(end);
        Set<Coordinate> visited = new HashSet<>();
        Queue<Coordinate> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            Coordinate current = queue.poll();
            if (current.distanceTo(end, 5) && hasBothEnd) return true;
            for (Coordinate neighbor : map.getOrDefault(current, Collections.emptyList())) {
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                    visited.add(neighbor);
                }
            }
        }
        return false;
    }

    public void addRoadPiece(RoadPiece roadPiece) {
        this.path.add(roadPiece);
    }

    public int getIndex(Coordinate coord) {
        return indexMap.getOrDefault(coord, -1);
    }

    public int getVertexCount() {
        return map.keySet().size();
    }

    public boolean hasVertex(Coordinate coord) {
        return map.containsKey(coord);
    }

    public ArrayList<RoadPiece> getPath() {
        return path;
    }

    public Map<Integer, Coordinate> getReverseIndexMap() {
        return reverseIndexMap;
    }

    public Set<List<Integer>> getAllPaths() {
        return allPaths;
    }

    @Override
    public Coordinate getCoord() {return null; }

    @Override
    public CanBuy getType() {return type; }

    @Override
    public double getPrice() {return price; }

    public double getVision() {return 32; }

    public void setAllPaths(Set<List<Integer>> allPaths) {
        this.allPaths = allPaths;
    }

    public void setReverseIndexMap(Map<Integer, Coordinate> indexToCoord) {
        this.reverseIndexMap = indexToCoord;
    }
}