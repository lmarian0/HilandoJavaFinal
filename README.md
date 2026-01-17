# HilandoJavaFinal - Sistema de Red de Petri con Monitor de Concurrencia

## Descripción

Este proyecto implementa un sistema de Red de Petri con un monitor de concurrencia para controlar el acceso concurrente a recursos compartidos. El sistema simula el procesamiento de trabajos con diferentes niveles de complejidad (Media, Simple y Alta) utilizando hilos concurrentes.

## Arquitectura

El sistema sigue el diagrama de clases proporcionado e implementa el patrón de diseño **Strategy** para las políticas de selección de transiciones.

### Componentes Principales

#### 1. **MonitorInterface**
Interfaz que define el contrato para el monitor de concurrencia.
- `fireTransition(int transition)`: Dispara una transición en la red

#### 2. **Monitor**
Implementación del monitor de concurrencia que garantiza:
- Exclusión mutua mediante `ReentrantLock`
- Sincronización mediante variables de condición (`Condition`)
- Política de selección de transiciones configurable

#### 3. **PetriNet**
Representa la lógica matemática de la Red de Petri:
- Matriz de incidencia (12 plazas × 12 transiciones)
- Marcado actual y operaciones de disparo
- Tiempos de procesamiento asociados a cada transición

#### 4. **Policy** (Interfaz) y sus implementaciones
- **RandomPolicy**: Selección aleatoria de transiciones sensibilizadas
- **PriorityPolicy**: Selección por prioridad (menor índice = mayor prioridad)

#### 5. **SegmentThread**
Hilos que ejecutan secuencias específicas de transiciones:
- **Proceso Media**: T0 → T1 → T2 → T3 → T4 → T11
- **Proceso Simple**: T0 → T1 → T5 → T6 → T11
- **Proceso Alta**: T0 → T1 → T7 → T8 → T9 → T10 → T11

#### 6. **Log**
Sistema de registro que almacena todas las transiciones disparadas para posterior verificación.

#### 7. **RELog**
Verificador de invariantes que valida el log contra las reglas del sistema:
- Verifica que T0, T1 y T11 aparezcan la misma cantidad de veces
- Valida la consistencia de las secuencias de procesamiento

## Compilación y Ejecución

### Compilar
```bash
javac *.java
```

### Ejecutar
```bash
java Main
```

## Resultados de Ejecución

El sistema ejecuta y reporta:

1. **Tiempo de ejecución**: Debe estar entre 20-40 segundos
2. **Marcado final**: Debe coincidir con el marcado inicial [3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0]
3. **Verificación de invariante**: Valida que todas las secuencias se ejecutaron correctamente

### Ejemplo de Salida

```
========== INICIO DE SIMULACIÓN ==========
Red de Petri creada
Marcado inicial: Marcado: [3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0]
Política seleccionada: RandomPolicy
Monitor creado

========== INICIANDO HILOS ==========
[...Hilos iniciados...]

========== TODOS LOS HILOS FINALIZADOS ==========
Log escrito correctamente en: log.txt
Marcado final: Marcado: [3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0]

========== RESULTADOS ==========
Tiempo de ejecución: 33764 ms (33,76 segundos)
✅ Tiempo válido (entre 20s y 40s)

========== VERIFICACIÓN DE INVARIANTE ==========
Conteo de transiciones:
  T0: 180 veces
  T1: 180 veces
  [...]
  T11: 180 veces
✅ El invariante se cumple correctamente
```

## Configuración

Para ajustar el comportamiento del sistema:

1. **Cambiar la política**: En `Main.java`, línea 25
   ```java
   Policy policy = new RandomPolicy(); // o new PriorityPolicy()
   ```

2. **Ajustar repeticiones**: En `Main.java`, línea 31
   ```java
   int repetitions = 20; // Aumentar o disminuir según necesidad
   ```

3. **Modificar tiempos de procesamiento**: En `PetriNet.java`, método `initializeTimes()`

## Verificación de Invariantes

El sistema verifica que:
- Cada proceso comienza con T0 (Arribo)
- Luego toma el bus con T1
- Ejecuta su secuencia de procesamiento específica
- Finaliza con T11 (Salida)
- El número de arribos = número de tomas de bus = número de salidas

## Estructura de la Red de Petri

- **P0**: Cola de entrada (3 tokens iniciales)
- **P2**: Bus disponible (1 token inicial)
- **P6**: CPU disponible (1 token inicial)
- **P11**: Cola de salida

El sistema garantiza que los recursos compartidos (Bus y CPU) sean accedidos de forma segura mediante el monitor de concurrencia.

## Autor

Sistema desarrollado siguiendo las especificaciones del diagrama de clases UML proporcionado.
