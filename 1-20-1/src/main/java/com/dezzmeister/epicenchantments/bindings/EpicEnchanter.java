package com.dezzmeister.epicenchantments.bindings;

import java.util.List;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class EpicEnchanter {
    private ItemStack item;

    public EpicEnchanter(ItemStack _item) {
        item = _item;
    }

    public ItemStack enchant() throws ItemConsumedException {
        if (item == null) {
            throw new ItemConsumedException();
        }

        final ItemStack out = item;
        item = null;

        return out;
    }

    private EpicEnchanter withRightBindings() {
        CompoundTag tag = item.getTag();

        if (tag == null) {
            tag = new CompoundTag();
        }

        Tag rightBindings = tag.get(NBTKeys.RIGHT_BINDINGS);

        if (!(rightBindings instanceof ListTag)) {
            rightBindings = new ListTag();
        }

        tag.put(NBTKeys.RIGHT_BINDINGS, rightBindings);
        item.setTag(tag);

        return this;
    }

    public EpicEnchanter withRightItemBinding(float radius, List<String> commands) throws ItemConsumedException {
        if (item == null) {
            throw new ItemConsumedException();
        }

        this.withRightBindings();
        final CompoundTag bindingTag = createRightBindingTag(commands);

        bindingTag.put(NBTKeys.ACTIVATION, StringTag.valueOf(Activation.RightClick.AIR.getNbtName()));
        bindingTag.put(NBTKeys.RADIUS, FloatTag.valueOf(radius));
        this.saveRightBindingTag(bindingTag);

        return this;
    }

    public EpicEnchanter withRightBlockBinding(float radius, List<String> commands) throws ItemConsumedException {
        if (item == null) {
            throw new ItemConsumedException();
        }

        this.withRightBindings();
        final CompoundTag bindingTag = createRightBindingTag(commands);

        bindingTag.put(NBTKeys.ACTIVATION, StringTag.valueOf(Activation.RightClick.BLOCK.getNbtName()));
        bindingTag.put(NBTKeys.RADIUS, FloatTag.valueOf(radius));
        this.saveRightBindingTag(bindingTag);

        return this;
    }

    public EpicEnchanter withRightEntityBinding(boolean asPlayer, List<String> commands) throws ItemConsumedException {
        if (item == null) {
            throw new ItemConsumedException();
        }

        this.withRightBindings();
        final CompoundTag bindingTag = createRightBindingTag(commands);

        bindingTag.put(NBTKeys.ACTIVATION, StringTag.valueOf(Activation.RightClick.ENTITY.getNbtName()));
        bindingTag.put(NBTKeys.AS_PLAYER, ByteTag.valueOf(asPlayer));
        this.saveRightBindingTag(bindingTag);

        return this;
    }

    public EpicEnchanter withRightAllBinding(float radius, boolean asPlayer, List<String> commands) throws ItemConsumedException {
        if (item == null) {
            throw new ItemConsumedException();
        }

        this.withRightBindings();
        final CompoundTag bindingTag = createRightBindingTag(commands);

        bindingTag.put(NBTKeys.ACTIVATION, StringTag.valueOf(Activation.RightClick.ALL.getNbtName()));
        bindingTag.put(NBTKeys.RADIUS, FloatTag.valueOf(radius));
        bindingTag.put(NBTKeys.AS_PLAYER, ByteTag.valueOf(asPlayer));
        this.saveRightBindingTag(bindingTag);

        return this;
    }

    private CompoundTag createRightBindingTag(List<String> commands) {
        this.withRightBindings();

        final ListTag commandsTag = listToTag(commands);
        final CompoundTag bindingTag = new CompoundTag();

        bindingTag.put(NBTKeys.COMMANDS, commandsTag);

        return bindingTag;
    }

    private void saveRightBindingTag(final CompoundTag bindingTag) {
        final CompoundTag tag = item.getTag();
        final ListTag rightBindings = (ListTag) tag.get(NBTKeys.RIGHT_BINDINGS);

        rightBindings.add(bindingTag);
        tag.put(NBTKeys.RIGHT_BINDINGS, rightBindings);
        item.setTag(tag);
    }

    private ListTag listToTag(List<String> items) {
        final ListTag out = new ListTag();

        for (String item : items) {
            out.add(StringTag.valueOf(item));
        }

        return out;
    }
}
