package correcter;

import java.io.*;
import java.util.Random;

public class Correcter {

    private final Mode method;

    public Correcter(String mode, String pathPrefix) {
        switch (mode.toLowerCase()) {
            case "encode":
                this.method = new Encode();
                break;
            case "send":
                this.method = new Send();
                break;
            case "decode":
                this.method = new Decode();
                break;
            default:
                throw new IllegalArgumentException("Error. Wrong action (expecting encode/send/decode).");
        }
        this.method.setPathPrefix(pathPrefix);
    }

    public void process() {
        method.readBytes();
        method.process();
        method.writeBytes();
    }

}

abstract class Mode {

    static final int MAX_BIT = 7;
    static final int[] parityBits = new int[] {0, 1, 3};
    static final int[] valueBits = new int[] {2, 4, 5, 6};
    String inputFile;
    String outputFile;
    String pathPrefix = "";
    byte[] inputBytes;
    byte[] outputBytes;

    abstract void process();

    void readBytes() {
        BufferedInputStream input;
        try (FileInputStream fileInputStream = new FileInputStream(pathPrefix + inputFile)) {
            input = new BufferedInputStream(fileInputStream);
            this.inputBytes = input.readAllBytes();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    void writeBytes() {
        BufferedOutputStream output;
        try (FileOutputStream fileOutputstream = new FileOutputStream(pathPrefix + outputFile)) {
            output = new BufferedOutputStream(fileOutputstream);
            output.write(this.outputBytes);
            output.flush();
        } catch (IOException | NullPointerException e) {
            System.out.println(e.getMessage());
        }
    }

    int getBitValue(byte b, int bitPosition) {
        return (b & 1 << MAX_BIT - bitPosition) == 0 ? 0 : 1;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }
}

class Encode extends Mode {

    Encode() {
        super();
        this.inputFile = "send.txt";
        this.outputFile = "encoded.txt";
    }

    @Override
    void process() {
        outputBytes = new byte[inputBytes.length * 2];
        int outputByteIndex = 0;
        for (byte b : inputBytes) {
            outputBytes[outputByteIndex++] = encodeHalfByte(b, 0);
            outputBytes[outputByteIndex++] = encodeHalfByte(b, 4);
        }
    }

    byte encodeHalfByte(byte b, int start) {
        byte encoded = 0;
        for (int i : valueBits) {
            int bitValue = getBitValue(b, start++);
            encoded |= bitValue << MAX_BIT - i;
        }
        encoded = writeParity(encoded);
        return encoded;
    }

    byte writeParity(byte b) {
        for (int parityBitIndex : parityBits) {
            int sum = 0;
            int step = parityBitIndex + 1;
            for (int start = parityBitIndex; start < MAX_BIT; start += step * 2) {
                for (int i = start; i < start + step; i++) {
                    sum += getBitValue(b, i);
                }
            }
            b |= sum % 2 << MAX_BIT - parityBitIndex;
        }
        return b;
    }
}

class Send extends Mode {

    Send() {
        super();
        this.inputFile = "encoded.txt";
        this.outputFile = "received.txt";
    }

    @Override
    void process() {
        outputBytes = inputBytes.clone();
        Random rnd = new Random();
        for (int i = 0; i < inputBytes.length; i++) {
            outputBytes[i] = (byte) (inputBytes[i] ^ (1 << rnd.nextInt(8)));
        }
    }
}

class Decode extends Mode {

    Decode() {
        super();
        this.inputFile = "received.txt";
        this.outputFile = "decoded.txt";
    }

    @Override
    void process() {
        outputBytes = new byte[inputBytes.length / 2];
        int outputIndex = 0;
        int counter = 0;
        int start = 0;
        byte decoded = 0;
        for (byte b: inputBytes) {
            byte fixed = fixByteErrors(b);
            for (int i : valueBits) {
                int bitValue = getBitValue(fixed, i);
                decoded |= bitValue << MAX_BIT - start++;
            }
            if (++counter % 2 == 0) {
                outputBytes[outputIndex++] = decoded;
                decoded = 0;
                start = 0;
            }
        }
    }

    private byte fixByteErrors(byte b) {
        int parityCounter = 0;
        int paritySum = 0;
        for (int parityBitIndex : parityBits) {
            int sum = 0;
            int step = parityBitIndex + 1;
            for (int start = parityBitIndex; start < MAX_BIT; start += step * 2) {
                for (int i = start; i < start + step; i++) {
                    if (i != parityBitIndex) {
                        sum += getBitValue(b, i);
                    }
                }
            }
            if (getBitValue(b, parityBitIndex) != sum % 2) {
                parityCounter++;
                paritySum += step;
            }
        }
        if (parityCounter > 0) {
            b ^= 1 << MAX_BIT - (paritySum - 1);
        }
        return b;
    }

}
