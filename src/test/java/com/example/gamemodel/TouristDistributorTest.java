package com.example.gamemodel;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TouristDistributorTest {

    @Test
    public void testSplitGroup_ExactMultiples() {
        List<Integer> result = distributorTestableSplit(8);
        assertEquals(List.of(4, 4), result);
    }

    @Test
    public void testSplitGroup_NonExact() {
        List<Integer> result = distributorTestableSplit(10);
        assertEquals(List.of(4, 4, 2), result);
    }

    @Test
    public void testSplitGroup_SmallGroup() {
        List<Integer> result = distributorTestableSplit(3);
        assertEquals(List.of(3), result);
    }

    @Test
    public void testReceiveAndSubmitGroup_Delegation() {
        Tourist mockTourist = mock(Tourist.class);
        TouristPipeline mockPipeline = mock(TouristPipeline.class);

        when(mockTourist.isGroupReady()).thenReturn(true);
        when(mockTourist.takeCurrentGroup()).thenReturn(7);

        TouristDistributor distributor = new TouristDistributor();

        distributor.receiveAndSubmitGroup(mockTourist, mockPipeline);

        verify(mockPipeline, times(1)).submit(4);
        verify(mockPipeline, times(1)).submit(3);
    }

    private List<Integer> distributorTestableSplit(int groupSize) {
        return new TouristDistributor() {
            public List<Integer> callSplitGroup(int g) {
                return super.splitGroup(g);
            }
        }.callSplitGroup(groupSize);
    }
}
