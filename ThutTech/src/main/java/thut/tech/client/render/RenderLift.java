package thut.tech.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import thut.tech.common.entity.EntityLift;

public class RenderLift<T extends EntityLivingBase> extends RenderLivingBase<T>
{
    float                    pitch = 0.0f;
    float                    yaw   = 0.0f;
    long                     time  = 0;
    boolean                  up    = true;

    static final Tessellator t     = new Tessellator(2097152);
    VertexBuffer             b     = t.getBuffer();

    ResourceLocation         texture;

    public RenderLift(RenderManager manager)
    {
        super(manager, null, 0);
    }

    @Override
    public void doRender(EntityLivingBase entity, double d0, double d1, double d2, float f, float f1)
    {
        render(entity, d0, d1, d2);
    }

    public void render(Entity te, double x, double y, double z)
    {
        if (te instanceof EntityLift)
        {
            renderBase(te, 1, x, y, z);
        }

    }

    private void renderBase(Entity te, float scale, double x, double y, double z)
    {

        try
        {
            EntityLift lift = (EntityLift) te;
            GL11.glPushMatrix();
            GL11.glTranslated(x, y, z);
            GL11.glScaled(0.999, 0.999, 0.999);
            MutableBlockPos pos = new MutableBlockPos();
            int xMin = lift.boundMin.intX();
            int zMin = lift.boundMin.intZ();
            int xMax = lift.boundMax.intX();
            int zMax = lift.boundMax.intZ();
            int yMin = lift.boundMin.intY();
            int yMax = lift.boundMax.intY();

            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        pos.setPos(i, j, k);
                        drawBlockAt(pos, lift);
                    }
            GL11.glPopMatrix();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void drawBlockAt(BlockPos pos, EntityLift lift)
    {
        boolean old = true;
        IBlockState iblockstate = lift.getLiftWorld().getBlockState(pos);
        if (old)
        {
            GL11.glPushMatrix();
            GL11.glTranslated(pos.getX(), pos.getY() + 0.5, pos.getZ());
            if (iblockstate.getMaterial() != Material.AIR)
            {
                BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                GlStateManager.enableRescaleNormal();
                GlStateManager.pushMatrix();
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-180.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.translate(0.5F, 0.5F, 0.5F);
                float f7 = 1.0F;
                GlStateManager.scale(-f7, -f7, f7);
                int i1 = lift.getBrightnessForRender(0);
                int j1 = i1 % 65536;
                int k1 = i1 / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                blockrendererdispatcher.renderBlockBrightness(iblockstate, 1.0F);
                GlStateManager.popMatrix();
                GlStateManager.disableRescaleNormal();
            }
            GL11.glPopMatrix();
        }
        else
        {
            b.begin(7, DefaultVertexFormats.BLOCK);
            b.setTranslation(-0.5, 0, -0.5);
            int i1 = lift.getBrightnessForRender(0);
            int j1 = i1 % 65536;
            int k1 = i1 / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            BlockRendererDispatcher blockrenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
            blockrenderer.renderBlock(iblockstate, pos, lift.getLiftWorld(), b);
            t.draw();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity)
    {
        return null;
    }
}
