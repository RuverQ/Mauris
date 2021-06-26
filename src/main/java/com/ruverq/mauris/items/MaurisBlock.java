package com.ruverq.mauris.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ruverq.mauris.DataHelper;
import com.ruverq.mauris.items.blocktypes.MaurisBlockType;
import com.ruverq.mauris.utils.BlockProperty;
import com.ruverq.mauris.utils.BlockStateParser;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import javax.xml.crypto.Data;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MaurisBlock extends MaurisItem {

    public MaurisBlock(MaurisFolder folder, String name, List<String> textures, String displayName, List<String> lore, Material material, boolean generateModel, boolean isBlock, MaurisBlock maurisBlock, File file, MaurisBlockType type, int hardness, HashMap<ItemStack, Integer> hardnessPerTool,  String breakSound,  String placeSound,  String stepSound, MaurisLootTable lootTable) {
        super(folder, name, textures, displayName, lore, material, generateModel, isBlock, maurisBlock, file);
        this.type = type;
        this.hardness = hardness;
        this.hardnessPerTool = hardnessPerTool;
        this.stepSound = stepSound;
        this.breakSound = breakSound;
        this.placeSound = placeSound;
        this.lootTable = lootTable;
    }

    @Getter
    MaurisLootTable lootTable;

    @Getter
    String breakSound;
    @Getter
    String stepSound;
    @Getter
    String placeSound;

    public String getPlaceSoundSafe() {
        if(placeSound == null) return type.material().createBlockData().getSoundGroup().getPlaceSound().getKey().toString();
        return placeSound;
    }

    public String getBreakSoundSafe() {
        if(breakSound == null) return type.material().createBlockData().getSoundGroup().getBreakSound().getKey().toString();
        return breakSound;
    }

    public String getStepSoundSafe() {
        if(stepSound == null) return type.material().createBlockData().getSoundGroup().getStepSound().getKey().toString();
        return stepSound;
    }

    @Getter
    int blockId;

    @Getter
    MaurisBlockType type;

    @Getter
    int hardness;

    HashMap<ItemStack, Integer> hardnessPerTool;

    public int getHardnessFromTool(ItemStack itemStack){
        Object hardnessTool = hardnessPerTool.get(itemStack);
        if(hardnessTool == null) return hardness;

        return (int) hardnessTool;
    }

    @Override
    public boolean isGenerated(){
        int i = DataHelper.getId(folder, name, "blocks." + type.material().name());
        return i != -1;
    }

    public BlockData getAsBlockData(){
        List<BlockProperty> properties = type.generate(blockId);
        return BlockStateParser.createData(type.material(), properties);
    }

    @Override
    public void generate(){
        MaurisItem item = (MaurisItem) this;
        item.generate("minecraft:block/cube_all");

        String fff = folder.getName() + ":";

        //First DIR

        JsonObject modelObject = new JsonObject();
        modelObject.addProperty("parent", "minecraft:block/cube_all");

        JsonObject texturesObject = new JsonObject();
        texturesObject.addProperty("all", fff + textures.get(0));
        texturesObject.addProperty("particle", fff + textures.get(0));

        modelObject.add("textures", texturesObject);

        DataHelper.deleteFile("resource_pack/assets/" + folder.getName() +"/models/" + name + ".json");
        DataHelper.createFolder("resource_pack/assets/" + folder.getName() +"/models");
        DataHelper.createFile("resource_pack/assets/" + folder.getName() +"/models/" + name + ".json", modelObject.toString());

        //Second DIR

        JsonObject generalBSObject = new JsonObject();

        JsonArray multipartArray = new JsonArray();
        String blockstatePath = "resource_pack/assets/minecraft/blockstates/" + type.material().name().toLowerCase() + ".json";
        File blockstateFile = DataHelper.getFile(blockstatePath);
        if(blockstateFile != null){
            generalBSObject = DataHelper.FileToJson(blockstateFile);
            multipartArray = generalBSObject.getAsJsonArray("multipart");
        }

        this.blockId = DataHelper.addId(folder, name, "blocks." + type.material().name());

        List<BlockProperty> properties = type.generate(blockId);
        JsonObject mPObject = new JsonObject();
        JsonObject whenObject = new JsonObject();
        JsonObject applyObject = new JsonObject();
        for(BlockProperty property : properties){
            whenObject = property.smartAdd(whenObject);
        }

        mPObject.add("when", whenObject);

        applyObject.addProperty("model", fff + name);
        mPObject.add("apply", applyObject);

        multipartArray.add(mPObject);

        generalBSObject.add("multipart", multipartArray);

        DataHelper.deleteFile(blockstatePath);
        DataHelper.createFolder("resource_pack/assets/minecraft/blockstates/");
        DataHelper.createFile(blockstatePath, generalBSObject.toString());
    }
}