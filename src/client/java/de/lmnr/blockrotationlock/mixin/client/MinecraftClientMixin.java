package de.lmnr.blockrotationlock.mixin.client;

import de.lmnr.blockrotationlock.BlockRotationLockClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

  @Unique
  private Hand currentHand;

  @ModifyArg(
    method = "doItemUse()V", at = @At(
    value = "INVOKE",
    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;" +
      "interactBlock(" +
      "Lnet/minecraft/client/network/ClientPlayerEntity;" +
      "Lnet/minecraft/util/Hand;" +
      "Lnet/minecraft/util/hit/BlockHitResult;" +
      ")" +
      "Lnet/minecraft/util/ActionResult;"
  ), index = 1
  )
  private Hand captureHand(Hand hand) {
    this.currentHand = hand;
    return hand;
  }

  @ModifyArg(
    method = "doItemUse()V", at = @At(
    value = "INVOKE",
    target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;" +
      "interactBlock(" +
      "Lnet/minecraft/client/network/ClientPlayerEntity;" +
      "Lnet/minecraft/util/Hand;" +
      "Lnet/minecraft/util/hit/BlockHitResult;" +
      ")" +
      "Lnet/minecraft/util/ActionResult;"
  ), index = 2
  )
  private BlockHitResult modifyBlockHitResult(BlockHitResult original) {
    MinecraftClient client = MinecraftClient.getInstance();
    ClientPlayerEntity player = client.player;

    if (player == null || this.currentHand == null) {
      return original;
    }

    return BlockRotationLockClient.modifyHitResult(original, player, this.currentHand);
  }
}
