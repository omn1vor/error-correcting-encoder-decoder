package correcter;

import java.io.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        String path = "";
        for (String arg : args) {
            if ("-local".equals(arg)) {
                path = "Error Correcting Encoder-Decoder/task/src/correcter/";
                break;
            }
        }
        final File sendFile = new File(path + "send.txt");
        final File encodedFile = new File(path + "encoded.txt");

        BufferedInputStream input;
        BufferedOutputStream output;
        try (FileInputStream fileInputStream = new FileInputStream(sendFile);
             FileOutputStream fileOutputstream = new FileOutputStream(encodedFile)) {
            input = new BufferedInputStream(fileInputStream);
            output = new BufferedOutputStream(fileOutputstream);
            byte[] bytes = input.readAllBytes();
            byte[] encoded = encode(bytes);
            output.write(encoded);
            output.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    static byte[] encode(byte[] bytes) {
        ArrayList<Byte> encoded = new ArrayList<>();
        BitWriter bitWriter = new BitWriter();
        for (byte b : bytes) {
            BitReader bitReader = new BitReader(b);
            while (bitReader.hasNext()) {
                int bitValue = bitReader.readBit();
                if (!bitWriter.hasSpaceForData()) {
                    bitWriter.writeParity();
                    encoded.add(bitWriter.getValue());
                    bitWriter = new BitWriter();
                }
                bitWriter.writeBit(bitValue);
            }
        }
        if (!bitWriter.isNew() && bitWriter.hasSpaceForData()) {
            bitWriter.flush();
            encoded.add(bitWriter.getValue());
        }
        byte[] arr = new byte[encoded.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = encoded.get(i);
        }
        return arr;
    }
}

abstract class ByteModel {

    static final int BYTE_LEN = 8;
    static final int BIT_CHUNK = 2;
    byte value;
    int cursor;

    public ByteModel() {
        this.cursor = 0;
        this.value = 0;
    }

    public byte getValue() {
        return value;
    }

    public int getCursor() {
        return cursor;
    }

    public boolean isNew() {
        return cursor == 0;
    }
}

class BitWriter extends ByteModel {

    private int parity = 0;
    private byte bitMask = 0;

    public BitWriter() {
        super();

        for (int i = 0; i < BIT_CHUNK; i++) {
            bitMask += Math.pow(2, i);
        }
    }

    public void writeBit(int bitValue) {
        final int bitsLeft = BYTE_LEN - cursor - BIT_CHUNK;
        if (cursor + BIT_CHUNK > BYTE_LEN) {
            String msg = String.format("Error. Trying to write %d bits while only %d bits left", BIT_CHUNK, bitsLeft);
            throw new IllegalArgumentException(msg);
        }

        if (bitValue != 0) {
            value |= (bitMask << bitsLeft);
        }
        cursor += BIT_CHUNK;
        parity ^= bitValue;
    }

    public void writeParity() {
        writeBit(parity);
    }

    public boolean hasSpaceForData() {
        return cursor < 6;
    }

    public void flush() {
        if (isNew()) {
            return;
        }
        while (hasSpaceForData()) {
            writeBit(0);
        }
        writeParity();
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




