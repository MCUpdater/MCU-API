package org.mcupdater.mojang.nbt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagFloat extends Tag {
    private final float value;

    public TagFloat(String name, float value) {
        super(name);
        this.value = value;
    }

    @Override
    public Float getValue() {
        return this.value;
    }

    @Override
    public List<Byte> toBytes(boolean doHeader) {
        List<Byte> bytes = new ArrayList<>();
        if (doHeader) { bytes.addAll(super.getHeader((byte) 0x05)); }
        int bits = Float.floatToIntBits(value);
        bytes.add((byte)((bits >> 24) & 0xff));
        bytes.add((byte)((bits >> 16) & 0xff));
        bytes.add((byte)((bits >> 8) & 0xff));
        bytes.add((byte)(bits & 0xff));
        return bytes;
    }

}
