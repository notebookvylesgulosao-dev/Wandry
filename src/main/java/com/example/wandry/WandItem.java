package com.example.wandry;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public final class WandItem extends Item {
    public enum Spell { MAGMA, BONE, CELESTIAL }

    private final Spell spell;

    public WandItem(Properties properties, Spell spell) {
        super(properties);
        this.spell = spell;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 12;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        int usedTicks = getUseDuration(stack, user) - timeLeft;
        if (level.isClientSide || usedTicks < 2 || !(user instanceof Player player)) return;

        SpellProjectile projectile = new SpellProjectile(level, player, spell);
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.65F, 0.35F);
        level.addFreshEntity(projectile);
    }
}
