package com.brandon3055.draconicevolution.client.model;

import com.brandon3055.brandonscore.utils.ModelUtils;
import com.brandon3055.draconicevolution.utils.LogHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Created by brandon3055 on 16/9/2015.
 */
public class ModelRenderOBJ extends RendererModel {
    private ResourceLocation customModel;
    //    private CCModel model;
    public ResourceLocation texture;
    private int displayList;
    private boolean compiled = false;
    private IBakedModel objModel;
    public float scale = 0;

    public ModelRenderOBJ(Model baseModel, ResourceLocation customModel, ResourceLocation texture) {
        super(baseModel);
        this.customModel = customModel;

        try {
//            objModel = OBJLoader.INSTANCE.loadModel(customModel).bake(TransformUtils.DEFAULT_TOOL, DefaultVertexFormats.ITEM, TextureUtils::getTexture);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

//        if (model == null) {
//            Map<String, CCModel> map = CCModel.parseObjModels(customModel);
//            model = CCModel.combine(map.values());
//        }

        this.texture = texture;
    }

    @Override
    public void render(float scale) {
        if (!this.isHidden && this.showModel) {
            if (!this.compiled) {
                this.compileDisplayList(scale);
            }

            GlStateManager.translatef(this.offsetX, this.offsetY, this.offsetZ);

            if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
                if (this.rotationPointX == 0.0F && this.rotationPointY == 0.0F && this.rotationPointZ == 0.0F) {
                    GlStateManager.callList(this.displayList);
                }
                else {
                    GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                    GlStateManager.callList(this.displayList);
                    GlStateManager.translatef(-this.rotationPointX * scale, -this.rotationPointY * scale, -this.rotationPointZ * scale);
                }
            }
            else {
                GlStateManager.pushMatrix();
                GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                if (this.rotateAngleZ != 0.0F) {
                    GlStateManager.rotatef(this.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F) {
                    GlStateManager.rotatef(this.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F) {
                    GlStateManager.rotatef(this.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
                }

                GlStateManager.callList(this.displayList);
                GlStateManager.popMatrix();
            }

            GlStateManager.translatef(-this.offsetX, -this.offsetY, -this.offsetZ);
        }
    }

    private void compileDisplayList(float scale) {
        if (this.scale == 0) {
            this.scale = scale;
        }

        if (objModel == null) {
            compiled = true;
            LogHelper.bigError("Armor Model Display List could not be compiled!!! Armor model is broken!");
            return;
        }

        scale = this.scale;
        this.displayList = GLAllocation.generateDisplayLists(1);
        GlStateManager.newList(this.displayList, GL11.GL_COMPILE);

        GlStateManager.pushMatrix();
        GlStateManager.scalef(scale, scale, scale);
        GlStateManager.rotated(180, -1, 0, 1);

        GlStateManager.bindTexture(Minecraft.getInstance().getTextureMap().getGlTextureId());
        ModelUtils.renderQuads(objModel.getQuads(null, null, ModelUtils.rand));

        GlStateManager.popMatrix();

        GlStateManager.endList();
        this.compiled = true;
    }

    @Override
    public void renderWithRotation(float scale) {
        if (!this.isHidden && this.showModel) {
            if (!this.compiled) {
                this.compileDisplayList(scale);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
            if (this.rotateAngleY != 0.0F) {
                GlStateManager.rotatef(this.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
            }

            if (this.rotateAngleX != 0.0F) {
                GlStateManager.rotatef(this.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
            }

            if (this.rotateAngleZ != 0.0F) {
                GlStateManager.rotatef(this.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);
            }

            GlStateManager.callList(this.displayList);
            GlStateManager.popMatrix();
        }

    }

    @Override
    public void postRender(float scale) {
        if (!this.isHidden && this.showModel) {
//			if(!this.compiled) {
//				this.compileDisplayList(scale);
//			}

            if (this.rotateAngleX == 0.0F && this.rotateAngleY == 0.0F && this.rotateAngleZ == 0.0F) {
                if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
                    GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                }
            }
            else {
                GlStateManager.translatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
                if (this.rotateAngleZ != 0.0F) {
                    GlStateManager.rotatef(this.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F) {
                    GlStateManager.rotatef(this.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F) {
                    GlStateManager.rotatef(this.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
                }
            }
        }

    }
}
