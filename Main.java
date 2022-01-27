package correcter;

import java.io.*;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        String path = "";
        for (String arg : args) {
            if ("-local".equals(arg)) {
                path = "Error Correcting Encoder-Decoder/task/src/correcter/";
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

    static byte addNoise(byte byteToChange) {
        final Random rnd = new Random();
        int bitToChange = rnd.nextInt(8);
        return (byte) (byteToChange ^ (1 << bitToChange));
    }

}


