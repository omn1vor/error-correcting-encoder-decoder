package correcter;

import java.io.*;
import java.util.ArrayList;
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
        ArrayList<Byte> encoded = new ArrayList<>();
        BitWriter bitWriter = new BitWriter(true);
        for (byte b : inputBytes) {
            BitReader bitReader = new BitReader(b);
            while (bitReader.hasNext()) {
                int bitValue = bitReader.readBit();
                if (!bitWriter.hasSpaceForData()) {
                    bitWriter.writeParity();
                    encoded.add(bitWriter.getValue());
                    bitWriter = new BitWriter(true);
                }
                bitWriter.writeBit(bitValue);
            }
        }
        if (!bitWriter.isNew()) {
            bitWriter.flush();
            encoded.add(bitWriter.getValue());
        }
        byte[] arr = new byte[encoded.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = encoded.get(i);
        }
        this.outputBytes = arr;
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
        byte[] corrupted = inputBytes.clone();
        Random rnd = new Random();
        for (int i = 0; i < inputBytes.length; i++) {
            corrupted[i] = (byte) (inputBytes[i] ^ (1 << rnd.nextInt(8)));
        }
        this.outputBytes = corrupted;
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
        ArrayList<Byte> decoded = new ArrayList<>();
        BitWriter bitWriter = new BitWriter();
        for (byte b : inputBytes) {
            int brokenPair = 0;
            int[] bits = new int[4]; // first 3 - values, 4th - parity
            BitReader bitReader = new BitReader(b);
            for (int i = 0; i < bits.length; i++) {
                int firstBit = bitReader.readBit();
                int secondBit = bitReader.readBit();
                if (firstBit != secondBit) {
                    brokenPair = i;
                } else {
                    bits[i] = firstBit;
                }
            }
            if (brokenPair != 3) {
                int parity = bits[3];
                int sum = 0;
                for (int i = 0; i < 3; i++) {
                    sum += bits[i];
                }
                if (sum % 2 != parity) {
                    bits[brokenPair] = 1;
                }
            }
            for (int i = 0; i < 3; i++) {
                if (!bitWriter.hasSpaceForData()) {
                    decoded.add(bitWriter.getValue());
                    bitWriter = new BitWriter();
                }
                bitWriter.writeBit(bits[i]);
            }
        }
        if (!bitWriter.isNew() && !bitWriter.hasSpaceForData()) {
            bitWriter.flush();
            decoded.add(bitWriter.getValue());
        }
        byte[] arr = new byte[decoded.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = decoded.get(i);
        }
        this.outputBytes = arr;
    }


}
