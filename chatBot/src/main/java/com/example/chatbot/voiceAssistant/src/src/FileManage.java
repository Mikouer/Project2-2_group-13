package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class FileManage {

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
        } catch (Exception IOException) {
            System.out.println(IOException.toString());
            return null;
        }
    }

    public static void writeFile(List<String> lines, String path) {
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

}
