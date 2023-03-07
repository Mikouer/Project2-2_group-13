package src;

import java.util.ArrayList;
import java.util.List;

public class Operator {
    public List<List<String>> separateQuestions(List<String> lines) {
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

    public List<Slot> getSlots(List<String> trunk) {
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
    public List<String> getActions(List<String> trunk) {
        List<String> actions =new ArrayList<String>();
        String action;
        for(String line:trunk)
        {
            if(line.indexOf("Action")==0)
            {
                action=line.substring(7,line.length());
                actions.add(action);
            }
        }
        return actions;
    }
}
