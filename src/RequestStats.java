public class RequestStats {
    private int requestId;
    private int total;
    private int errors;
    private String[] services = new String[5];
    private int serviceCount = 0;

    public RequestStats(int requestId) {
        this.requestId = requestId;
    }

    public void addRecord(String service, String level) {
        total++;
        if (level.equals("ERROR")) errors++;
        addServiceIfNew(service);
    }

    private void addServiceIfNew(String service) {
        for (int i = 0; i < serviceCount; i++) {
            if (services[i].equals(service)) return;
        }
        if (serviceCount == services.length) {
            String[] bigger = new String[services.length * 2];
            for (int i = 0; i < services.length; i++) bigger[i] = services[i];
            services = bigger;
        }
        services[serviceCount++] = service;
    }

    public int getRequestId() { return requestId; }
    public int getTotal() { return total; }
    public int getErrors() { return errors; }
    public boolean isFailed() { return errors > 0; }
    public String[] getServices() { return services; }
    public int getServiceCount() { return serviceCount; }
}