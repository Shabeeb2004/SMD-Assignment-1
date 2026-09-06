public class LogEntry {
    private String timestamp;
    private String service;
    private String level;
    private int requestId;
    private String message;

    public LogEntry(String timestamp, String service, String level, int requestId, String message) {
        this.timestamp = timestamp;
        this.service = service;
        this.level = level;
        this.requestId = requestId;
        this.message = message;
    }

    public String getTimestamp() { return timestamp; }
    public String getService() { return service; }
    public String getLevel() { return level; }
    public int getRequestId() { return requestId; }
    public String getMessage() { return message; }
}