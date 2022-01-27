package correcter;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        String path = "Error Correcting Encoder-Decoder/task/src/correcter/";
        for (String arg : args) {
            if ("-local".equals(arg)) {
                path = "";
                break;
            }
        }
        final File inFile = new File(path + "send.txt");
        final File outFile = new File(path + "received.txt");

        BufferedInputStream input;
        BufferedOutputStream output;
        try (FileInputStream fileInputStream = new FileInputStream(inFile);
             FileOutputStream fileOutputstream = new FileOutputStream(outFile)) {
            input = new BufferedInputStream(fileInputStream);
            output = new BufferedOutputStream(fileOutputstream);
            byte[] bytes = input.readAllBytes();
            for (byte currentByte : bytes) {
                output.write(addNoise(currentByte));
            }
            output.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    static byte[] byteToBits(byte b) {
        final byte zero = 0;
        byte[] bits = new byte[8];
        Arrays.fill(bits, zero);
        int i = bits.length - 1;
        while (b != 0 && i >= 0) {
            if (b % 2 != 0) {
                bits[i] = 1;
            }
            i--;
            b >>>= 1;
        }
        return bits;
    }

    static byte bitsToByte(byte[] bits) {
        byte value = 0;
        final int lastIndex = bits.length - 1;
        for (int i = lastIndex; i >= 0; i--) {
            value += bits[i] * Math.pow(2, lastIndex - i);
        }
        return value;
    }

    static byte addNoise(byte byteToChange) {
        final Random rnd = new Random();
        int bitToChange = rnd.nextInt(8);
        byte[] bits = byteToBits(byteToChange);
        bits[bitToChange] = (byte) (bits[bitToChange] == 0 ? 1 : 0);
        return bitsToByte(bits);
    }

}


