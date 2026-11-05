package cl.treecs.smwaystones;
import java.util.*;
public final class BalanceVerification {
    public static void main(String[] args) {
        UUID player = UUID.fromString("c9372de9-b6fb-4716-995a-69f747f90a56");
        for (int tier = 1; tier <= 3; tier++) {
            Map<String, Integer> hits = new HashMap<>();
            for (int run = 0; run < 10000; run++) {
                var items = TrialMaterials.roll(tier, "balance/" + run, player);
                if (!items.equals(TrialMaterials.roll(tier, "balance/" + run, player))) throw new AssertionError("Unstable roll");
                for (var item : items) {
                    if (!Set.of("minecraft:amethyst_shard", "minecraft:ender_pearl", "minecraft:gold_ingot").contains(item.item()))
                        throw new AssertionError("Unexpected item");
                    int max = item.item().endsWith("amethyst_shard") ? tier : item.item().endsWith("ender_pearl") && tier == 3 ? 2 : 1;
                    if (item.count() < 1 || item.count() > max) throw new AssertionError("Count");
                    hits.merge(item.item(), 1, Integer::sum);
                }
            }
            int[] expected = {20 + 15 * tier, 5 + 10 * tier, 10 + 10 * tier};
            String[] keys = {"amethyst_shard", "ender_pearl", "gold_ingot"};
            for (int i = 0; i < keys.length; i++)
                if (Math.abs(hits.getOrDefault("minecraft:" + keys[i], 0) / 100.0 - expected[i]) > 2)
                    throw new AssertionError("Probability drift tier " + tier);
        }
        System.out.println("Balance verification passed: 30,000 rolls, counts, probabilities, determinism.");
    }
}
