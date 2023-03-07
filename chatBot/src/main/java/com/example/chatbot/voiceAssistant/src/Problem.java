package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Problem {
    public String pname;
    public List<Slot> slots;
    public List<String> actions;
    public void init() {
        slots = new ArrayList<Slot>();
    }

    public void create(Scanner scanner) {
        boolean finish = false;
        System.out.println("");
        System.out.println("---------------------------------------------------------------");
        System.out.println("Set a Template-based skill with a command with placeholders: ");
        System.out.println("For example: Which lectures are there on <DAY> at <TIME>");
        System.out.println("---------------------------------------------------------------");
        System.out.println("Please enter a prototype sentence in the format above: ");
        String protoSen = scanner.nextLine().trim();
        List<String> placeHolders = splitProblem(protoSen);

        String question = "   Question   " + protoSen;
        pname = question;
        System.out.println(placeHolders.toString());
        slots = recordSlots(placeHolders);
        finish = true;
        List<String> lines = FileManage.readFile("src\\sample.txt");
        lines.add(pname);
        for (Slot slot : slots) {
            List<String> values = slot.values;
            for (String value : values) {
                lines.add("Slot   <" + slot.sname + ">   " + value);
            }
        }
        FileManage.writeFile(lines, "src\\sample.txt");
        System.out.println("---------------------------------------------------------------");
        System.out.println("The prototype sentence has been saved.");
        System.out.println("---------------------------------------------------------------");

    }

    private List<String> splitProblem(String problemname) {
        String[] substrings = problemname.split(" ");
        String resp = "";
        System.out.println(substrings.toString());
        List<String> placeholderList = new ArrayList<String>();
        for (String subStr : substrings) {
            if (subStr.indexOf("<") >= 0 && subStr.indexOf(">") >= 0) {
                subStr = subStr.replace("<", "").replace(">", "");
                resp += "<Placeholder: " + subStr + ">";
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

    private List<Slot> recordSlots(List<String> placeHolders) {
        Scanner scanner = new Scanner(System.in);
        List<Slot> slotLines = new ArrayList<Slot>();
        for (String slot : placeHolders) {
            Slot slotres = new Slot();
            slotres.Setsname(slot);
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
            for (String value : values) {
                slotres.addValues(value);
                System.out.println(value);
            }
            slotLines.add(slotres);
            System.out.println("---------------------------------------------------------------");
        }
        return slotLines;
    }
}
