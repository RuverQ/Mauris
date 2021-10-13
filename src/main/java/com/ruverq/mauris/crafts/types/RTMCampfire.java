package com.ruverq.mauris.crafts.types;

import com.ruverq.mauris.items.ItemsLoader;
import com.ruverq.mauris.items.MaurisItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.*;

public class RTMCampfire implements RecipeType{
    @Override
    public String name() {
        return "CAMPFIRE";
    }

    @Override
    public Recipe loadFromConfigurationSection(RecipePreloadInformation rpi) {
        ConfigurationSection cs = rpi.getCraftCS();

        float experience = (float) cs.getDouble("experience", 20);
        int cookingTime = cs.getInt("cookingTime", 20);
        String ingrS = cs.getString("ingredient");
        if(ingrS == null) return null;

        ChoiceUnit cu = new ChoiceUnit();
        String[] splitted = ingrS.split(" ");
        cu.addItems(splitted);

        CampfireRecipe recipe = new CampfireRecipe(rpi.getKey(), rpi.getResult(), cu.buildChoice(), experience, cookingTime);

        if(rpi.getGroup() != null && !rpi.getGroup().isEmpty()){
            recipe.setGroup(rpi.getGroup());
        }

        return recipe;
    }

}
