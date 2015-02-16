package org.mcupdater.mojang.nbt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagLong extends Tag {
    private final long value;

    public TagLong(String name, long value) {
        super(name);
        this.value = value;
    }

    @Override
    public Long getValue() {
        return this.value;
    }

    @Override
    public List<Byte> toBytes(boolean doHeader) {
        List<Byte> bytes = new ArrayList<>();
        if (doHeader) { bytes.addAll(super.getHeader((byte) 0x04)); }
        bytes.add((byte)((value >> 56) & 0xff));
        bytes.add((byte)((value >> 48) & 0xff));
        bytes.add((byte)((value >> 40) & 0xff));
        bytes.add((byte)((value >> 32) & 0xff));
        bytes.add((byte)((value >> 24) & 0xff));
        bytes.add((byte)((value >> 16) & 0xff));
        bytes.add((byte)((value >> 8) & 0xff));
        bytes.add((byte)(value & 0xff));
        return bytes;
    }

}
