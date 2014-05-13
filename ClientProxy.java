package droptorch;

import cpw.mods.fml.common.FMLCommonHandler;

public class ClientProxy extends CommonProxy {

	@Override
	void init() {
        FMLCommonHandler.instance().bus().register(new ClientTickHandler());
        FMLCommonHandler.instance().bus().register(new ModeKeyHandler());
	}
}
