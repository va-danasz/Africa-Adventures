package com.example.gamemodel;

import java.io.Serializable;

public class DifficultyGoals implements Serializable {
    private static final int[] monthGoals = {3,6,12};
    private static final int[] visitorGoals = {20,25,30};
    private static final int[] moneyGoals = {1000,2000,3000};
    private static final int[] carnivoreGoals = {2,10,15};
    private static final int[] herbivoreGoals = {2,15,20};

    public static int getMonth(int n) {return monthGoals[n];}
    public static int getVisitor(int n) {return visitorGoals[n];}
    public static int getMoney(int n) {return moneyGoals[n];}
    public static int getCarnivore(int n) {return carnivoreGoals[n];}
    public static int getHerbivore(int n) {return herbivoreGoals[n];}
}
