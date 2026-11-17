package com.example.wandry;

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
        return 12;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        int usedTicks = getUseDuration(stack, user) - timeLeft;
        if (level.isClientSide() || usedTicks < 2 || !(user instanceof Player player)) return false;

        SpellProjectile projectile = new SpellProjectile(level, player, spell);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.65F, 0.35F);
        level.addFreshEntity(projectile);
        return true;
    }
}
