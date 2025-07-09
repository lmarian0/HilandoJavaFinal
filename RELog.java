import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RELog {
    // Entrada sobre la que se ejecutará el matcher
    private String input;
    private Pattern pattern;
    private Matcher matcher;

    // Singleton
    private static final RELog INSTANCE = new RELog();

    private RELog() { }

    public static RELog getInstance() {
        return INSTANCE;
    }

    /**
     * Fija la cadena sobre la que buscar.
     */
    public void setInput(String input) {
        this.input = input;
        if (this.pattern != null) {
            this.matcher = this.pattern.matcher(this.input);
        }
    }

    /**
     * Compila el patrón y crea el matcher para la entrada actual.
     * @param regexPattern expresión regular de Java
     */
    public void setPattern(String regexPattern) {
        this.pattern = Pattern.compile(regexPattern);
        if (this.input != null) {
            this.matcher = this.pattern.matcher(this.input);
        }
    }

    /**
     * Comprueba si toda la entrada coincide con el patrón.
     */
    public boolean matches() {
        ensureMatcher();
        return matcher.matches();
    }

    /**
     * Busca la siguiente coincidencia.
     * @return true si encontró otra coincidencia
     */
    public boolean find() {
        ensureMatcher();
        return matcher.find();
    }

    /**
     * Reinicia el matcher para volver a buscar desde el principio.
     */
    public void reset() {
        ensureMatcher();
        matcher.reset();
    }

    /**
     * Devuelve el texto de todo el grupo 0 (coincidencia completa).
     */
    public String getGroup() {
        ensureMatcher();
        return matcher.group();
    }

    /**
     * Devuelve el texto capturado por el grupo indicado.
     * @param group número de grupo (1…n)
     */
    public String getGroup(int group) {
        ensureMatcher();
        return matcher.group(group);
    }

    /**
     * Devuelve una lista con todos los grupos capturados en la última coincidencia.
     */
    public List<String> getAllGroups() {
        ensureMatcher();
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
        ensurePatternAndInput();
        return this.pattern.matcher(this.input).replaceAll(replacement);
    }

    /**
     * Reemplaza la primera coincidencia por la cadena dada, dejando intacto lo no coincidente.
     * @param replacement cadena de reemplazo, puede usar referencias a grupos: "$1", "$2", etc.
     * @return el String resultante con la primera sustitución
     */
    public String replaceFirst(String replacement) {
        ensurePatternAndInput();
        return this.pattern.matcher(this.input).replaceFirst(replacement);
    }

    private void ensureMatcher() {
        if (matcher == null) {
            throw new IllegalStateException("Pattern o input no inicializados.");
        }
    }

    private void ensurePatternAndInput() {
        if (this.pattern == null || this.input == null) {
            throw new IllegalStateException("Debe inicializar pattern e input antes de reemplazar.");
        }
    }

    @Override
    public String toString() {
        return "RELog{" +
               "input='" + input + '\'' +
               ", pattern=" + (pattern != null ? pattern.pattern() : "null") +
               '}';
    }
}
