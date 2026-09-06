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
    static RequestStats[] requests = new RequestStats[5];
    static int requestCount = 0;

    static LogEntry[] entries = new LogEntry[5];
    static int entryCount = 0;

    static void recordRequest(int requestId, String service, String level) {
        RequestStats r = findRequest(requestId);
        if (r == null) {
            r = new RequestStats(requestId);
            if (requestCount == requests.length) {
                RequestStats[] bigger = new RequestStats[requests.length * 2];
                for (int i = 0; i < requests.length; i++) bigger[i] = requests[i];
                requests = bigger;
            }
            requests[requestCount++] = r;
        }
        r.addRecord(service, level);
    }

    static RequestStats findRequest(int id) {
        for (int i = 0; i < requestCount; i++) {
            if (requests[i].getRequestId() == id) return requests[i];
        }
        return null;
    }

    static void printRequestStats() {
        System.out.println();
        System.out.println("4. REQUEST STATISTICS");
        for (int i = 0; i < requestCount; i++) {
            RequestStats r = requests[i];
            System.out.println("Request: " + r.getRequestId());
            System.out.println("Status: " + (r.isFailed() ? "FAILED" : "SUCCESS"));
            System.out.println("Records: " + r.getTotal());
            System.out.println("Errors: " + r.getErrors());
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < r.getServiceCount(); j++) {
                sb.append(r.getServices()[j]);
                if (j < r.getServiceCount() - 1) sb.append(" ");
            }
            System.out.println("Services: " + sb.toString());
        }
    }
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
    static void sortServicesByErrorRate() {
        for (int i = 1; i < serviceCount; i++) {
            ServiceStats key = services[i];
            int j = i - 1;
            while (j >= 0 && shouldComeBefore(key, services[j])) {
                services[j + 1] = services[j];
                j--;
            }
            services[j + 1] = key;
        }
    }
    static void sortEntriesByTimestamp() {
        int n = entryCount;
        long[] keys = new long[n];
        int[] originalIndex = new int[n];
        for (int i = 0; i < n; i++) {
            keys[i] = toSeconds(entries[i].getTimestamp());
            originalIndex[i] = i;
        }

        for (int i = 1; i < n; i++) {
            LogEntry keyEntry = entries[i];
            long keyTime = keys[i];
            int keyIdx = originalIndex[i];
            int j = i - 1;
            while (j >= 0 && isAfter(keys[j], originalIndex[j], keyTime, keyIdx)) {
                entries[j + 1] = entries[j];
                keys[j + 1] = keys[j];
                originalIndex[j + 1] = originalIndex[j];
                j--;
            }
            entries[j + 1] = keyEntry;
            keys[j + 1] = keyTime;
            originalIndex[j + 1] = keyIdx;
        }
    }

    // true if (time1, idx1) should be placed AFTER (time2, idx2)
    static boolean isAfter(long time1, int idx1, long time2, int idx2) {
        if (time1 != time2) return time1 > time2;
        return idx1 < idx2;
    }

    // returns true if 'a' should be placed before 'b'
    static boolean shouldComeBefore(ServiceStats a, ServiceStats b) {
        double rateA = a.getErrorRate();
        double rateB = b.getErrorRate();
        if (rateA != rateB) return rateA > rateB; // descending error rate
        return a.getServiceName().compareTo(b.getServiceName()) < 0; // ascending name on tie
    }

    static void printServiceStats() {
        sortServicesByErrorRate();
        System.out.println();
        System.out.println("2. SERVICE STATISTICS");
        for (int i = 0; i < serviceCount; i++) {
            ServiceStats s = services[i];
            System.out.println("Service: " + s.getServiceName());
            System.out.println("Total: " + s.getTotal());
            System.out.println("INFO: " + s.getInfo());
            System.out.println("WARN: " + s.getWarn());
            System.out.println("ERROR: " + s.getError());
            System.out.printf("Error Rate: %.2f%%%n", s.getErrorRate());
        }
    }
    static void detectIncidents() {
        System.out.println();
        System.out.println("3. INCIDENTS");
        boolean any = false;

        for (int i = 0; i < serviceCount; i++) {
            String svc = services[i].getServiceName();
            LogEntry[] errs = new LogEntry[5];
            int errCount = 0;

            for (int j = 0; j < entryCount; j++) {
                if (entries[j].getService().equals(svc) && entries[j].getLevel().equals("ERROR")) {
                    if (errCount == errs.length) {
                        LogEntry[] bigger = new LogEntry[errs.length * 2];
                        for (int k = 0; k < errs.length; k++) bigger[k] = errs[k];
                        errs = bigger;
                    }
                    errs[errCount++] = entries[j];
                }
            }

            int idx = 0;
            while (idx < errCount) {
                int groupStart = idx;
                int groupEnd = idx;
                while (groupEnd + 1 < errCount &&
                        secondsBetween(errs[groupStart].getTimestamp(), errs[groupEnd + 1].getTimestamp()) <= 60) {
                    groupEnd++;
                }
                int groupSize = groupEnd - groupStart + 1;
                if (groupSize >= 3) {
                    any = true;
                    System.out.println("Service: " + svc);
                    System.out.println("First Error: " + errs[groupStart].getTimestamp());
                    System.out.println("Last Error: " + errs[groupEnd].getTimestamp());
                }
                idx = groupEnd + 1;
            }
        }

        if (!any) System.out.println("No incidents detected.");
    }

    static long secondsBetween(String t1, String t2) {
        return toSeconds(t2) - toSeconds(t1);
    }

    static long toSeconds(String timestamp) {
        int year = parseIntRange(timestamp, 0, 4);
        int month = parseIntRange(timestamp, 5, 7);
        int day = parseIntRange(timestamp, 8, 10);
        int hour = parseIntRange(timestamp, 11, 13);
        int minute = parseIntRange(timestamp, 14, 16);
        int second = parseIntRange(timestamp, 17, 19);
        long days = year * 372L + month * 31L + day;
        return days * 86400L + hour * 3600L + minute * 60L + second;
    }

    static int parseIntRange(String s, int start, int end) {
        int value = 0;
        for (int i = start; i < end; i++) value = value * 10 + (s.charAt(i) - '0');
        return value;
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