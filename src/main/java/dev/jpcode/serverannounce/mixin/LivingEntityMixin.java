package dev.jpcode.serverannounce.mixin;

import net.minecraft.entity.LivingEntity;

import net.minecraft.entity.damage.DamageSource;

import net.minecraft.network.MessageType;
import net.minecraft.text.Text;

import net.minecraft.util.Util;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.TitleScreen;

import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static dev.jpcode.serverannounce.ServerAnnounce.LOGGER;

@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    /**
     * Injects into the beginning of the onDeath method and sends a chat death message if the entity has a custom name.
     *
     * @param info callback info
     */
    @Inject(method = "onDeath",
        at = @At("HEAD"),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void onDeath(DamageSource source, CallbackInfo info) {
        LivingEntity self = ((LivingEntity)(Object)this);
        if (self.hasCustomName()) {
            Text text = self.getDamageTracker().getDeathMessage();
            self.getServer().getPlayerManager().broadcastChatMessage(text, MessageType.SYSTEM, Util.NIL_UUID);
        }
    }
}
