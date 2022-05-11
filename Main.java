package flashcards;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    static Map<String, String> cards;
    static Map<String, Integer> errors;
    static String imFileName;
    static String exFileName;
    static StringBuilder log;

    static {
        cards = new HashMap<>();
        errors = new HashMap<>();
        log = new StringBuilder();
        imFileName = "";
        exFileName = "";
    }

    public static void main(String[] args) {

        String[] userArgs = new String[(int) Math.floor(args.length / 2.0)];
        int counter = 0;
        for (int i = 0; i < args.length; i += 2) {
            userArgs[counter] = args[i] + "::" + args[i + 1];
            counter++;
        }
        for (String uStr : userArgs) {
            if (uStr.contains("-import")) {
                imFileName = uStr.split("::")[1];
            }
            if (uStr.contains("-export")) {
                exFileName = uStr.split("::")[1];
            }
        }

        if (imFileName.compareTo("") != 0) {
            importCards();
        }

        boolean on = true;

        while (on) {

            Scanner scanner = new Scanner(System.in);
            sysOut("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            String action = scanner.nextLine();
            log.append(action).append("\n");

            switch (action) {
                case "add":
                    addCard();
                    break;
                case "remove":
                    removeCard();
                    break;
                case "import":
                    importCards();
                    break;
                case "export":
                    exportCards();
                    break;
                case "ask":
                    askCard();
                    break;
                case "log":
                    saveLog();
                    break;
                case "hardest card":
                    hardestCard();
                    break;
                case "reset stats":
                    errors.clear();
                    sysOut("Card statistics have been reset.");
                    break;
                case "exit":
                    sysOut("Bye bye!");
                    if (exFileName.compareTo("") != 0) {
                        exportCards();
                    }
                    on = false;
                    break;
                default:
                    break;
            }

           //printCurrentDb();

        }

    }


    public static void sysOut(String str) {
        log.append(str).append("\n");
        System.out.println(str);
    }

    private static void addCard() {
        sysOut("The card:");
        Scanner scanner0 = new Scanner(System.in);
        String q = scanner0.nextLine();
        log.append(q).append("\n");
        if (cards.containsValue(q)) {
            sysOut("The card \"" + q + "\" already exists.");
            return;
        }

        sysOut("The definition of the card:");
        Scanner scanner1 = new Scanner(System.in);
        String d = scanner1.nextLine();
        if (cards.containsKey(d)) {
            sysOut("The definition \"" + d + "\" already exists.");
            return;
        }

        cards.put(d, q);
        sysOut(String.format("The pair (\"%s\":\"%s\") has been added.\n", q, d));
    }

    private static void removeCard() {
        sysOut("Which card?");
        Scanner scanner0 = new Scanner(System.in);
        String q = scanner0.nextLine();
        log.append(q).append("\n");
        boolean deleteOk = false;
        for (var card : cards.entrySet()) {
            if (card.getValue().compareTo(q) == 0) {
                cards.remove(card.getKey(), card.getValue());
                deleteOk = true;
                break;
            }
        }

        if (deleteOk) {
            sysOut("The card has been removed.");
        } else {
            sysOut(String.format("Can't remove \"%s\": there is no such card.\n", q));
        }

    }

    private static void exportCards() {
        String fileName;
        if (exFileName.compareTo("") == 0) {
            sysOut("File name:");
            Scanner scanner0 = new Scanner(System.in);
            fileName = scanner0.nextLine();
            log.append(fileName).append("\n");
        } else {
            fileName = exFileName;
        }
        int count = 0;
        try (FileWriter writer = new FileWriter(fileName)) {
            for (var card : cards.entrySet()) {
                writer.write(card.getValue() + "||" + card.getKey() + "||" + errors.get(card.getKey()) + "\n");
                count++;
            }
        } catch (IOException e) {
            sysOut("Error: couldn't create a file.");
            return;
        }

        sysOut(String.format("%d cards have been saved.\n", count));
    }

    private static void saveLog() {
        sysOut("File name:");
        Scanner scanner0 = new Scanner(System.in);
        String fileName = scanner0.nextLine();
        log.append(fileName).append("\n");
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(log.toString());
        } catch (IOException e) {
            sysOut("Error: couldn't create a file.");
            return;
        }
        sysOut("The log has been saved.");
    }

    private static void hardestCard() {
        if (errors.isEmpty()) {
            sysOut("There are no cards with errors.");
            return;
        }
        int biggest = 0;
        StringBuilder bigCardNames = new StringBuilder();
        for (var error : errors.entrySet()) {
            if (error.getValue() > biggest) {
                biggest = error.getValue();
            }
        }
        int biggestCards = 0;
        for (var error : errors.entrySet()) {
            if (error.getValue() == biggest) {
                biggestCards++;
                bigCardNames.append(cards.get(error.getKey())).append("||");
            }
        }

        if (biggestCards == 1) {
            String n = bigCardNames.toString().split("\\|\\|")[0];
            sysOut(String.format("The hardest card is \"%s\". You have %d errors answering it.\n", n, biggest));
            return;
        }

        System.out.print("The hardest cards are ");
        for (int i = 0; i < biggestCards; i++) {
            String n = bigCardNames.toString().split("\\|\\|")[i];
            System.out.printf("\"%s\"", n);
            if (i != biggestCards - 1) {
                System.out.print(", ");
            }
        }
        sysOut(String.format(". You have %d errors answering them.\n", biggest));
    }

    private static void importCards() {
        String fileName;
        if (imFileName.compareTo("") == 0) {
            sysOut("File name:");
            Scanner scanner0 = new Scanner(System.in);
            fileName = scanner0.nextLine();
            log.append(fileName).append("\n");
        } else {
            fileName = imFileName;
        }
        int count = 0;
        try (FileReader reader = new FileReader(fileName)) {

            Scanner readScanner = new Scanner(reader);
            while (readScanner.hasNextLine()) {
                String cardLine = readScanner.nextLine();
                if (cardLine.contains("||")) {

                    String cKey = cardLine.split("\\|\\|")[1];
                    String cVal = cardLine.split("\\|\\|")[0];
                    String errVal = cardLine.split("\\|\\|")[2];
                    AtomicReference<String> oldKey = new AtomicReference<>("");

                    cards.forEach((key, value) -> {
                        if (value.equals(cVal)) {
                            oldKey.set(key);
                        }
                    });

                    if (cards.containsValue(cVal)) {
                        cards.remove(oldKey.toString(), cVal);
                    }

                    cards.put(cKey, cVal);
                    if (!errVal.contains("null")) {
                        errors.put(cKey, Integer.valueOf(errVal));
                    }
                    count++;

                }
            }

        } catch (IOException e) {
            sysOut("File not found.");
            return;
        }

        sysOut(String.format("%d cards have been loaded.\n", count));
    }


    private static void askCard() {
        Object[] rndKeys = cards.keySet().toArray();
        Scanner scan = new Scanner(System.in);
        sysOut("How many times to ask?");
        int askNum;
        try {
            askNum = Integer.parseInt(scan.nextLine());
            log.append(askNum).append("\n");
        } catch (Exception e) {
            askNum = 1;
        }

        for (int i = 0; i < askNum; i++) {

            try {
                Object keyC = rndKeys[new Random().nextInt(rndKeys.length)];
                sysOut("Print the definition of \"" + cards.get(keyC.toString()) + "\":");
                String answer = scan.nextLine();
                if (answer.compareTo(keyC.toString()) == 0) {
                    sysOut("Correct!");
                } else if (cards.containsKey(answer)) {
                    sysOut("Wrong. The right answer is \"" + keyC + "\", but your definition is correct for \"" + cards.get(answer) + "\".");
                    int errorAdd = (errors.get(keyC.toString()) == null) ? 1 : errors.get(keyC.toString()) + 1;
                    errors.put(keyC.toString(), errorAdd);
                } else {
                    sysOut("Wrong. The right answer is " + "\"" + keyC + "\".");
                    int errorAdd = (errors.get(keyC.toString()) == null) ? 1 : errors.get(keyC.toString()) + 1;
                    errors.put(keyC.toString(), errorAdd);
                }
            } catch (Exception e) {
                sysOut("There is no cards!");
            }

        }

    }

    private static void printCurrentDb() {
        System.out.println("----------\nDB:");
        for (var card : cards.entrySet()) {
            System.out.println(card.getValue() + " | " + card.getKey() + " | " + errors.get(card.getKey()));
        }
        System.out.println("----------");
    }

}
