package cl.treecs.smwaystones;

import cl.treecs.skyblockmulti.VanillaRecipeConfig;
import cl.treecs.skyblockmulti.addon.AddonFeatureConfig;
import cl.treecs.skyblockmulti.tree.TreeCatalog;
import net.minecraft.server.MinecraftServer;

/** Exercises all four distributed mods and the 26.3 reloadable recipe registry. */
final class UpgradeSuiteVerification {
    static void start(MinecraftServer server, Runnable finish) {
        try {
            check(net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("nexus_parties"), "Nexus Parties loaded");
            check(TreeCatalog.dataPackDefinitions().keySet().stream().filter(id -> id.startsWith("skyblockmulti_bop:")).count() == 11,
                    "Eleven BOP trees loaded");
            check(bopRecipes(server) == 84, "All 84 BOP recipes loaded");
            check(managedRecipes(server) == 23, "All managed vanilla recipes loaded");
            setRecipes(false);
            server.reloadResources(server.getPackRepository().getSelectedIds()).thenRunAsync(() -> {
                check(bopRecipes(server) == 0, "BOP recipes disabled after reload");
                check(managedRecipes(server) == 0, "Vanilla recipes disabled after reload");
                setRecipes(true);
                server.reloadResources(server.getPackRepository().getSelectedIds()).thenRunAsync(() -> {
                    check(bopRecipes(server) == 84, "BOP recipes restored after reload");
                    check(managedRecipes(server) == 23, "Vanilla recipes restored after reload");
                    System.out.println("UPGRADE SUITE PASSED: four mods, BOP trees, recipes and two reloads");
                    finish.run();
                    server.halt(false);
                }, server).exceptionally(error -> fail(server, error));
            }, server).exceptionally(error -> fail(server, error));
        } catch (Throwable error) { fail(server, error); }
    }

    private static void setRecipes(boolean enabled) {
        VanillaRecipeConfig.save(enabled);
        var states = AddonFeatureConfig.configuredStates();
        states.put("skyblockmulti_biomesoplenty_trees:decoration_recipes", enabled);
        check(AddonFeatureConfig.save(states), "Recipe feature saved");
    }

    private static long bopRecipes(MinecraftServer server) {
        return server.getRecipeManager().getRecipes().stream()
                .filter(recipe -> recipe.id().identifier().getNamespace().equals("skyblockmulti_bop")).count();
    }

    private static long managedRecipes(MinecraftServer server) {
        return server.getRecipeManager().getRecipes().stream()
                .filter(recipe -> VanillaRecipeConfig.isManagedRecipe(recipe.id().identifier())).count();
    }

    private static Void fail(MinecraftServer server, Throwable error) {
        error.printStackTrace();
        server.halt(false);
        return null;
    }

    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }
}
