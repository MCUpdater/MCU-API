package org.mcupdater.mojang.nbt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbarbour on 2/15/15.
 */
public class TagList extends Tag {
    private final List<Tag> values;
    private final NBTType type;

    public TagList(String name, NBTType type) {
        super(name);
        this.type = type;
        this.values = new ArrayList<>();
    }

    @Override
    public List<Tag> getValue() {
        return this.values;
    }

    @Override
    public byte[] toBytes(boolean doHeader) {
        byte[] header = new byte[0];
        if (doHeader) {
            header = super.getHeader(NBTType.LIST.getValue());
        }
        List<byte[]> children = new ArrayList<>();
        int totalBytes = header.length + 5;
        for (Tag entry : values) {
            byte[] child = entry.toBytes(false);
            totalBytes += child.length;
            children.add(child);
        }
        ByteBuffer bb = ByteBuffer.allocate(totalBytes);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(header);
        bb.put(type.getValue());
        bb.putInt(values.size());
        for (byte[] child : children) {
            bb.put(child);
        }
        bb.rewind();
        return bb.array();
    }

    @Override
    public void add(Tag entry) {
        values.add(entry);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        if (!this.getName().isEmpty()) {
            output.append(String.format("@name=%s ",this.getName()));
        }
        output.append(String.format("@type=%s @size=%d List: {",this.type.name(),this.values.size())).append("\n");
        for (Tag child : values) {
            output.append(child.toString()).append("\n");
        }
        output.append("}");
        return output.toString();
    }
}
