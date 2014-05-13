package droptorch;

import java.util.logging.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = DropTorch.modid, name = DropTorch.name, version = "1.0")
public class DropTorch {
    public static final String modid = "DropTorch";
    public static final String name = "DropTorch";
	@SidedProxy(clientSide = "droptorch.ClientProxy", serverSide = "droptorch.CommonProxy")
	public static CommonProxy proxy;

	@Instance("DropTorch")
	public static DropTorch instance;

    public static final PacketPipeline packetPipeline = new PacketPipeline();
	public static Logger logger = Logger.getLogger("Minecraft");

	public static Config config = new Config();

    @Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.init();
        packetPipeline.init(DropTorch.modid);
        packetPipeline.registerPacket(PacketHandler.class);
	}

    @Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config.load(event.getSuggestedConfigurationFile());
	}

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        packetPipeline.postInit();
    }}
