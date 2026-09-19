package com.example.gamemodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TouristDistributor implements Serializable {

    public void startDistributing(final AtomicBoolean isOver,
                                  final Tourist tourist, final TouristPipeline pipeline) {
        while (!isOver.get()) {
            receiveAndSubmitGroup(tourist, pipeline);
        }
    }

    /**
     * Waiting for tourists to be ready then splitting them among jeeps and submitting them to the pipeline.
     * @param tourist
     * @param pipeline
     */
    void receiveAndSubmitGroup(final Tourist tourist, final TouristPipeline pipeline){
        synchronized (tourist){
            while(!tourist.isGroupReady()) {
                try {
                    tourist.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        int currentGroup = tourist.takeCurrentGroup();
        splitGroup(currentGroup).forEach(pipeline::submit);
    }

    /**
     * splitting incoming tourists between available jeeps in a way to make as many full rides as possible
     * @param groupSize
     * @return
     */
    List<Integer> splitGroup(final int groupSize) {
        List<Integer> result = new ArrayList<>();
        int remainingGroup = groupSize;
        final int JEEP_CAPACITY = 4;

        while (remainingGroup > 0) {
            if (remainingGroup >= JEEP_CAPACITY) {
                result.add(JEEP_CAPACITY);
                remainingGroup -= JEEP_CAPACITY;
            } else {
                result.add(remainingGroup);
                remainingGroup = 0;
            }
        }
        return result;
    }
}
