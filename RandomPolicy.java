import java.util.List;
import java.util.Random;

/**
 * Política que selecciona una transición aleatoria entre las sensibilizadas.
 */
public class RandomPolicy implements Policy {
    private Random random;

    public RandomPolicy() {
        this.random = new Random();
    }

    @Override
    public int decide(List<Integer> sensitized) {
        if (sensitized == null || sensitized.isEmpty()) {
            throw new IllegalArgumentException("No hay transiciones sensibilizadas para elegir");
        }
        int index = random.nextInt(sensitized.size());
        return sensitized.get(index);
    }
}
