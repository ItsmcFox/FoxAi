package itsmcfox.foxai;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLServerStartingEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.*;

@Mod("foxai")
public class FoxAi {
    public static final String MODID = "foxai";

    private static final Map<String, FakeAIPlayer> aiPlayers = new HashMap<>();
    private static final Map<String, Boolean> onJoinMessage = new HashMap<>();
    private static final Map<String, Boolean> onLeaveMessage = new HashMap<>();
    private static boolean showNametag = true;
    private static int aiMax = 16;
    private static int commandLevel = 1;

    public FoxAi() {}

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        System.out.println("FoxAi loaded.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("ai")
                        .requires(source -> source.hasPermission(commandLevel))
                        .then(Commands.literal("spawn")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("skin", StringArgumentType.string())
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    String skin = StringArgumentType.getString(ctx, "skin");
                                                    return spawnAI(ctx.getSource(), name, skin);
                                                }))
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            return spawnAI(ctx.getSource(), name, "default");
                                        }))
                                .executes(ctx -> spawnAI(ctx.getSource(), generateDefaultName(), "default"))
                        )
                        .then(Commands.literal("setting")
                                .requires(source -> source.hasPermission(2)) // OP level 2
                                .then(Commands.literal("onOffMessage")
                                        .executes(ctx -> {
                                            ctx.getSource().sendSuccess(() ->
                                                    Component.literal("Join: " + getOnJoinStatus() + ", Leave: " + getOnLeaveStatus()), false);
                                            return 1;
                                        })
                                        .then(Commands.argument("type", StringArgumentType.word())
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(ctx -> {
                                                            String type = StringArgumentType.getString(ctx, "type");
                                                            boolean value = BoolArgumentType.getBool(ctx, "value");
                                                            switch (type.toLowerCase()) {
                                                                case "join" -> onJoinMessage.put("global", value);
                                                                case "leave" -> onLeaveMessage.put("global", value);
                                                                case "both" -> {
                                                                    onJoinMessage.put("global", value);
                                                                    onLeaveMessage.put("global", value);
                                                                }
                                                                default -> ctx.getSource().sendFailure(Component.literal("Invalid type."));
                                                            }
                                                            return 1;
                                                        }))))
                                .then(Commands.literal("nametag")
                                        .then(Commands.argument("value", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    showNametag = BoolArgumentType.getBool(ctx, "value");
                                                    return 1;
                                                })))
                                .then(Commands.literal("aiMax")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 128))
                                                .executes(ctx -> {
                                                    aiMax = IntegerArgumentType.getInteger(ctx, "value");
                                                    return 1;
                                                })))
                                .then(Commands.literal("aiCommandLevel")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 4))
                                                .executes(ctx -> {
                                                    commandLevel = IntegerArgumentType.getInteger(ctx, "value");
                                                    return 1;
                                                })))
                        )
        );
    }

    private int spawnAI(CommandSourceStack source, String name, String skin) {
        if (aiPlayers.size() >= aiMax) {
            source.sendFailure(Component.literal("AI limit reached."));
            return 0;
        }

        ServerLevel level = source.getLevel();
        FakeAIPlayer ai = new FakeAIPlayer(name, skin, level, showNametag);
        ai.spawn();

        aiPlayers.put(name, ai);

        if (onJoinMessage.getOrDefault("global", false)) {
            source.getServer().getPlayerList().broadcastSystemMessage(Component.literal(name + " has joined (AI)."), false);
        }

        return 1;
    }

    private static String generateDefaultName() {
        int i = 1;
        while (aiPlayers.containsKey("AI" + i)) {
            i++;
        }
        return "AI" + i;
    }

    private String getOnJoinStatus() {
        return onJoinMessage.getOrDefault("global", false) ? "true" : "false";
    }

    private String getOnLeaveStatus() {
        return onLeaveMessage.getOrDefault("global", false) ? "true" : "false";
    }
}
