public class Main {
    public static void main(String[] args) {
        String regexPattern = "(T0)(.?)(T1)(.?)((T2)(.?)(T3)(.?)(T4)|(T5)(.?)(T6)|(T7)(.?)(T8)(.?)(T9)(.?)(T10))(.?)(T11)";
        String text = "T0T0T1T2T3T4T11T1T0T1T5T6T11";

        RELog logger = RELog.getInstance();
        logger.setInput(text);
        logger.setPattern(regexPattern);

        System.out.println("Log obtenido tras la ejecucion: " + text);

        String replaced = logger.replaceAll("");
        System.out.println("Reemplazo All: " + replaced);

        String first = logger.replaceFirst("");
        System.out.println("Reemplazo First: " + first);
    }
}
