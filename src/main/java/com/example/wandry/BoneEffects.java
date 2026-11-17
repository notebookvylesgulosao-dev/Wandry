package com.example.wandry;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side bone particle effects for the Bone Wand. Particles are sent to
 * clients by the server, so they work with the vanilla projectile entity type
 * and need no client-side code. Particles are short-lived and fade on their own.
 */
final class BoneEffects {
    private BoneEffects() {}

    private static ItemParticleOption bone() {
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.BONE));
    }

    /** Burst of bone fragments flying out of the wand tip when the spell is cast. */
    static void launch(ServerLevel level, Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 origin = player.getEyePosition().add(look.scale(0.9)).add(0.0, -0.25, 0.0);
        RandomSource random = level.getRandom();
        ItemParticleOption bone = bone();
        for (int i = 0; i < 14; i++) {
            // count = 0 makes the (dx, dy, dz) arguments a velocity; "speed" scales it.
            Vec3 v = look.scale(0.35).add(
                    random.nextGaussian() * 0.12,
                    random.nextGaussian() * 0.12,
                    random.nextGaussian() * 0.12);
            level.sendParticles(bone, origin.x, origin.y, origin.z, 0, v.x, v.y, v.z, 1.0);
        }
        // Small ring of bone dust-like fragments around the wand tip.
        for (int i = 0; i < 8; i++) {
            double a = Math.PI * 2.0 * i / 8.0;
            Vec3 side = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
            Vec3 up = side.cross(look).normalize();
            Vec3 v = side.scale(Math.cos(a) * 0.08).add(up.scale(Math.sin(a) * 0.08)).add(look.scale(0.12));
            level.sendParticles(bone, origin.x, origin.y, origin.z, 0, v.x, v.y, v.z, 1.0);
        }
    }

    /** Called every tick while the projectile flies: drops a couple of bone fragments behind it. */
    static void trail(ServerLevel level, Entity projectile) {
        RandomSource random = level.getRandom();
        ItemParticleOption bone = bone();
        Vec3 motion = projectile.getDeltaMovement();
        for (int i = 0; i < 2; i++) {
            // Spread the fragments along the path travelled this tick so the trail has no gaps.
            double t = random.nextDouble();
            double x = projectile.getX() - motion.x * t + random.nextGaussian() * 0.04;
            double y = projectile.getY() - motion.y * t + random.nextGaussian() * 0.04;
            double z = projectile.getZ() - motion.z * t + random.nextGaussian() * 0.04;
            // Low velocity: fragments hang behind and drop out, then vanish with the particle lifetime.
            level.sendParticles(bone, x, y, z, 0,
                    random.nextGaussian() * 0.01, random.nextDouble() * 0.02, random.nextGaussian() * 0.01, 1.0);
        }
    }

    /** Bone shatter when the projectile hits something. */
    static void impact(ServerLevel level, double x, double y, double z) {
        level.sendParticles(bone(), x, y, z, 14, 0.12, 0.12, 0.12, 0.12);
    }
}
