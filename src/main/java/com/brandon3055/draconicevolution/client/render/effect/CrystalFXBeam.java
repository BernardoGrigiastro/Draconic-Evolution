package com.brandon3055.draconicevolution.client.render.effect;

import codechicken.lib.math.MathHelper;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.lib.Vec3D;
import com.brandon3055.brandonscore.utils.BCProfiler;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.draconicevolution.api.energy.ICrystalLink;
import com.brandon3055.draconicevolution.api.energy.IENetEffectTile;
import com.brandon3055.draconicevolution.client.handler.ClientEventHandler;
import com.brandon3055.draconicevolution.utils.ResourceHelperDE;
import com.brandon3055.draconicevolution.client.DETextures;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

/**
 * Created by brandon3055 on 29/11/2016.
 */
public class CrystalFXBeam<T extends TileEntity & IENetEffectTile> extends CrystalFXBase<T> {

    private final Vec3D linkTarget;
    private final boolean terminateSource;
    private final boolean terminateTarget;
    private long lSeed = 0;
    private boolean bolt = false;
    private float powerLevel = 0;

    public CrystalFXBeam(World worldIn, T tile, ICrystalLink linkTarget) {
        super((ClientWorld)worldIn, tile);
        this.age = worldIn.rand.nextInt(1024);
        this.setPosition(tile.getBeamLinkPos(((TileEntity) linkTarget).getPos()));
        this.terminateSource = tile.renderBeamTermination();
        this.linkTarget = linkTarget.getBeamLinkPos(tile.getPos());
        this.terminateTarget = linkTarget.renderBeamTermination();
    }

    @Override
    public void tick() {
        super.tick();
        BCProfiler.TICK.start("crystal_beam_fx_update");
        if (ticksTillDeath-- <= 0) {
            setExpired();
        }

        float[] r = {0.0F, 0.8F, 1.0F};
        float[] g = {0.8F, 0.1F, 0.7F};
        float[] b = {1F, 1F, 0.2F};

        particleRed = r[tile.getTier()];
        particleGreen = g[tile.getTier()];
        particleBlue = b[tile.getTier()];

        powerLevel = (float) MathHelper.approachExp(powerLevel, fxState, 0.05);
        BCProfiler.TICK.stop();
    }

    @Override
    public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
        if (powerLevel <= 0 && !ClientEventHandler.playerHoldingWrench) {
            return;
        }
        BCProfiler.RENDER.start("crystal_beam_fx");

        float scale = 0.1F * powerLevel;
        if (ClientEventHandler.playerHoldingWrench) {
            scale = 0.1F;
        }

        Vector3d viewVec = renderInfo.getProjectedView();
        Vector3 source = new Vector3(posX - viewVec.x, posY - viewVec.y, posZ - viewVec.z);
        Vector3 target = linkTarget.toVector3().subtract(viewVec.x, viewVec.y, viewVec.z);
        Vector3 dirVec = source.copy().subtract(target).normalize();
        Vector3 planeA = dirVec.copy().perpendicular().normalize();
        Vector3 planeB = dirVec.copy().crossProduct(planeA);
        Vector3 planeC = planeB.copy().rotate(45 * MathHelper.torad, dirVec).normalize();
        Vector3 planeD = planeB.copy().rotate(-45 * MathHelper.torad, dirVec).normalize();
        planeA.multiply(scale);
        planeB.multiply(scale);
        planeC.multiply(scale);
        planeD.multiply(scale);
        float dist = 0.2F * (float) Utils.getDistanceAtoB(new Vec3D(source), new Vec3D(target));
        float anim = (ClientEventHandler.elapsedTicks + partialTicks) / -15F;

        Vector3 p1 = source.copy().add(planeA);
        Vector3 p2 = target.copy().add(planeA);
        Vector3 p3 = source.copy().subtract(planeA);
        Vector3 p4 = target.copy().subtract(planeA);
        bufferQuad(buffer, p1, p2, p3, p4, anim, dist);

        p1 = source.copy().add(planeB);
        p2 = target.copy().add(planeB);
        p3 = source.copy().subtract(planeB);
        p4 = target.copy().subtract(planeB);
        bufferQuad(buffer, p1, p2, p3, p4, anim, dist);

        p1 = source.copy().add(planeC);
        p2 = target.copy().add(planeC);
        p3 = source.copy().subtract(planeC);
        p4 = target.copy().subtract(planeC);
        bufferQuad(buffer, p1, p2, p3, p4, anim, dist);

        p1 = source.copy().add(planeD);
        p2 = target.copy().add(planeD);
        p3 = source.copy().subtract(planeD);
        p4 = target.copy().subtract(planeD);
        bufferQuad(buffer, p1, p2, p3, p4, anim, dist);

        scale *= 2;
        float minU = 0.0F;
        float maxU = 0.53F;
        float minV = 0.0F;
        float maxV = 0.53F;


        if (terminateSource) {
            float viewX = (float) (this.posX - viewVec.getX());
            float viewY = (float) (this.posY - viewVec.getY());
            float viewZ = (float) (this.posZ - viewVec.getZ());
            Vector3f[] renderVector = getRenderVectors(renderInfo, viewX, viewY, viewZ, scale);
            buffer.pos(renderVector[0].getX(), renderVector[0].getY(), renderVector[0].getZ()).tex(maxU, maxV).endVertex();
            buffer.pos(renderVector[1].getX(), renderVector[1].getY(), renderVector[1].getZ()).tex(maxU, minV).endVertex();
            buffer.pos(renderVector[2].getX(), renderVector[2].getY(), renderVector[2].getZ()).tex(minU, minV).endVertex();
            buffer.pos(renderVector[3].getX(), renderVector[3].getY(), renderVector[3].getZ()).tex(minU, maxV).endVertex();
        }

        if (terminateTarget) {
            float viewX = (float) (this.linkTarget.x - viewVec.getX());
            float viewY = (float) (this.linkTarget.y - viewVec.getY());
            float viewZ = (float) (this.linkTarget.z - viewVec.getZ());
            Vector3f[] renderVector = getRenderVectors(renderInfo, viewX, viewY, viewZ, scale);
            buffer.pos(renderVector[0].getX(), renderVector[0].getY(), renderVector[0].getZ()).tex(maxU, maxV).endVertex();
            buffer.pos(renderVector[1].getX(), renderVector[1].getY(), renderVector[1].getZ()).tex(maxU, minV).endVertex();
            buffer.pos(renderVector[2].getX(), renderVector[2].getY(), renderVector[2].getZ()).tex(minU, minV).endVertex();
            buffer.pos(renderVector[3].getX(), renderVector[3].getY(), renderVector[3].getZ()).tex(minU, maxV).endVertex();
        }

        BCProfiler.RENDER.stop();
    }

    private void bufferQuad(IVertexBuilder buffer, Vector3 p1, Vector3 p2, Vector3 p3, Vector3 p4, float anim, float dist) {
        BCProfiler.RENDER.start("buffer_quad");
        buffer.pos(p1.x, p1.y, p1.z).tex(0.5F, anim).endVertex();
        buffer.pos(p2.x, p2.y, p2.z).tex(0.5F, dist + anim).endVertex();
        buffer.pos(p4.x, p4.y, p4.z).tex(1.0F, dist + anim).endVertex();
        buffer.pos(p3.x, p3.y, p3.z).tex(1.0F, anim).endVertex();
        BCProfiler.RENDER.stop();
    }

    @Override
    public IParticleRenderType getRenderType() {
        return tile.getTier() == 0 ? BASIC_HANDLER : tile.getTier() == 1 ? WYVERN_HANDLER : DRACONIC_HANDLER;
    }

    private static final IParticleRenderType BASIC_HANDLER = new FXHandler(DETextures.ENERGY_BEAM_BASIC);
    private static final IParticleRenderType WYVERN_HANDLER = new FXHandler(DETextures.ENERGY_BEAM_WYVERN);
    private static final IParticleRenderType DRACONIC_HANDLER = new FXHandler(DETextures.ENERGY_BEAM_DRACONIC);

    public static class FXHandler implements IParticleRenderType {
        private String texture;
        private float green;

        public FXHandler(String texture) {
            this.texture = texture;
            this.green = texture.endsWith(DETextures.ENERGY_BEAM_WYVERN) ? 0.3F : 1F;
        }

        @Override
        public void beginRender(BufferBuilder builder, TextureManager p_217600_2_) {
            ResourceHelperDE.bindTexture(texture);
            RenderSystem.color4f(1.0F, green, 1.0F, 1.0F);

            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.alphaFunc(516, 0.003921569F);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.glMultiTexCoord2f(0x84c2, 240.0F, 240.0F); //Lightmap

            if (ClientEventHandler.playerHoldingWrench) {
                RenderSystem.color4f(0, 0, 1, 1);
            }

            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        }

        @Override
        public void finishRender(Tessellator tessellator) {
            tessellator.draw();
        }
    }
}