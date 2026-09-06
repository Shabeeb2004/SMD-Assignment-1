import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LogForge {
    static ServiceStats[] services = new ServiceStats[5];
    static int serviceCount = 0;

    static int totalLines = 0;
    static int validRecords = 0;
    static int invalidRecords = 0;
    static int infoCount = 0;
    static int warnCount = 0;
    static int errorCount = 0;

    static LogEntry[] entries = new LogEntry[5];
    static int entryCount = 0;
    static void recordService(String service, String level) {
        ServiceStats s = findService(service);
        if (s == null) {
            s = new ServiceStats(service);
            if (serviceCount == services.length) {
                ServiceStats[] bigger = new ServiceStats[services.length * 2];
                for (int i = 0; i < services.length; i++) bigger[i] = services[i];
                services = bigger;
            }
            services[serviceCount++] = s;
        }
        s.addRecord(level);
    }

    static ServiceStats findService(String name) {
        for (int i = 0; i < serviceCount; i++) {
            if (services[i].getServiceName().equals(name)) return services[i];
        }
        return null;
    }

    static void printServiceStats() {
        System.out.println();
        System.out.println("2. SERVICE STATISTICS");
        for (int i = 0; i < serviceCount; i++) {
            ServiceStats s = services[i];
            System.out.println("Service: " + s.getServiceName());
            System.out.println("Total: " + s.getTotal());
            System.out.println("INFO: " + s.getInfo());
            System.out.println("WARN: " + s.getWarn());
            System.out.println("ERROR: " + s.getError());
        }
    }

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

                String timestamp = fields[0];
                String service = fields[1];
                String level = fields[2];
                int requestId = parseInt(fields[3]);
                String message = fields[4];

                if (level.equals("INFO")) infoCount++;
                else if (level.equals("WARN")) warnCount++;
                else if (level.equals("ERROR")) errorCount++;

                addEntry(new LogEntry(timestamp, service, level, requestId, message));
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

    static void addEntry(LogEntry e) {
        if (entryCount == entries.length) {
            LogEntry[] bigger = new LogEntry[entries.length * 2];
            for (int i = 0; i < entries.length; i++) bigger[i] = entries[i];
            entries = bigger;
        }
        entries[entryCount++] = e;
    }

    static int parseInt(String s) {
        int value = 0;
        for (int i = 0; i < s.length(); i++) value = value * 10 + (s.charAt(i) - '0');
        return value;
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