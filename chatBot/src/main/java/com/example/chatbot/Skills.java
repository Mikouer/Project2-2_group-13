package com.example.chatbot;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Skills {
    private final String SKILLDOC = "chatBot/src/skills.csv";


    public String[] returnAllSkills() throws FileNotFoundException {
        String line = "";
        String regex = ",";
        try {
            BufferedReader br = new BufferedReader(new FileReader(SKILLDOC));

            while((line = br.readLine()) != null) {
                String[] skills = line.split(regex);
                return skills;
            }

        } catch (FileNotFoundException e) { //BufferedReader
            e.printStackTrace();
        } catch (IOException e) {           //readLine
            throw new RuntimeException(e);
        }
        return null;
    }

    public void addSkill(Skill skill) throws IOException {
        String skillName = skill.getSKILLNAME();
        writeSkill(skillName);
    }

    private void writeSkill(String skill) throws IOException {
        StringBuilder sb = new StringBuilder();
        if(!(returnAllSkills()==null)){
            for (int i = 0; i < returnAllSkills().length; i++) {
                sb.append(returnAllSkills()[i]);
                sb.append(",");
            }
        }

        sb.append(skill);
        String skillString = sb.toString();
        FileWriter fw = new FileWriter(SKILLDOC);
        fw.write(skillString);
        fw.close();
    }

    public void deleteSkill(Skill skill) throws IOException {
        String skillName = skill.getSKILLNAME();
        removeSkill(skillName);
    }

    private void removeSkill(String skill) throws IOException {
        String[] allSkills = returnAllSkills();
        ArrayList<String> newSkillList = new ArrayList<>();
        for (int i = 0; i < allSkills.length; i++) {
            if (!allSkills[i].contains(skill)) {
                newSkillList.add(allSkills[i]);
            }
        }
        deleteAllSkills();
        for (String s : newSkillList) {
            writeSkill(s);
        }

    }

    public void deleteAllSkills() throws IOException {
        File newFile = new File(getSKILLDOC());
        FileWriter fw = new FileWriter(newFile, false);
        fw.close();
    }

    public String getSKILLDOC(){
        return SKILLDOC;
    }


    public static void main(String[] args) throws IOException {
        Skills skills = new Skills();
        System.out.println(Arrays.toString(skills.returnAllSkills()));
        Skill skill4 = new Skill("Writing");
        Skill skill2 = new Skill("throwing ball");
        skills.addSkill(skill4);
        System.out.println(Arrays.toString(skills.returnAllSkills()));
        skills.deleteAllSkills();
        skills.addSkill(skill2);
        skills.addSkill(skill4);
        System.out.println(Arrays.toString(skills.returnAllSkills()));
        skills.deleteAllSkills();
        System.out.println(Arrays.toString(skills.returnAllSkills()));

    }

}
