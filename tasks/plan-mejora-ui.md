# Gradify v3.0 – Mega actualización

> Creado 2026-11-26. Base: comparación entre v2.1.1 (`f956536`) y `main` (`149d09a`).
> **Idea:** recuperar el color y la comodidad de la v2.1, mantener el orden del sistema de diseño nuevo y sumar todo lo pendiente del ROADMAP en una sola versión grande.
> **Regla de diseño:** ni gris plano ni carnaval. El color siempre dice algo (marca, estado, materia, tipo de evento); los fondos donde se lee mucho siguen tranquilos. Contraste AA en claro y en oscuro.
> **Verificación tras cada bloque** (JDK 21, nunca 25): `JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew testDebugUnitTest lintDebug assembleDebug`, y revisarlo en el emulador en claro, en oscuro y con Material You.
> Un commit por bloque. Los bloques marcados con ⚠ cambian la base de datos (migración Room v8, todos juntos en una sola migración).

---

## Diagnóstico de partida

| Qué cambió | v2.1.1 | Ahora | Efecto |
|---|---|---|---|
| Material You | Activo por defecto | Apagado por defecto (`UserPreferencesRepository.kt:76`) | Se pierden los colores del fondo de pantalla |
| Tarjetas | `primaryContainer`, `errorContainer`, banners de color | Todo `surfaceContainerLow` (`SurfaceCard`) | Todo se ve gris |
| Estados | Banner con fondo de color (`ResultBanner`) | Punto de 8 dp + texto (`StatusLabel`, `ResultCard`) | El estado casi no se nota |
| Resumen en Inicio | Tarjeta hero en `primaryContainer` | Superficie neutra | Inicio sin punto focal |
| Nota meta | Campo decimal | **Roto** desde `a018605`: abría directo "¿Quitar meta?" | No se podía poner meta |
| Aprobación y porcentajes | Slider | Slider de 100 pasos | Difícil acertar 3.0 o 33 % con el dedo |

---

## Bloque A – Regresiones ✅

- [x] **A1** `MetaDialog` (`DetailMeta.kt`): vuelve el campo decimal (coma o punto, error si pasa del máximo, vacío = sin meta, "Quitar meta" con confirmación).
- [x] **A2** Revisados los demás `GradifyDialog` (Calendario, Ajustes, Renombrar, Notas): bien.

## Bloque B – Escribir en vez de deslizar ✅

- [x] **B1** `ui/components/DecimalField.kt`: campo decimal compartido; `parseGrade`, `toInputString`, `GRADE_INPUT` e `isGradeInvalid` movidos ahí.
- [x] **B2** Asistente, paso 2: nota de aprobación escrita, con "/ máx." como sufijo.
- [x] **B3** Asistente, paso 3: peso de cada componente escrito en % (admite 33,33) y botón **"Repartir lo que falta"**.
- [x] **B4** `EditPorcentajesScreen`: lo mismo; el arrastre para reordenar se mantiene.
- [x] **B5** Tests: `GradeCalculator.repartirRestante` (ceros, tercios, pasarse de 100) y casos nuevos en `GradeInputTest`.
- [ ] **B6** "Siguiente" en el teclado salta al próximo campo en la entrada rápida y en las subnotas (`ImeAction.Next` + `LocalFocusManager`).
- [ ] **B7** Botones −/+ opcionales a los lados del campo de nota (pasos de 0.1), para ajustar sin teclado.

---

## Bloque C – Color e identidad visual

### C.1 Base del tema
- [ ] **C1** Material You activo por defecto (`?: true`), como en la v2.1. El interruptor se queda.
- [ ] **C2** **Elegir color de la app** en Ajustes → Apariencia, para quien no usa Material You: Azul (actual), Verde, Morado, Naranja, Rosa y Turquesa. Cada uno con su `lightColorScheme`/`darkColorScheme` generado con Material Theme Builder y guardado en DataStore.
- [ ] **C3** Tema **Claro / Oscuro / Sistema** a mano (hoy solo sigue al sistema) + opción **negro puro (AMOLED)**.
- [ ] **C4** Paleta fija más viva: `primaryContainer` y `secondaryContainer` un punto más saturados; revisar contraste en oscuro.

### C.2 Componentes del sistema de diseño (`DesignSystem.kt`)
- [ ] **C5** `Tone.container()` y `Tone.onContainer()` (OK → `secondaryContainer`, WARN → `tertiaryContainer`, BAD → `errorContainer`, NONE → `surfaceContainerHigh`).
- [ ] **C6** `ResultCard` y `ErrorBanner` con el fondo de su tono (vuelve el banner de color de la calculadora y el simulador).
- [ ] **C7** `StatusLabel` como píldora suave con el fondo del tono, no solo un punto.
- [ ] **C8** `SectionLabel` en `primary`; iconos de sección dentro de un círculo `primaryContainer`.
- [ ] **C9** `SurfaceCard(accent = Color?)`: franja lateral opcional de 4 dp.
- [ ] **C10** Previews en `DesignSystemPreviews.kt` de cada tono en claro y oscuro.

### C.3 Color por materia ⚠
- [ ] **C11** Columna `colorIndex` en `MateriaEntity` (paleta de 12 colores accesibles; por defecto se asigna rotando).
- [ ] **C12** Selector de color en el asistente (paso 1) y en "Editar materia".
- [ ] **C13** El color de la materia se usa en su tarjeta (franja), en la cabecera del detalle, en los eventos del calendario, en los gráficos y en los widgets.
- [ ] **C14** Icono o emoji opcional por materia (📐 Cálculo, 🧪 Química…).

### C.4 Pantallas
- [ ] **C15** Inicio: hero del semestre en `primaryContainer` (promedio grande, créditos, aprobadas / en riesgo / sin notas).
- [ ] **C16** Inicio: aviso "Materias en riesgo" en `errorContainer`, que al tocarlo filtra la lista.
- [ ] **C17** Tarjeta de materia: franja del color de la materia, barra de progreso con el color del estado y la nota en el color del tono.
- [ ] **C18** Detalle: cabecera con el color de la materia (degradado suave hacia `surface`), anillo o barra grande del promedio con el color del estado.
- [ ] **C19** Componentes: cada tipo (parcial, quiz, taller, final…) con su icono y su color.
- [ ] **C20** Estadísticas y Calendario con color por estado y por tipo de evento (detalle en los bloques G y H).
- [ ] **C21** Onboarding y Login con ilustración o degradado de marca (no una pantalla blanca con texto).
- [ ] **C22** Capturas antes y después (claro, oscuro, Material You) en `docs/assets/`; sustituyen la maqueta SVG del README.

---

## Bloque D – Inicio

- [ ] **D1** "Próxima evaluación" arriba (la más cercana del calendario, con cuenta atrás: "Parcial de Física · en 2 días").
- [ ] **D2** Ordenar materias por nombre, promedio, riesgo o créditos, y fijar favoritas arriba.
- [ ] **D3** Filtros rápidos con chips: Todas · En riesgo · Aprobadas · Sin notas.
- [ ] **D4** Acción rápida en cada tarjeta (mantener pulsado): "Agregar nota", "¿Qué necesito?", "Editar", "Archivar".
- [ ] **D5** Estado vacío con ilustración y botón "Crear mi primera materia" / "Importar respaldo".
- [ ] **D6** Revisar que el "Deshacer" del borrado (ya existe en `HomeViewModel`) aparezca también al archivar y al cerrar semestre.

## Bloque E – Detalle de materia

- [ ] **E1** "¿Qué necesito?" en un toque desde la tarjeta de Inicio y como chip fijo en el detalle (hoy está escondida).
- [ ] **E2** Simulador "¿y si saco X?" visible como botón propio, con el resultado en vivo y en color.
- [ ] **E3** Resumen arriba: "Llevas 3.4 con el 60 % evaluado · necesitas 2.6 en el 40 % restante para pasar".
- [ ] **E4** Deshacer al borrar componentes, subnotas y detalles (snackbar), como en Inicio.
- [ ] **E5** Reordenar subnotas arrastrando (la librería `reorderable` ya está instalada).
- [ ] **E6** Fecha opcional en cada nota ⚠, para tener historial y gráfico de evolución dentro de la materia.
- [ ] **E7** Duplicar la estructura de una materia (los componentes sin notas) para el siguiente semestre o para otra materia parecida.
- [ ] **E8** Compartir la materia como imagen (tarjeta bonita con nota y componentes) además del PDF.
- [ ] **E9** Celebración discreta al aprobar o alcanzar la meta (animación del número + vibración corta; sin confeti a pantalla completa).

## Bloque F – Crear materia

- [ ] **F1** Plantillas de evaluación: "3 cortes 30/30/40", "Parciales + final 25/25/50", "Igual peso (N)", "Personalizada".
- [ ] **F2** "Copiar estructura de…" otra materia existente.
- [ ] **F3** Paso final de vista previa: tarjeta de la materia como quedará, con su color.
- [ ] **F4** Escala personalizada escrita (mín., máx. y aprobación), además de las plantillas por país.
- [ ] **F5** Crear varias materias seguidas ("Guardar y crear otra").

## Bloque G – Estadísticas

- [ ] **G1** Evolución del promedio por semestre (línea), con el color de marca.
- [ ] **G2** Distribución de notas por materia (barras con el color de cada materia).
- [ ] **G3** Promedio acumulado ponderado por créditos (PAPA / GPA) y proyección "si sigues así terminas con…".
- [ ] **G4** Tarjetas de logro: mejor materia, la que más subió, racha de aprobadas.
- [ ] **G5** Semántica de accesibilidad en los gráficos (TalkBack lee los valores).

## Bloque H – Calendario

- [ ] **H1** Color por tipo de evento (examen, entrega, quiz, exposición) y por materia.
- [ ] **H2** Vista Agenda (lista por días) además del mes.
- [ ] **H3** Vincular un evento a un componente: tras la fecha del examen, aviso "¿Ya tienes la nota del Parcial 2?" que lleva directo al campo.
- [ ] **H4** Recordatorio configurable por evento (1 h, 1 día, 3 días, 1 semana antes).
- [ ] **H5** Exportar a `.ics` o abrir en Google Calendar mediante un Intent (sin dependencias nuevas).

## Bloque I – Widgets y notificaciones

- [ ] **I1** Rediseño de los 3 widgets con colores de marca y de materia, y esquinas redondeadas del sistema (Android 12+).
- [ ] **I2** Acciones directas en el widget: tocar una materia → abre el detalle; botón "+" → entrada rápida.
- [ ] **I3** Widget nuevo "Próxima evaluación".
- [ ] **I4** Notificación semanal opcional: "Esta semana: 2 evaluaciones · Física en riesgo".
- [ ] **I5** Aviso de meta: "Te falta 3.8 en el final para llegar a tu meta de 4.0".
- [ ] **I6** Canales de notificación separados (recordatorios, resumen, metas) para que el usuario apague solo lo que quiera.

## Bloque J – Datos y respaldo

- [ ] **J1** Respaldo automático visible: "Último respaldo hace 2 días" en Inicio o Ajustes, con aviso si pasa de 14 días.
- [ ] **J2** Importar CSV (nombre, componente, peso, nota) con vista previa antes de guardar.
- [ ] **J3** PDF de exportación con color (encabezado de marca, estado de cada materia en color).
- [ ] **J4** Archivar semestres viejos sin borrarlos y verlos en solo lectura.
- [ ] **J5** Decidir la exportación a Excel (código muerto según el ROADMAP): conectarla o borrarla.

## Bloque K – IA y recursos

- [ ] **K1** Tarjetas de recomendación con color por prioridad y por materia.
- [ ] **K2** Mensajes de error claros para 401/429 (ROADMAP Fase 2.1), con un botón "Revisar clave".
- [ ] **K3** "Plan de estudio" de la semana a partir de las próximas evaluaciones y las materias en riesgo.
- [ ] **K4** Todo funciona sin IA: la sección se oculta limpio si no hay clave ni proxy.

## Bloque L – Ajustes

- [ ] **L1** Apariencia agrupada: tema, color de la app, Material You, AMOLED y una vista previa en vivo.
- [ ] **L2** Decimales y redondeo con ejemplo en vivo ("3.456 → 3.46").
- [ ] **L3** Escala por defecto para las materias nuevas.
- [ ] **L4** Sección "Notificaciones" con los canales de I6.
- [ ] **L5** Iconos con color en cada fila de ajustes (como en los ajustes de Android).

## Bloque M – Micro-interacciones y movimiento

- [ ] **M1** Animar los números de nota y promedio al cambiar (`animateFloatAsState`).
- [ ] **M2** Vibración corta al guardar una nota, al completar el 100 % y al alcanzar la meta.
- [ ] **M3** Transiciones entre pantallas (contenedor que se expande de la tarjeta al detalle).
- [ ] **M4** Respetar "reducir animaciones" del sistema.

## Bloque N – Accesibilidad y rendimiento

- [ ] **N1** Objetivos táctiles ≥ 48 dp, `onClickLabel` y roles (ROADMAP Fase 4).
- [ ] **N2** Contraste AA comprobado en todos los tonos y temas nuevos.
- [ ] **N3** Texto grande (200 %) sin cortes en las tarjetas y en el hero.
- [ ] **N4** Baseline Profile para un arranque más rápido; medir el arranque en frío antes y después.

## Bloque O – Limpieza técnica (del ROADMAP, se hace en paralelo)

- [ ] **O1** Fase 1 del ROADMAP: borrar el código muerto (DAO, `ConversorLetras`, Excel, colores sin uso, permisos).
- [ ] **O2** Fase 2: la app lee `error`/`retryable` del backend; un solo dueño del prompt.
- [ ] **O3** Fase 3: los textos fijos que quedan → `strings.xml` (ES/EN).
- [ ] **O4** Una sola migración Room v8 para C11, E6 y J4, con test de migración.

## Bloque P – Calidad

- [ ] **P1** Tests Compose de 3 flujos: crear materia (con porcentajes escritos), ingresar una nota, poner la meta.
- [ ] **P2** Test de `DecimalField`: escribir "3,5" guarda 3.5, "9" con máx. 5 no guarda.
- [ ] **P3** CI: job de emulador con `androidTest`.
- [ ] **P4** Probar en un celular pequeño (5"), en uno grande y en una tablet.

## Bloque Q – Lanzamiento v3.0.0

- [ ] **Q1** CHANGELOG con secciones "Nuevo / Mejorado / Arreglado".
- [ ] **Q2** README (ES/EN) con capturas reales y un GIF de la app en uso.
- [ ] **Q3** Pantalla "Novedades de la 3.0" la primera vez que se abre tras actualizar.
- [ ] **Q4** Tag `v3.0.0` y Release firmado (el CI ya lo hace por tag).

---

## Orden sugerido y entregas

| Entrega | Bloques | Por qué en este orden |
|---|---|---|
| **3.0-alpha1** | A ✅, B ✅, C.1, C.2, C.4 (sin color por materia) | Lo que más se nota: vuelve el color y se arregla la entrada |
| **3.0-alpha2** | O4 + C.3 + E6 + J4 (migración v8), D, E | Una sola migración; Inicio y Detalle completos |
| **3.0-beta1** | F, G, H, L, M | Crear, estadísticas, calendario y ajustes |
| **3.0-beta2** | I, J, K, N, O1-O3 | Widgets, datos, IA, accesibilidad y limpieza |
| **3.0.0** | P, Q | Tests, capturas, lanzamiento |

**Sin dependencias nuevas** salvo que un bloque lo justifique (los gráficos y los widgets se hacen con lo ya instalado; Baseline Profile usa `androidx.profileinstaller`, que es oficial).

---

## Bloque R – Personalidad y calidad de vida (2026-11-26)

Hecho:
- [x] Axo, mascota ajolote (vectores propios, `docs/assets/axo/gen.py`, guía en `docs/PERSONALIDAD.md`) en login, onboarding, estados vacíos, tarjeta de ánimo, calculadora, simulador y entrada rápida.
- [x] Textos ES/EN reescritos con la voz de Axo (bienvenida, ánimo, notificaciones, calculadora, simulador, errores).
- [x] Calculadora: abre ya calculada con la meta o la nota para pasar, atajos "Pasar"/"Mi meta" y acepta coma.
- [x] Simulador: acepta coma y calcula en vivo, sin botón "Simular".
- [x] Entrada rápida: las filas ya no desaparecen al escribir; "Siguiente" salta a la próxima nota.
- [x] Detalle: atajos visibles (Poner notas, ¿Y si saco…?, ¿Qué necesito?).
- [x] Deshacer al borrar notas y detalles (10 s).
- [x] Inicio: plural "1 materia"; mantener pulsado → menú (Abrir / Eliminar con Deshacer) en lugar del swipe. **Falta probarlo en el teléfono.**

Pendiente, encontrado al probar en el emulador:
- [ ] **R1 (prioridad)** El número grande del detalle es el *acumulado* (1.50 en rojo con 50 % evaluado) aunque el estudiante vaya en 3.0 en lo evaluado. Mostrar "Vas con 3.0 en lo evaluado" como principal y el acumulado como dato secundario.
- [ ] **R2** Textos fijos en español: "0 de 1 evaluado", nombres por defecto "Primer corte", "Nota Primer corte" → `strings.xml`.
- [ ] **R3** Asistente paso 2: 10 chips de país empujan el campo fuera de la pantalla → lista desplegable.
- [ ] **R4** Inicio vacío: ocultar "0 materias", la búsqueda y el orden cuando no hay materias.
- [x] Mantener pulsado probado en el emulador: menú Abrir / Eliminar materia.
- [x] Aviso de versión nueva desde GitHub Releases (`UpdateChecker`, interruptor en Ajustes, `UPDATE_REPO`).
- [ ] **R6** Descargar e instalar el APK dentro de la app (DownloadManager + `REQUEST_INSTALL_PACKAGES`) en vez de abrir el enlace; solo en builds de GitHub.
- [ ] **R5** Más opciones en el menú de la tarjeta: "¿Qué necesito?", "Editar" y "Archivar" (D4).

### Revisión de calidad de vida, ronda 2 (ordenada por impacto ÷ esfuerzo)

- [x] Calendario: fecha y hora con selectores nativos. Antes eran ±1 día / ±1 h (el botón de fecha no hacía nada y no se podían poner minutos).
- [x] **Q1** R1: rendimiento ("vas con 3.0 en lo evaluado") como número principal; el acumulado, secundario.
- [x] **Q2** Crear materias en lote, típico al empezar semestre: recordar la última escala y el periodo, "Copiar estructura de…" (F2) y "Guardar y crear otra" (F5).
- [x] **Q3** Campos de nota del Detalle con "Siguiente", igual que la entrada rápida.
- [x] **Q4** Si no hay carpeta de respaldo elegida, aviso en Inicio al tener 3 o más materias (al desinstalar se pierde todo).
- [x] **Q5** Tocar la notificación de un examen abre su materia (revisar a qué pantalla lleva hoy el `PendingIntent`).
- [x] **Q6** R2: textos fijos en español ("0 de 1 evaluado", "Primer corte").
- [x] **Q7** Widgets: tocar una materia abre su detalle (I2).
- [x] **Q8** R4 (Inicio vacío limpio), R5 (más acciones en el menú de la tarjeta) y R3 (países en lista desplegable).

### Ronda 3 (hecha)
- [x] Identidad: degradado de marca (azul → violeta → rosa Axo) en el resumen de Inicio, color por materia (avatar con inicial, barra de progreso, cabecera del detalle), estados en píldora y títulos de sección en color de marca.
- [x] Calendario: un color por tipo de evento y nombre del tipo traducido.
- [x] Porcentajes con decimales (0,5 %) también al agregar notas y partes; ningún porcentaje se muestra cortado a entero.
- [x] `Materia.promedio` = rendimiento en lo evaluado (antes era igual al acumulado); un corte a medias cuenta a medias. Tests en `MateriaPromedioTest`.
- [x] Widgets: mensaje según la fracción de la escala (antes umbrales fijos de 0–5), textos traducidos y el de materia abre esa materia.
- [x] Notificaciones de examen abren su materia; cada una con su `requestCode`.
- [ ] Color por materia elegido por el usuario (hoy sale del id; necesita columna en Room).
- [ ] Elegir color de la app y tema claro/oscuro/AMOLED en Ajustes (C2, C3).
