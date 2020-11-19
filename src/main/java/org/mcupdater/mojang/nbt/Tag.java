package org.mcupdater.mojang.nbt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

    public abstract byte[] toBytes(boolean doHeader);

    public byte[] getHeader(byte type) {
        ByteBuffer bb = ByteBuffer.allocate(3 + getName().getBytes().length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(type);
        bb.putShort((short) getName().getBytes().length);
        bb.put(getName().getBytes(StandardCharsets.UTF_8));
        bb.rewind();
        return bb.array();
    }

    public void add(Tag child) throws Exception {
        throw new Exception("TagType does not support children");
    }
}
