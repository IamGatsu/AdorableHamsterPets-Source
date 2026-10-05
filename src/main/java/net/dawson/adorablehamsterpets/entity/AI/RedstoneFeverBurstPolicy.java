package net.dawson.adorablehamsterpets.entity.AI;

import net.minecraft.world.phys.Vec3;

/**
 * Pure geometric and interval rules used by the Redstone Fever burst goal.
 */
final class RedstoneFeverBurstPolicy {

    private static final double ORBIT_RADIUS = 2.0D;

    private RedstoneFeverBurstPolicy() {}

    static int[] normalizeIntervalBounds(int first, int second) {
        return new int[] {Math.min(first, second), Math.max(first, second)};
    }

    static Vec3 orbitTarget(Vec3 anchor, double angle) {
        return new Vec3(
                anchor.x + Math.cos(angle) * ORBIT_RADIUS,
                anchor.y,
                anchor.z + Math.sin(angle) * ORBIT_RADIUS);
    }

    static boolean isAnchorStable(Vec3 position, Vec3 anchor) {
        double maximumOffset = ORBIT_RADIUS + 1.5D;
        double offsetX = position.x - anchor.x;
        double offsetZ = position.z - anchor.z;
        return offsetX * offsetX + offsetZ * offsetZ <= maximumOffset * maximumOffset;
    }
}
