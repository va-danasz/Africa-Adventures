package com.example.viewmodel;

import com.example.gamemodel.*;

public class BuyableViewFactory {
    /**
     * @param obj the model
     * @return The corresponding view class to the model
     */
    public static BuyableView createView(Buyable obj) {
        if (obj instanceof Pond pond) return new PondView(pond);
        if (obj instanceof Tree tree) return new TreeView(tree);
        if (obj instanceof Bush bush) return new BushView(bush);
        if (obj instanceof Grass grass) return new GrassView(grass);
        if (obj instanceof Antilope antilope) return new AntilopeView(antilope);
        if (obj instanceof Cheetah cheetah) return new CheetahView(cheetah);
        if (obj instanceof Jeep jeep) return new JeepView(jeep);
        if (obj instanceof Lion lion) return new LionView(lion);
        if (obj instanceof Ranger ranger) return new RangerView(ranger);
        if (obj instanceof Zebra zebra) return new ZebraView(zebra);
        throw new IllegalArgumentException("No view for " + obj.getClass());
    }
}
