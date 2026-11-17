package com.example.wandry;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class SpellProjectile extends Snowball {
    private final WandItem.Spell spell;

    public SpellProjectile(Level level, Player owner, WandItem.Spell spell) {
        super(level, owner, new ItemStack(switch (spell) {
            case MAGMA -> WandryMod.MAGMA_ORB;
            case BONE -> WandryMod.BONE_ORB;
            case CELESTIAL -> WandryMod.CELESTIAL_ORB;
        }));
        this.spell = spell;
    }

    private int color() {
        return switch (spell) {
            case MAGMA -> 0xFF5314;
            case BONE -> 0xEEE8CF;
            case CELESTIAL -> 0x68CAFF;
        };
    }

    private DustParticleOptions dust(float scale) {
        return new DustParticleOptions(color(), scale);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() || !isAlive()) return;

        // Launch flare, then four independent drifting/rotating-looking shard trails.
        if (tickCount <= 2) {
            for (int i = 0; i < 12; i++) {
                double a = (Math.PI * 2.0 * i) / 12.0;
                double vx = Math.cos(a) * 0.045;
                double vz = Math.sin(a) * 0.045;
                level().addParticle(dust(0.85F), getX(), getY(), getZ(), vx, 0.025, vz);
            }
        }
        double phase = tickCount * 0.55;
        for (int i = 0; i < 4; i++) {
            double a = phase + (Math.PI * 2.0 * i / 4.0);
            double radius = 0.16 + (i % 2) * 0.035;
            double px = getX() + Math.cos(a) * radius;
            double py = getY() + Math.sin(a * 1.35) * 0.10;
            double pz = getZ() + Math.sin(a) * radius;
            double vx = -Math.sin(a) * 0.025;
            double vy = Math.cos(a * 1.35) * 0.012;
            double vz = Math.cos(a) * 0.025;
            level().addParticle(dust(0.55F), px, py, pz, vx, vy, vz);
        }
        if (tickCount % 2 == 0) {
            level().addParticle(dust(0.9F), getX(), getY(), getZ(), 0, 0, 0);
        }
    }

    private void impactBurst() {
        if (!level().isClientSide()) return;
        for (int i = 0; i < 18; i++) {
            double a = Math.PI * 2.0 * i / 18.0;
            double y = ((i % 3) - 1) * 0.045;
            level().addParticle(dust(0.75F), getX(), getY(), getZ(),
                    Math.cos(a) * 0.12, y, Math.sin(a) * 0.12);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        impactBurst();
        super.onHitEntity(hit);
        Entity entity = hit.getEntity();
        if (!(entity instanceof LivingEntity target) || !(level() instanceof ServerLevel serverLevel)) return;

        // Exactly 1.0 damage point (half a heart), shared by all three wands.
        target.hurtServer(serverLevel, damageSources().thrown(this, getOwner()), 1.0F);
        switch (spell) {
            case MAGMA -> target.igniteForSeconds(3.0F);
            case BONE -> target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 0));
            case CELESTIAL -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        impactBurst();
        super.onHitBlock(hit);
    }
}
