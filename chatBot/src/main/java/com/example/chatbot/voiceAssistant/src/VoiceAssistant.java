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

    public static void mainMenu() { // 主菜单键盘进入界面
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("--------------------------main menu----------------------------");
            System.out.println("Select one option from below: press number");
            System.out.println("1. Create a prototype quetion.");
            System.out.println("2. Create actions for a prototype question.");
            System.out.println("3. Start using VoiceCommand.");
            System.out.println("---------------------------------------------------------------");
            String num = scanner.nextLine().trim(); // 读取用户输入

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

    // 第一个菜单项目，接受一个文字指令给一个回复number3
    public static void voiceCommand(Scanner scanner) {
        while (true) {
            System.out.println("Please enter command: ");
            String command = scanner.nextLine().trim();
            if (command.equalsIgnoreCase("quit"))
                break;
            getAnswer(command); // 用户输入command传入getAnswer,调取getAnswer函数
            System.out.println("Enter QUIT to quit");
        }
    }

    // 给一个回答打印在txt里面
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
            int score = compareSen(questions.get(i), command); // 调用
                                                               // compareSen,输入的问题和模板在比较，相似度最高我这才能知道该找哪一个trunk,找哪一个question
            if (score >= maxScore) {
                maxScore = score;
                question = questions.get(i);
                questionIndex = i;
            }
        }

        String[] words = question.split(" ");
        String[] commandWords = command.split(" ");
        List<Integer> indexes = getPlaceHolderIndexes(words);
        HashMap<String, HashMap<String, String>> findAnswer = getPossibleAnswers(trunks.get(questionIndex)); // 调用了getPossibleAnswers，
        String answer = searchAnswer(findAnswer, words, commandWords, indexes); // 调用了searchAnswer，这四个参数，words是question那一行每一单词是有自己的索引的，是一个String数组，commandWords是传入的command拆开的每一个单词组成的数组，indexes是与所有的placeholder的位置组成的数组
        System.out.println("---------------------------------------------------------------");
        System.out.println("Response: " + answer);
        System.out.println("---------------------------------------------------------------");
    }

    // txt里面几个问题模块就是几个trunks，question index[0]就是第一个问题，返回的是这个问题里面的所有的action
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
            String[] splits = line.split(" "); // 把subject里面的分割成一个一个的单词
            for (int i = 0; i < splits.length; i++) {
                if (splits[i].indexOf("Action") < 0) {// change 判断equal
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

    // 传进来的是所有可能的答案，找到索引完全匹配的值，然后根据所有的action找到一个答案
    public static String searchAnswer(HashMap<String, HashMap<String, String>> findAnswer,
            String[] words, String[] commandWords, List<Integer> indexes) {
        // index是模板问题里面的placeholder的索引
        HashMap<String, String> commandInput = new HashMap<String, String>();
        for (int index : indexes) {// change 用户输入问题格式与问题关键字位置相同？
            commandInput.put(words[index], commandWords[index]);
        }
        int maxScore = 0; //
        String finalAnswer = "";
        String noMatchAnswer = "";
        Iterator<Map.Entry<String, HashMap<String, String>>> iterator = findAnswer.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, HashMap<String, String>> entry = iterator.next(); // hashmap逻辑参考了，entry是遍历每一个元素，entrySet()是固定语法
            String answer = entry.getKey(); // 大的hashmap的key就是answer，取出这个值
            int score = 0;
            HashMap<String, String> valueMap = entry.getValue();
            for (Map.Entry<String, String> entry2 : valueMap.entrySet()) { // 里层的小的哈希函数遍历每一个元素取key和value
                String key = entry2.getKey();
                String inputValue = commandInput.get(key); //
                String testValue = entry2.getValue();
                if (inputValue.equals(testValue)) { // command里面placeholder对应的值，就是CNN，DSA这些才是input value
                    score += 1;
                }
            }
            if (score > maxScore) { // 用匹配值加分来看，哪一个action是最对的，movie匹配上+1，day匹配上+1分，time匹配上+1分最后3分最高分他的action就是最对的
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

    // 传进来的是question里面的<subject>那句话，拆成一个个单词后组成的数组，我就知道每一个问题的单词的索引了
    public static List<Integer> getPlaceHolderIndexes(String[] words) {
        //
        List<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < words.length; i++) {
            if (words[i].indexOf("<") >= 0 && words[i].indexOf(">") >= 0) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    // 完全引用别人的借鉴的 可改
    public static int compareSen(String a, String b) {// unchange 已足够优
        // compare the two sentences, and return a score for similarity，出现一样的单词越多，相似度越高
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
        return w1.size();// 返回一样的单词数量
    }

    // 主菜单的number2，在一个问题下建立对应的action
    public static void createActions(Scanner scanner) {
        // 添加如下代码
        List<String> lines = readFile("src\\sample.txt");
        List<List<String>> trunks = separateQuestions(lines); // 调用Questions，lines是txt里面的每一行
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
                // 用户想要退出
                if (trunkIndex == counter - 1) {
                    break;
                }

                List<String> resTrunk = trunks.get(trunkIndex);
                String actionLine = createActionsFromQ(resTrunk, getPlaceHolders(resTrunk), scanner); // 调用
                                                                                                      // createActionsFromQ()
                List<String> trunkMod = trunks.get(trunkIndex);
                trunkMod.add(actionLine);
                newTrunks = new ArrayList<List<String>>(trunks);
                newTrunks.set(trunkIndex, trunkMod);

                // 覆盖文档，包括新添加的操作
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

    // 传入txt所有行，返回所有的trunks，是一个二维数组，里面是trunk的每一行
    public static List<List<String>> separateQuestions(List<String> lines) {// j将每类问题的问题、关键字放一起
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

    // 用户选择的某一个trunk,这个问题里面的所有的placeholder，读取用户输入的scanner是一个系统级输入，
    public static String createActionsFromQ(List<String> trunk, List<String> placeHolders, Scanner scanner) {// j有问题
        List<String> actionValues = new ArrayList<String>();
        System.out.println("==============================================================");
        for (String t : trunk) {
            System.out.println(t);
        }
        System.out.println("==============================================================");

        for (String p : placeHolders) {
            System.out.println("Set value for <" + p + "> " + "or Enter SKIP");
            String value = scanner.nextLine().trim(); // Read user input
            if (value.equalsIgnoreCase("skip")) {
                value = "";
            }
            actionValues.add(value);
        }

        System.out.println("Set response: ");
        String res = scanner.nextLine().trim(); // Read user input

        String lineFinal = "Action";
        for (int i = 0; i < placeHolders.size(); i++) {
            if (actionValues.get(i) != "") {
                lineFinal += " <" + placeHolders.get(i) + "> " + actionValues.get(i);
            }
        }
        lineFinal += " " + res;
        return lineFinal; // 返回一行，以action开头的一句话
    }

    // 穿进去一个trunk,获取slot对应的placeholder，实际上穿进去一个问题然后返回一个placeholder这么写麻烦了，可以优化
    // changed
    public static List<String> getPlaceHolders(List<String> trunk) {
        List<String> placeHolders = new ArrayList<String>();
        String pre = "";
        for (String ea : trunk) {// 获取关键词
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

    // 主界面第一个功能number1的，创建一个原型模板问句
    public static void createProtoSen(Scanner scanner) {
        boolean finish = false;
        while (!finish) {
            System.out.println("");
            System.out.println("---------------------------------------------------------------");
            System.out.println("Set a Template-based skill with a command with placeholders: ");
            System.out.println("For example: Which lectures are there on <DAY> at <TIME>");
            System.out.println("---------------------------------------------------------------");
            System.out.println("Please enter a prototype sentence in the format above: ");
            String protoSen = scanner.nextLine().trim(); // Read user input
            List<String> placeHolders = splitProtoSen(protoSen);// 输入语句分割成字符串
            if (!userPrompt(scanner))
                continue;

            // 将原型命令记录到文档中）
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

    // 读取文件，也是借鉴的
    public static List<String> readFile(String path) {// change
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

    // 这个是写入数据，借鉴的
    public static void writeFile(List<String> lines, String path) {// change
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

    // 让你确认，yes no返回的是TRUE，FALSE，这个借鉴
    public static boolean userPrompt(Scanner scanner) {
        System.out.println("Enter YES to continue, Enter NO to redo: ");
        String conti = scanner.nextLine().trim();
        if (conti == "Y" || conti == "y") { // .equalsIgnoreCase()
            return true;
        } else {
            return false;
        }
    }

    // 传进一个句子然后返回placeholder list,有的函数可以合并在一起的功能没有拆分好，
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

    // 用户输入数据变成slot那一行那样的字符串
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

                if (!userPrompt(scanner)) {// 位置可改进
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