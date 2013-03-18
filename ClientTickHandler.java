package droptorch;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.packet.Packet14BlockDig;
import net.minecraft.network.packet.Packet15Place;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.src.ModLoader;
import net.minecraft.util.EnumMovingObjectType;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements ITickHandler {

	int prev_blockHitWait = 0;
	int targetBlockId = 0;
	int targetBlockMetadata = 0;
	Coord blockCoord = new Coord();
	int sideHit = 0;

	Queue<Coord> nextTarget = new LinkedList<Coord>();
	Set<Coord> vectors = new LinkedHashSet();

	int count = 0;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (type.equals(EnumSet.of(TickType.CLIENT))) {
			GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;
			if (guiscreen == null) {
				onTickInGame();
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public String getLabel() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	private int getTorchIndex() {
		Minecraft mc = Minecraft.getMinecraft();
		int max = DropTorch.config.use_inventory ? mc.thePlayer.inventory.mainInventory.length : 9;
		for(int iInventory = 0; iInventory < max; iInventory++) {
			ItemStack itemStack = mc.thePlayer.inventory.mainInventory[iInventory];
			if(itemStack == null) {
				continue;
			}
			if(itemStack.itemID == Block.torchWood.blockID) {
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
					int blockID = mc.theWorld.getBlockId(x, y, z);
					if(blockID == Block.torchWood.blockID) {
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
					int underBlockID = mc.theWorld.getBlockId(x, y-1, z);
					Block underBlock = Block.blocksList[underBlockID];
					if(underBlock == null) continue;
					if(underBlock.canPlaceTorchOnTop(mc.theWorld, x, y - 1, z) == false) {
						continue;
					}

					//if(underBlock.canPlaceBlockOnSide(mc.theWorld, x, y-1, z, 0) == false) {
					//	continue;
					//}
					if(mc.theWorld.getBlockMaterial(x, y-1, z).isSolid() == false) {
						continue;
					}
					if(blockID == Block.snow.blockID || mc.theWorld.isAirBlock(x, y, z)) {
						ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(index);
						//if(itemStack.tryPlaceItemIntoWorld(mc.thePlayer, mc.theWorld, x, y, z, 0, 0, 0, 0)) {
						//Block.torchWood.onBlockAdded(mc.theWorld, x, y, z);

						//itemStack = itemStack.useItemRightClick(mc.theWorld, mc.thePlayer);
						if(mc.theWorld.setBlockAndMetadataWithNotify(x, y, z, Block.torchWood.blockID, 0, 3)) {
							sendPacket(EnumCommand.TORCH, index, new Coord(x,y,z));
							itemStack.stackSize--;
							mc.thePlayer.inventory.setInventorySlotContents(index, itemStack);


							//mc.thePlayer.inventoryContainer.putStackInSlot(index, itemStack);
							//mc.thePlayer.inventoryContainer.detectAndSendChanges();
							return;
							//mc.thePlayer.inventory.decrStackSize(index, 1);
							//mc.thePlayer.inventory.onInventoryChanged();
							//mc.thePlayer.sendQueue.addToSendQueue(new Packet15Place(-1, -1, -1, 255, itemStack, 0.0F, 0.0F, 0.0F));
							//setTorch = true;
						}
					}
				}
			}
		}
	}

	private void sendPacket(EnumCommand command, int index, Coord pos) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);
		try {
			stream.writeUTF(command.toString());
			stream.writeInt(index);
			stream.writeInt(pos.x);
			stream.writeInt(pos.y);
			stream.writeInt(pos.z);

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = Config.channel;
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;
			Minecraft mc = Minecraft.getMinecraft();
			mc.thePlayer.sendQueue.addToSendQueue(packet);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

}
