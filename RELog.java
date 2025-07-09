import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RELog {
    // Entrada sobre la que se ejecutará el matcher
    private String inputLog;
    private final String regexPattern;
    private Pattern pattern;
    private Matcher matcher;

    // Singleton
    private static final RELog INSTANCE = new RELog();

    private RELog() { 
        this.regexPattern = "(T0)(.?)(T1)(.?)((T2)(.?)(T3)(.?)(T4)|(T5)(.?)(T6)|(T7)(.?)(T8)(.?)(T9)(.?)(T10))(.?)(T11)";
        this.pattern = Pattern.compile(this.regexPattern);
    }

    public static RELog getInstance() {
        return INSTANCE;
    }

    /**
     * Fija la cadena sobre la que buscar.
     */
    public void setLog(String log) {
        this.inputLog = log;
        if (this.pattern != null) {
            this.matcher = this.pattern.matcher(this.inputLog);
        }
    }

    /**
     * Compila el patrón y crea el matcher para la entrada actual.
     * @param regexPattern expresión regular de Java
     */
    public void setPattern(String regexPattern) {
        this.pattern = Pattern.compile(regexPattern);
        if (this.inputLog != null) {
            this.matcher = this.pattern.matcher(this.inputLog);
        }
    }

    /**
     * Busca la siguiente coincidencia.
     * @return true si encontró otra coincidencia
     */
    public boolean find() {
        matcherVerification();
        return matcher.find();
    }

    /**
     * Reinicia el matcher para volver a buscar desde el principio.
     */
    public void reset() {
        matcherVerification();
        matcher.reset();
    }

    /**
     * Devuelve el texto de todo el grupo 0 (coincidencia completa).
     */
    public String getGroup() {
        matcherVerification();
        return matcher.group();
    }

    /**
     * Devuelve el texto capturado por el grupo indicado.
     * @param group número de grupo (1…n)
     */
    public String getGroup(int group) {
        matcherVerification();
        return matcher.group(group);
    }

    /**
     * Devuelve una lista con todos los grupos capturados en la última coincidencia.
     */
    public List<String> getAllGroups() {
        matcherVerification();
        int count = matcher.groupCount();
        List<String> groups = new ArrayList<>(count + 1);
        for (int i = 0; i <= count; i++) {
            groups.add(matcher.group(i));
        }
        return groups;
    }

    /**
     * Reemplaza todas las coincidencias por la cadena dada, dejando intacto lo no coincidente.
     * @param replacement cadena de reemplazo, puede usar referencias a grupos: "$1", "$2", etc.
     * @return el String resultante con las sustituciones
     */
    public String replaceAll(String replacement) {
        patternVerification();
        return this.pattern.matcher(this.inputLog).replaceAll(replacement);
    }

    /**
     * Reemplaza la primera coincidencia por la cadena dada, dejando intacto lo no coincidente.
     * @param replacement cadena de reemplazo, puede usar referencias a grupos: "$1", "$2", etc.
     * @return el String resultante con la primera sustitución
     */
    public String replaceFirst(String replacement) {
        // Veridica que el patrón y el inputLog están inicializados
        patternVerification();
        inputVerification();
        // Reinicia el matcher con el nuevo inputLog
        matcher.reset(this.inputLog);
        return this.pattern.matcher(this.inputLog).replaceFirst(replacement);
    }

    public void replaceSequence() {
        int contadorCoincidencias = 1;
        // Verifica que el patrón y el inputLog están inicializados
        patternVerification();
        inputVerification();
        matcherVerification();
        System.out.println("Log obtenido tras la ejecucion: " + this.inputLog + "\n");

        if (this.inputLog == null || this.inputLog.isEmpty()) {
            System.out.println("No se encontraron coincidencias.");
        }
       
        // Mientras haya coincidencias, recorre el string y las reemplaza, mostrando el log tras cada reemplazo
        while (matcher.find()) {
            // Capturo lo que se acaba de emparejar
            String matcheo = matcher.group();
            // Reemplaza la coincidencia actual
            this.inputLog = replaceFirst("");
            // Muestra el log actualizado con su numero de reemplazo
            System.out.println("Reemplazo " + contadorCoincidencias +":");
            System.out.println("Secuencia encontrada: " + matcheo + " Residuos: " + this.inputLog + "\n");  
            contadorCoincidencias++;

            if (!matcher.find()){
            break; // Si no hay más coincidencias, salimos del bucle
            } 
        }

        if (!matcher.find()){
            System.out.println("No se encontraron nuevas coincidencias.\n");
            validateSystem();
        }

    }

    private void matcherVerification() {
        if (matcher == null) {
            throw new IllegalStateException("Pattern o input no inicializados.");
        }
    }

    private void patternVerification() {
        if (this.pattern == null) {
            throw new IllegalStateException("Debe inicializar pattern antes de reemplazar.");
        }
    }

    private void inputVerification() {
        if (this.inputLog == null) {
            throw new IllegalStateException("Debe establecer un log antes de buscar y/o reemplazar.");
        }
    }

    // Valida si el funcionamiento del sistema concurrente es correcto
    // Si todo es correcto, no deberiamos tener residuos en el log, pues todas las invariantes se ejecutaron
    // Si hay residuos, significa que el sistema no ha funcionado correctamente y se dispararon transiciones no deseadas
    private void validateSystem() {
        if (!this.inputLog.isEmpty()) {
            System.out.println("El sistema no ha funcionado correctamente, quedan residuos en el log: " + this.inputLog +"\n");
        } else {
            System.out.println("El sistema ha funcionado correctamente, no quedan residuos en el log. \n");
        }
    }

    @Override
    public String toString() {
        return "RELog{" +
               "input='" + inputLog + '\'' +
               ", pattern=" + (pattern != null ? pattern.pattern() : "null") +
               '}';
    }
}
