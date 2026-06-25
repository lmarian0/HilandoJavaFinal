# Análisis de Rendimiento: 5 vs 7 Hilos

Este documento recopila los resultados experimentales obtenidos al comparar el rendimiento del monitor de concurrencia y la red de Petri ante diferentes topologías de hilos.

## Objetivo
Evaluar si el incremento de hilos concurrentes procesando tareas (transiciones temporales) reduce el tiempo global de procesamiento (gain de performance) o si la penalización por contención en el monitor (`ReentrantLock`) y cambios de contexto ("context switching") termina reduciendo el rendimiento.

## Condiciones de Prueba
- **Invariantes a procesar:** 200 en total (200 ingresos T0 y 200 egresos T11).
- **Semántica Temporal:** Ventanas de tiempo activas (EFT=70ms, LFT=1000ms).
- **Política de Conflictos:** Aleatoria (`random`).
- **Validación:** Comprobación estricta de T-invariantes mediante expresiones regulares en el Log final.

## Configuraciones

### Escenario A: 5 Hilos (Baseline)
- Hilos: Entrada, Salida, Media, Simple, Alta.
- **Tiempo de ejecución:** `2465 ms`

*Distribución de la carga de trabajo observada en las ramas internas:*
- Media (T2, T3, T4): 50
- Simple (T5, T6): 75
- Alta (T7, T8, T9, T10): 75
- *Total ramas: 200*

### Escenario B: 7 Hilos
- Hilos: Entrada, Salida, Media, Simple, Alta, Media2, Simple2.
- Se añadió redundancia en las ramas Media y Simple para observar cómo responde el pool de recursos matemáticos (las plazas de la CPU P8).
- **Tiempo de ejecución:** `2064 ms`

*Distribución de la carga de trabajo observada en las ramas internas:*
- Media (T2, T3, T4): 69
- Simple (T5, T6): 74
- Alta (T7, T8, T9, T10): 57
- *Total ramas: 200*

## Conclusión
Se observa una **ganancia de rendimiento del ~16%** al incorporar 2 hilos extra en las etapas de procesamiento medio y simple. 
- Al haber más hilos compitiendo por obtener los tokens de P8 (CPU disponible), el sistema nunca se queda "ocioso" cuando un hilo se bloquea temporalmente por los EFT, reduciendo el cuello de botella.
- Curiosamente, al inyectar más hilos en los caminos Simple y Media, el camino de complejidad Alta se ejecutó menos veces (de 75 a 57 iteraciones), demostrando cómo los hilos extra ganan la carrera en la disputa por el token central (y alteran la distribución estocástica natural de la política Random).
- La arquitectura del **Monitor con Fairness** previno cualquier interbloqueo o desensibilización por tiempo, validando que la solución matemática de la red de Petri es altamente escalable.
