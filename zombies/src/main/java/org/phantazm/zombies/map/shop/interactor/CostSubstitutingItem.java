package org.phantazm.zombies.map.shop.interactor;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;
import org.phantazm.core.item.UpdatingItem;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionModifierSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Model("item.cost_substituting")
@Cache(false)
public class CostSubstitutingItem implements UpdatingItem {
    private static final int UPDATE_INTERVAL = 10;

    private final Data data;
    private final TransactionModifierSource modifierSource;

    private ItemStack itemStack;
    private int ticks;

    private boolean hasOldCost;
    private int oldCost;

    @FactoryMethod
    public CostSubstitutingItem(@NotNull Data data, @NotNull TransactionModifierSource modifierSource) {
        this.data = data;
        this.modifierSource = modifierSource;
        this.itemStack = computeItemStack(data.cost);
    }

    @Override
    public @NotNull ItemStack update(long time, @NotNull ItemStack current) {
        int cost = cost();
        this.oldCost = cost;
        this.hasOldCost = true;
        return itemStack = computeItemStack(cost);
    }

    @Override
    public boolean hasUpdate(long time, @NotNull ItemStack current) {
        return (ticks++ % UPDATE_INTERVAL == 0 && (hasOldCost && cost() != oldCost));
    }

    @Override
    public @NotNull ItemStack currentItem() {
        return itemStack;
    }

    private ItemStack computeItemStack(int cost) {
        ItemStack.Builder builder = ItemStack.builder(data.material);
        if (data.tag != null) {
            try {
                builder.meta((NBTCompound)new SNBTParser(new StringReader(data.tag)).parse());
            }
            catch (NBTException ignored1) {
            }
        }

        TagResolver costTag = Placeholder.unparsed("cost", Integer.toString(cost));
        if (data.displayName != null) {
            builder.displayName(MiniMessage.miniMessage().deserialize(data.displayName, costTag));
        }

        if (data.lore != null) {
            List<Component> components = new ArrayList<>(data.lore.size());
            for (String format : data.lore) {
                components.add(MiniMessage.miniMessage().deserialize(format, costTag));
            }
            builder.lore(components);
        }

        return builder.build();
    }

    private int cost() {
        int cost = data.cost;
        for (Transaction.Modifier modifier : modifierSource.modifiers(data.modifier)) {
            cost = modifier.modify(cost);
        }

        return cost;
    }

    @DataObject
    public record Data(@NotNull Material material,
                       @Nullable String displayName,
                       @Nullable List<String> lore,
                       @Nullable String tag,
                       int cost,
                       @NotNull Key modifier) {
        @Default("displayName")
        public static @NotNull ConfigElement defaultDisplayName() {
            return ConfigPrimitive.NULL;
        }

        @Default("lore")
        public static @NotNull ConfigElement defaultLore() {
            return ConfigPrimitive.NULL;
        }

        @Default("tag")
        public static @NotNull ConfigElement defaultTag() {
            return ConfigPrimitive.NULL;
        }
    }
}
