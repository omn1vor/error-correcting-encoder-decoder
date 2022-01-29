package correcter;

public abstract class ByteModel {

    static final int BYTE_LEN = 8;
    byte value;
    int cursor;

    public ByteModel() {
        this.cursor = 0;
        this.value = 0;
    }

    public byte getValue() {
        return value;
    }

    public boolean isNew() {
        return cursor == 0;
    }
}

class BitWriter extends ByteModel {

    private boolean encodingMode = false;
    private int parity = 0;
    private byte bitMask = 0;
    private int numberOfBits = 1;

    public BitWriter() {
        super();
        setBitMask();
    }

    public BitWriter(boolean encodingMode) {
        this();
        this.encodingMode = encodingMode;
        if (encodingMode) {
            this.numberOfBits = 2;
            setBitMask();
        }
    }

    private void setBitMask() {
        bitMask = 0;
        for (int i = 0; i < numberOfBits; i++) {
            bitMask += Math.pow(2, i);
        }
    }

    public void writeBit(int bitValue) {
        final int bitsLeft = BYTE_LEN - cursor - numberOfBits;
        if (cursor + numberOfBits > BYTE_LEN) {
            String msg = String.format("Error. Trying to write %d bits while only %d bits left", numberOfBits, bitsLeft);
            throw new IllegalArgumentException(msg);
        }

        if (bitValue != 0) {
            value |= (bitMask << bitsLeft);
        }
        cursor += numberOfBits;
        parity ^= bitValue;
    }

    public void writeParity() {
        writeBit(parity);
    }

    public boolean hasSpaceForData() {
        return cursor < (encodingMode ? 6 : 8);
    }

    public void flush() {
        if (isNew()) {
            return;
        }
        while (hasSpaceForData()) {
            writeBit(0);
        }
        if (encodingMode) {
            writeParity();
        }
    }

}

class BitReader extends ByteModel {

    public BitReader(byte value) {
        super();
        this.value = value;
    }

    public int readBit() {
        int bitValue = (value & (1 << BYTE_LEN - 1 - cursor)) == 0 ? 0 : 1;
        cursor++;
        return bitValue;
    }

    public boolean hasNext() {
        return cursor < BYTE_LEN;
    }
}
