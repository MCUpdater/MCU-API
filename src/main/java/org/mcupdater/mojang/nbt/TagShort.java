package org.mcupdater.mojang.nbt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    public byte[] toBytes(boolean doHeader) {
        byte[] header = new byte[0];
        if (doHeader) {
            header = super.getHeader(NBTType.SHORT.getValue());
        }
        ByteBuffer bb = ByteBuffer.allocate(header.length + 2);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(header);
        bb.putShort(value);
        bb.rewind();
        return bb.array();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        if (!this.getName().isEmpty()) {
            output.append(String.format("@name=%s ",this.getName()));
        }
        output.append(String.format("Short: %d",this.value));
        return output.toString();
    }

}
