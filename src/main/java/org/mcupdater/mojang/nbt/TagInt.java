package org.mcupdater.mojang.nbt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    public byte[] toBytes(boolean doHeader) {
        byte[] header = new byte[0];
        if (doHeader) {
            header = super.getHeader(NBTType.INT.getValue());
        }
        ByteBuffer bb = ByteBuffer.allocate(header.length + 4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(header);
        bb.putInt(value);
        bb.rewind();
        return bb.array();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        if (!this.getName().isEmpty()) {
            output.append(String.format("@name=%s ",this.getName()));
        }
        output.append(String.format("Int: %d",this.value));
        return output.toString();
    }

}
