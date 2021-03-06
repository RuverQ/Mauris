package com.ruverq.mauris.crafts.listeners;

import com.ruverq.mauris.crafts.CraftingManager;
import com.ruverq.mauris.crafts.events.CancelMaurisItemCraftEvent;
import com.ruverq.mauris.items.ItemsLoader;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CraftCancel implements Listener {

    @EventHandler
    public void onCraft(PrepareItemCraftEvent e){

        for(ItemStack item : e.getInventory().getMatrix()){
            if(item == null) continue;
            item = item.clone();
            item.setAmount(1);
            if(ItemsLoader.getMaurisItem(item) != null){
                Recipe recipe = e.getRecipe();
                if(recipe instanceof Keyed){
                    Keyed keyed = (Keyed) recipe;
                    String fullkey = keyed.getKey().getNamespace() + ":" + keyed.getKey().getKey();
                    if(CraftingManager.getNamespaceKeysOfRecipes().contains(fullkey)) return;
                }

                CancelMaurisItemCraftEvent MaurisItemCraftEvent = new CancelMaurisItemCraftEvent(e.getInventory(), e.getView(), e.isRepair());
                Bukkit.getPluginManager().callEvent(MaurisItemCraftEvent);
                if(MaurisItemCraftEvent.isCancelled()) return;

                e.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
        }
    }

}
