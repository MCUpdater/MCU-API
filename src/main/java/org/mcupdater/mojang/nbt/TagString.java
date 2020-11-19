package org.mcupdater.mojang.nbt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
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
    public byte[] toBytes(boolean doHeader) {
        byte[] header = new byte[0];
        if (doHeader) {
            header = super.getHeader(NBTType.STRING.getValue());
        }
        ByteBuffer bb = ByteBuffer.allocate(header.length + 2 + value.getBytes(StandardCharsets.UTF_8).length);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(header);
        bb.putShort((short) value.getBytes(StandardCharsets.UTF_8).length);
        bb.put(value.getBytes(StandardCharsets.UTF_8));
        bb.rewind();
        return bb.array();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        if (!this.getName().isEmpty()) {
            output.append(String.format("@name=%s ",this.getName()));
        }
        output.append(String.format("String: %s",this.value));
        return output.toString();
    }

}
