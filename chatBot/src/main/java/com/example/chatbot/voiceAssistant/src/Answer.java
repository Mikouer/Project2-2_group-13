package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Answer {
    Operator operator;

    public void getAnswer(Scanner scanner) {
        operator = new Operator();
        List<Problem> problems = new ArrayList<Problem>();
        List<String> lines = FileManage.readFile("src\\sample.txt");
        System.out.println("You can ask the following questions：");
        List<List<String>> trunks = operator.separateQuestions(lines);
        List<String> questions = new ArrayList<String>();
        Problem problem = new Problem();
        int count = 1;
        for (List<String> t : trunks) {
            String question = t.get(0);
            question = question.substring(14);
            questions.add(question);
            problem.pname = question;
            System.out.println(count++ + ":" + question);
            problem.slots = operator.getSlots(t);
            problem.actions = operator.getActions(t);
            problems.add(problem);
        }
        while (true) {
            System.out.println("Please enter command: ");
            String command = scanner.nextLine().trim();
            if (command.equalsIgnoreCase("quit"))
                break;

            System.out.println("Enter QUIT to quit");
            int maxScore = 0;
            String question = "";
            int questionIndex = 0;
            for (int i = 0; i < questions.size(); i++) {
                // 3.5 23.26
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
    }

    public int compareSen(String a, String b) {
        // compare the two sentences, and return a score for similarity
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

    public String searchAnswer(HashMap<String, HashMap<String, String>> findAnswer,
            String[] words, String[] commandWords, List<Integer> indexes) {

        HashMap<String, String> commandInput = new HashMap<String, String>();
        for (int index : indexes) {
            commandInput.put(words[index], commandWords[index]);
        }
        int maxScore = 0; //
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
        //
        List<Integer> indexes = new ArrayList<Integer>();
        for (int i = 0; i < words.length; i++) {
            if (words[i].indexOf("<") >= 0 && words[i].indexOf(">") >= 0) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public static HashMap<String, HashMap<String, String>> getPossibleAnswers(List<String> trunk) {
        List<String> filteredT = new ArrayList<String>();
        for (String ea : trunk) { // ea is every line of trunk，each line
            if (ea.indexOf("Action") == 0) {
                filteredT.add(ea);
            }
        }
        HashMap<String, HashMap<String, String>> findAnswer = new HashMap<String, HashMap<String, String>>();
        for (String line : filteredT) {
            String answer = "";
            HashMap<String, String> valueMap = new HashMap<String, String>();
            String[] splits = line.split(" ");
            for (int i = 0; i < splits.length; i++) {
                if (splits[i].indexOf("Action") != 0) {
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
}
