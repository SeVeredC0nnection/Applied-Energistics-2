/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.parts.misc;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import var;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.AEBasePart;
import appeng.parts.PartModel;

public class ToggleBusPart extends AEBasePart {

    @PartModels
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/toggle_bus_base");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/toggle_bus_status_off");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/toggle_bus_status_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation(AppEng.MOD_ID,
            "part/toggle_bus_status_has_channel");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_STATUS_HAS_CHANNEL);

    private final IManagedGridNode outerNode = GridHelper
            .createManagedNode(this, NodeListener.INSTANCE)
            .setTagName("outer")
            .setInWorldNode(true)
            .setIdlePowerUsage(0.0)
            .setFlags(GridFlags.PREFERRED);

    private IGridConnection connection;
    private boolean hasRedstone = false;

    private boolean clientSideEnabled;

    public ToggleBusPart(IPartItem<?> partItem) {
        super(partItem);

        this.getMainNode().setIdlePowerUsage(0.0);
        this.getMainNode().setFlags(GridFlags.PREFERRED);
    }

    @Override
    public void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(isEnabled());
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf data) {
        var changed = super.readFromStream(data);
        var wasEnabled = this.clientSideEnabled;
        this.clientSideEnabled = data.readBoolean();
        return changed || wasEnabled != clientSideEnabled;
    }

    @Override
    public void writeVisualStateToNBT(CompoundTag data) {
        super.writeVisualStateToNBT(data);
        data.putBoolean("on", isEnabled());
    }

    @Override
    public void readVisualStateFromNBT(CompoundTag data) {
        super.readVisualStateFromNBT(data);
        this.clientSideEnabled = data.getBoolean("on");
    }

    protected boolean isEnabled() {
        if (isClientSide()) {
            return clientSideEnabled;
        } else {
            return this.getHost().hasRedstone();
        }
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(6, 6, 11, 10, 10, 16);
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        final boolean oldHasRedstone = this.hasRedstone;
        this.hasRedstone = this.getHost().hasRedstone();

        if (this.hasRedstone != oldHasRedstone) {
            this.updateInternalState();
            this.getHost().markForUpdate();
        }
    }

    @Override
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        this.getOuterNode().loadFromNBT(extra);
    }

    @Override
    public void writeToNBT(CompoundTag extra) {
        super.writeToNBT(extra);
        this.getOuterNode().saveToNBT(extra);
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.getOuterNode().destroy();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.getOuterNode().create(getLevel(), getBlockEntity().getBlockPos());
        this.hasRedstone = this.getHost().hasRedstone();
        this.updateInternalState();
    }

    @Override
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        super.setPartHostInfo(side, host, blockEntity);
        this.outerNode.setExposedOnSides(EnumSet.of(side));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return this.getOuterNode().getNode();
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 5;
    }

    @Override
    public void onPlacement(Player player) {
        super.onPlacement(player);
        this.getOuterNode().setOwningPlayer(player);
    }

    private void updateInternalState() {
        final boolean intention = this.isEnabled();
        if (intention == (this.connection == null)
                && this.getMainNode().getNode() != null && this.getOuterNode().getNode() != null) {
            if (intention) {
                this.connection = GridHelper.createConnection(this.getMainNode().getNode(),
                        this.getOuterNode().getNode());
            } else {
                this.connection.destroy();
                this.connection = null;
            }
        }
    }

    IManagedGridNode getOuterNode() {
        return this.outerNode;
    }

    @Override
    public IPartModel getStaticModels() {
        if (isEnabled() && this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (isEnabled() && this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }
}
