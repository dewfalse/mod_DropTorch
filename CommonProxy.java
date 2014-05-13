package droptorch;

import cpw.mods.fml.common.FMLCommonHandler;

public class CommonProxy {

    void init() {
        FMLCommonHandler.instance().bus().register(new ConnectionHandler());
    }

}
