package dev.fsg262.lava;

import dev.fsg262.coords.BlockCoordinate;

public record LavaPoolObservation(
        BlockCoordinate center,
        int sourceBlockCount,
        boolean exposedToSurface,
        boolean accessible,
        boolean completelySubmerged,
        int excavationBlocks,
        boolean insideUnusableCave,
        boolean portalBuildable,
        LavaPoolKind kind
) {
    public boolean qualifiesAsNatural(LavaSearchConfig config, BlockCoordinate origin) {
        return kind == LavaPoolKind.NATURAL
                && distanceTo(origin) <= config.maximumDistanceBlocks()
                && sourceBlockCount > 0
                && exposedToSurface
                && accessible
                && !completelySubmerged
                && excavationBlocks <= config.maximumExcavationBlocks()
                && !insideUnusableCave
                && portalBuildable;
    }

    public int distanceTo(BlockCoordinate origin) {
        return (int) Math.ceil(Math.hypot(
                (double) center.x() - origin.x(),
                (double) center.z() - origin.z()));
    }
}