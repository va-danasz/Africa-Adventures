package com.example.gamemodel;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class Tourist implements Serializable {
    private static final int MIN_NUM_PEOPLE = 1;
    private static final int MAX_NUM_PEOPLE = 6;
    private static final int MIN_NUM_PERIOD = 4;
    private static final int MAX_NUM_PERIOD = 15;
    private transient final Object groupLock = new Object();
    private Integer currentGroup;
    private transient GameModel gm;

    public Tourist(GameModel gm) {
        this.gm = gm;
    }

    /**
     * Restricts tourists' spawning to valid times.
     * @param isOver
     */
    public void startSpawning(final AtomicBoolean isOver) {
        while (!isOver.get()) {
            if (gm.getIsNight().get()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
            spawn();
        }
    }

    public Integer takeCurrentGroup(){
        synchronized (groupLock) {
            final Integer current = this.currentGroup;
            this.currentGroup = null;
            return current;
        }
    }

    public boolean isGroupReady(){
        synchronized (groupLock){
            return this.currentGroup != null;
        }
    }

    /**
     * Randomizing tourists spawn time period and their incoming group size, meanwhile,
     * spawnPeriod being overwritten by the current satisfaction rate.
     */
    private void spawn(){
        double spawnPeriod;
        synchronized (groupLock) {
            double satisfactionMultiplier = gm.getSatisfactionMultiplier();
            spawnPeriod = Math.abs((ThreadLocalRandom.current().nextInt(MIN_NUM_PERIOD, MAX_NUM_PERIOD)) * (1/ satisfactionMultiplier));
        }

        if (Double.isInfinite(spawnPeriod)){
            spawnPeriod = 1.0;
        }

        try {
            Thread.sleep((long) (spawnPeriod * 1000L));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (groupLock) {
            currentGroup = ThreadLocalRandom.current().nextInt(MIN_NUM_PEOPLE, MAX_NUM_PEOPLE);
        }

        synchronized (this){
            this.notifyAll();
        }
    }
}
