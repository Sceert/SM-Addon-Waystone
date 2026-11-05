package cl.treecs.smwaystones;

import cl.treecs.skyblockmulti.addon.AddonFeatureConfig;
import cl.treecs.skyblockmulti.addon.AddonFeatureRegistry;
import cl.treecs.skyblockmulti.api.addon.AddonRewardApi;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

/** Real dedicated-server runtime, synthetic participant UUIDs; no player login simulation. */
public final class IntegrationVerification implements ModInitializer {
    private boolean ran;
    private static final UUID PLAYER = UUID.fromString("c9372de9-b6fb-4716-995a-69f747f90a56");
    @Override public void onInitialize() {
        if (!Boolean.getBoolean("smwaystones.integration")) return;
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (ran) return;
            ran = true;
            if (Boolean.getBoolean("smwaystones.upgradeSuite")) {
                UpgradeSuiteVerification.start(server, () -> {
                    try { verify(server); }
                    catch (Exception error) { throw new IllegalStateException(error); }
                });
                return;
            }
            try { verify(server); }
            catch (Throwable error) { error.printStackTrace(); throw new IllegalStateException("Integration verification failed", error); }
            finally { server.halt(false); }
        });
    }
    private static void verify(MinecraftServer server) throws Exception {
        Path report = Path.of("verification.txt");
        boolean restart = Files.exists(report);
        var features = AddonFeatureRegistry.features().stream()
                .filter(feature -> feature.addonModId().equals(SmWaystonesAddon.MOD_ID)).toList();
        require(features.size() == 2, "Two independently discovered features");
        var player = new ServerPlayer(server, server.overworld(), new GameProfile(PLAYER, "AddonTest"), ClientInformation.createDefault());
        var config = AddonFeatureConfig.configuredStates();
        if (!restart) {
            for (int combo = 0; combo < 4; combo++) {
                boolean chestEnabled = (combo & 1) != 0;
                boolean lootEnabled = (combo & 2) != 0;
                config.put(SmWaystonesAddon.MOD_ID + ":starter_waystone", chestEnabled);
                config.put(SmWaystonesAddon.MOD_ID + ":infernal_trial_loot", lootEnabled);
                require(AddonFeatureConfig.save(config), "Config saved");
                BlockPos origin = new BlockPos(3000 + combo * 32, 64, 3000);
                server.overworld().getChunkAt(origin);
                server.overworld().setBlockAndUpdate(origin.below(), Blocks.BEDROCK.defaultBlockState());
                var source = server.createCommandSourceStack().withEntity(player).withPosition(Vec3.atLowerCornerOf(origin));
                server.getCommands().performPrefixedCommand(source, "function skyblockmulti:island/starter_chest");
                require(count(server, origin.offset(2, 1, 0)) == (chestEnabled ? 1 : 0), "Starter combination " + combo);
                server.getCommands().performPrefixedCommand(source, "function skyblockmulti:island/starter_chest");
                require(count(server, origin.offset(2, 1, 0)) == (chestEnabled ? 1 : 0), "Repeated starter callback " + combo);
                boolean found = false;
                for (int i = 0; i < 100; i++) {
                    var context = new AddonRewardApi.TrialContext(PLAYER, "test:trial", "test/" + i, 3, 2);
                    var grants = AddonRewardApi.trialItems(context);
                    require(grants.equals(AddonRewardApi.trialItems(context)), "Stable repeated reward roll");
                    found |= !grants.isEmpty();
                }
                require(found == lootEnabled, "Loot combination " + combo);
            }
        }
        Class<?> ledger = Class.forName("cl.treecs.skyblockmulti.infernal.InfernalRewardLedger");
        Field pending = ledger.getDeclaredField("PENDING"); pending.setAccessible(true);
        Method complete = ledger.getDeclaredMethod("completeAddonTrial", MinecraftServer.class, String.class, String.class, Set.class);
        complete.setAccessible(true);
        if (!restart) {
            for (int i = 0; i < 20; i++) complete.invoke(null, server, "verify/" + i, "test:trial", Set.of(PLAYER));
            var before = pending.get(null).toString();
            for (int i = 0; i < 20; i++) complete.invoke(null, server, "verify/" + i, "test:trial", Set.of(PLAYER));
            require(before.equals(pending.get(null).toString()), "Completion callback deduplication");
            require(!((Map<?, ?>) pending.get(null)).isEmpty(), "Offline UUID has pending rewards");
            Files.writeString(Path.of("expected-pending.txt"), before);
            Files.writeString(report, "PASS: four combinations, starter idempotence, stable rolls, offline queue, completion deduplication.\n");
        } else {
            require(Files.readString(Path.of("expected-pending.txt")).equals(pending.get(null).toString()), "Pending rewards survive server restart");
            for (int combo = 0; combo < 4; combo++) {
                BlockPos origin = new BlockPos(3000 + combo * 32, 64, 3000);
                server.overworld().getChunkAt(origin);
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withEntity(player).withPosition(Vec3.atLowerCornerOf(origin)), "function skyblockmulti:island/starter_chest");
                require(count(server, origin.offset(2, 1, 0)) == ((combo & 1) != 0 ? 1 : 0), "Persisted starter decision " + combo);
            }
            Files.writeString(report, "PASS: pending rewards and all starter decisions survive restart.\n", StandardOpenOption.APPEND);
            Method claim = ledger.getDeclaredMethod("claim", ServerPlayer.class); claim.setAccessible(true);
            var queue = (Deque<?>) ((Map<?, ?>) pending.get(null)).get(PLAYER);
            int before = queue.size();
            claim.invoke(null, player);
            require(queue.size() == before - 1, "One claim consumes one reward");
            int inventory = 0;
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
                inventory += player.getInventory().getItem(slot).getCount();
            require(inventory > 0, "Claim delivers actual registered items to inventory");
            String afterClaim = pending.get(null).toString();
            Method reload = ledger.getDeclaredMethod("load", MinecraftServer.class); reload.setAccessible(true);
            reload.invoke(null, server);
            require(afterClaim.equals(pending.get(null).toString()), "Claim consumption survives ledger reload");
            Files.writeString(report, "PASS: synthetic player claims one reward; inventory receives items; reload does not restore consumed reward.\n", StandardOpenOption.APPEND);
        }
        config.put(SmWaystonesAddon.MOD_ID + ":starter_waystone", false);
        config.put(SmWaystonesAddon.MOD_ID + ":infernal_trial_loot", true);
        AddonFeatureConfig.save(config);
        System.out.println("SM Addon: Waystone INTEGRATION PASSED (restart=" + restart + ")");
    }
    private static int count(MinecraftServer server, BlockPos pos) {
        if (!(server.overworld().getBlockEntity(pos) instanceof Container container)) return 0;
        int result = 0;
        for (int i = 0; i < container.getContainerSize(); i++)
            if (BuiltInRegistries.ITEM.getKey(container.getItem(i).getItem()).toString().equals("waystones:andesite_waystone"))
                result += container.getItem(i).getCount();
        return result;
    }
    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
