public class Main {
    public static void main(String[] args) {
        String log = "T0T1T2T3T4T11T0T1T5T6T11T1";

        RELog REController = RELog.getInstance();
        REController.setLog(log);

        REController.replaceSequence();
    }
}
