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

    public static void mainMenu() {
        while (true) {
            Scanner scanner = new Scanner(System.in);  // Create a Scanner object
            System.out.println("--------------------------main menu----------------------------");
            System.out.println("Select one option from below: press number");
            System.out.println("1. Create a prototype quetion.");
            System.out.println("2. Create actions for a prototype question.");
            System.out.println("3. Start using VoiceCommand.");
            System.out.println("---------------------------------------------------------------");
            String num = scanner.nextLine().trim();  // Read user input
            
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

    public static void getAnswer(String command) {
        List<String> lines = readFile("sample.txt");
        List<List<String>> trunks = separateQuestions(lines);
        List<String> questions = new ArrayList<String>();

        for(List<String> t: trunks) {
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

    public static HashMap<String, HashMap<String, String>> getPossibleAnswers(List<String> trunk) {
        List<String> filteredT = new ArrayList<String>();
        for (String ea: trunk) {    //ea is every line of trunk
            if (ea.contains("Action")) {
                filteredT.add(ea);
            }
        }
        HashMap<String, HashMap<String, String>> findAnswer = new HashMap<String, HashMap<String, String>>();
        for (String line: filteredT) {
            String answer = "";
            HashMap<String, String> valueMap = new HashMap<String, String>();
            String[] splits = line.split(" ");   // 把subject里面的分割成一个一个的单词
            for (int i = 0; i < splits.length; i++) {
                if (!splits[i].contains("Action")) {
                    if (splits[i-1].contains(">")) {
                        answer = "";
                        valueMap.put(splits[i-1], splits[i]);
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

    public static String searchAnswer(HashMap<String, HashMap<String, String>> findAnswer,
                                      String[] words, String[] commandWords, List<Integer> indexes) {
        //index是模板问题里面的placeholder的索引
        HashMap<String, String> commandInput = new HashMap<String, String>();
        for (int index: indexes) {
            commandInput.put(words[index], commandWords[index]);
        }
        int maxScore = 0;   //
        String finalAnswer = "";
        String noMatchAnswer = "";
        for (Map.Entry<String, HashMap<String, String>> entry : findAnswer.entrySet()) {
            String answer = entry.getKey();
            int score = 0;
            HashMap<String, String> valueMap = entry.getValue();
            for (Map.Entry<String, String> entry2 : valueMap.entrySet()) {
                String key = entry2.getKey();
                String inputValue = commandInput.get(key);
                String testValue = entry2.getValue();
                if (inputValue.equals(testValue)) {
                    score += 1;
                }
            }
            if (score > maxScore) {
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

    public static List<Integer> getPlaceHolderIndexes(String[] words) {
        //Get placeholder indexes
        List<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < words.length; i++) {
            if (words[i].contains("<") && words[i].contains(">")) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public static int compareSen(String a, String b) {
        //compare the two sentences, and return a score for similarity
        final Set<String> words = new HashSet<>(Arrays.asList(a.toLowerCase().split("\\s+")));
        final Set<String> words2 = new HashSet<>(Arrays.asList(b.toLowerCase().split("\\s+")));
        words.retainAll(words2);
        return words.size();
    }

    public static void createActions(Scanner scanner) {
        List<String> lines = readFile("sample.txt");
        List<List<String>> trunks = separateQuestions(lines);
        List<List<String>> newTrunks = new ArrayList<List<String>>();

        while(true) {
            System.out.println("");
            System.out.println("---------------------------------------------------------------");
            System.out.println("Enter question number to create actions");
            int counter = 1;
            for(List<String> t: trunks) {
                System.out.println(counter++ + "." + t.get(0));
            }
            System.out.println(counter + ".   Exit");
            System.out.println("---------------------------------------------------------------");
            String num = scanner.nextLine().trim();  // Read user input
            int trunkIndex = Integer.parseInt(num) - 1;

            try {
                //user wants to exit
                if (trunkIndex == counter - 1) {
                    break;
                }

                List<String> resTrunk = trunks.get(trunkIndex);
                String actionLine = createActionsFromQ(resTrunk, getPlaceHolders(resTrunk), scanner);
                List<String> trunkMod = trunks.get(trunkIndex);
                trunkMod.add(actionLine);
                newTrunks = new ArrayList<List<String>>(trunks);
                newTrunks.set(trunkIndex, trunkMod);

                //Overwrite file including the newly added action
                lines = new ArrayList<String>();
                for (List<String> t: newTrunks) {
                    for (String line: t) {
                        lines.add(line);
                    }
                }
                writeFile(lines, "sample.txt");
                trunks = newTrunks;
            } catch(Exception e) {
                System.out.println(e.toString());
                break;
            }
        }
    }

    public static List<List<String>> separateQuestions(List<String> lines) {
        List<String> trunk = new ArrayList<String>();
        List<List<String>> trunks = new ArrayList<List<String>>();
        //divide proto sentences in trunks
        int lineNum = 1;
        for (String line: lines) {
            if (!line.contains("Question")) {
                trunk.add(line);
            } else {
                if(trunk.size() > 0) 
                    trunks.add(trunk);
                trunk = new ArrayList<String>();
                trunk.add(line);
            }
            //read the last trunk
            if(lineNum == lines.size())
                trunks.add(trunk);
            lineNum++;
        }
        return trunks;
    }

    public static String createActionsFromQ(List<String> trunk, List<String> placeHolders, Scanner scanner) {
        List<String> actionValues = new ArrayList<String>();
        System.out.println("==============================================================");
        for (String t: trunk) {
            System.out.println(t);
        }
        System.out.println("==============================================================");

        for (String p: placeHolders) {
            System.out.println("Set value for <" + p + "> " + "or Enter SKIP");
            String value = scanner.nextLine().trim();  // Read user input
            if (value.equalsIgnoreCase("skip")) {
                value = "";
            }
            actionValues.add(value);
        }
        
        System.out.println("Set response: ");
        String res = scanner.nextLine().trim();  // Read user input
        
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
        String currentPlaceholder = "";
        List<String> placeHolders = new ArrayList<String>();

        List<String> filteredT = new ArrayList<String>();
        for (String ea: trunk) {
            if (ea.contains("Slot")) {
                filteredT.add(ea);
            }
        }
        for (String line: filteredT) {
            String placeHolder = findMatch("<(\\S*)>", line);
            if (!placeHolder.equals(currentPlaceholder)) {
                currentPlaceholder = placeHolder;
                placeHolders.add(currentPlaceholder);
            }
        }
        return placeHolders;
    }

    public static HashMap<String, List<String>> getValueSets(List<String> trunk) { 
        HashMap<String, List<String>> valueSets = new HashMap<String, List<String>>();
        String currentPlaceholder = "";
        List<String> values = new ArrayList<String>();
        int lineNum = 1;
        List<String> filteredT = new ArrayList<String>();
        for (String ea: trunk) {
            if (ea.contains("Slot")) {
                filteredT.add(ea);
            }
        }
        for (String line: filteredT) {
            String placeHolder = findMatch("<(\\S*)>", line);
            if (!placeHolder.equals(currentPlaceholder)) {
                if (lineNum > 1) 
                    valueSets.put(currentPlaceholder, values);
                values = new ArrayList<String>();
                currentPlaceholder = placeHolder;
                String value = findMatch("^\\S* *\\S* *(\\S*)$", line);
                values.add(value);
            } else {
                String value = findMatch("^\\S* *\\S* *(\\S*)$", line);
                values.add(value);
            }
            // read values from last line
            if (lineNum == filteredT.size())
                valueSets.put(currentPlaceholder, values);
            lineNum++;
        }
        return valueSets;
    }

    public static void createProtoSen(Scanner scanner) {
        boolean finish = false; 
        while(!finish) {
            System.out.println("");
            System.out.println("---------------------------------------------------------------");
            System.out.println("Set a Template-based skill with a command with placeholders: ");
            System.out.println("For example: Which lectures are there on <DAY> at <TIME>");
            System.out.println("---------------------------------------------------------------");
            System.out.println("Please enter a prototype sentence in the format above: ");
            String protoSen = scanner.nextLine().trim();  // Read user input
            List<String> placeHolders = splitProtoSen(protoSen);
            if (!userPrompt(scanner))
                continue;

            // Record the prototype command to the document
            String question = "   Question   " + protoSen;
            List<String> slotLines = recordSlots(placeHolders, scanner);
            slotLines.add(0, question);
            List<String> lines = readFile("sample.txt");
            lines.addAll(slotLines);
            writeFile(lines, "sample.txt");

            System.out.println("---------------------------------------------------------------");
            System.out.println("The prototype sentence has been saved.");
            System.out.println("---------------------------------------------------------------");
            finish = true;
        }
    }

    public static String findMatch(String regex, String input) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        boolean matchFound = matcher.find();
        if(matchFound) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    public static List<String> findMatch2(String regex, String input) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        List<String> matchlist = new ArrayList<String>();
        while (matcher.find()) {
            matchlist.add(matcher.group());
        }
        return matchlist;
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
        } catch(Exception IOException) {
            System.out.println(IOException.toString());
            return null;
        }
    }

    public static void writeFile(List<String> lines, String path) {
        try {
            File fout = new File(path);
            FileOutputStream fos = new FileOutputStream(fout);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            for (String line: lines) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        } catch(Exception IOException) {
            System.out.println(IOException.toString());
        }
    }

    public static boolean userPrompt(Scanner scanner) {
        System.out.println("Enter YES to continue, Enter NO to redo: ");
        String conti = scanner.nextLine().trim();  // Read user input
        if (conti.equalsIgnoreCase("yes")) {
            return true;
        } else {
            return false;
        }
    }

    public static List<String> splitProtoSen(String protoSen) {
        String[] substrings = protoSen.split(" ");
        String resp = "";
        List<String> placeholderList = new ArrayList<String>();
        for (String subStr : substrings) {
            if (subStr.contains("<") && subStr.contains(">")) {
                subStr = subStr.replace("<","").replace(">", "");
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

    public static List<String> recordSlots(List<String> placeHolders, Scanner scanner) {
        List<String> slotLines = new ArrayList<String>();
        for (String slot: placeHolders) {
            while(true) {
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
                for (String value: values) {
                    System.out.println(counter++ + ". " + value);
                }
                System.out.println("---------------------------------------------------------------");

                if (!userPrompt(scanner)) {
                    continue;
                }
                for (String value: values) {
                    slotLines.add("Slot   <" + slot + ">   " + value);
                }
                break;   
            }
        }
        return slotLines;
    }

    


}