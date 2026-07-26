package de.lmnr.blockrotationlock;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class BlockRotationLockClient implements ClientModInitializer {
  public static final String MOD_ID = "blockrotationlock";

  private static BlockRotationLockClient instance;
  private Direction lockedDirection;

  public static BlockHitResult modifyHitResult(BlockHitResult original, ClientPlayerEntity player, Hand hand) {

    Item item = player.getStackInHand(hand).getItem();

    // Only modify for BlockItems
    if (!(item instanceof BlockItem)) {
      return original;
    }

    Block block = ((BlockItem) item).getBlock();

    // Only modify blocks with axis property
    if (!block.getStateManager().getProperties().contains(Properties.AXIS)) {
      return original;
    }

    // Get the singleton instance to access the locked direction
    if (instance == null || instance.lockedDirection == null) {
      return original;
    }

    // Create a new BlockHitResult with the modified side
    return new BlockHitResult(
      original.getPos(),
      instance.lockedDirection,
      original.getBlockPos().offset(original.getSide()),
      original.isInsideBlock()
    );
  }

  private static void renderIndicator(DrawContext context, RenderTickCounter tickCounter) {
    if (instance == null || instance.lockedDirection == null) {
      return;
    }

    String dir = instance.lockedDirection.getAxis().asString().toUpperCase();

    int x = context.getScaledWindowWidth() / 2 + 8;
    int y = context.getScaledWindowHeight() / 2 - 4;

    context.drawText(MinecraftClient.getInstance().textRenderer, dir, x, y, Colors.WHITE, false);
  }

  @Override
  public void onInitializeClient() {
    instance = this;

    KeyBinding toggleLock = KeyBindingHelper.registerKeyBinding(new KeyBinding(
      "key.blockrotationlock.toggleLock",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_H,
      KeyBinding.Category.create(Identifier.of(BlockRotationLockClient.MOD_ID, "blockrotationlock"))
    ));

    HudElementRegistry.attachElementAfter(
      VanillaHudElements.CROSSHAIR,
      Identifier.of(BlockRotationLockClient.MOD_ID, "indicator"),
      BlockRotationLockClient::renderIndicator
    );

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      while (toggleLock.wasPressed()) {
        if (client.player != null) {

          Direction newDirection;

          if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            // If player is targeting a block, lock axis to target face axis
            BlockHitResult hit = (BlockHitResult) client.crosshairTarget;
            newDirection = hit.getSide();

          } else {
            // Otherwise lock axis to closest axis to look direction
            Vec3d look = client.player.getRotationVecClient();
            newDirection = Direction.getFacing(look.x, look.y, look.z);
          }

          if (newDirection.equals(lockedDirection)) {
            // Disable lock if already locked to this direction
            lockedDirection = null;
          } else {
            // Otherwise set new lock direction
            lockedDirection = newDirection;
          }
        }
      }
    });
  }
}