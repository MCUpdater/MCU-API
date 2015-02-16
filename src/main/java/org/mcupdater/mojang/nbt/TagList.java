package org.mcupdater.mojang.nbt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagList extends Tag {
    private final List<Tag> values;
    private final Type type;

    public TagList(String name, Type type) {
        super(name);
        this.type = type;
        this.values = new ArrayList<>();
    }

    @Override
    public List<Tag> getValue() {
        return this.values;
    }

    @Override
    public List<Byte> toBytes(boolean doHeader) {
        List<Byte> bytes = new ArrayList<>();
        if (doHeader) { bytes.addAll(super.getHeader((byte) 0x09)); }
        bytes.add(type.getValue());
        bytes.add((byte)((values.size() >> 24) & 0xFF));
        bytes.add((byte)((values.size() >> 16) & 0xFF));
        bytes.add((byte)((values.size() >> 8) & 0xFF));
        bytes.add((byte)(values.size() & 0xFF));
        for (Tag entry : values) {
            bytes.addAll(entry.toBytes(false));
        }
        return bytes;
    }

    public void add(Tag entry) {
        values.add(entry);
    }

    public enum Type {
        Byte ((byte) 0x01),
        Short ((byte) 0x02),
        Integer ((byte) 0x03),
        Long ((byte) 0x04),
        Float ((byte) 0x05),
        Double ((byte) 0x06),
        Byte_Array ((byte) 0x07),
        String ((byte) 0x08),
        List ((byte) 0x09),
        Compound ((byte) 0x0a);

        private final byte value;
        Type(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }
    }
}
