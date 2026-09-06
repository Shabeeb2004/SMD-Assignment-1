import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LogForge {

    static int totalLines = 0;
    static int validRecords = 0;
    static int invalidRecords = 0;
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
                totalLines++;
                String[] fields = splitByPipe(line);

                if (!isValidRecord(fields)) {
                    invalidRecords++;
                    continue;
                }
                validRecords++;
                String level = fields[2];
                if (level.equals("INFO")) infoCount++;
                else if (level.equals("WARN")) warnCount++;
                else if (level.equals("ERROR")) errorCount++;
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + args[0]);
            return;
        }

        System.out.println("Total lines: " + totalLines);
        System.out.println("Valid records: " + validRecords);
        System.out.println("Invalid records: " + invalidRecords);
        System.out.println("INFO: " + infoCount);
        System.out.println("WARN: " + warnCount);
        System.out.println("ERROR: " + errorCount);
    }

    static boolean isValidRecord(String[] fields) {
        if (fields.length != 5) return false;

        String timestamp = fields[0];
        String level = fields[2];
        String requestIdStr = fields[3];

        if (timestamp.length() != 19) return false;
        if (timestamp.charAt(4) != '-' || timestamp.charAt(7) != '-' ||
                timestamp.charAt(10) != ' ' || timestamp.charAt(13) != ':' ||
                timestamp.charAt(16) != ':') return false;

        int[] digitPositions = {0,1,2,3,5,6,8,9,11,12,14,15,17,18};
        for (int idx : digitPositions) {
            char c = timestamp.charAt(idx);
            if (c < '0' || c > '9') return false;
        }

        if (!(level.equals("INFO") || level.equals("WARN") || level.equals("ERROR"))) return false;

        if (requestIdStr.length() == 0) return false;
        for (int i = 0; i < requestIdStr.length(); i++) {
            char c = requestIdStr.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        long value = 0;
        for (int i = 0; i < requestIdStr.length(); i++) value = value * 10 + (requestIdStr.charAt(i) - '0');
        if (value <= 0) return false;

        return true;
    }

    static String[] splitByPipe(String line) {
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