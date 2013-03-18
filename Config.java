package droptorch;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;

public class Config {
	public static final String channel = "dt";
	public static int x_plus = 0;
	public static int z_plus = 0;
	public static int distance = 5;
	public static boolean use_inventory = true;
	public static EnumMode mode = EnumMode.ON;

	public void load(File file) {
		Configuration cfg = new Configuration(file);
		try {
			cfg.load();
			x_plus = cfg.get(Configuration.CATEGORY_GENERAL, "x_plus", x_plus).getInt();
			z_plus = cfg.get(Configuration.CATEGORY_GENERAL, "z_plus", z_plus).getInt();
			distance = cfg.get(Configuration.CATEGORY_GENERAL, "distance", distance).getInt();
			use_inventory = cfg.get(Configuration.CATEGORY_GENERAL, "use_inventory", true).getBoolean(true);
			if(cfg.get(Configuration.CATEGORY_GENERAL, "mode", true).getBoolean(true)) {
				mode = EnumMode.ON;
			}
			else {
				mode = EnumMode.OFF;
			}
			cfg.save();
		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e, "DropTorch load config exception");
		} finally {
			cfg.save();
		}
	}

	public void toggleMode() {
		for(int i = 0; i < EnumMode.values().length; ++i) {
			if(EnumMode.values()[i].equals(mode)) {
				i = (i + 1) % EnumMode.values().length;
				mode = EnumMode.values()[i];
				break;
			}
		}
	}

	public EnumMode getMode() {
		return mode;
	}

	public void sendTargetToPlayer(INetworkManager manager) {
	}
}
