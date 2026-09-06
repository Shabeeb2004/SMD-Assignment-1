import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LogForge {

    static int total = 0;
    static int infoCount = 0;
    static int warnCount = 0;
    static int errorCount = 0;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java LogForge <logfile>");
            return;
        }

        try {
            Scanner sc = new Scanner(new File(args[0]));
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                total++;
                String[] fields = splitByPipe(line);
                if (fields.length >= 3) {
                    String level = fields[2];
                    if (level.equals("INFO")) infoCount++;
                    else if (level.equals("WARN")) warnCount++;
                    else if (level.equals("ERROR")) errorCount++;
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + args[0]);
            return;
        }

        System.out.println("Total records: " + total);
        System.out.println("INFO: " + infoCount);
        System.out.println("WARN: " + warnCount);
        System.out.println("ERROR: " + errorCount);
    }

    private static String[] splitByPipe(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) if (line.charAt(i) == '|') count++;
        String[] fields = new String[count + 1];
        int fieldIndex = 0, start = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '|') {
                fields[fieldIndex++] = line.substring(start, i);
                start = i + 1;
            }
        }
        fields[fieldIndex] = line.substring(start);
        return fields;
    }
}