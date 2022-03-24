package dev.metanoia.smartitemsort.griefprevention;

import dev.metanoia.smartitemsort.BindDropTargetEvent;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.function.Supplier;

import static org.bukkit.event.EventPriority.HIGHEST;



public final class BindTargetListener implements Listener {

    private final SmartItemSortGriefPrevention plugin;
    private final DataStore dataStore;

    public BindTargetListener(final SmartItemSortGriefPrevention plugin) {
        this.plugin = plugin;

        final PluginManager pluginManager = plugin.getServer().getPluginManager();
        final Plugin dependentPlugin = pluginManager.getPlugin("GriefPrevention");

        if (dependentPlugin instanceof final GriefPrevention griefPreventionPlugin) {
            info(() -> String.format("Found GriefPrevention %s", griefPreventionPlugin.getDescription().getVersion()));
            this.dataStore = GriefPrevention.instance.dataStore;
        } else {
            this.dataStore = null;
            error(() -> "Could not find GriefPrevention plugin");
        }
    }


    @EventHandler(priority = HIGHEST, ignoreCancelled = true)
    public void onBindDropTarget(final BindDropTargetEvent e) {
        final Block source = e.getSource();
        final ItemFrame target = e.getTarget();

        if (!isPermittedTarget(source, target)) {
            debug(() -> String.format("Canceled targeting of %s from %s", target, source.getLocation()));
            e.setCancelled(true);
        }
    }


    private boolean isPermittedTarget(Block srcBlock, ItemFrame target) {
        final Config config = this.plugin.getPluginConfig();
        final boolean ignoreHeight = config.getIgnoreHeight();
        final boolean ignoreSubClaims = config.getIgnoreSubClaims();

        Claim srcClaim = dataStore.getClaimAt(srcBlock.getLocation(), ignoreHeight, ignoreSubClaims, null);

        // if the teleport is initiated from unclaimed land, any destination within range is allowed.
        if (srcClaim == null) {
            if (config.getAllowFromUnclaimedLand()) {
                trace(() -> String.format("All nearby targets allowed by policy. %s is in unclaimed land.", srcBlock));
                return true;
            }

            debug(() -> String.format("Item teleport from %s is not allowed by policy. It is in unclaimed land.", srcBlock));
            return false;
        }

        // see if the target is in the same claim as the source block. We are optimistic and provide the claim
        // from the source block as a hint in the search for the target's claim (as it should be most of the time).
        final Block targetBlock = target.getLocation().getBlock().getRelative(target.getAttachedFace());
        final Claim targetClaim = dataStore.getClaimAt(targetBlock.getLocation(), ignoreHeight, ignoreSubClaims, srcClaim);

        if (!srcClaim.equals(targetClaim)) {
            debug(() -> String.format("Target cannot be bound to source. Source claim is %s. Target claim is %s.", srcClaim, targetClaim));
            return false;
        }

        trace(() -> "Source and target are in the same claim.");
        return true;
    }


    private void debug(Supplier<String> message) { this.plugin.debug(message); }
    private void error(Supplier<String> message) { this.plugin.error(message); }
    private void info(Supplier<String> message) { this.plugin.info(message); }
    private void trace(Supplier<String> message) { this.plugin.trace(message); }

}
