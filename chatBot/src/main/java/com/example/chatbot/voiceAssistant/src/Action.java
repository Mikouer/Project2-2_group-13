package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Action {
    String answer;
    Operator operator;

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void create(Scanner scanner){
        operator=new Operator();
        List<String> lines = FileManage.readFile("src\\sample.txt");
        List<List<String>> trunks = operator.separateQuestions(lines); //Call separateQuestions, lines are each line inside txt
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
            String num = scanner.nextLine().trim(); // set which number question's action
            int trunkIndex = Integer.parseInt(num) - 1;

            try {
                // The user wants to exit
                if (trunkIndex == counter - 1) {
                    break;
                }

                List<String> resTrunk = trunks.get(trunkIndex);
                String actionLine = createActionsFromQ(resTrunk, getPlaceHolders(resTrunk), scanner);
                List<String> trunkMod = trunks.get(trunkIndex);
                trunkMod.add(actionLine);
                newTrunks = new ArrayList<List<String>>(trunks);
                newTrunks.set(trunkIndex, trunkMod);

                // Override txt, including newly added operations
                lines = new ArrayList<String>();
                for (List<String> t : newTrunks) {
                    for (String line : t) {
                        lines.add(line);
                    }
                }
                FileManage.writeFile(lines, "src\\sample.txt");
                trunks = newTrunks;
            } catch (Exception e) {
                System.out.println(e.toString());
                break;
            }
        }
    }

    // get slot
    public List<String> getPlaceHolders(List<String> trunk) {
        List<String> placeHolders = new ArrayList<String>();
        String pre = "";
        for (String ea : trunk) {
            if (ea.indexOf("Slot") == 0 && ea.indexOf("<") >= 0) {
                String word = ea.substring(ea.indexOf("<") + 1, ea.indexOf(">"));
                if (!word.equals(pre)) {
                    pre = word;
                    placeHolders.add(pre);
                }
            }
        }
        return placeHolders;
    }

    public String createActionsFromQ(List<String> trunk, List<String> placeHolders, Scanner scanner) {
        List<String> actionValues = new ArrayList<String>();
        System.out.println("==============================================================");
        for (String t : trunk) {
            System.out.println(t);
        }
        System.out.println("==============================================================");
        List<Slot> slots = new ArrayList<Slot>();
        slots = chooseValue(trunk);
        for (Slot p : slots) {

            int count = 1;
            System.out.println("choose a  value for <" + p.sname + "> ");
            List<String> values = p.values;
            System.out.println(values.size());
            for (String value : values) {
                System.out.println(count + " " + value);
                count++;
            }
            int value = Integer.parseInt(scanner.nextLine().trim()); // The input is value
            actionValues.add(values.get(value - 1));
        }
        System.out.println("Set response: ");
        String res = scanner.nextLine().trim(); // The answer corresponding to the action
        String lineFinal = "Action";
        for (int i = 0; i < placeHolders.size(); i++) {
            if (actionValues.get(i) != "") {
                lineFinal += " <" + placeHolders.get(i) + "> " + actionValues.get(i);
            }
        }
        lineFinal += " " + res;
        return lineFinal; // Returns a line, a sentence that begins with action
    }

    public List<Slot> chooseValue(List<String> trunk) {
        List<Slot> keys = new ArrayList<Slot>();
        Slot slot = new Slot();
        String key = null;
        for (String line : trunk) {
            if (line.indexOf("Slot") == 0) {
                int front, end;
                front = line.indexOf("<");
                end = line.indexOf(">");
                if (key == null) {
                    key = line.substring(front + 1, end);
                    slot.Setsname(key);
                    slot.addValues(line.substring(end + 4, line.length()));
                } else {
                    if (!key.equals(line.substring(front + 1, end))) {
                        keys.add(slot);
                        slot = new Slot();
                        key = line.substring(front + 1, end);
                        slot.Setsname(key);
                        slot.addValues(line.substring(end + 4, line.length()));
                    } else {
                        slot.addValues(line.substring(end + 4, line.length()));
                    }
                }
            }
        }
        keys.add(slot);
        return keys;
    }
}
