package org.mcupdater.mojang.nbt;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagLongArray extends Tag {
    private final Long[] values;

    public TagLongArray(String name, Long[] values) {
        super(name);
        this.values = values;
    }

    @Override
    public Long[] getValue() {
        return this.values;
    }

    public byte[] toBytes(boolean doHeader) {
        byte[] header = new byte[0];
        if (doHeader) {
            header = super.getHeader(NBTType.LONG_ARRAY.getValue());
        }
        ByteBuffer bb = ByteBuffer.allocate(header.length + 4 + (values.length * 8));
        int size = values.length;
        bb.putInt(size);
        for (Long value : values) {
            bb.putLong(value);
        }
        bb.rewind();
        return bb.array();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        if (!this.getName().isEmpty()) {
            output.append(String.format("@name=%s ",this.getName()));
        }
        output.append(String.format("@size=%d LongArray: {\n",this.values.length));
        for (Long entry : values) {
            output.append(String.format("%d",entry)).append("\n");
        }
        output.append("}");
        return output.toString();
    }
}
