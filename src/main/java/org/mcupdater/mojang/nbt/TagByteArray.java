package org.mcupdater.mojang.nbt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagByteArray extends Tag {
    private final Byte[] value;

    public TagByteArray(String name, Byte[] value) {
        super(name);
        this.value = value;
    }

    @Override
    public Byte[] getValue() {
        return this.value;
    }

    @Override
    public List<Byte> toBytes(boolean doHeader) {
        List<Byte> bytes = new ArrayList<>();
        if (doHeader) { bytes.addAll(super.getHeader((byte) 0x07)); }
        int size = value.length;
        bytes.add((byte)((size >> 24) & 0xff));
        bytes.add((byte)((size >> 16) & 0xff));
        bytes.add((byte)((size >> 8) & 0xff));
        bytes.add((byte)(size & 0xff));
        bytes.addAll(Arrays.asList(value));
        return bytes;
    }

}
