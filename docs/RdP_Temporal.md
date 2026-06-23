# Redes de Petri tiempo real
## MODELADO E IMPLEMENTACIÓN DE SISTEMAS DE TIEMPO REAL MEDIANTE REDES DE PETRI CON TIEMPO

### Formas de especificar el tiempo asociado a las transiciones

**Redes de Petri con Delay:**
- El tiempo asociado al disparo de una transición
- Cada transición modela el final de una actividad (representará la duración de la actividad)
- Se puede hacer explicita esta dependencia especificando los delay de las transiciones mediante expresiones en funciones de timestamps o colores asociados a las marca

**Redes de Petri con tiempo**
- Se especifica el tiempo de disparo de una transición mediante la asociación de un intervalo que abarque todas las posibilidades de duración de la actividad.
- Esto permite le análisis del peor caso, o el calculo de limites de tiempo de ejecución.

### Semántica de tiempo débil y fuerte

**Semántica de tiempo fuerte (strong time semantic):**
- Se debe indicar específicamente en que momento y con que condiciones se debe realizar el disparo
- Es apropiada para el modelado de sistemas de tiempo Real

**Semántica de tempo débil (weak time semantic):**
- La transición no esta obligada a disparar, pero si lo hace, debe ser en el intervalo especiado
- Esta semántica puede ser útil cuando se trata de modelar una acciones que puede ocurrir únicamente durante un periodo de tiempo, pero que no necesariamente debe ocurrir.
- Sin embargo, esta ultima puede ser modelada, de acuerdo a la semántica fuerte, con una transición en
conflicto que aborte la posibilidad de ejecución. Esta
ultima alternativa, además, es mas clara.

# Redes de Petri con Tiempo (RdPT)
 
## Definición formal
 
> Una Red de Petri con Tiempo (RdPT), N, es una tupla
> **N {P; T; Pre, Post; Mo; CIS}**
 
### donde:
 
- **P** es un conjunto finito y no vacío de **lugares**
- **T** es un conjunto finito y no vacío de **transiciones**
- **P ∩ T = ∅**
- **Pre** : *P×T* → **N** es la función de incidencia previa, donde **N** es el conjunto de números naturales
- **Post** : *P×T* → **N** es la función de incidencia post, donde **N** es el conjunto de números naturales
- **Mo** es la función de marcado inicial **Mo : P → N**
- **CIS** es una correspondencia de intervalos estáticos, **CIS : T → Q⁺×(Q⁺ ∪ ∞)**, donde Q⁺ es el conjunto de los números racionales positivos junto con el cero
---
 
La última función asocia a cada transición un par **CIS(tᵢ) = (αᵢ, βᵢ)**, que define un intervalo temporal, por lo que se debe verificar:
 
$$0 \leq \alpha_i < \infty \;,\; 0 \leq \beta_i \leq \infty \;\text{ y }\; \alpha_i \leq \beta_i \;\text{ si }\; \beta_i \neq \infty \;\text{ o }\; \alpha_i < \beta_i \;\text{ si }\; \beta_i = \infty$$

# Regla de disparo

## Este intervalo permite enunciar la regla:

- Suponiendo que la transición *ti* comienza a estar sensibilizada en el instante *wi*, y que continua sensibilizada,
  - el disparo de la transición se producirá no
    - antes del instante wi+*αi*,
    - y no mas tarde del instante wi+*βi*.
  - El intervalo de tiempos de disparo válidos para *ti* será, por tanto
    - [wi+*αi*, wi+*βi*].

---

- ❖ La semántica de este disparo es del tipo de **tiempo débil**, el disparo se puede producir durante el intervalo de tiempo.
- ❖ Las marcas permanecen en los lugares de entrada durante el tiempo necesario, y una vez que se produce el disparo, este no consume tiempo: **es instantáneo**. 

# Casos Particulares de Disparo

## Intervalo estático de disparo [αᵢ, βᵢ]

- Al valor **αᵢ** se le llama **instante de disparo más cercano (EFT)** — *earliest firing time*
- Al valor **βᵢ** se le llama **instante de disparo más lejano (LFT)** — *latest firing time*

---

## Intervalo puntual [αᵢ, αᵢ]

- Transiciones de tiempo de sensibilización fijo

---

## Intervalo sin restricción temporal [0, ∞]

- Formalismo autónomo de redes de Petri
- Por conveniencia no suele representarse explícitamente

# Estado de una Red de Petri Temporal

- Para decidir sobre las posibilidades de disparo debemos tener en cuenta dos aspectos:
  - La sensibilización de una transición
  - El tiempo que ha transcurrido desde que ha sido sensibilizada

- Por lo cual se define al estado como
  - **S = (M, I)**, donde
  - **M** es el marcado e
  - **I** es un vector de todos los posibles intervalos de disparo de todas las transiciones sensibilizadas por el marcado M.