# Monitor con politica Signal and Exit

## Objetivo

Este documento técnico describe de forma detallada todos los cambios introducidos durante la migración de la arquitectura del Monitor de concurrencia de la Red de Petri. Se detalla la transición desde la implementación original basada en `ReentrantLock` + `Condition` hacia la nueva implementación basada en Semáforos Binarios.
El objetivo es fundamentar el razonamiento técnico detrás de cada decisión de diseño, explicando cómo se logró la herencia estricta del mutex y el impacto que esto tuvo sobre la fiabilidad y el determinismo del sistema concurrente.

---

## Definición de Conceptos Previos

Para comprender el refactor, es vital definir cómo se manejaba la política de señalización antes y después:

1. **La Política Vieja (Signal and Exit sin preferencia por el señalizado):**
   En la implementación original con `ReentrantLock(true)`, la exclusión mutua ya contaba con equidad (fairness), por lo que el orden de llegada a la cola principal se respetaba. Sin embargo, al llamar a `Condition.await()`, el hilo se dormía en su cola de condición específica liberando el lock principal. Cuando otro hilo ejecutaba `Condition.signal()`, el hilo dormido era movido de la cola de condición de vuelta a la cola de entrada del `ReentrantLock`. Como el hilo señalizador estaba obligado a liberar el cerrojo principal al salir del monitor, no había garantías de que el cerrojo fuese adquirido inmediatamente por el hilo despertado.

2. **La Política Nueva (Signal and Exit con herencia de mutex):**
   En esta implementación, cuando un hilo señaliza a otro que está dormido, le transfiere el acceso exclusivo al monitor de forma instantánea. El hilo señalizador abandona el monitor **sin liberar el cerrojo principal (el semáforo `mutex` se mantiene con 0 permisos)**. 
   Las colas de condición son ahora semáforos binarios inicializados en 0, por lo que cualquier `acquire()` bloquea al hilo instantáneamente. Al ser despertado mediante un `release()` dirigido, el hilo sale de su cola de condición y, dado que el `mutex` del monitor sigue en 0 permisos, ningún hilo de la cola de entrada principal puede ingresar. El hilo despertado "hereda" el acceso y obtiene preferencia absoluta asegurada.

---

## 1. Reemplazo del Mecanismo de Exclusión Mutua (Mutex)

### 1.1. Descripción del cambio
Se reemplazó el uso de la clase `ReentrantLock` por un `Semaphore(1, true)` (semáforo binario equitativo) en `Monitor.java`.

### 1.2. Justificación técnica
La clase `ReentrantLock` de Java impone una restricción estructural inquebrantable llamada **"Ownership" (Posesión)**: el mismo hilo que adquiere el cerrojo es el único autorizado para liberarlo. 
Para implementar la herencia de mutex, es un requisito teórico que el hilo que señaliza le "transfiera" el acceso al hilo despertado. Como `ReentrantLock` exige que el señalizador libere su propio lock, la transferencia directa es imposible. 
El `Semaphore` en Java, por el contrario, no tiene concepto de ownership: cualquier hilo puede adquirir permisos y cualquier otro hilo puede liberarlos. Esto es lo que permite que un hilo despierte a otro y le ceda el control de la estructura sin abrir la puerta principal.

### 1.3. Impacto del cambio
- **Determinismo y Efecto Ping-Pong:** Si bien ambas implementaciones usan colas justas (fairness), la herencia del mutex garantiza que el hilo despertado ejecute inmediatamente sin competir en la cola de entrada, generando un entrelazado perfecto y predecible.

  En el modo de política priorizada, donde se da preferencia a las transiciones de `HiloSimple` (T5) sobre las de `HiloMedia` (T2) y `HiloAlta` (T7), se genera un fenómeno emergente coordinado por la exclusividad de la plaza `P6` (procesador físico con 1 token inicial) y el contador global de 200 iteraciones (`counter.getAndIncrement()`). El ciclo opera a través de una alternancia estricta descrita en la siguiente secuencia de eventos:

  1. **Finalización de `HiloSimple`:** `HiloSimple` tiene el procesador (`P6`). Al disparar `T6`, el token vuelve a `P6` y se produce un token en `P11` (buffer de salida). Al evaluar qué transiciones despertar, como la plaza `P3` (buffer de entrada) se encuentra temporalmente vacía, las transiciones de procesamiento `T2`, `T5` y `T7` **no están habilitadas por marcado (no sensibilizadas)**. La única transición habilitada en toda la red que posee un hilo esperando en su cola de condición es `T11` (HiloSalida). Por ende, la intersección contiene únicamente a `T11` y el monitor está obligado a cederle el mutex a `HiloSalida`.
  2. **Traspaso de mutex por herencia:** `HiloSimple` despierta a `HiloSalida` (T11) mediante Signal-and-Exit y sale de `fireTransition()`. `HiloSimple` vuelve a su bucle de ejecución, pide un nuevo ticket del contador global y llama a `fireTransition(T5)`. Sin embargo, dado que `HiloSalida` heredó el mutex del monitor, `HiloSimple` **se bloquea en la cola de entrada del monitor** (`mutex.acquire()`).
  3. **Cadena de relevos inmediatos:** `HiloSalida` consume `P11`, deposita un token en `P0` y cede por herencia el mutex a `HiloEntrada` (T0). Esto es así porque, al inyectar el token en `P0` estando el bus `P2` con token, la única transición habilitada y con hilos esperando es `T0`. `HiloEntrada` consume `P0` y `P2`, deposita en `P1` e intenta disparar `T1` (transición temporal con `alpha=75ms`). Al no haberse cumplido el tiempo mínimo, `HiloEntrada` libera el mutex principal del monitor y se duerme fuera de él.
  4. **Encolado de `HiloSimple`:** Al liberarse el mutex del monitor, `HiloSimple` (que esperaba en la cola de entrada) ingresa, evalúa `T5` y detecta que `P6` está con token pero `P3` no tiene tokens (pues `HiloEntrada` está durmiendo). `HiloSimple` se suspende en la cola de condición de `T5`. **Ahora `T5` figura como un hilo esperando.**
  5. **El Rebote (Handover de retorno):** 75ms después, `HiloEntrada` despierta de su espera temporal, re-adquiere el mutex, dispara `T1` y deposita el token en `P3`. En este instante, con `P6` y `P3` con un token, las transiciones `T2`, `T5` y `T7` se encuentran **simultáneamente habilitadas por marcado (sensibilizadas)**. Como los hilos correspondientes están durmiendo en sus colas de condición, la intersección contiene a `[T2, T5, T7]`. Aquí entra en juego la política priorizada, la cual elige a `T5` (Simple) de forma absoluta. `HiloSimple` hereda el mutex, consume `P6` y `P3`, iniciando su ejecución (`InvSimple`).

  **Alternancia de Invariantes (Ping-Pong Simple <-> Medio/Alto):**
  * **Hacia InvMedio / InvAlto:** Cuando `HiloSimple` finaliza su ejecución al disparar `T6` (devolviendo el token a `P6` y depositándolo en `P11`), si la plaza `P3` posee tokens acumulados (de ciclos previos), las transiciones `T2` (HiloMedia) y `T7` (HiloAlta) quedan habilitadas por marcado (sensibilizadas) en paralelo con `T11` (HiloSalida). Como `HiloSimple` no está en su cola de condición y debe encolarse afuera en la cola de entrada del mutex (`mutex.acquire()`), el monitor tiene múltiples puntos de ramificación donde puede delegar la exclusión mutua a los hilos de procesamiento de mayor complejidad:
    1. **En la resolución de T6:** El monitor evalúa la intersección `m = [T11, T2, T7]` y, por política aleatoria (al no estar `T5` presente en las colas de condición), puede elegir despertar a `HiloMedia` (T2) o `HiloAlta` (T7) de forma directa, puenteando a `HiloSimple` y entregándoles el mutex por herencia.
    2. **En la resolución de T11:** Si se seleccionó `HiloSalida` (T11), este consume `P11` y deposita en `P0`. Al finalizar, el monitor evalúa `m = [T0, T2, T7]` y puede elegir despertar de forma aleatoria a `HiloMedia` (T2) o `HiloAlta` (T7) en lugar de a `HiloEntrada` (T0).
    3. **En la resolución de T0:** Si se seleccionó `HiloEntrada` (T0), este consume `P0` y `P2`, depositando en `P1`. Al finalizar `T0`, al no estar `T1` en colas de condición, el monitor evalúa `m = [T2, T7]` y puede delegar el mutex a `HiloMedia` o `HiloAlta`.
    4. **Al liberar el mutex en T1:** Si `HiloEntrada` llega a iniciar `T1` y debe liberar el mutex para dormirse en su espera temporal de 75ms, el mutex queda libre en el monitor. Si `HiloSimple` aún no ha reingresado de la cola de entrada, cualquier otro hilo que intente ingresar puede obtener el control.
  * **Retorno a InvSimple:** Mientras `HiloMedia` (o `HiloAlta`) ejecuta sus etapas internas (T3-T4, o T8-T9-T10) y retiene el procesador `P6` (0 tokens), `HiloSimple` ingresa al monitor por la cola de entrada. Al evaluar `T5` y ver el procesador ocupado, se suspende en su cola de condición de `T5`, liberando el mutex. Tan pronto como `HiloMedia`/`HiloAlta` finaliza su última transición (T4 o T10), devuelve el token a `P6` y produce en `P11`. Dependiendo del estado de la plaza `P3` (si tiene tokens acumulados o está vacía), el retorno se realiza por uno de dos caminos deterministas:
    - **Camino A (Despertar Directo por prioridad):** Si la plaza `P3` ya posee un token acumulado, al devolverse el token a `P6`, la transición `T5` queda habilitada por marcado de forma inmediata. Al estar `HiloSimple` durmiendo en su cola de condición, `T5` entra en la intersección `m = [T11, T2, T7, T5]`. Debido a la política prioritaria, el monitor selecciona a `T5` de forma absoluta y despierta directamente a `HiloSimple` cediéndole el mutex por herencia, puenteando a todos los demás hilos (incluido `HiloSalida`).
    - **Camino B (Despertar en Cascada):** Si la plaza `P3` está vacía, al devolverse el token a `P6`, `T5` no se habilita por marcado. Por ende, la intersección sólo contiene a `m = [T11]`, lo que obliga al monitor a despertar a `HiloSalida` (T11). `HiloSalida` consume `P11`, deposita en `P0` y cede el mutex a `HiloEntrada` (T0). `HiloEntrada` consume `P0` y `P2`, deposita en `P1` e intenta disparar `T1`. Al ser temporal, libera el mutex y se duerme por 75ms. Al despertar, re-adquiere el mutex, dispara `T1` y deposita el token en `P3`. En este instante, con `P6` y `P3` habilitados, `T5` se sensibiliza. Dado que `HiloSimple` ya esperaba en la cola de condición de `T5`, la intersección es `m = [T2, T5, T7]`. La política priorizada elige a `T5` de forma absoluta y `HiloEntrada` le cede el mutex por herencia a `HiloSimple`, retornando a `InvSimple`.

### 4. Comparación con la implementación anterior
En la implementación anterior el traspaso dependía de re-adquirir el lock compitiendo en la cola de entrada. En la versión nueva, el hilo señalizado toma posesión inmediata.
**¿Por qué esto es seguro?** Al analizar la ejecución a nivel de los **stack frames** de los hilos: el hilo que señaliza ejecuta el `release` sobre la cola de condición del hilo despertado, e inmediatamente deja de interactuar con el Heap (donde residen los datos compartidos: la matriz de estado `PetriNet.currentMarking`, el array de contadores `Queue.waitingCounts`, el array de semáforos `Queue.condSemaphores`, el buffer estático del `Logger` y el estado interno del propio `Semaphore mutex`). 
Sus únicas instrucciones restantes son sobre su propio Stack local y privado (`holdingMutex = false` y `return true`). El hilo despertado hereda el mutex y retoma su ejecución manipulando el Heap de forma totalmente segura, garantizando que nunca existirá superposición ni colisión en la memoria compartida.

---

## 2. Reemplazo de las Colas de Condición

### 2.1. Descripción del cambio
Se eliminó la dependencia de `Condition`, reemplazándolas por un array de semáforos binarios inicializados en 0 (`Semaphore(0, true)`) en `Queue.java`.

### 2.2. Justificación técnica
Las variables `Condition` nativas automatizaban la suspensión y liberación del lock de forma fuertemente acoplada a su `ReentrantLock` creador. Para lograr la herencia de mutex, necesitábamos desacoplar ambas acciones por completo. Un semáforo inicializado en 0 actúa como una barrera restrictiva pura: bloquea inmediatamente a cualquier hilo que haga `acquire()`, emulando una cola de espera donde podemos decidir con precisión matemática a quién despertar.

### 2.3. Impacto del cambio
- Control absoluto y explícito sobre el ciclo de suspensión. La semántica de los permisos (1 para libre, 0 para bloqueado) refleja directamente el estado físico del monitor.

### 2.4. Comparación con la implementación anterior
En la implementación anterior en `Queue.java`, `conditions[i].await()` liberaba el lock y dormía al hilo internamente. En la nueva arquitectura, el hilo primero libera explícitamente la entrada principal (`mutex.release()`) e inmediatamente después se suspende a sí mismo llamando a `condSemaphores[i].acquireUninterruptibly()`, dejando al monitor listo para la herencia del mutex.

---

## 3. Rastreo Manual de la Posesión del Mutex (Double-Release Prevention)

### 3.1. Descripción del cambio
Se introdujo una variable lógica de estado `boolean holdingMutex = true` en `Monitor.fireTransition()`. Si el hilo cede el control por herencia o falla en una readquisición temporal, la variable pasa a `false`. El bloque `finally` evalúa esta variable para decidir si debe ejecutar `mutex.release()`.

### 3.2. Justificación técnica
Al usar `Semaphore`, perdimos el método nativo `lock.isHeldByCurrentThread()`. Cuando un hilo cede el monitor por herencia, abandona prematuramente el método `fireTransition()`. Si el bloque `finally` ejecutara `mutex.release()` a ciegas (efectuando un double-release), inyectaría un permiso extra al semáforo principal (llevándolo a 2 permisos). 
Esto rompería instantáneamente la exclusión mutua: permitiría que el hilo despertado internamente y un nuevo hilo externo desde la cola de entrada accedan al mismo tiempo al Heap. El acceso simultáneo a la matriz de marcado de la Red de Petri y a los contadores de hilos (`waitingCounts`) generaría condiciones de carrera y un estado corrupto irrecuperable.

### 3.3. Impacto del cambio
- **Prevención de colisiones:** Asegura matemáticamente que el semáforo principal del monitor solo oscile entre 0 (ocupado) y 1 (libre), manteniendo la barrera de exclusión mutua dentro del monitor.

### 3.4. Comparación con la implementación anterior
Antes, el `ReentrantLock` internamente rastreaba qué hilo era el propietario del cerrojo y solo le permitía a ese hilo liberarlo. Este mecanismo impedía automáticamente que un hilo ajeno corrompiera el estado del lock. Al migrar a `Semaphore`, donde no existe el concepto de propietario, cualquier hilo puede ejecutar `release()` e inyectar permisos sin restricciones. El rastro de estado `holdingMutex` cumple la función de suplir esa validación ausente, asegurando que solo se libere el mutex cuando el hilo efectivamente lo posee.

---

## 4. Blindaje contra Interrupciones Teóricas en la Cola (acquireUninterruptibly)

### 4.1. Descripción del cambio
En `Queue.java`, se reemplazó la espera clásica que capturaba `InterruptedException` por una llamada ininterrumpible: `condSemaphores[transition].acquireUninterruptibly()`.

### 4.2. Justificación técnica
El comportamiento de una interrupción sin blindaje en un hilo suspendido en la cola de condición es altamente destructivo para la consistencia del monitor:
1. Si el hilo es interrumpido en `condSemaphores[transition].acquire()`, se lanza `InterruptedException` y el hilo entra al bloque `catch` sin poseer el mutex.
2. Aunque el bloque `finally` de Java se ejecuta obligatoriamente y decrementa el contador `waitingCounts[transition]--`, esta modificación del Heap compartido se realiza **sin poseer el mutex del monitor**, lo que genera una condición de carrera inmediata sobre la contabilidad de hilos en espera.
3. El hilo retorna a `Monitor.fireTransition()` sin exclusión mutua y re-evalúa el bucle `while(true)`. Si en ese momento otro hilo posee el mutex, ambos hilos leerán y escribirán la red de Petri de forma concurrente, corrompiendo el estado de marcado.
4. Si intentáramos mitigar esto llamando a `mutex.acquire()` dentro del `catch` para asegurar que el hilo solo salga del método con el mutex en su poder, una **segunda interrupción** durante esta adquisición abortaría la espera. El hilo decrementaría de todos modos el contador en `finally` (nuevamente sin exclusión mutua) y saldría al cuerpo del monitor sin el mutex.

Al no estar garantizada la posesión del mutex en todo momento tras despertar (debido al flujo irregular de la interrupción), la contabilidad de las colas y la exclusión mutua del monitor quedan expuestas a fallos de sincronización y potenciales deadlocks.

### 4.3. Impacto del cambio
- **Robustez Extrema:** El método `acquireUninterruptibly()` vuelve al hilo sordo ante las interrupciones mientras está dormido. El hilo se ancla a la cola y se niega a abandonarla hasta que recibe un `release()` legítimo. Las interrupciones son retenidas en el hilo pero no perturban la estricta contabilidad del monitor.

### 4.4. Comparación con la implementación anterior
En la implementación anterior con `Condition.await()`, la clase interna de sincronización de Java (`AbstractQueuedSynchronizer` o AQS, que es la infraestructura común sobre la cual se implementan `ReentrantLock` y `Semaphore`) gestiona la interrupción de forma transparente: al interrumpirse el hilo, lo mueve automáticamente de la cola de condición a la cola de entrada del lock. El hilo espera allí hasta adquirir el lock y, recién cuando lo posee, se lanza la excepción. Esto garantizaba que el bloque `finally` decrementara `waitingCounts` de manera consistente bajo exclusión mutua.

Si intentáramos replicar este comportamiento de forma interruptible usando semáforos, el flujo sería vulnerable a fallos graves de concurrencia:
1. Al interrumpirse la espera en `condSemaphores[transition].acquire()`, el hilo despertaría sin el mutex.
2. Para no violar el contrato del monitor (que exige poseer la exclusión mutua al retornar de la cola), el bloque `catch` debería llamar manualmente a `mutex.acquire()`. Esto encolaría al hilo en la cola de entrada del semáforo `mutex` del monitor.
3. Si ocurriera una **segunda interrupción** mientras el hilo espera en esta cola de entrada (`mutex.acquire()`), el hilo abortaría la espera definitivamente. Esto provocaría que el bloque `finally` decrementara `waitingCounts` sin poseer el mutex (corrompiendo el Heap) y que el hilo retornara a `Monitor.fireTransition()` sin exclusión mutua, rompiendo por completo la seguridad del monitor.

Al utilizar `acquireUninterruptibly()`, se elimina este estado intermedio de riesgo: el hilo se mantiene bloqueado en la cola de condición ignorando cualquier interrupción hasta recibir un `release()` legítimo. De esta forma, se garantiza la consistencia del contador y de la exclusión mutua sin depender de re-adquisiciones manuales propensas a fallas en la cola de entrada del mutex.

---

## Conclusiones

La migración hacia una arquitectura basada en Semáforos Binarios consolidó una semántica **Signal-and-Exit con herencia estricta**, superando las limitaciones impuestas por el *Ownership* del `ReentrantLock`.

Las principales mejoras de este refactor son:

1. **Determinismo y Herencia:** El hilo señalizador transfiere el acceso exclusivo de forma directa al hilo dormido (manteniendo el mutex en 0). Esto fomenta la creación de patrones de alternancia altamente eficientes (como el Ping-Pong estricto entre `HiloSimple`, `HiloMedia` e `HiloAlta`) al eliminar por completo la necesidad de competir nuevamente por la exclusión mutua.
2. **Inmunidad a Fallos Estructurales:** Gracias a la introducción de defensas explícitas (el tracking riguroso del Heap vs Stack frames con `holdingMutex` y las colas blindadas con `acquireUninterruptibly`), el sistema es invulnerable a inyecciones accidentales de permisos y a deadlocks derivados de desincronizaciones por interrupción del sistema.
3. **Garantía Formal de Liveness (Viveza):** La propiedad de liveness no depende del azar, sino que está matemáticamente garantizada por la topología de la Red de Petri a través de sus **T-Invariantes**. Un invariante de transición define una secuencia cerrada de disparos que devuelve la red a su marcado inicial (**T0T1T5T6T11** para complejidad simple, **T0T1T2T3T4T11** para complejidad media, **T0T1T7T8T9T10T11** para complejidad alta). Esto implica que por cada token que un hilo consume (y por el cual se duerme), existe matemáticamente otro hilo en la misma secuencia cíclica obligado a producirlo. 
   Dado que el programa está configurado para ejecutar exactamente 200 invariantes completos, al finalizar la ejecución la red retorna a su marcado inicial: no sobran tokens, no faltan tokens, y las colas de condición quedan vacías. Ningún hilo queda durmiendo esperando un `release()` que nunca llegará, porque todos los `release()` correspondientes fueron gatillados por la producción de tokens de los invariantes. Es formalmente imposible que un hilo sufra de inanición (starvation) permanente, validando la detención limpia y el funcionamiento ininterrumpido del sistema.