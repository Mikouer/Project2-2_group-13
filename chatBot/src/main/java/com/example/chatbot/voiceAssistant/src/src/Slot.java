package src;

import java.util.ArrayList;
import java.util.List;

public class Slot {
    public String sname;
    public List<String> values;

    public Slot() {
        values = new ArrayList<String>();
    }

    public void addValues(String value) {
        values.add(value);
    }

    public void Setsname(String name) {
        this.sname = name;
    }
}
