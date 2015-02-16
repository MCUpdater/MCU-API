package org.mcupdater.mojang.nbt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagString extends Tag {
    private final String value;

    public TagString(String name, String value) {
        super(name);
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public List<Byte> toBytes(boolean doHeader) {
        List<Byte> bytes = new ArrayList<>();
        if (doHeader) { bytes.addAll(super.getHeader((byte) 0x08)); }
        bytes.add((byte)((value.getBytes().length >> 8) & 0xff));
        bytes.add((byte)(value.getBytes().length & 0xff));
        bytes.addAll(stringToList(value));
        return bytes;
    }

}
