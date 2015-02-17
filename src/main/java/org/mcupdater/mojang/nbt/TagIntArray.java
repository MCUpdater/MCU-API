package org.mcupdater.mojang.nbt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagIntArray extends Tag {
    private final Integer[] values;

    public TagIntArray(String name, Integer[] values) {
        super(name);
        this.values = values;
    }

    @Override
    public Integer[] getValue() {
        return this.values;
    }

    @Override
    public List<Byte> toBytes(boolean doHeader) {
        List<Byte> bytes = new ArrayList<>();
        if (doHeader) { bytes.addAll(super.getHeader((byte) 0x0b)); }
        int size = values.length;
        bytes.add((byte)((size >> 24) & 0xff));
        bytes.add((byte)((size >> 16) & 0xff));
        bytes.add((byte)((size >> 8) & 0xff));
        bytes.add((byte)(size & 0xff));
        for (Integer entry : values) {
            bytes.add((byte)((entry >> 24) & 0xff));
            bytes.add((byte)((entry >> 16) & 0xff));
            bytes.add((byte)((entry >> 8) & 0xff));
            bytes.add((byte)(entry & 0xff));
        }
        return bytes;
    }

}
