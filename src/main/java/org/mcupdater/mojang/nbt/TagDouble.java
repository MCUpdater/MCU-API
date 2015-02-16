package org.mcupdater.mojang.nbt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagDouble extends Tag {
    private final double value;

    public TagDouble(String name, double value) {
        super(name);
        this.value = value;
    }

    @Override
    public Double getValue() {
        return this.value;
    }

    @Override
    public List<Byte> toBytes(boolean doHeader) {
        List<Byte> bytes = new ArrayList<>();
        if (doHeader) { bytes.addAll(super.getHeader((byte) 0x06)); }
        long bits = Double.doubleToLongBits(value);
        bytes.add((byte)((bits >> 56) & 0xff));
        bytes.add((byte)((bits >> 48) & 0xff));
        bytes.add((byte)((bits >> 40) & 0xff));
        bytes.add((byte)((bits >> 32) & 0xff));
        bytes.add((byte)((bits >> 24) & 0xff));
        bytes.add((byte)((bits >> 16) & 0xff));
        bytes.add((byte)((bits >> 8) & 0xff));
        bytes.add((byte)(bits & 0xff));
        return bytes;
    }

}
