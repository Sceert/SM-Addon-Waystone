package cl.treecs.smwaystones;

import java.nio.charset.StandardCharsets;
import java.util.*;

/** Materials in the upstream 26.2 warp_stone recipe. Stable per completion/player. */
public final class TrialMaterials {
    public record Material(String item, int count) {}
    private TrialMaterials() {}
    public static List<Material> roll(int requestedLevel, String completion, UUID player) {
        int tier = Math.clamp(requestedLevel, 1, 3);
        UUID seed = UUID.nameUUIDFromBytes(("sm_waystones/v1/" + completion + "/" + player).getBytes(StandardCharsets.UTF_8));
        Random random = new Random(seed.getMostSignificantBits() ^ seed.getLeastSignificantBits());
        List<Material> items = new ArrayList<>();
        if (random.nextInt(100) < 20 + 15 * tier)
            items.add(new Material("minecraft:amethyst_shard", 1 + random.nextInt(tier)));
        if (random.nextInt(100) < 5 + 10 * tier)
            items.add(new Material("minecraft:ender_pearl", tier == 3 ? 1 + random.nextInt(2) : 1));
        if (random.nextInt(100) < 10 + 10 * tier)
            items.add(new Material("minecraft:gold_ingot", 1));
        return List.copyOf(items);
    }
}
