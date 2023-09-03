package com.dezzmeister.epicenchantments.bindings;

public class ItemConsumedException extends Exception {
    public ItemConsumedException() {
        super("The item has already been enchanted");
    }
}
