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
package org.spongepowered.common.event.tracking.context.transaction;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.world.BlockChange;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

@DefaultQualifier(NonNull.class)
public abstract class GameTransaction<E extends Event & Cancellable> {

    // State definitions
    final BlockPos affectedPosition;
    final BlockState originalState;
    boolean cancelled = false;

    // Children Definitions
    @Nullable LinkedList<ResultingTransactionBySideEffect> sideEffects;

    // LinkedList node definitions
    @Nullable GameTransaction<@NonNull ?> previous;
    @Nullable GameTransaction<@NonNull ?> next;

    GameTransaction(final BlockPos affectedPosition, final BlockState originalState) {
        this.affectedPosition = affectedPosition;
        this.originalState = originalState;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GameTransaction.class.getSimpleName() + "[", "]")
            .add("affectedPosition=" + this.affectedPosition)
            .add("originalState=" + this.originalState)
            .toString();
    }


    Deque<ResultingTransactionBySideEffect> getEffects() {
        if (this.sideEffects == null) {
            this.sideEffects = new LinkedList<>();
        }
        return this.sideEffects;
    }

    public final boolean hasChildTransactions() {
        return this.sideEffects != null && this.sideEffects.stream().anyMatch(effect -> effect.head != null);
    }

    public abstract Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator();

    public abstract void addToPrinter(PrettyPrinter printer);

    public boolean acceptTileRemoval(final TileEntity tileentity) {
        return false;
    }

    public boolean acceptTileAddition(final TileEntity tileEntity) {
        return false;
    }

    public boolean acceptTileReplacement(final @Nullable TileEntity existing, final TileEntity proposed) {
        return false;
    }

    public abstract E generateEvent(PhaseContext<@NonNull ?> context, ImmutableList<GameTransaction<E>> transactions, Cause currentCause);

    public abstract void restore();

    public abstract boolean canBatchWith(@Nullable final GameTransaction<@NonNull ?> next);

    public boolean avoidsEvent() {
        return false;
    }

    public void markCancelled() {
        this.cancelled = true;
    }

    public abstract boolean markCancelledTransactions(E event, ImmutableList<? extends GameTransaction<E>> transactions);

}
