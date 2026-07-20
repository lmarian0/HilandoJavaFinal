package tpfinal.utils;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

/**
 * Tests unitarios del patrón de T-invariantes y su reducción recursiva.
 */
public class RELogTest extends TestCase {

    /**
     * Verifica que la secuencia T0-T1-T2-T3-T4-T11 sea reconocida como un
     * único invariante de complejidad media, sin transiciones residuales.
     */
    public void testValidMediumInvariant() {
        ValidationResult result = new RELog().validate(
                Arrays.asList("T0", "T1", "T2", "T3", "T4", "T11"));

        assertValidCounts(result, 1, 0, 0);
    }

    /**
     * Verifica que la secuencia T0-T1-T5-T6-T11 sea reconocida como un único
     * invariante de complejidad simple, sin transiciones residuales.
     */
    public void testValidSimpleInvariant() {
        ValidationResult result = new RELog().validate(
                Arrays.asList("T0", "T1", "T5", "T6", "T11"));

        assertValidCounts(result, 0, 1, 0);
    }

    /**
     * Verifica que la secuencia T0-T1-T7-T8-T9-T10-T11 sea reconocida
     * como un único invariante de complejidad alta, sin residuo.
     */
    public void testValidHighInvariant() {
        ValidationResult result = new RELog().validate(
                Arrays.asList("T0", "T1", "T7", "T8", "T9", "T10", "T11"));

        assertValidCounts(result, 0, 0, 1);
    }

    /**
     * Verifica que T10 y T11 no se interpreten como T1.
     *
     * La traza reemplaza el T1 obligatorio del camino alto por T10.
     * Aunque la cadena concatenada contiene el prefijo T1,
     * el lookahead (T1)(?!\d) debe impedir una coincidencia incorrecta.
     *
     * Se esperan cero invariantes y la traza completa como residuo.
     */
    public void testT10AndT11AreNotT1() {
        ValidationResult result = new RELog().validate(
                Arrays.asList("T0", "T10", "T7", "T8", "T9", "T10", "T11"));

        assertFalse(
                "Se esperaba un resultado inválido, pero se obtuvo: " + result.toString(),
                result.isValid());
        assertEquals(0, result.getTotalMatches());
        assertEquals(
                Arrays.asList("T0", "T10", "T7", "T8", "T9", "T10", "T11"),
                result.getRemainingTransitions());
    }

    /**
     * Comprueba que varios ciclos completos concatenados se clasifiquen por su
     * camino y que el total sea la suma de los tres tipos de invariantes.
     */
    public void testConsecutiveInvariants() {
        ValidationResult result = new RELog().validate(Arrays.asList(
                "T0", "T1", "T2", "T3", "T4", "T11",
                "T0", "T1", "T5", "T6", "T11",
                "T0", "T1", "T7", "T8", "T9", "T10", "T11"));

        assertValidCounts(result, 1, 1, 1);
    }

    /**
     * Comprueba que la reducción de un ciclo simple conserve las transiciones
     * intercaladas de un ciclo medio pendiente y pueda reducirlo después.
     */
    public void testInterleavedInvariants() {
        ValidationResult result = new RELog().validate(Arrays.asList(
                "T0", "T1", "T5",
                "T0", "T1", "T6",
                "T2", "T11", "T3", "T4", "T11"));

        assertValidCounts(result, 1, 1, 0);
    }

    /**
     * Verifica el tratamiento de un T5 adicional dentro de un camino simple.
     * El patrón debe reconocer un único ciclo T0-T1-T5-T6-T11,
     * conservar el segundo T5 capturado como interleaving y rechazar la
     * traza completa porque ese duplicado queda como transición residual.
     */
    public void testDuplicatedTransitionRemains() {
        ValidationResult result = new RELog().validate(
                Arrays.asList("T0", "T1", "T5", "T5", "T6", "T11"));

        assertFalse(
                "Se esperaba un resultado inválido, pero se obtuvo: " + result.toString(),
                result.isValid());
        assertEquals(1, result.getSimpleCount());
        assertEquals(1, result.getTotalMatches());
        assertEquals(Collections.singletonList("T5"), result.getRemainingTransitions());
    }

    /**
     * Verifica que tener todas las transiciones de un camino no sea suficiente
     * cuando T5 y T6 aparecen en un orden incompatible con el invariante.
     */
    public void testWrongOrderIsNotReduced() {
        ValidationResult result = new RELog().validate(
                Arrays.asList("T0", "T1", "T6", "T5", "T11"));

        assertFalse(
                "Se esperaba un resultado inválido, pero se obtuvo: " + result.toString(),
                result.isValid());
        assertEquals(0, result.getTotalMatches());
        assertEquals(
                Arrays.asList("T0", "T1", "T6", "T5", "T11"),
                result.getRemainingTransitions());
    }

    /**
     * Verifica que la ausencia de una transición obligatoria invalide el ciclo
     * y preserve la secuencia completa como residuo estructurado.
     */
    public void testIncompleteInvariantIsInvalid() {
        RELog validator = new RELog();

        ValidationResult result = validator.validate(
                Arrays.asList("T0", "T1", "T5", "T11"));

        assertFalse(
                "Se esperaba un resultado inválido, pero se obtuvo: " + result.toString(),
                result.isValid());
        assertEquals(0, result.getTotalMatches());
        assertEquals(4, result.getRemainingCount());
        assertEquals(
                Arrays.asList("T0", "T1", "T5", "T11"),
                result.getRemainingTransitions());
    }

    /**
     * Comprueba el estado común esperado para un resultado completamente válido.
     *
     * @param result resultado producido por el validador
     * @param expectedMedium invariantes medios esperados
     * @param expectedSimple invariantes simples esperados
     * @param expectedHigh invariantes altos esperados
     */
    private void assertValidCounts(
            ValidationResult result,
            int expectedMedium,
            int expectedSimple,
            int expectedHigh) {
        int expectedTotal = expectedMedium + expectedSimple + expectedHigh;

        assertTrue(
                "Se esperaba un resultado válido, pero se obtuvo: " + result.toString(),
                result.isValid());
        assertEquals(expectedMedium, result.getMediumCount());
        assertEquals(expectedSimple, result.getSimpleCount());
        assertEquals(expectedHigh, result.getHighCount());
        assertEquals(expectedTotal, result.getTotalMatches());
        assertEquals(0, result.getRemainingCount());
        assertTrue(result.getRemainingTransitions().isEmpty());
    }
}
