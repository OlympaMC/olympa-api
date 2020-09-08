package fr.olympa.api.utils.spigot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers.NBT;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.NBTBase;
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.TileEntity;

public class Schematic {

	public String name;
	public EmptyBuildBlock[][][] blocks;

	public short width, height, length;

	private Schematic(short width, short height, short length) {
		this.width = width;
		this.height = height;
		this.length = length;
		this.blocks = new EmptyBuildBlock[width][height][length];
	}

	public void paste(Location location, boolean setAir) {
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				for (int z = 0; z < length; ++z) {
					EmptyBuildBlock buildBlock = blocks[x][y][z];
					Block block = location.getWorld().getBlockAt(location.getBlockX() + x, location.getBlockY() + y, location.getBlockZ() + z);
					buildBlock.setBlock(block, setAir);
				}
			}
		}
	}

	public static Schematic load(File file) throws Exception {
		if (!file.exists()) throw new FileNotFoundException("File not found");

		FileInputStream fis = new FileInputStream(file);
		NBTTagCompound nbt = NBTCompressedStreamTools.a(fis);
		fis.close();

		short width = nbt.getShort("Width");
		short height = nbt.getShort("Height");
		short length = nbt.getShort("Length");

		byte[] blockData = nbt.getByteArray("BlockData");
		Map<Integer, BlockData> blocks = new HashMap<>();

		NBTTagCompound palette = nbt.getCompound("Palette");

		palette.getKeys().forEach(rawState -> {
			int id = palette.getInt(rawState);
			BlockData blockData2 = Bukkit.createBlockData(rawState);
			blocks.put(id, blockData2);
		});

		Schematic out = new Schematic(width, height, length);

		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				for (int z = 0; z < length; ++z) {
					int index = y * width * length + z * width + x;

					BlockData data = blocks.get((int) blockData[index]);

					EmptyBuildBlock M = null;

					if (data != null) {
						M = new DataBuildBlock(x, y, z, data);
					}else {
						M = new EmptyBuildBlock(x, y, z);
					}

					out.blocks[x][y][z] = M;
				}
			}
		}
		
        NBTTagList tileEntities = nbt.getList("TileEntities", NBT.TAG_COMPOUND);
		if (tileEntities == null || tileEntities.isEmpty()) tileEntities = nbt.getList("BlockEntities", NBT.TAG_COMPOUND);
        if (tileEntities != null) {
            for (NBTBase tileEntityBase : tileEntities) {
            	NBTTagCompound tileEntity = (NBTTagCompound) tileEntityBase;
                int[] pos = tileEntity.getIntArray("Pos");
				if (tileEntity.hasKey("Id")) tileEntity.setInt("id", tileEntity.getInt("Id"));
				EmptyBuildBlock block = out.blocks[pos[0]][pos[1]][pos[2]];
				((DataBuildBlock) block).tileEntity = tileEntity;
            }
        }

		out.name = file.getName();
		return out;

	}
	
	public static class EmptyBuildBlock {

		public int x, y, z;

		public EmptyBuildBlock(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public void setBlock(Block block, boolean setAir) {
			if (setAir) block.setType(Material.AIR);
		}

	}
	
	public static class DataBuildBlock extends EmptyBuildBlock {

		public BlockData data;
		public NBTTagCompound tileEntity;

		public DataBuildBlock(int x, int y, int z, BlockData data) {
			super(x, y, z);
			this.data = data;
		}
		
		@Override
		public void setBlock(Block block, boolean setAir) {
			block.setBlockData(data);
			if (tileEntity != null) {
				BlockPosition position = ((CraftBlock) block).getPosition();
				TileEntity tileEntity2 = ((CraftWorld) block.getWorld()).getHandle().getTileEntity(position);
				tileEntity2.load(tileEntity);
				tileEntity2.setPosition(position);
				tileEntity2.update();
			}
		}

	}

}
