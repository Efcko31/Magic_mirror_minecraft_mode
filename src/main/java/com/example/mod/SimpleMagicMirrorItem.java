package com.example.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

public class SimpleMagicMirrorItem extends Item {

    private static final int COOLDOWN_TICKS = 100;
    private static final int CHARGE_TICKS = 20;
    public static boolean isMirrorTeleporting = false;

    public SimpleMagicMirrorItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull ItemUseAnimation getUseAnimation(final @NonNull ItemStack itemStack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(final ItemStack itemStack, final LivingEntity user) {
        return CHARGE_TICKS;
    }

    @Override
    public ItemStack finishUsingItem(final ItemStack itemStack, final Level level, final LivingEntity entity) {

        if (!level.isClientSide() && entity instanceof ServerPlayer serverPlayer) {

            var respawnConfig = serverPlayer.getRespawnConfig();
            LevelData.RespawnData teleportationData = (
                    respawnConfig != null && respawnConfig.respawnData() != null
            )
                    ? respawnConfig.respawnData()
                    : level.getRespawnData();

            if (teleportationData == null || teleportationData.pos() == null) {
                return itemStack;
            }

            MinecraftServer server = serverPlayer.createCommandSourceStack().getServer();
            ServerLevel targetLevel = server.getLevel(teleportationData.dimension());

            if (targetLevel == null) {
                serverPlayer.sendSystemMessage(Component.literal("Целевой мир недоступен!"));
                return itemStack;
            }

            int xpForThisLevel = serverPlayer.getXpNeededForNextLevel();
            int xpToTake = xpForThisLevel / 2;
            serverPlayer.giveExperiencePoints(-xpToTake);

            teleport(teleportationData.pos(), targetLevel, serverPlayer);
            serverPlayer.giveExperienceLevels(-1);

            serverPlayer.sendSystemMessage(Component.literal(serverPlayer.getName().getString() +
                    " has been teleported!"));
            serverPlayer.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);
        }
        return itemStack;
    }

    @Override
    public InteractionResult use(Level level, @NonNull Player player, @NonNull InteractionHand hand) {
        if (level.isClientSide()) {
            isMirrorTeleporting = true;
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {

            var respawnConfig = serverPlayer.getRespawnConfig();

            //проверка наличия кровати, если нет то тп в точку старта
            LevelData.RespawnData teleportationData = (respawnConfig != null && respawnConfig.respawnData() != null)
                    ? serverPlayer.getRespawnConfig().respawnData()
                    : level.getRespawnData();

            //проверка на соответствие мира игрока и мира точки телепортации
            if (!level.dimension().equals(teleportationData.dimension())) {
                serverPlayer.sendSystemMessage(Component.literal("Перемещение между мирами недоступно!"));
                return InteractionResult.FAIL;
            }

            if (teleportationData == null || teleportationData.pos() == null) {
                serverPlayer.sendSystemMessage(Component.literal("Точка спавна не найдена!"));
                return InteractionResult.FAIL;
            }

            //списывает -1 прогресса при телефортаии (пол уровня)
            if (serverPlayer.experienceLevel < 1) {
                serverPlayer.sendSystemMessage(Component.literal("Недостаточно опыта для телепортации! (мин 1 уровень)"));
                return InteractionResult.FAIL;
            }

            MinecraftServer server = (serverPlayer.level()).getServer();
            if (server != null) {
                ServerLevel targetLevel = server.getLevel(teleportationData.dimension());
                if (targetLevel != null) {
                    targetLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                            MagicMirrorModMine.MIRROR_USE_SOUND, SoundSource.PLAYERS, 1.0f, 1.0f);


                    BlockPos targetPos = teleportationData.pos();
                    ChunkPos chunkPos = new ChunkPos(targetPos.getX() >> 4, targetPos.getZ() >> 4);
                    targetLevel.getChunkSource().addTicketWithRadius(TicketType.PORTAL, chunkPos, 4);
                }
            }

            player.startUsingItem(hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide()) {
            isMirrorTeleporting = false;
        }
        super.releaseUsing(stack, level, entity, timeLeft);
        return false;
    }

    public static void teleport(BlockPos teleportationPoint,
                                ServerLevel targetLevel,
                                ServerPlayer serverPlayer) {

        ServerLevel currentLevel = serverPlayer.level();
        double oldX = serverPlayer.getX();
        double oldY = serverPlayer.getY();
        double oldZ = serverPlayer.getZ();
        currentLevel.playSound(null, oldX, oldY, oldZ,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        currentLevel.sendParticles(ParticleTypes.PORTAL,
                oldX, oldY + 1.0, oldZ, 40, 0.5, 0.8, 0.5, 0.2);

        Vec3 targetVec = new Vec3(
                teleportationPoint.getX() + 0.5,
                teleportationPoint.getY() + 1.0,
                teleportationPoint.getZ() + 0.5);

        TeleportTransition transition = new TeleportTransition(
                targetLevel,
                targetVec,
                Vec3.ZERO,
                serverPlayer.getYRot(),
                serverPlayer.getXRot(),
                TeleportTransition.DO_NOTHING
        );

        serverPlayer.teleport(transition);

        targetLevel.playSound(null, targetVec.x, targetVec.y, targetVec.z,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.2f);
        targetLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                targetVec.x, targetVec.y + 1.0, targetVec.z, 30, 0.5, 0.8, 0.5, 0.1);
    }
}
