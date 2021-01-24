package dev.lazurite.dropz.mixin.client;

import dev.lazurite.dropz.util.DropType;
import dev.lazurite.dropz.util.storage.ItemEntityStorage;
import dev.lazurite.rayon.physics.body.EntityRigidBody;
import dev.lazurite.rayon.physics.helper.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Quaternion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import physics.javax.vecmath.Quat4f;

/**
 * Modifies the renderer for {@link ItemEntity} by removing
 * features such as hovering and rotating automatically.
 */
@Environment(EnvType.CLIENT)
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity> {
    @Shadow @Final private ItemRenderer itemRenderer;

    protected ItemEntityRendererMixin(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
    }

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(ItemEntity itemEntity, float f, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
        ItemStack itemStack = itemEntity.getStack();
        BakedModel bakedModel = this.itemRenderer.getHeldItemModel(itemStack, itemEntity.world, null);
        EntityRigidBody body = EntityRigidBody.get(itemEntity);
        Quaternion orientation = QuaternionHelper.quat4fToQuaternion(QuaternionHelper.slerp(body.getPrevOrientation(new Quat4f()), body.getTickOrientation(new Quat4f()), tickDelta));
        DropType type = ((ItemEntityStorage) itemEntity).getDropType();
        this.shadowRadius = 0.0f;

        matrixStack.push();
        matrixStack.translate(0, body.getBox().getYLength() / 2.0, 0);
        matrixStack.multiply(orientation);
        matrixStack.translate(0, -type.getOffset(), 0);

        if (type.equals(DropType.DRAGON)) {
            matrixStack.translate(0, 0, -0.25f);
        }

        this.itemRenderer.renderItem(itemStack, ModelTransformation.Mode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);
        matrixStack.pop();
        info.cancel();
    }
}