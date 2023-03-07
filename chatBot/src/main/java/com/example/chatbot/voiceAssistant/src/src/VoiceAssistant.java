package src;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class VoiceAssitant {
    static List<Problem> problems;

    public static void main(String[] args) {
        problems = new ArrayList<Problem>();
        mainMenu();
    }

    public static void mainMenu() { // The main menu keyboard enters the interface
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("--------------------------main menu----------------------------");
            System.out.println("Select one option from below: press number");
            System.out.println("1. Create a prototype question.");
            System.out.println("2. Create actions for a prototype question.");
            System.out.println("3. Start using VoiceCommand.");
            System.out.println("---------------------------------------------------------------");
            String num = scanner.nextLine().trim(); // press number read user input
            Problem problem = new Problem();   //creat prototype question
            Action action = new Action();      //action corresponding to the question
            Answer answer =new Answer();       //Ask questions based on the template's questions
            try {
                int selection = Integer.parseInt(num);
                switch (selection) {
                    case 1:
                        problem.create(scanner);
                        break;
                    case 2:
                        action.create(scanner);
                        break;
                    case 3:
                        answer.getAnswer(scanner);
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
}