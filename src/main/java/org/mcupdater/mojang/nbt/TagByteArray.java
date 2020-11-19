package org.mcupdater.mojang.nbt;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagByteArray extends Tag {
    private final byte[] value;

    public TagByteArray(String name, byte[] value) {
        super(name);
        this.value = value;
    }

    @Override
    public byte[] getValue() {
        return this.value;
    }

    @Override
    public byte[] toBytes(boolean doHeader) {
        byte[] header = new byte[0];
        if (doHeader) {
            header = super.getHeader(NBTType.BYTE_ARRAY.getValue());
        }
        ByteBuffer bb = ByteBuffer.allocate(header.length + 4 + value.length);
        int size = value.length;
        bb.putInt(size);
        bb.put(value);
        bb.rewind();
        return bb.array();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        if (!this.getName().isEmpty()) {
            output.append(String.format("@name=%s ",this.getName()));
        }
        output.append(String.format("@size=%d ByteArray: {\n",this.value.length));
        for (Byte entry : value) {
            output.append(String.format("%x",entry)).append("\n");
        }
        output.append("}");
        return output.toString();
    }

}
