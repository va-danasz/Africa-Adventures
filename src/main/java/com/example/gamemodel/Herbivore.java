package com.example.gamemodel;

public class Herbivore extends Animal{
    public Herbivore(Coordinate coord, double price, GameModel gameModel) {
        super(coord, price, gameModel, false);
    }
}
