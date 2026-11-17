package com.example.wandry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

public final class WandItem extends Item {
    public enum Spell { MAGMA, BONE, CELESTIAL }

    private final Spell spell;

    public WandItem(Properties properties, Spell spell) {
        super(properties);
        this.spell = spell;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        // Bone Wand can be held (charged) for as long as the player likes; the others keep 12 ticks.
        return spell == Spell.BONE ? 72000 : 12;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (spell == Spell.BONE && user instanceof Player player && level instanceof ServerLevel serverLevel) {
            BoneEffects.charge(serverLevel, player, getUseDuration(stack, user) - remainingUseDuration);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        int usedTicks = getUseDuration(stack, user) - timeLeft;
        if (level.isClientSide() || usedTicks < 2 || !(user instanceof Player player)) return false;

        SpellProjectile projectile = new SpellProjectile(level, player, spell);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.65F, 0.35F);
        level.addFreshEntity(projectile);
        if (spell == Spell.BONE && level instanceof ServerLevel serverLevel) {
            BoneEffects.launch(serverLevel, player, usedTicks);
        }
        return true;
    }
}
