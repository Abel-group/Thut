package thut.api.entity.genetics;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import thut.api.entity.ai.AIThreadManager;

public class GeneRegistry
{
    static Map<ResourceLocation, Class<? extends Gene>> geneMap = Maps.newHashMap();

    public static Class<? extends Gene> getClass(ResourceLocation location)
    {
        return geneMap.get(location);
    }

    public static Collection<Class<? extends Gene>> getGenes()
    {
        return geneMap.values();
    }

    public static void register(Class<? extends Gene> gene)
    {
        Gene temp;
        try
        {
            // Ensure the gene has a blank constructor for registration
            temp = gene.newInstance();
            geneMap.put(temp.getKey(), gene);
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            AIThreadManager.logger.log(Level.SEVERE, "Error with registry of " + gene, e);
        }
    }

    public static NBTTagCompound save(Gene gene)
    {
        NBTTagCompound tag = gene.save();
        tag.setString("K", gene.getKey().toString());
        return tag;
    }

    public static Gene load(NBTTagCompound tag) throws Exception
    {
        Gene ret = null;
        ResourceLocation resource = new ResourceLocation(tag.getString("K"));
        ret = geneMap.get(resource).newInstance();
        ret.load(tag);
        return ret;
    }

}
