package thut.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import thut.api.WorldCache.ChunkCache;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;

public class TickHandler
{

    private static TickHandler instance;

    public TickHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
        new PacketHandler();
        instance = this;
    }

    public static TickHandler getInstance()
    {
        if (instance == null) new TickHandler();
        return instance;
    }

    public HashMap<Integer, Vector<BlockChange>> blocks      = new HashMap<Integer, Vector<BlockChange>>();
    public static int                            maxChanges  = 200;
    /** This is a map of dimension to worldcache, it can be used for thread-safe
     * world access */
    public HashMap<Integer, WorldCache>          worldCaches = new HashMap<Integer, WorldCache>();
    HashMap<Integer, HashSet<Long>>              toRefresh   = new HashMap<>();

    public WorldCache getWorldCache(int dimension)
    {
        return worldCaches.get(dimension);
    }

    @SubscribeEvent
    public void worldTickEvent(WorldTickEvent evt)
    {
        // At the start of the phase, once per second, check to see if any of
        // the vectorpools should be removed.
        if (evt.phase == Phase.START)
        {
            if (evt.world.getTotalWorldTime() % 20 == 0)
            {
                if (evt.world.getTotalWorldTime() % 40 == 0)
                {
                    WorldCache world = worldCaches.get(evt.world.provider.getDimensionId());
                    if (world != null)
                    {
                        for (ChunkCache chunk : world.cache)
                        {
                            chunk.update();
                        }
                    }
                }
            }
        }
        if (evt.phase != Phase.START || !blocks.containsKey(evt.world.provider.getDimensionId())
                || blocks.get(evt.world.provider.getDimensionId()).size() == 0 || evt.world.isRemote)
            return;

        int num = 0;
        ArrayList<BlockChange> removed = Lists.newArrayList();
        Vector<BlockChange> blocks = this.blocks.get(evt.world.provider.getDimensionId());
        ArrayList<BlockChange> toRemove = Lists.newArrayList(blocks);

        // remove the blocks needed for that world.
        for (int i = 0; i < toRemove.size(); i++)
        {
            BlockChange b = toRemove.get(i);
            b.changeBlock(evt.world);
            removed.add(b);
            num++;
            if (num >= maxChanges * 5) break;
        }
        for (BlockChange b : removed)
            blocks.remove(b);
        removed.clear();
    }

    @SubscribeEvent
    public void WorldUnloadEvent(Unload evt)
    {
        if (evt.world.provider.getDimensionId() == 0)
        {
            blocks.clear();
            ExplosionCustom.explosions.clear();
        }
        // Remove world cache for dimension
        worldCaches.remove(evt.world.provider.getDimensionId());
    }

    @SubscribeEvent
    public void WorldLoadEvent(Load evt)
    {
        if (evt.world.isRemote) return;
        // Initialize a world cache for this dimension
        worldCaches.put(evt.world.provider.getDimensionId(), new WorldCache(evt.world));
    }

    @SubscribeEvent
    public void ChunkLoadEvent(net.minecraftforge.event.world.ChunkEvent.Load evt)
    {
        if (evt.world.isRemote) return;
        // Add the chunk to the corresponding world cache.
        WorldCache world = worldCaches.get(evt.world.provider.getDimensionId());
        if (world == null)
        {
            world = new WorldCache(evt.world);
            worldCaches.put(evt.world.provider.getDimensionId(), world);
        }
        world.addChunk(evt.getChunk());
    }

    @SubscribeEvent
    public void ChunkUnLoadEvent(net.minecraftforge.event.world.ChunkEvent.Unload evt)
    {
        if (evt.world.isRemote) return;
        // Remove the chunk from the cache
        WorldCache world = worldCaches.get(evt.world.provider.getDimensionId());
        if (world != null)
        {
            world.removeChunk(evt.getChunk());
        }
    }

    public static void addBlockChange(Vector3 location, int dimension, Block blockTo)
    {
        addBlockChange(location, dimension, blockTo, 0);
    }

    public static void addBlockChange(Vector3 location, int dimension, Block blockTo, int meta)
    {
        addBlockChange(new BlockChange(location, dimension, blockTo, meta), dimension);
    }

    static Map<Thread, ArrayList<BlockChange>> lists = Maps.newConcurrentMap();

    public static void cleanup()
    {
        Thread thread = Thread.currentThread();
        lists.remove(thread);
        System.gc();
    }

    private static ArrayList<BlockChange> getList()
    {
        Thread thread = Thread.currentThread();
        if (lists.containsKey(thread))
        {
            ArrayList<BlockChange> ret = lists.get(thread);
            ret.clear();
            return ret;

        }
        else
        {
            ArrayList<BlockChange> ret;
            ret = Lists.newArrayList();
            lists.put(thread, ret);
            return ret;
        }
    }

    public static void addBlockChange(BlockChange b1, int dimension)
    {

        if (b1.location.y > 255) return;

        getInstance();
        ArrayList<BlockChange> blocks = getList();
        blocks.addAll(TickHandler.getListForDimension(dimension));
        for (BlockChange b : blocks)
        {
            if (b.equals(b1)) return;
        }
        getInstance();
        TickHandler.getListForDimension(dimension).add(b1);
    }

    public static Vector<BlockChange> getListForWorld(World worldObj)
    {
        Vector<BlockChange> ret = getInstance().blocks.get(worldObj.provider.getDimensionId());
        if (ret == null)
        {
            ret = new Vector<BlockChange>();
            getInstance().blocks.put(worldObj.provider.getDimensionId(), ret);
        }
        return ret;
    }

    public static Vector<BlockChange> getListForDimension(int dim)
    {
        Vector<BlockChange> ret = getInstance().blocks.get(dim);
        if (ret == null)
        {
            ret = new Vector<BlockChange>();
            getInstance().blocks.put(dim, ret);
        }
        return ret;
    }

    public static class BlockChange
    {
        public int     dimension;
        public Vector3 location;
        public Block   blockTo;
        public Block   blockFrom;
        public int     metaTo = 0;
        public int     flag   = 3;

        public BlockChange(Vector3 location, int dim, Block blockTo)
        {
            dimension = dim;
            this.location = location.copy();
            this.blockTo = blockTo;
        }

        public BlockChange(Vector3 location, int dim, Block blockTo, int meta)
        {
            dimension = dim;
            this.location = location.copy();
            this.blockTo = blockTo;
            this.metaTo = meta;
        }

        public boolean changeBlock(World worldObj)
        {
            boolean ret = location.setBlock(worldObj, blockTo, metaTo, flag);
            return ret;
        }

        @Override
        public String toString()
        {
            return blockTo + " " + dimension + " " + location;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof BlockChange)
            {
                BlockChange b = (BlockChange) o;
                return dimension == b.dimension && location.sameBlock(b.location);
            }

            return false;
        }

    }
}
