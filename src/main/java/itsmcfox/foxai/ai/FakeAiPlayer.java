package itsmcfox.foxai.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;

import java.util.UUID;

public class FakeAiPlayer extends ServerPlayer {

    public FakeAiPlayer(ServerLevel level, UUID uuid, Component name, ProfilePublicKey publicKey) {
        super(level.getServer(), level, name, publicKey);
    }

    @Override
    public void tick() {
        // Logic for AI player behavior (e.g., movement, pathfinding, etc.)
        super.tick();
    }

    public void sendToPlayer(ServerPlayer player) {
        this.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this));
        this.connection.send(new ClientboundAddPlayerPacket(this));
    }

    public void removeFromLevel() {
        this.getLevel().getServer().getPlayerList().broadcastAll(new ClientboundRemoveEntitiesPacket(this.getId()));
        this.remove(Player.RemovalReason.DISCARDED);
    }
}
