package correcter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Main {

    static final int BYTE_LEN = 8;

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
        byte parity = 0;
        ArrayList<Byte> encoded = new ArrayList<>();
        int outBitNum = 0;
        byte outByte = 0;
        for (byte b : bytes) {
            for (int i = 0; i < BYTE_LEN; i++) {
                int bitValue = (b & (1 << BYTE_LEN - 1 - i)) == 0 ? 0 : 1;
                if (outBitNum > 5) {
                    if (parity == 1) {
                        outByte |= (1 << BYTE_LEN - 1 - outBitNum++);
                        outByte |= (1 << BYTE_LEN - 1 - outBitNum);
                    } else {
                        outByte &= ~(1 << BYTE_LEN - 1 - outBitNum++);
                        outByte &= ~(1 << BYTE_LEN - 1 - outBitNum);
                    }
                    encoded.add(outByte);
                    outByte = 0;
                    outBitNum = 0;
                    parity = 0;
                }
                if (bitValue != 0) {
                    outByte |= (1 << BYTE_LEN - 1 - outBitNum++);
                    outByte |= (1 << BYTE_LEN - 1 - outBitNum++);
                } else {
                    outByte &= ~(1 << BYTE_LEN - 1 - outBitNum++);
                    outByte &= ~(1 << BYTE_LEN - 1 - outBitNum++);
                }
                parity ^= bitValue;
            }
        }
        byte[] arr = new byte[encoded.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = encoded.get(i);
        }
        return arr;
    }

    static byte addNoise(byte byteToChange) {
        final Random rnd = new Random();
        int bitToChange = rnd.nextInt(BYTE_LEN);
        return (byte) (byteToChange ^ (1 << bitToChange));
    }

}


