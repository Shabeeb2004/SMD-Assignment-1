public class ServiceStats {
    private String serviceName;
    private int total;
    private int info;
    private int warn;
    private int error;

    public ServiceStats(String serviceName) {
        this.serviceName = serviceName;
    }

    public void addRecord(String level) {
        total++;
        if (level.equals("INFO")) info++;
        else if (level.equals("WARN")) warn++;
        else if (level.equals("ERROR")) error++;
    }

    public String getServiceName() { return serviceName; }
    public int getTotal() { return total; }
    public int getInfo() { return info; }
    public int getWarn() { return warn; }
    public int getError() { return error; }

    public double getErrorRate() {
        if (total == 0) return 0.0;
        return (error * 100.0) / total;
    }
}