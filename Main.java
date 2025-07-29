public class Main {
    public static void main(String[] args) {
        String log = "T0T1T2T3T4T11T0T1T5T6T11T0T1T7T8T9T10T11";

        RELog REController = RELog.getInstance();
        REController.setLog(log);

        System.out.println(REController.getAllGroups());
        // Reiniciar el matcher antes de usar replaceSequence
        REController.reset();

        REController.replaceSequence();
    }
}
