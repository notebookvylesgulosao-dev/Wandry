package com.example.wandry;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side bone particle effects for the Bone Wand (charge, launch, trail, impact).
 * Particles are sent to clients by the server, so they work with the vanilla projectile
 * entity type and need no client-side code. All particles are short-lived and fade on their own.
 */
final class BoneEffects {
    /** Ticks of holding after which the wand counts as fully charged. */
    static final int FULL_CHARGE = 14;

    private static final ItemParticleOption BONE =
            new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.BONE));
    private static final ItemParticleOption BONE_BLOCK =
            new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.BONE_BLOCK));
    private static final ItemParticleOption BONE_MEAL =
            new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.BONE_MEAL));

    private BoneEffects() {}

    private static ParticleOptions pick(RandomSource r) {
        int n = r.nextInt(5);
        return n < 3 ? BONE : (n == 3 ? BONE_BLOCK : BONE_MEAL);
    }

    private static void spawn(ServerLevel level, ParticleOptions p, Vec3 pos, Vec3 vel) {
        // count = 0 makes the (dx, dy, dz) arguments a velocity; "speed" scales it.
        level.sendParticles(p, pos.x, pos.y, pos.z, 0, vel.x, vel.y, vel.z, 1.0);
    }

    private static Vec3 tip(Player player, Vec3 look) {
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        return player.getEyePosition().add(look.scale(0.85)).add(right.scale(0.28)).add(0.0, -0.3, 0.0);
    }

    /** Charging while the button is held: bones gather in a tightening spiral at the wand tip. */
    static void charge(ServerLevel level, Player player, int ticks) {
        RandomSource r = level.getRandom();
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        Vec3 up = right.cross(look).normalize();
        Vec3 tip = tip(player, look);
        double progress = Math.min(1.0, ticks / (double) FULL_CHARGE);

        // Orbiting bones: more of them and tighter as the charge builds.
        int orbit = 3 + (int) (progress * 5);
        double radius = 0.75 - 0.45 * progress;
        double spin = ticks * 0.45;
        for (int i = 0; i < orbit; i++) {
            double a = spin + Math.PI * 2.0 * i / orbit;
            Vec3 off = right.scale(Math.cos(a) * radius).add(up.scale(Math.sin(a) * radius));
            Vec3 pos = tip.add(off);
            // Velocity pulls toward the tip, with a bit of swirl.
            Vec3 vel = off.scale(-0.06).add(right.scale(-Math.sin(a) * 0.05)).add(up.scale(Math.cos(a) * 0.05));
            spawn(level, pick(r), pos, vel);
        }
        // Bones rising around the caster's body.
        for (int i = 0; i < 2; i++) {
            double a = r.nextDouble() * Math.PI * 2.0;
            double rad = 0.6 + r.nextDouble() * 0.3;
            Vec3 pos = player.position().add(Math.cos(a) * rad, 0.1, Math.sin(a) * rad);
            spawn(level, pick(r), pos, new Vec3(0.0, 0.07 + progress * 0.06, 0.0));
        }
        if (ticks % 3 == 0) {
            spawn(level, ParticleTypes.WHITE_ASH, tip.add(r.nextGaussian() * 0.15, r.nextGaussian() * 0.15, r.nextGaussian() * 0.15),
                    new Vec3(0.0, 0.02, 0.0));
        }
        // Fully charged: pulse that repeats every few ticks.
        if (ticks >= FULL_CHARGE && (ticks - FULL_CHARGE) % 4 == 0) {
            ring(level, tip, right, up, 0.5, 14, 0.0, BONE);
            level.sendParticles(ParticleTypes.POOF, tip.x, tip.y, tip.z, 3, 0.1, 0.1, 0.1, 0.02);
        }
    }

    private static void ring(ServerLevel level, Vec3 center, Vec3 right, Vec3 up, double speed, int n, double forward, ParticleOptions p) {
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2.0 * i / n;
            Vec3 dir = right.scale(Math.cos(a)).add(up.scale(Math.sin(a))).scale(speed * 0.25);
            spawn(level, p, center, dir);
        }
    }

    /** Big burst of bones out of the wand tip on release. Scales with how long it was charged. */
    static void launch(ServerLevel level, Player player, int ticks) {
        RandomSource r = level.getRandom();
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        Vec3 up = right.cross(look).normalize();
        Vec3 tip = tip(player, look);
        double progress = Math.min(1.0, ticks / (double) FULL_CHARGE);

        int cone = 40 + (int) (progress * 40);
        for (int i = 0; i < cone; i++) {
            Vec3 v = look.scale(0.35 + r.nextDouble() * 0.45).add(
                    r.nextGaussian() * 0.14, r.nextGaussian() * 0.14, r.nextGaussian() * 0.14);
            spawn(level, pick(r), tip, v);
        }
        // Three expanding rings of bones around the cast direction.
        for (int k = 0; k < 3; k++) {
            double speed = 0.45 + k * 0.35;
            for (int i = 0; i < 16; i++) {
                double a = Math.PI * 2.0 * i / 16.0 + k * 0.2;
                Vec3 dir = right.scale(Math.cos(a)).add(up.scale(Math.sin(a))).scale(speed * 0.25).add(look.scale(0.12 + k * 0.05));
                spawn(level, pick(r), tip, dir);
            }
        }
        level.sendParticles(ParticleTypes.POOF, tip.x, tip.y, tip.z, 8, 0.15, 0.15, 0.15, 0.04);
        level.sendParticles(ParticleTypes.WHITE_ASH, tip.x, tip.y, tip.z, 20, 0.3, 0.3, 0.3, 0.05);
    }

    /** Every tick while flying: dense helix of bones around the path plus loose fragments and ash. */
    static void trail(ServerLevel level, Entity projectile, int age) {
        RandomSource r = level.getRandom();
        Vec3 motion = projectile.getDeltaMovement();
        Vec3 dir = motion.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : motion.normalize();
        Vec3 side = dir.cross(new Vec3(0.0, 1.0, 0.0));
        side = side.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        Vec3 up = side.cross(dir).normalize();
        Vec3 pos = projectile.position();

        // Double helix of bones, sampled along the distance travelled this tick (no gaps).
        for (int i = 0; i < 6; i++) {
            double t = i / 6.0;
            double a = age * 0.9 + t * 1.5 + (i % 2) * Math.PI;
            Vec3 off = side.scale(Math.cos(a) * 0.22).add(up.scale(Math.sin(a) * 0.22));
            Vec3 p = pos.subtract(motion.scale(t)).add(off);
            spawn(level, pick(r), p, off.scale(0.04).add(0.0, 0.01, 0.0));
        }
        // Loose fragments that drop out behind.
        for (int i = 0; i < 4; i++) {
            double t = r.nextDouble();
            Vec3 p = pos.subtract(motion.scale(t)).add(r.nextGaussian() * 0.1, r.nextGaussian() * 0.1, r.nextGaussian() * 0.1);
            spawn(level, pick(r), p, new Vec3(r.nextGaussian() * 0.02, r.nextDouble() * 0.03, r.nextGaussian() * 0.02));
        }
        if (age % 2 == 0) {
            level.sendParticles(ParticleTypes.WHITE_ASH, pos.x, pos.y, pos.z, 2, 0.12, 0.12, 0.12, 0.01);
        }
    }

    /** Large bone shatter when the projectile hits something. */
    static void impact(ServerLevel level, double x, double y, double z) {
        RandomSource r = level.getRandom();
        for (int i = 0; i < 60; i++) {
            Vec3 v = new Vec3(r.nextGaussian(), r.nextGaussian() * 0.8 + 0.3, r.nextGaussian()).scale(0.22);
            spawn(level, pick(r), new Vec3(x, y, z), v);
        }
        for (int i = 0; i < 20; i++) {
            double a = Math.PI * 2.0 * i / 20.0;
            spawn(level, BONE, new Vec3(x, y, z), new Vec3(Math.cos(a) * 0.35, 0.05, Math.sin(a) * 0.35));
        }
        level.sendParticles(ParticleTypes.POOF, x, y, z, 10, 0.2, 0.2, 0.2, 0.05);
        level.sendParticles(ParticleTypes.WHITE_ASH, x, y, z, 25, 0.35, 0.35, 0.35, 0.06);
    }
}
