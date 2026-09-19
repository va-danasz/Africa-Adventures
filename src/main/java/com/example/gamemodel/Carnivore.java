package com.example.gamemodel;

public class Carnivore extends Animal{
    public Carnivore(Coordinate coord, double price, GameModel gameModel) {
        super(coord, price, gameModel, true);
    }
}
