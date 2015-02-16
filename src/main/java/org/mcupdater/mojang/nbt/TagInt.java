package org.mcupdater.mojang.nbt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagInt extends Tag {
    private final int value;

    public TagInt(String name, int value) {
        super(name);
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public List<Byte> toBytes(boolean doHeader) {
        List<Byte> bytes = new ArrayList<>();
        if (doHeader) { bytes.addAll(super.getHeader((byte) 0x03)); }
        bytes.add((byte)((value >> 24) & 0xff));
        bytes.add((byte)((value >> 16) & 0xff));
        bytes.add((byte)((value >> 8) & 0xff));
        bytes.add((byte)(value & 0xff));
        return bytes;
    }

}
