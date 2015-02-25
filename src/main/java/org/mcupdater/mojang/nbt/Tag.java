package org.mcupdater.mojang.nbt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public abstract class Tag {
    private final String name;

    public Tag(String name) {
        this.name = name;
    }

    public final String getName() {
        return this.name;
    }

    public abstract Object getValue();

    public abstract List<Byte> toBytes(boolean doHeader);

    public List<Byte> getHeader(byte type) {
        List<Byte> bytes = new ArrayList<>();
        bytes.add(type);
        bytes.add((byte)((getName().getBytes().length >> 8) & 0xFF));
        bytes.add((byte)(getName().getBytes().length & 0xFF));
        bytes.addAll(stringToList(getName()));
        return bytes;
    }

    protected List<Byte> stringToList(String in) {
        List<Byte> bytes = new ArrayList<>();
        for (byte b : in.getBytes(StandardCharsets.UTF_8)) {
            bytes.add(b);
        }
        return bytes;
    }
}
