package org.mcupdater.mojang.nbt;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class NBTParser {

    public static RVPair parse(byte[] raw, NBTType baseType) throws Exception {
        int index = 0;
        while (index <= raw.length) {
            String name = "";
            NBTType type;
            if (baseType != null) {
                type = baseType;
            } else {
                try {
                    type = NBTType.fromByte(raw[index++]);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                name = getName(raw, index);
                index += 2 + name.getBytes(StandardCharsets.UTF_8).length;
            }
            if (type == NBTType.COMPOUND) {
                TagCompound compound = new TagCompound(name);
                byte[] payload = ArrayUtils.subarray(raw, index, raw.length);
                int subindex = 0;
                while (subindex <= payload.length) {
                    RVPair result = parse(ArrayUtils.subarray(payload, subindex, payload.length), null);
                    subindex += result.bytesUsed;
                    index += result.bytesUsed;
                    compound.add(result.tag);
                    if (raw[index] == NBTType.TAGEND.getValue()) { break; }
                }
                return new RVPair(compound, index);
            }
            if (type == NBTType.BYTE) {
                byte value = raw[index++];
                return new RVPair(new TagByte(name, value), index);
            }
            if (type == NBTType.BYTE_ARRAY) {
                int arraySize = getSize(ArrayUtils.subarray(raw, index++,index+3));
                index += 3;
                byte[] values = new byte[arraySize];
                for (int i=0; i < arraySize; i++) {
                    values[i] = raw[index++];
                }
                return new RVPair(new TagByteArray(name, values), index);
            }
            if (type == NBTType.DOUBLE) {
                ByteBuffer bb = ByteBuffer.allocate(8);
                bb.order(ByteOrder.BIG_ENDIAN);
                bb.put(ArrayUtils.subarray(raw,index,index+8));
                bb.rewind();
                index += 8;
                double value = bb.getDouble();
                return new RVPair(new TagDouble(name,value), index);
            }
            if (type == NBTType.FLOAT) {
                ByteBuffer bb = ByteBuffer.allocate(4);
                bb.order(ByteOrder.BIG_ENDIAN);
                bb.put(ArrayUtils.subarray(raw,index,index+4));
                bb.rewind();
                index += 4;
                float value = bb.getFloat();
                return new RVPair(new TagFloat(name,value), index);
            }
            if (type == NBTType.INT) {
                ByteBuffer bb = ByteBuffer.allocate(4);
                bb.order(ByteOrder.BIG_ENDIAN);
                bb.put(ArrayUtils.subarray(raw,index,index+4));
                bb.rewind();
                index += 4;
                int value = bb.getInt();
                return new RVPair(new TagInt(name,value), index);
            }
            if (type == NBTType.INT_ARRAY) {
                int arraySize = getSize(ArrayUtils.subarray(raw, index++,index+3));
                index += 3;
                Integer[] values = new Integer[arraySize];
                for (int i=0; i < arraySize; i++) {
                    ByteBuffer bb = ByteBuffer.allocate(4);
                    bb.order(ByteOrder.BIG_ENDIAN);
                    bb.put(ArrayUtils.subarray(raw,index,index+4));
                    bb.rewind();
                    index += 4;
                    values[i] = bb.getInt();
                }
                return new RVPair(new TagIntArray(name, values), index);
            }
            if (type == NBTType.LIST) {
                try {
                    NBTType listType = NBTType.fromByte(raw[index++]);
                    int arraySize = getSize(ArrayUtils.subarray(raw, index++,index+3));
                    index += 3;
                    TagList tagList = new TagList(name, listType);
                    for (int i=0; i < arraySize; i++) {
                        RVPair result = parse(ArrayUtils.subarray(raw, index, raw.length), listType);
                        index += result.bytesUsed;
                        tagList.add(result.tag);
                    }
                    return new RVPair(tagList, index);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            if (type == NBTType.LONG) {
                ByteBuffer bb = ByteBuffer.allocate(8);
                bb.order(ByteOrder.BIG_ENDIAN);
                bb.put(ArrayUtils.subarray(raw,index,index+8));
                bb.rewind();
                index += 8;
                long value = bb.getLong();
                return new RVPair(new TagLong(name,value), index);
            }
            if (type == NBTType.LONG_ARRAY) {
                int arraySize = getSize(ArrayUtils.subarray(raw, index++,index+3));
                index += 3;
                Long[] values = new Long[arraySize];
                for (int i=0; i < arraySize; i++) {
                    ByteBuffer bb = ByteBuffer.allocate(8);
                    bb.order(ByteOrder.BIG_ENDIAN);
                    bb.put(ArrayUtils.subarray(raw,index,index+8));
                    bb.rewind();
                    index += 8;
                    values[i] = bb.getLong();
                }
                return new RVPair(new TagLongArray(name, values), index);
            }
            if (type == NBTType.SHORT) {
                ByteBuffer bb = ByteBuffer.allocate(2);
                bb.order(ByteOrder.BIG_ENDIAN);
                bb.put(ArrayUtils.subarray(raw,index,index+2));
                bb.rewind();
                index += 2;
                short value = bb.getShort();
                return new RVPair(new TagShort(name,value), index);
            }
            if (type == NBTType.STRING) {
                String value = getName(raw, index);
                index += 2 + value.getBytes(StandardCharsets.UTF_8).length;
                return new RVPair(new TagString(name, value), index);
            }
            throw new Exception("Something has gone horribly wrong!  This code should be unreachable!");
        }
        // Failure state
        throw new Exception("Ran out of bytes while parsing!  This should not happen!");
    }

    private static int getSize(byte[] subList) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(subList);
        bb.rewind();
        return bb.getInt();
    }

    private static String getName(byte[] raw, int index) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.put(ArrayUtils.subarray(raw,index,index+2));
        bb.rewind();
        index += 2;
        short nameSize = bb.getShort();
        byte[] subset = ArrayUtils.subarray(raw, index, index + nameSize);
        return new String(subset);
    }

    public static class RVPair {
        public final Tag tag;
        public final int bytesUsed;

        public RVPair(Tag tag, int bytesUsed) {
            this.tag = tag;
            this.bytesUsed = bytesUsed;
        }
    }
}
