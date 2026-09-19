package com.example.gamemodel;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

public class TouristPipeline implements Serializable {
    private final Queue<Integer> touristPipeline = new LinkedList<>();

    public TouristPipeline() {}

    public void submit(final int split){
        synchronized (touristPipeline){
            touristPipeline.add(split);
            touristPipeline.notifyAll();
        }
    }

    /**
     * Implementing a BlockingQueue's necessary methods: submitting and taking elements out if possible
     * @return
     */
    public int tryToTake(){
        synchronized (touristPipeline){
            while(touristPipeline.isEmpty()){
                try {
                    touristPipeline.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return touristPipeline.remove();
        }
    }

}
