# HilandoJavaFinal

Simulación concurrente de una **Red de Petri** con monitor de concurrencia, colas de condición y verificación automática de invariantes. Trabajo Práctico Final de **Programación Concurrente**.

---

## Descripción

El proyecto implementa un sistema multi-hilo que modela el flujo de datos a través de una red de Petri con 12 plazas y 12 transiciones. La red representa un pipeline de procesamiento con:

- **Bus de acceso** compartido (recurso exclusivo).
- **Unidad de procesamiento (CPU)** compartida, con tres caminos de procesamiento de distinta complejidad:
  - **Media** — transiciones T2 → T3 → T4 (75 ms c/u).
  - **Simple** — transiciones T5 → T6 (75 ms).
  - **Alta** — transiciones T7 → T8 → T9 → T10 (75 ms c/u).
- **Cola de entrada** y **buffer de salida**.

La sincronización se realiza mediante un **Monitor** basado en `ReentrantLock` con colas de condición (`Condition`), garantizando exclusión mutua y ausencia de deadlocks.

---

## Funcionalidad

| Funcionalidad | Detalle |
|---|---|
| **Modelado de Red de Petri** | Ecuación fundamental `M(i+1) = M(i) + I × S` aplicada sobre la matriz de incidencia. |
| **Monitor de concurrencia** | Control de acceso con `ReentrantLock` (fair) y colas de condición por transición. |
| **Políticas de decisión** | Selección de transiciones habilitadas: **aleatoria** (`RandomPolicy`) o **priorizada** (`PriorityFiring`, prioriza T5). |
| **Verificación de P-invariantes** | Tras cada disparo se verifica que los invariantes de plaza se mantengan (conservación de datos, bus, CPU). |
| **Verificación de T-invariantes** | Al finalizar, se analiza el log con expresiones regulares para validar la estructura de las secuencias de disparo. |
| **Logging atómico** | El registro de disparos se realiza dentro del monitor para reflejar el orden real de ejecución. |
| **Transiciones temporales** | Red de Petri con Tiempo (RdPT) bajo semántica de tiempo débil y rangos `[alpha, Long.MAX_VALUE]`. Se realiza una espera pre-disparo reteniendo los tokens en las plazas de entrada. |

---

## Objetivo

Demostrar la correcta sincronización de hilos concurrentes mediante un monitor basado en Redes de Petri, verificando formalmente que:

1. Los **P-invariantes** (invariantes de plaza) se cumplen en todo momento durante la ejecución.
2. Los **T-invariantes** (invariantes de transición) se verifican sobre el log final mediante expresiones regulares.
3. No existen **deadlocks** ni **condiciones de carrera** gracias al diseño del monitor con colas de condición.

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

El programa ejecuta 200 invariantes completos (200 datos de entrada → procesamiento → salida) utilizando 5 hilos concurrentes:

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
--- Conteo de transiciones ---
T0: 200
T1: 200
T2: 67
T3: 67
T4: 67
T5: 69
T6: 69
T7: 64
T8: 64
T9: 64
T10: 64
T11: 200
--- Invariantes de transición detectados (regex) ---
IT1 - Media  (T2,T3,T4)    : 67
IT2 - Simple (T5,T6)       : 69
IT3 - Alta   (T7,T8,T9,T10): 64
Total de invariantes       : 200
--- Resultado ---
El log CUMPLE con todos los T-invariantes.
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
