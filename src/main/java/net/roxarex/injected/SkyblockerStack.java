package net.roxarex.injected;


import de.hysky.skyblocker.skyblock.item.PetInfo;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.utils.ItemAbility;

import java.util.List;

public interface SkyblockerStack {

    default String getSkyblockId() {
        return "";
    }

    default String getSkyblockApiId() {
        return "";
    }

    default String getNeuName() {
        return "";
    }

    default String getUuid() {
        return "";
    }

    default List<String> roxarexmods$getLoreStrings() {
        return List.of();
    }

    default List<ItemAbility> roxarexmods$getAbilities() {
        return List.of();
    }

    default PetInfo getPetInfo() {
        return PetInfo.EMPTY;
    }

    default SkyblockItemRarity getSkyblockRarity() { return SkyblockItemRarity.UNKNOWN; }
}
