package com.ruverq.mauris.gui;

import com.ruverq.mauris.items.ItemsLoader;
import com.ruverq.mauris.items.MaurisFolder;
import com.ruverq.mauris.items.MaurisItem;
import com.ruverq.mauris.utils.ItemBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_16_R3.ItemBanner;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.Items;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;

public class FoldersGUI extends GUI{

    @Getter
    @Setter
    public Set<String> folders = ItemsLoader.getLoadedFolders();

    public void setDefaultSettings(){

        setDisplayName("Mauris folders");
        setSize(9);

        int i = 0;
        for(String folder : folders){
            List<MaurisItem> items =  ItemsLoader.getMaurisItems(folder);
            int count = items.size();

            slotGUI slot = new slotGUI(this);
            slot.setSlotNumber(i);
            slot.setItem(new ItemBuilder().fastItem(
                    "#d777f2" + folder,
                    Material.BOOK,
                    "#9c9c9c" + count + "#fcba03 entries"
            ).build());
            slot.setLock(true);

            slot.clickOn((e)->{
                ItemsGUI itemsGUI = new ItemsGUI();
                itemsGUI.setFor(getPlayer());
                itemsGUI.setDisplayName("#d777f2" + folder);

                itemsGUI.setMaurisItems(ItemsLoader.getMaurisItems(folder));
                itemsGUI.setDefaultSettings();
                itemsGUI.openFor(getPlayer());
            });

            addItem(slot);
            i++;
        }


    }

}