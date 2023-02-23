package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class VoiceAssitant {
    public static void main(String[] args) {
        mainMenu();
    }

    public static void mainMenu() { // The main menu enters the interface
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("--------------------------main menu----------------------------");
            System.out.println("Select one option from below: press number");
            System.out.println("1. Create a prototype quetion.");
            System.out.println("2. Create actions for a prototype question.");
            System.out.println("3. Start using VoiceCommand.");
            System.out.println("---------------------------------------------------------------");
            String num = scanner.nextLine().trim();

            try {
                int selection = Integer.parseInt(num);
                switch (selection) {
                    case 1:
                        createProtoSen(scanner);
                        break;
                    case 2:
                        createActions(scanner);
                        break;
                    case 3:
                        voiceCommand(scanner);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                System.out.println(e);
                continue;
            }
        }
    }

    // The third menu item, accept the command to reply number3
    public static void voiceCommand(Scanner scanner) {
        while (true) {
            System.out.println("Please enter command: ");
            String command = scanner.nextLine().trim();
            if (command.equalsIgnoreCase("quit"))
                break;
            getAnswer(command);
            System.out.println("Enter QUIT to quit");
        }
    }

    // The answer is printed in txt
    public static void getAnswer(String command) {
        List<String> lines = readFile("src\\sample.txt");
        List<List<String>> trunks = separateQuestions(lines);
        List<String> questions = new ArrayList<String>();

        for (List<String> t : trunks) {
            String q = t.get(0);
            q = q.substring(14);
            questions.add(q);
        }
        int maxScore = 0;
        String question = "";
        int questionIndex = 0;
        for (int i = 0; i < questions.size(); i++) {
            int score = compareSen(questions.get(i), command);

            if (score >= maxScore) {
                maxScore = score;
                question = questions.get(i);
                questionIndex = i;
            }
        }

        String[] words = question.split(" ");
        String[] commandWords = command.split(" ");
        List<Integer> indexes = getPlaceHolderIndexes(words);
        HashMap<String, HashMap<String, String>> findAnswer = getPossibleAnswers(trunks.get(questionIndex));
        String answer = searchAnswer(findAnswer, words, commandWords, indexes);
        System.out.println("---------------------------------------------------------------");
        System.out.println("Response: " + answer);
        System.out.println("---------------------------------------------------------------");
    }

    // Several problems in txt are several trunks，All actions in the problem are returned
    public static HashMap<String, HashMap<String, String>> getPossibleAnswers(List<String> trunk) {
        List<String> filteredT = new ArrayList<String>();
        for (String ea : trunk) { // ea is every line of trunk，each line
            if (ea.indexOf("Action") >= 0) {
                filteredT.add(ea);
            }
        }
        HashMap<String, HashMap<String, String>> findAnswer = new HashMap<String, HashMap<String, String>>();
        for (String line : filteredT) {
            String answer = "";
            HashMap<String, String> valueMap = new HashMap<String, String>();
            String[] splits = line.split(" "); // <subject> Split into words
            for (int i = 0; i < splits.length; i++) {
                if (splits[i].indexOf("Action") < 0) {
                    if (splits[i - 1].indexOf(">") >= 0) {
                        answer = "";
                        valueMap.put(splits[i - 1], splits[i]);
                    } else {
                        answer += splits[i] + " ";
                    }
                }
            }
            answer = answer.trim();
            findAnswer.put(answer, valueMap);
        }
        return findAnswer;
    }

    // What comes in is all possible answers, and an answer is found based on all actions
    public static String searchAnswer(HashMap<String, HashMap<String, String>> findAnswer,
            String[] words, String[] commandWords, List<Integer> indexes) {

        HashMap<String, String> commandInput = new HashMap<String, String>();
        for (int index : indexes) {
            commandInput.put(words[index], commandWords[index]);
        }
        int maxScore = 0;
        String finalAnswer = "";
        String noMatchAnswer = "";
        Iterator<Map.Entry<String, HashMap<String, String>>> iterator = findAnswer.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, HashMap<String, String>> entry = iterator.next();
            String answer = entry.getKey();
            int score = 0;
            HashMap<String, String> valueMap = entry.getValue();
            for (Map.Entry<String, String> entry2 : valueMap.entrySet()) {
                String key = entry2.getKey();
                String inputValue = commandInput.get(key);
                String testValue = entry2.getValue();
                if (inputValue.equals(testValue)) { // The value corresponding to the placeholder in the command (that is, CNN, DSA) is the input value
                    score += 1;
                }
            }
            if (score > maxScore) { // Score the highest and its action is the most correct
                finalAnswer = answer;
                maxScore = score;
            }
            if (valueMap.size() == 0) {
                noMatchAnswer = answer;
            }
        }
        if (maxScore == 0) {
            finalAnswer = noMatchAnswer;
        }
        return finalAnswer;
    }

    // That sentence, broken into an array of words
    public static List<Integer> getPlaceHolderIndexes(String[] words) {

        List<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < words.length; i++) {
            if (words[i].indexOf("<") >= 0 && words[i].indexOf(">") >= 0) {
                indexes.add(i);
            }
        }
        return indexes;
    }


    public static int compareSen(String a, String b) {
        // Compare the similarity of two sentences
        String[] word1 = a.split(" ");
        String[] word2 = b.split(" ");
        HashSet<String> w1 = new HashSet<>();
        HashSet<String> w2 = new HashSet<>();
        for (String e : word1) {
            w1.add(e);
        }
        for (String e : word2) {
            w2.add(e);
        }
        w1.retainAll(w2);
        return w1.size();
    }

    // Number 2 of the main menu, which creates the corresponding action under a question
    public static void createActions(Scanner scanner) {

        List<String> lines = readFile("src\\sample.txt");
        List<List<String>> trunks = separateQuestions(lines); // Lines are each line inside the txt
        List<List<String>> newTrunks = new ArrayList<List<String>>();

        while (true) {
            System.out.println("");
            System.out.println("---------------------------------------------------------------");
            System.out.println("Enter question number to create actions");
            int counter = 1;
            for (List<String> t : trunks) {
                System.out.println(counter++ + "." + t.get(0));
            }
            System.out.println(counter + ".   Exit");
            System.out.println("---------------------------------------------------------------");
            String num = scanner.nextLine().trim(); // Read user input
            int trunkIndex = Integer.parseInt(num) - 1;

            try {
                // The printed sequence number is 1, but the index is 0
                if (trunkIndex == counter - 1) {
                    break;
                }

                List<String> resTrunk = trunks.get(trunkIndex);
                String actionLine = createActionsFromQ(resTrunk, getPlaceHolders(resTrunk), scanner);

                List<String> trunkMod = trunks.get(trunkIndex);
                trunkMod.add(actionLine);
                newTrunks = new ArrayList<List<String>>(trunks);
                newTrunks.set(trunkIndex, trunkMod);

                // Overwrite documents
                lines = new ArrayList<String>();
                for (List<String> t : newTrunks) {
                    for (String line : t) {
                        lines.add(line);
                    }
                }
                writeFile(lines, "src\\sample.txt");
                trunks = newTrunks;
            } catch (Exception e) {
                System.out.println(e.toString());
                break;
            }
        }
    }

    // Pass in all rows of txt
    public static List<List<String>> separateQuestions(List<String> lines) {
        List<String> trunk = new ArrayList<String>();
        List<List<String>> trunks = new ArrayList<List<String>>();
        // divide proto sentences in trunks
        int lineNum = 1;
        for (String line : lines) {
            if (line.indexOf("Question") < 0) {
                trunk.add(line);
            } else {
                if (!trunk.isEmpty())
                    trunks.add(trunk);
                trunk = new ArrayList<String>();
                trunk.add(line);
            }
            // read the last trunk
            if (lineNum == lines.size())
                trunks.add(trunk);
            lineNum++;
        }
        return trunks;
    }

    // A trunk selected by the user
    public static String createActionsFromQ(List<String> trunk, List<String> placeHolders, Scanner scanner) {
        List<String> actionValues = new ArrayList<String>();
        System.out.println("==============================================================");
        for (String t : trunk) {
            System.out.println(t);
        }
        System.out.println("==============================================================");

        for (String p : placeHolders) {
            System.out.println("Set value for <" + p + "> " + "or Enter SKIP");
            String value = scanner.nextLine().trim();
            if (value.equalsIgnoreCase("skip")) {
                value = "";
            }
            actionValues.add(value);
        }

        System.out.println("Set response: ");
        String res = scanner.nextLine().trim();

        String lineFinal = "Action";
        for (int i = 0; i < placeHolders.size(); i++) {
            if (actionValues.get(i) != "") {
                lineFinal += " <" + placeHolders.get(i) + "> " + actionValues.get(i);
            }
        }
        lineFinal += " " + res;
        return lineFinal; 
    }



    public static List<String> getPlaceHolders(List<String> trunk) {
        List<String> placeHolders = new ArrayList<String>();
        String pre = "";
        for (String ea : trunk) {
            if (ea.indexOf("Slot") >= 0 && ea.indexOf("<") >= 0) {
                String word = ea.substring(ea.indexOf("<") + 1, ea.indexOf(">"));
                if (!word.equals(pre)) {
                    pre = word;
                    placeHolders.add(pre);
                }
            }
        }
        return placeHolders;
    }

    // The first function of the main interface number1, create a prototype template question
    public static void createProtoSen(Scanner scanner) {
        boolean finish = false;
        while (!finish) {
            System.out.println("");
            System.out.println("---------------------------------------------------------------");
            System.out.println("Set a Template-based skill with a command with placeholders: ");
            System.out.println("For example: Which lectures are there on <DAY> at <TIME>");
            System.out.println("---------------------------------------------------------------");
            System.out.println("Please enter a prototype sentence in the format above: ");
            String protoSen = scanner.nextLine().trim();
            List<String> placeHolders = splitProtoSen(protoSen);// The input statement is split into strings
            if (!userPrompt(scanner))
                continue;

            // The command is logged to a txt
            String question = "   Question   " + protoSen;
            List<String> slotLines = recordSlots(placeHolders, scanner);
            slotLines.add(0, question);
            List<String> lines = readFile("src\\sample.txt");
            lines.addAll(slotLines);
            writeFile(lines, "src\\sample.txt");

            System.out.println("---------------------------------------------------------------");
            System.out.println("The prototype sentence has been saved.");
            System.out.println("---------------------------------------------------------------");
            finish = true;
        }
    }


    public static List<String> readFile(String path) {
        try {
            File file = new File(path);
            List<String> lines = new ArrayList<String>();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
                return lines;
            }
        } catch (Exception IOException) {
            System.out.println(IOException.toString());
            return null;
        }
    }


    public static void writeFile(List<String> lines, String path) {
        try {
            File fout = new File(path);
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        } catch (Exception IOException) {
            System.out.println(IOException.toString());
        }
    }

    // Let you confirm, yes or no
    public static boolean userPrompt(Scanner scanner) {
        System.out.println("Enter YES to continue, Enter NO to redo: ");
        String conti = scanner.nextLine().trim();
        if (conti == "Y" || conti == "y") { // .equalsIgnoreCase()
            return true;
        } else {
            return false;
        }
    }

    // Pass in a sentence and return to the placeholder list
    public static List<String> splitProtoSen(String protoSen) {
        String[] substrings = protoSen.split(" ");
        String resp = "";
        List<String> placeholderList = new ArrayList<String>();
        for (String subStr : substrings) {
            if (subStr.indexOf("<") >= 0 && subStr.indexOf(">") >= 0) {
                subStr = subStr.replace("<", "").replace(">", "");
                resp += "<Placeholder: " + subStr + "> ";
                placeholderList.add(subStr);
            } else {
                resp += subStr + " ";
            }
        }
        System.out.println("---------------------------------------------------------------");
        System.out.println("The prototype sentence is: " + resp);
        System.out.println("---------------------------------------------------------------");
        return placeholderList;
    }

    // The user enters data into the same as the slot line
    public static List<String> recordSlots(List<String> placeHolders, Scanner scanner) {
        List<String> slotLines = new ArrayList<String>();
        for (String slot : placeHolders) {
            while (true) {
                System.out.println("");
                System.out.println("---------------------------------------------------------------");
                System.out.println("Enter values for <" + slot + "> " + "separated by \",\"");
                System.out.println("For example: value1,value2,value3");
                System.out.println("---------------------------------------------------------------");
                String input = scanner.nextLine().trim();
                String[] values = input.split(",");
                System.out.println("");
                System.out.println("---------------------------------------------------------------");
                System.out.println("Possible Values are: ");
                int counter = 1;
                for (String value : values) {
                    System.out.println(counter++ + ". " + value);
                }
                System.out.println("---------------------------------------------------------------");

                if (!userPrompt(scanner)) {
                    continue;
                }
                for (String value : values) {
                    slotLines.add("Slot   <" + slot + ">   " + value);
                }
                break;
            }
        }
        return slotLines;
    }

}