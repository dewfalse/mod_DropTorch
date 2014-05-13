package droptorch;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import ibxm.Player;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PacketHandler extends AbstractPacket {

    EnumCommand command;
    int index;
    Coord pos = new Coord();

    public PacketHandler() {

    }

    public PacketHandler(EnumCommand command, int index, Coord pos) {
        this.command = command;
        this.index = index;
        this.pos = pos;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.writeInt(command.ordinal());
        buffer.writeInt(index);
        buffer.writeInt(pos.x);
        buffer.writeInt(pos.y);
        buffer.writeInt(pos.z);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
        command = EnumCommand.values()[buffer.readInt()];
        index = buffer.readInt();
        pos.x = buffer.readInt();
        pos.y = buffer.readInt();
        pos.z = buffer.readInt();
    }

    @Override
    public void handleClientSide(EntityPlayer player) {

    }

    @Override
    public void handleServerSide(EntityPlayer player) {
        setBlock(player, index, pos.x, pos.y, pos.z);
    }

    private void setBlock(EntityPlayer player, int index, int x, int y, int z) {
        EntityPlayerMP thePlayer = (EntityPlayerMP) player;
        World theWorld = thePlayer.worldObj;
        if(theWorld.setBlock(x, y, z, Block.getBlockFromName("torch"), 0, 3)) {
            thePlayer.inventory.decrStackSize(index, 1);
            thePlayer.inventory.markDirty();
            thePlayer.inventoryContainer.detectAndSendChanges();
        }

    }

}
