package itsmcfox.foxai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.RegisterCommandsEvent;

@Mod("foxai")
public class FoxAi {

    public FoxAi() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
    }

    private void setup(final RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("ai")
                .then(Commands.literal("spawn").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    spawnAI(player.getLevel(), player.getX(), player.getY(), player.getZ());
                    return 1;
                }))
                .then(Commands.literal("remove").executes(context -> {
                    removeAI(context.getSource().getLevel());
                    return 1;
                }))
        );
    }

    private void spawnAI(ServerLevel level, double x, double y, double z) {
        ServerPlayer aiEntity = new ServerPlayer(level.getServer(), level, null, null);
        aiEntity.setPos(x, y, z);
        aiEntity.setGameMode(Player.GameMode.SURVIVAL);
        level.addFreshEntity(aiEntity);
        level.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, aiEntity));
        level.getServer().getPlayerList().broadcastAll(new ClientboundAddPlayerPacket(aiEntity));
    }

    private void removeAI(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (player.getName().getString().equals("AI_Player")) {
                level.getServer().getPlayerList().broadcastAll(new ClientboundRemoveEntitiesPacket(player.getId()));
                player.remove(Player.RemovalReason.DISCARDED);
            }
        }
    }
}
