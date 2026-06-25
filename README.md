# HilandoJavaFinal: Sistema Concurrente basado en Redes de Petri Temporales

Simulación de procesamiento concurrente modelado mediante una **Red de Petri con Tiempo (RdPT)**. El sistema coordina múltiples hilos mediante un **Monitor de Concurrencia** centralizado, asegurando exclusión mutua, prevención de interbloqueos (deadlocks) y el estricto cumplimiento de los límites temporales.

Trabajo Práctico Final de **Programación Concurrente**.

---

## 1. Fundamentación Teórica

### 1.1 Modelo del Sistema: Redes de Petri
El sistema orquesta un flujo de datos a lo largo de un pipeline de procesamiento utilizando el modelo matemático de Redes de Petri.
Se emplean **12 plazas (P)** que representan estados y recursos, y **12 transiciones (T)** que modelan eventos o procesamiento de tareas. Las plazas modelan elementos clave:
- Cola de entrada y Buffer de salida.
- Bus de acceso compartido (recurso crítico).
- Unidad de Procesamiento CPU (recurso crítico con caminos de complejidad media, simple y alta).

El cálculo de disparo se basa en la **Ecuación Fundamental de Estado**:
`M(i+1) = M(i) + I × S`
Donde `M` es el marcado, `I` la matriz de incidencia y `S` el vector de disparo.

### 1.2 Redes de Petri con Tiempo (RdPT) y Prevención de Desensibilización
El sistema emplea **semántica de tiempo débil** asignando a las transiciones temporales una ventana de disparo `[α, β]` (EFT - Earliest Firing Time y LFT - Latest Firing Time).
- **EFT (α = 70 ms):** Un hilo debe retener sus tokens en las plazas de entrada y esperar este tiempo mínimo antes de poder procesar la transición.
- **LFT (β = 1000 ms):** Establece el límite máximo en el que la transición debe ser disparada. Si el tiempo supera el LFT, se produce una **desensibilización por tiempo**.

**Decisión de Diseño:** En un sistema con contención (como el planificador de hilos de Java), un LFT infinito o extremadamente corto provoca fallas estructurales. Al establecer un límite superior finito pero "suficientemente grande" ($\beta = 1000$ ms), el sistema garantiza teóricamente que los hilos tengan un marco de tiempo estricto para operar, pero lo suficientemente flexible para absorber latencias de scheduling (cambios de contexto) y prevenir la desensibilización artificial de las transiciones.

### 1.3 Monitor de Concurrencia
Para gestionar la concurrencia se implementó un **Monitor** usando mecanismos nativos de Java (`java.util.concurrent.locks`):
- **Exclusión Mutua Equitativa:** Uso de un `ReentrantLock(true)` (Fair Lock) para evitar la inanición de hilos y respetar un orden FIFO de llegada al monitor.
- **Colas de Condición Múltiples:** En lugar de sincronizar sobre el propio objeto, se utiliza un arreglo de `Condition`, uno por cada transición. Esto permite un control granular: cuando un hilo intenta disparar una transición inhabilitada, se encola en la condición de esa transición específica, evitando esperas activas ("busy waiting").
- Al ejecutarse una transición, el monitor calcula la intersección matemática entre las transiciones ahora habilitadas por marcado y los hilos encolados, despertando únicamente a aquellos que realmente pueden avanzar.

### 1.4 Verificación Estructural
- **P-Invariantes (Invariantes de Plaza):** Evaluados en tiempo real por el monitor tras cada disparo exitoso. Verifican la conservación de tokens (ej. la suma de tokens de los recursos compartidos debe ser siempre 1).
- **T-Invariantes (Invariantes de Transición):** Al concluir la ejecución, el log ordenado (generado atómicamente dentro del monitor) es procesado mediante Expresiones Regulares para validar que todas las sub-secuencias (flujos de procesamiento completos) se hayan ejecutado el número de veces requerido sin desorden.

---

## 2. Arquitectura de Hilos

El programa procesa un total de **200 invariantes**. La carga puede distribuirse en distintas configuraciones de hilos.

### Configuración Estándar (5 Hilos)
| Hilo | Secuencia de transiciones | Rol |
|---|---|---|
| `HiloEntrada` | T0, T1 | Ingreso de datos al sistema |
| `HiloMedia` | T2, T3, T4 | Procesamiento de complejidad media |
| `HiloSimple` | T5, T6 | Procesamiento de complejidad simple |
| `HiloAlta` | T7, T8, T9, T10 | Procesamiento de complejidad alta |
| `HiloSalida` | T11 | Salida de datos procesados |

### Configuración Extendida (7 Hilos)
Incrementa la concurrencia del procesamiento agregando dos trabajadores extra sobre el mismo pool de datos a procesar:
- `HiloMedia2` (T2, T3, T4)
- `HiloSimple2` (T5, T6)

*(El análisis de impacto de rendimiento de estas topologías se evalúa con métricas experimentales).*

---

## 3. Políticas de Resolución de Conflictos

Cuando múltiples hilos están esperando en la cola y sus transiciones asociadas se habilitan simultáneamente, el monitor delega la decisión a una Política:
1. **Política Aleatoria (`RandomPolicy`):** Elige aleatoriamente de la bolsa de hilos habilitados. Fomenta un comportamiento uniforme.
2. **Política Priorizada (`PriorityFiring`):** Fuerza la prioridad sobre ciertas transiciones (por ejemplo, T5 correspondiente al procesamiento de complejidad simple), alterando el throughput del sistema hacia un camino específico de la red.

---

## 4. Instrucciones de Uso

### Requisitos
- **Java** 11+
- **Maven** 3.6+

### Compilación
```bash
mvn compile
```

### Ejecutar Simulación
El argumento principal define la cantidad de hilos (`5` o `7`), seguido por la política de ejecución (`random` o `priority`).

```bash
# 5 Hilos, política aleatoria
java -cp target/classes tpfinal.Main 5 random

# 7 Hilos, política priorizada
java -cp target/classes tpfinal.Main 7 priority
```

### Salida Esperada
La salida de consola confirmará la topología elegida, realizará el análisis final con Regex y garantizará la ausencia de desensibilización y el éxito de los T-Invariantes.

```text
Politica: ALEATORIA (5 hilos)
Tiempo de ejecucion: 30555 ms
Log cargado correctamente. Transiciones: 1198
--- Conteo de transiciones ---
...
El log CUMPLE con todos los T-invariantes.
Programa finalizado. No quedan hilos activos.
```
