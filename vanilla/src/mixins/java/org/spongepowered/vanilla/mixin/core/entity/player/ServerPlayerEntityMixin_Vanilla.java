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
package org.spongepowered.vanilla.mixin.core.entity.player;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.network.packet.ChangeViewerEnvironmentPacket;
import org.spongepowered.common.network.packet.RegisterDimensionTypePacket;
import org.spongepowered.common.network.packet.SpongePacketHandler;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_Vanilla implements ServerPlayerEntityBridge  {

    @Override
    public void bridge$sendDimensionData(final NetworkManager manager, final DimensionType dimensionType) {
        SpongePacketHandler.getChannel().sendTo((ServerPlayer) this, new RegisterDimensionTypePacket(dimensionType));
    }

    @Override
    public void bridge$sendChangeDimension(final DimensionType type, final WorldType generator, final GameType gameType) {
        ((ServerPlayerEntity) (Object) this).connection.sendPacket(new SRespawnPacket(type, generator, gameType));
    }

    @Override
    public void bridge$sendViewerEnvironment(final org.spongepowered.api.world.dimension.DimensionType dimensionType) {
        SpongePacketHandler.getChannel().sendTo((ServerPlayer) this, new ChangeViewerEnvironmentPacket(dimensionType));
    }
}