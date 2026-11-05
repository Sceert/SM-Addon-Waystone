package cl.treecs.smwaystones;

import cl.treecs.skyblockmulti.api.addon.AddonFeatureApi;
import cl.treecs.skyblockmulti.api.addon.AddonRewardApi;
import net.blay09.mods.waystones.api.WaystoneType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import java.util.List;

public final class SmWaystonesAddon implements ModInitializer {
    public static final String MOD_ID = "skyblockmulti_waystones";
    @Override public void onInitialize() {
        try {
            Class.forName("cl.treecs.skyblockmulti.api.addon.AddonRewardApi");
        } catch (ClassNotFoundException error) {
            throw new IllegalStateException("SM Addon: Waystone requires Skyblock Multi with AddonRewardApi v1; install the companion core build.", error);
        }
        AddonRewardApi.registerStarter(MOD_ID + ":starter_waystone", context -> {
            if (!AddonFeatureApi.isEnabled(MOD_ID + ":starter_waystone")) return List.of();
            // Public Waystones type registry; no ModItems/ModBlocks/internal classes.
            WaystoneType type = WaystoneType.get(Identifier.parse("waystones:andesite_waystone"));
            if (type == null) throw new IllegalStateException("Missing public Waystone type");
            String item = BuiltInRegistries.ITEM.getKey(type.block().asItem()).toString();
            return List.of(new AddonRewardApi.ItemGrant(item, 1));
        });
        AddonRewardApi.registerTrial(MOD_ID + ":infernal_trial_loot", context -> {
            if (!AddonFeatureApi.isEnabled(MOD_ID + ":infernal_trial_loot")) return List.of();
            return TrialMaterials.roll(context.level(), context.completionId(), context.player()).stream()
                    .map(item -> new AddonRewardApi.ItemGrant(item.item(), item.count())).toList();
        });
    }
}
