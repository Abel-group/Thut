package thut.core.common.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thut.core.common.items.ItemDusts;
import thut.core.common.items.ItemSpout;
import thut.core.common.items.ItemTank;

public class ConfigHandler {

	public static boolean collisionDamage;
	public static boolean paneFix;
	
	public ConfigHandler(File configFile){
		// Loads The Configuration File into Forges Configuration
		Configuration conf = new Configuration(configFile);
		try{
			conf.load();
			
		}catch(RuntimeException e){
			
		}finally{
			conf.save();
		}
		

		items.add(new ItemSpout());
		items.add(new ItemTank());
//		items.add(new ItemSpreader());
        items.add(new ItemDusts());
		
		for(Item item: items){
			GameRegistry.registerItem(item, item.getUnlocalizedName().substring(5));
		}
	}
	
	private static List<Item> items = new ArrayList<Item>();
}
