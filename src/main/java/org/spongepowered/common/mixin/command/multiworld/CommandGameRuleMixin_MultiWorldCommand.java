/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.command.multiworld;

import net.minecraft.command.CommandGameRule;
import net.minecraft.command.ICommandSender;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.GameRulesBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;

@Mixin(CommandGameRule.class)
public abstract class CommandGameRuleMixin_MultiWorldCommand {

    private static int currentDimension;

    private static GameRules multiWorldcommand$getGameRules(final ICommandSender sender) {
        currentDimension = ((WorldServerBridge) sender.getEntityWorld()).bridge$getDimensionId();
        return sender.getEntityWorld().getGameRules();
    }

    @Redirect(method = "execute",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/command/CommandGameRule;getOverWorldGameRules(Lnet/minecraft/server/MinecraftServer;)Lnet/minecraft/world/GameRules;"))
    private GameRules multiWorldCommand$getWorldGameRule(final CommandGameRule self, final MinecraftServer server, final MinecraftServer server2,
        final ICommandSender sender, final String[] args) {
        return multiWorldcommand$getGameRules(sender);
    }

    @Redirect(method = "execute",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;setOrCreateGameRule(Ljava/lang/String;Ljava/lang/String;)V"))
    private void multiWorldCommand$callBridgeMethodToAdjustGameRule(GameRules gameRules, String key, String ruleValue) {
        ((GameRulesBridge) gameRules).bridge$setOrCreateGameRule(key, ruleValue);
    }

    @Redirect(method = "getTabCompletions",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/command/CommandGameRule;getOverWorldGameRules(Lnet/minecraft/server/MinecraftServer;)Lnet/minecraft/world/GameRules;"))
    private GameRules multiWorldCommand$getWorldGameRule(final CommandGameRule self, final MinecraftServer server, final MinecraftServer server2,
            final ICommandSender sender, final String[] args, final BlockPos pos) {
        return multiWorldcommand$getGameRules(sender);
    }

    @Redirect(method = "notifyGameRuleChange",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetHandlerPlayServer;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private static void multiWorldCommand$sendCurrentDimensionPacket(final NetHandlerPlayServer connection, final Packet<?> packet) {
        if (connection.player.dimension == currentDimension) {
            connection.sendPacket(packet);
        }
    }

}
