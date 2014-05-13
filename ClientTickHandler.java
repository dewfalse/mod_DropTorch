package droptorch;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

public class ClientTickHandler  {

	int prev_blockHitWait = 0;
	int targetBlockId = 0;
	int targetBlockMetadata = 0;
	Coord blockCoord = new Coord();
	int sideHit = 0;

	Queue<Coord> nextTarget = new LinkedList<Coord>();
	Set<Coord> vectors = new LinkedHashSet();

	int count = 0;

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !FMLClientHandler.instance().isGUIOpen(GuiChat.class)) {
            onTickInGame();
        }
    }

	private int getTorchIndex() {
		Minecraft mc = Minecraft.getMinecraft();
        if(mc == null) return -1;
        if(mc.thePlayer == null) return -1;
		int max = DropTorch.config.use_inventory ? mc.thePlayer.inventory.mainInventory.length : 9;
		for(int iInventory = 0; iInventory < max; iInventory++) {
			ItemStack itemStack = mc.thePlayer.inventory.mainInventory[iInventory];
			if(itemStack == null) {
				continue;
			}
            Block block = Block.getBlockFromItem(itemStack.getItem());
			if(block == Block.getBlockFromName("torch")) {
				return iInventory;
			}
		}
		return -1;
	}

	public void onTickInGame() {
		if(DropTorch.config.getMode() == EnumMode.OFF) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();

		int index = getTorchIndex();
		if(index == -1) {
			return;
		}

		int distance = DropTorch.config.distance;
		int width = distance / 2 + 1;
		int posX = (int)Math.round(mc.thePlayer.posX);
		int posY = (int)Math.round(mc.thePlayer.posY);
		int posZ = (int)Math.round(mc.thePlayer.posZ);
		for(int x = posX - width; x <= posX + width; ++x) {
			for(int z = posZ - width; z <= posZ + width; ++z) {
				boolean setTorch = false;
				for(int y = posY - 3; y <= posY + 1; ++y) {
					Block block = mc.theWorld.getBlock(x, y, z);
					if(block == Block.getBlockFromName("torch")) {
						setTorch = true;
					}
					if(setTorch) continue;
					int x_mod = x % distance;
					if(x_mod < 0) {
						x_mod += distance;
					}
					int z_mod = z % distance;
					if(z_mod < 0) {
						z_mod += distance;
					}
					if( (x_mod != DropTorch.config.x_plus) || (z_mod != DropTorch.config.z_plus) ) {
						continue;
					}
					Block underBlock = mc.theWorld.getBlock(x, y-1, z);
					if(underBlock == null) continue;
					if(underBlock.canPlaceTorchOnTop(mc.theWorld, x, y - 1, z) == false) {
						continue;
					}

					if(underBlock.getMaterial().isSolid() == false) {
						continue;
					}
					if(block == Block.getBlockFromName("snow") || mc.theWorld.isAirBlock(x, y, z)) {
						ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(index);
						if(mc.theWorld.setBlock(x, y, z, Block.getBlockFromName("torch"), 0, 3)) {
							sendPacket(EnumCommand.TORCH, index, new Coord(x,y,z));
							return;
						}
					}
				}
			}
		}
	}

	private void sendPacket(EnumCommand command, int index, Coord pos) {
		DropTorch.packetPipeline.sendPacketToServer(new PacketHandler(command, index, pos));
	}

}
