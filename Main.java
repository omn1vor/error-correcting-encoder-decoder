package correcter;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String path = "./";
        for (String arg : args) {
            if ("-local".equals(arg)) {
                path = "Error Correcting Encoder-Decoder/task/src/correcter/";
                break;
            }
        }

        Scanner scanner = new Scanner(System.in);
        Correcter correcter = new Correcter(scanner.nextLine(), path);
        correcter.process();
    }


}





