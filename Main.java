package correcter;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Sender sender = new Sender(scanner.nextLine());
        sender.printMessage();
        sender.encode();
        sender.printMessage();
        sender.addNoise();
        sender.printMessage();
        sender.decode();
        sender.printMessage();
    }

}

class Dictionary {

    private final int len = 26*2 + 10 + 1; // a-z, A-Z, 0-9, space
    private final char[] dict = new char[len];
    private final Random rnd = new Random();

    Dictionary() {
        int i = 0;

        for (char c = 'a'; c <= 'z'; c++) {
            dict[i] = c;
            i++;
        }

        for (char c = 'A'; c <= 'Z'; c++) {
            dict[i] = c;
            i++;
        }

        for (char c = '0'; c <= '9'; c++) {
            dict[i] = c;
            i++;
        }

        dict[i] = ' ';
    }

    char getRandom() {
        return dict[rnd.nextInt(len)];
    }

    char getRandomInstead(char c) {
        char random = getRandom();
        while (random == c) {
            random = getRandom();
        }
        return random;
    }
}

class Sender {

    final int errorFrequency = 3;
    private String message;

    Sender(String message) {
        this.message = message;
    }

    void printMessage() {
        System.out.println(message);
    }

    void encode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            sb.append(String.valueOf(message.charAt(i)).repeat(errorFrequency));
        }
        this.message = sb.toString();
    }

    void addNoise() {
        StringBuilder sb = new StringBuilder(message);
        Dictionary dict = new Dictionary();
        Random rnd = new Random();

        for (int i = 0; i < sb.length(); i += errorFrequency) {
            int randomIndex = i + rnd.nextInt(errorFrequency);
            char current = sb.charAt(randomIndex);
            sb.setCharAt(randomIndex, dict.getRandomInstead(current));
        }
        this.message = sb.toString();
    }

    void decode() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < message.length(); i += errorFrequency) {
            char c = getMostFrequentChar(Arrays.copyOfRange(message.toCharArray(), i, i + errorFrequency));
            sb.append(c);
        }
        message = sb.toString();
    }

    char getMostFrequentChar(char[] arr) {
        char mostFrequent = '\0';
        int maxCount = 0;

        for (char currentChar : arr) {
            int count = 0;
            for (char c : arr) {
                if (currentChar == c) {
                    count++;
                }
            }
            if (count > maxCount) {
                maxCount = count;
                mostFrequent = currentChar;
            }
        }
        return mostFrequent;
    }
}
