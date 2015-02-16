package org.mcupdater.mojang.nbt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagCompound extends Tag {
    private final List<Tag> values;

    public TagCompound(String name) {
        super(name);
        this.values = new ArrayList<>();
    }

    @Override
    public List<Tag> getValue() {
        return this.values;
    }

    @Override
    public List<Byte> toBytes(boolean doHeader) {
        List<Byte> bytes = new ArrayList<>();
        if (doHeader) { bytes.addAll(super.getHeader((byte) 0x0a)); }
        for (Tag entry : values) {
            bytes.addAll(entry.toBytes(true));
        }
        bytes.add((byte) 0x00);
        return bytes;
    }

    public void add(Tag entry) {
        values.add(entry);
    }
}
