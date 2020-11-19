package org.mcupdater.mojang.nbt;

public enum NBTType {
    TAGEND((byte) 0x00),
    BYTE((byte) 0x01),
    SHORT((byte) 0x02),
    INT((byte) 0x03),
    LONG((byte) 0x04),
    FLOAT((byte) 0x05),
    DOUBLE((byte) 0x06),
    BYTE_ARRAY((byte) 0x07),
    STRING((byte) 0x08),
    LIST((byte) 0x09),
    COMPOUND((byte) 0x0a),
    INT_ARRAY((byte) 0x0b),
    LONG_ARRAY((byte) 0x0c);

    private final byte value;
    NBTType(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static NBTType fromByte(byte value) throws Exception {
        for (NBTType type : NBTType.values()){
            if (type.value == value) {
                return type;
            }
        }
        throw new Exception(String.format("%x not a recognized NBT type classifier", value));
    }
}
