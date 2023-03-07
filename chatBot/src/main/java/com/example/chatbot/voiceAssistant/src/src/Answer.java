package src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Answer {
    Operator operator;
    //Show the scope of questions in the template library that users can ask
    public void getAnswer(Scanner scanner) {
        operator = new Operator();
        List<Problem> problems = new ArrayList<Problem>();
        List<String> lines = FileManage.readFile("src\\src\\sample.txt");
        System.out.println("You can ask the following questions：");
        System.out.println("---------------------------------------------------------------");
        List<List<String>> trunks = operator.separateQuestions(lines);
        List<String> questions = new ArrayList<String>();
        Problem problem;
        for (List<String> t : trunks) {
            problem = new Problem();
            String question = t.get(0);
            question = question.substring(14);
            questions.add(question);
            System.out.println(question);
            problem.pname = question;
            problem.slots = operator.getSlots(t);
            problem.actions = operator.getActions(t);
            problems.add(problem);
        }
        while (true) {
            System.out.println("---------------------------------------------------------------");
            System.out.println("Please enter a command of a similar format with the <  > symbol: ");
            String command = scanner.nextLine().trim();
            if (command.equalsIgnoreCase("quit"))
                break;
            System.out.println("Enter QUIT to quit");
            String answer = compareSen(problems, command);
            System.out.println("---------------------------------------------------------------");
            System.out.println("Response: " + answer);
            System.out.println("---------------------------------------------------------------");
        }
    }

    //Determine which problem the slot in the input belongs to
    public String compareSen(List<Problem> problems, String input) {
        String[] words = input.split(" ");
        List<String> keys = new ArrayList<String>();
        for (String word : words) {
            if (word.contains("<")) {
                keys.add(word.substring(1, word.indexOf(">")));
            }
        }
        for (Problem problem : problems) {
            List<Slot> slots = new ArrayList<Slot>();
            slots = problem.slots;
            for (Slot slot : slots) {
                List<String> values = slot.values;
                for (String value : values) {
                    if (keys.contains(value)) {
                        return searchAnswer(keys, problem.actions);
                    }
                }
            }
        }

        return "I don't know this question.";
    }

    //Determine whether the slots in the action of the problem are equal to the slots entered
    public String searchAnswer(List<String> keys, List<String> actions) {
        // The number of slots entered is num, and the same slot content is count
        for (String action : actions) {
            int num = 0;
            for (int i = 0; i < action.length(); i++) {
                if (action.charAt(i) == '<')
                    num++;
            }
            int count = 0;
            if (num == keys.size())
                for (String key : keys) {
                    if (action.contains(key)) {
                        count++;
                    }
                }
            if (count == keys.size()) {
                return action;
            }
        }
        return actions.get(actions.size() - 1);
    }
}
