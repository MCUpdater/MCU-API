package org.mcupdater.mojang.nbt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagShort extends Tag {
    private final short value;

    public TagShort(String name, short value) {
        super(name);
        this.value = value;
    }

    @Override
    public Short getValue() {
        return this.value;
    }

    @Override
    public List<Byte> toBytes(boolean doHeader) {
        List<Byte> bytes = new ArrayList<>();
        if (doHeader) { bytes.addAll(super.getHeader((byte) 0x02)); }
        bytes.add((byte)((value >> 8) & 0xff));
        bytes.add((byte)(value & 0xff));
        return bytes;
    }

}
