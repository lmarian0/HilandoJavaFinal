# HilandoJavaFinal

Simulación concurrente de una **Red de Petri Temporal** con monitor de concurrencia basado en **semáforos binarios y semántica Signal-and-Exit**, verificación automática de invariantes y políticas de decisión inyectables. Trabajo Práctico Final de **Programación Concurrente**.

---

## Descripción

El proyecto implementa un sistema multi-hilo que modela el flujo de datos a través de una Red de Petri con Tiempo (RdPT) compuesta por 12 plazas y 12 transiciones. La red representa un pipeline de procesamiento con:

- **Bus de acceso** compartido (recurso exclusivo).
- **Unidad de procesamiento (CPU)** compartida, con tres caminos de procesamiento de distinta complejidad:
  - **Media**: transiciones T2, T3, T4.
  - **Simple**: transiciones T5, T6.
  - **Alta**: transiciones T7, T8, T9, T10.
- **Cola de entrada** y **buffer de salida**.

La sincronización se realiza mediante un **Monitor** construido con semáforos binarios (`java.util.concurrent.Semaphore`) que implementa la política **Signal-and-Exit**: al señalizar a un hilo dormido, el señalizador cede el acceso exclusivo al monitor por herencia directa del mutex (sin liberarlo), otorgando preferencia absoluta al hilo señalizado sobre los hilos en la cola de entrada.

---

## Arquitectura del Monitor

El monitor utiliza dos capas de semáforos con roles diferenciados:

| Componente | Implementación | Rol |
|---|---|---|
| **Mutex del monitor** | `Semaphore(1, true)` | Exclusión mutua con cola FIFO (fairness). `permits=1` indica monitor libre, `permits=0` indica monitor ocupado. |
| **Colas de condición** | `Semaphore[N](0, true)` | Una por transición, inicializadas en 0. Los hilos se suspenden con `acquireUninterruptibly()` y solo despiertan ante un `release()` legítimo. |
| **Rastreo de ownership** | `boolean holdingMutex` | Variable local que suple la ausencia de ownership nativo en `Semaphore` (a diferencia de `ReentrantLock`). Previene double-release en el bloque `finally`. |

### Signal-and-Exit vs Mesa-style

A diferencia de los monitores Mesa-style (como los basados en `ReentrantLock` + `Condition`), donde el hilo señalizado compite nuevamente por el lock al despertar, en Signal-and-Exit:

1. El señalizador **no libera el mutex** al despertar a otro hilo (`mutex.permits` permanece en 0).
2. El hilo despertado **hereda** el acceso exclusivo directamente, sin pasar por la cola de entrada.
3. El señalizador ejecuta su cleanup local (`holdingMutex = false`, `return true`) y sale del monitor.

Esto garantiza que el hilo despertado ejecute inmediatamente, sin riesgo de que otro hilo le robe el turno.

### Blindaje contra interrupciones

Las colas de condición utilizan `acquireUninterruptibly()` para prevenir que un hilo abandone la cola prematuramente por una interrupción del SO. Esto evita:
- **Falsos positivos** en el contador `waitingCounts` (el monitor creería que hay un hilo esperando cuando ya se fue).
- **Deadlocks teóricos** donde el mutex se pierde porque el señalizador entrega el permiso a una cola vacía.

---

## Funcionalidad

| Funcionalidad | Detalle |
|---|---|
| **Modelado de Red de Petri** | Ecuación fundamental `M(i+1) = M(i) + I × S` aplicada sobre la matriz de incidencia. |
| **Monitor Signal-and-Exit** | Exclusión mutua mediante `Semaphore(1, true)` con herencia directa del mutex al señalizar. Colas de condición ininterrumpibles (`acquireUninterruptibly`). |
| **Políticas de decisión** | Selección de transiciones habilitadas: **aleatoria** (`RandomPolicy`) o **priorizada** (`PriorityFiring`, prioriza T5). |
| **Verificación de P-invariantes** | Tras cada disparo se verifica que los invariantes de plaza se mantengan (conservación de datos, bus, CPU). |
| **Verificación de T-invariantes** | Al finalizar, se analiza el log con reducción recursiva por expresiones regulares para validar la estructura de las secuencias de disparo. |
| **Logging atómico** | El registro de disparos se realiza dentro del monitor (bajo exclusión mutua) para reflejar el orden real de ejecución. |
| **Transiciones temporales** | Red de Petri con Tiempo (RdPT) bajo semántica de tiempo débil y rangos `[alpha, Long.MAX_VALUE]`. El hilo libera el mutex antes de dormir (`Thread.sleep`) y lo re-adquiere por la cola de entrada al despertar. |

---

## Objetivo

Demostrar la correcta sincronización de hilos concurrentes mediante un monitor Signal-and-Exit basado en Redes de Petri, verificando formalmente que:

1. Los **P-invariantes** (invariantes de plaza) se cumplen en todo momento durante la ejecución.
2. Los **T-invariantes** (invariantes de transición) se verifican sobre el log final mediante reducción recursiva por expresiones regulares.
3. No existen **deadlocks** ni **condiciones de carrera** gracias al diseño del monitor con semáforos binarios y herencia del mutex.
4. La **Liveness (viveza)** de la red está garantizada matemáticamente por los T-Invariantes: cada token consumido es producido por otro hilo en el ciclo, asegurando que ningún hilo se quede dormido eternamente.

---

## Requisitos

- **Java** 11 o superior (`java -version`).
- **Apache Maven** 3.6+ (`mvn -version`).

---

## Instalación

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/lmarian0/HilandoJavaFinal.git
   cd HilandoJavaFinal
   ```

2. **Compilar el proyecto**:
   ```bash
   mvn compile
   ```

3. **(Opcional) Empaquetar en JAR**:
   ```bash
   mvn package
   ```
   Genera `target/HilandoJavaFinal-1.0-SNAPSHOT.jar`.

---

## Uso

El programa ejecuta 200 invariantes completos (200 datos de entrada -> procesamiento -> salida) utilizando 5 hilos concurrentes:

| Hilo | Secuencia de transiciones | Rol |
|---|---|---|
| `HiloEntrada` | T0, T1 | Ingreso de datos al sistema |
| `HiloMedia` | T2, T3, T4 | Procesamiento de complejidad media |
| `HiloSimple` | T5, T6 | Procesamiento de complejidad simple |
| `HiloAlta` | T7, T8, T9, T10 | Procesamiento de complejidad alta |
| `HiloSalida` | T11 | Salida de datos procesados |

Al finalizar, el programa genera un archivo de log (`log_random.txt` o `log_priority.txt`) y verifica automáticamente los T-invariantes.

### Salida esperada

```
Politica: ALEATORIA
Tiempo de ejecucion: ~30000 ms
Log cargado correctamente. Transiciones: 1195
--- Invariantes de transición detectados (reducción recursiva) ---
IT1 - Complejidad media (T0-T1-T2-T3-T4-T11)    : 67
IT2 - Complejidad simple (T0-T1-T5-T6-T11)      : 69
IT3 - Complejidad alta (T0-T1-T7-T8-T9-T10-T11) : 64
Coincidencias (ciclos válidos)                  : 200
--- Resultado ---
VALIDACION DE INVARIANTES EXITOSA
Programa finalizado. No quedan hilos activos.
```

---

## Comandos

### Compilar

```bash
mvn compile
```

### Ejecutar con política aleatoria

```bash
java -cp target/classes tpfinal.Main random
```

### Ejecutar con política priorizada

```bash
java -cp target/classes tpfinal.Main priority
```

### Empaquetar y ejecutar el JAR

```bash
mvn package
java -cp target/HilandoJavaFinal-1.0-SNAPSHOT.jar tpfinal.Main random
java -cp target/HilandoJavaFinal-1.0-SNAPSHOT.jar tpfinal.Main priority
```

### Ejecutar los tests

```bash
mvn test
```

### Limpiar artefactos de compilación

```bash
mvn clean
```

---

## Validación de Logs con RELog (Testing de Invariantes)

El proyecto incluye la herramienta `RELog` que permite analizar y validar archivos de log de forma autónoma e independiente, sin necesidad de lanzar la simulación completa. Esto es ideal para realizar pruebas de regresión, simular fallos de sincronización y testear la robustez del verificador.

### Algoritmo de Validación
`RELog` implementa un algoritmo de **reducción recursiva por expresiones regulares**. Analiza el flujo buscando ciclos completos de transiciones según los T-invariantes de la red:
*   **Complejidad media**: `T0 -> T1 -> T2 -> T3 -> T4 -> T11`
*   **Complejidad simple**: `T0 -> T1 -> T5 -> T6 -> T11`
*   **Complejidad alta**: `T0 -> T1 -> T7 -> T8 -> T9 -> T10 -> T11`

Si un ciclo se completa con éxito, es extraído de la traza manteniendo intacto el orden del resto de las transiciones concurrentes (interleaving). El proceso se repite hasta que no hay más coincidencias. Si el archivo es válido, la traza se reduce por completo a vacío.

### Modos de Ejecución

Para ejecutar el validador sobre los logs generados por defecto:

```bash
# Validar el log generado por la última simulación Aleatoria (log_random.txt)
java -cp target/classes tpfinal.utils.RELog random

# Validar el log generado por la última simulación Priorizada (log_priority.txt)
java -cp target/classes tpfinal.utils.RELog priority
```

### Validación de Logs Personalizados / Alterados

Para validar cualquier archivo de log en una ruta arbitraria (por ejemplo, para testear logs donde se simulan errores manualmente):

```bash
java -cp target/classes tpfinal.utils.RELog /ruta/al/archivo_de_log.txt
```

#### Ejemplo práctico de Testing Manual (Inyección de Errores)

1. **Generar un log válido:**
   Ejecuta la simulación con `java -cp target/classes tpfinal.Main random` para obtener un `log_random.txt` correcto.
2. **Copiar y alterar el log:**
   Crea una copia de prueba y elimina manualmente alguna transición (por ejemplo, una línea que contenga `T8` o `T11`):
   ```bash
   cp log_random.txt log_prueba.txt
   # Elimina la primera ocurrencia de T11 del archivo
   sed -i '0,/^T11$/{//d}' log_prueba.txt
   ```
3. **Ejecutar la validación:**
   ```bash
   java -cp target/classes tpfinal.utils.RELog log_prueba.txt
   ```
4. **Resultado esperado de error:**
   El validador rechazará el log indicando qué invariantes logró rescatar y listando con precisión las transiciones huérfanas que no pudieron asociarse a ningún ciclo:
   ```text
   --- Invariantes de transición detectados (reducción recursiva) ---
   IT1 - Complejidad media (T0-T1-T2-T3-T4-T11)  : 65
   IT2 - Complejidad simple (T0-T1-T5-T6-T11)    : 69
   IT3 - Complejidad alta (T0-T1-T7-T8-T9-T10-T11): 65
   Coincidencias (ciclos válidos)                : 199
   --- Resultado ---
   VALIDACION DE INVARIANTES FALLIDA
   Transiciones restantes: T0T1T7T8T9T10
   ```
