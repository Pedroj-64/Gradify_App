# Plan: Gradify v2.3, limpieza, UI coherente y lanzamiento open source

## Context
Gradify (Android Compose + Room + Hilt, backend Spring opcional) ya tiene CI, release firmado, rediseño parcial y un ROADMAP desactualizado. Tres auditorías (código sin uso, UI/interfaces, salud OSS) y una búsqueda de referentes dan este diagnóstico:

- **Bloqueantes para publicar**: API key de Google filtrada en el historial git (sin rotar); licencia con dos titulares distintos; email y datos personales en README/CHANGELOG; `.vscode/` versionado.
- **Faltan** CONTRIBUTING, CODE_OF_CONDUCT, SECURITY, privacidad, plantillas de issue/PR, dependabot, CODEOWNERS, metadatos F-Droid/fastlane, capturas, primer tag/release.
- **README miente**: dice JUnit5+MockK, Detekt/ktlint, WorkManager→Sheets, árbol de archivos obsoleto. `README.en.md` va atrasado. ROADMAP con ítems ya hechos sin marcar y mezcla de análisis viejo.
- **Sin uso (~1.150 líneas, ~6 %)**: ~120 claves de strings, simulador entero (`SimuladorBottomSheet`, `SimularNotaUseCase`, su test), `PromedioGauge`, `ResponsiveDimens`, `ConversorLetras`, exportación Excel muerta, ~15 métodos DAO y 5 del repositorio, `androidx-security-crypto`, 3 deps de test UI, 9 colores, 3 rutas en `Screen.kt`, permiso `WRITE_EXTERNAL_STORAGE`.
- **Interfaces**: contrato app↔backend coincide (endpoint, header, DTOs, modelos). Fallos reales: la app ignora `error`/`retryable` (un 401/429 cae en silencio a las claves directas), el prompt está duplicado app/backend, `RecomendacionesViewModel` usa el literal `"materiaId"`.
- **UI incoherente**: hojas inferiores, diálogos, Exportar, Recomendaciones (colores fijos, elevación), pasos 2-3 del wizard y PermissionDialogs no usan el sistema de diseño. ~110 textos fijos en español (la app no cambia a inglés), ~60 mensajes de error en ViewModels, errores técnicos mostrados al usuario, eventos de una sola vez como estado persistente, sin roles/semántica de accesibilidad, objetivos táctiles < 48 dp en calendario.
- **Referentes** (apps de notas del medio): todas ofrecen nota necesaria para el final, escenarios "qué pasa si", escalas personalizadas, promedio ponderado/GPA, exportación. Peticiones frecuentes: colores de nota personalizados, historial gráfico, multi-escala. Gradify ya cubre lo básico; falta destacar escalas LATAM (Chile 1-7, Perú/Venezuela 0-20, etc.) y la simulación, hoy oculta.

**Supuesto a confirmar**: tu mensaje se cortó en "gente política de…". Asumo público objetivo = estudiantes hispanohablantes de LATAM (primaria idioma ES, EN secundario). Dime si era otro.

## Orden y dependencias
Higiene/seguridad → limpieza (reduce superficie) → base de strings/eventos → sistema de diseño → pulido por pantalla → comunidad/lanzamiento. La limpieza va antes de migrar strings para no traducir código muerto.

## Fase 0 — Seguridad y legal (bloquea publicidad)
- 0.1 Rotar/restringir la key filtrada en Google Cloud (paquete + SHA-1); decidir `git filter-repo` o asumirla comprometida. *Acción manual tuya.* Verificar: gitleaks de CI pasa (o allowlist del historial).
- 0.2 Unificar titular de `LICENSE`, READMEs, CHANGELOG; quitar email personal y línea "bareta in the hood"; confirmar consentimiento de nombres citados.
- 0.3 `git rm --cached .vscode`; limpiar duplicados de `.gitignore`; borrar rama `fase-0-1-ia-y-build`.
- Checkpoint 0: `git grep` sin email ni key; licencia coherente.

## Fase 1 — Eliminar lo no usado (verificar con `./gradlew lintDebug testDebugUnitTest assembleDebug` tras cada bloque)
- 1.1 **Decisión**: simulador. Recomendado: *conectarlo* (es la función estrella "qué pasa si" de los referentes) en vez de borrar 400 líneas; alternativa: borrarlo. Ver pregunta abierta.
- 1.2 Borrar `PromedioGauge.kt` + 6 imports, `ResponsiveDimens.kt`, `GradeBadges` (EstadoBadge/CountBadge), `RowSpacer`, `FullWidthColumn`, `RenameableText`, rutas muertas de `Screen.kt`.
- 1.3 `ConversorLetras`: borrar con su test salvo que se quiera escala por letras (revisar `TipoEscala` primero).
- 1.4 Export muerto: `ExportViewModel.exportarExcel`, `ExcelExporter.exportar(materia)` si no tiene otro llamador, `sugerirNombreResumen`.
- 1.5 DAO/repositorio: quitar métodos sin llamadores (lista en auditoría), luego `relations/*` si quedan huérfanas. Mantener `app/schemas`.
- 1.6 Dependencias: `androidx-security-crypto`, espresso/ui-test si no se usan (o escribir los tests UI del 4.x y conservarlas). Manifiesto: `WRITE_EXTERNAL_STORAGE`, `ACCESS_NETWORK_STATE` tras comprobar con lint. `colors.xml` a lo usado.
- 1.7 Otros: `duplicarMateria`, `updateEscala*`, `RecomendacionesCache.clearAll`, `touchLastSync`, getters de modelo sin lectores.
- Checkpoint 1: build + tests + lint verdes; ~-800 líneas netas; APK abre en emulador.

## Fase 2 — Corrección de interfaces
- 2.1 App lee `error`/`retryable` del proxy: 401/429 muestran mensaje propio y no caen a claves directas sin avisar (`GeminiService.kt` ~l.441-497). Test de parseo nuevo en `GeminiServiceParseTest`.
- 2.2 Un solo dueño del prompt: con proxy, la app envía solo datos; `buildPrompt` solo para clave directa.
- 2.3 `RecomendacionesViewModel` usa `Screen.RecomendacionesMateria.ARG_MATERIA_ID`; quitar `materiaId` duplicado como parámetro de pantalla.
- 2.4 Documentar `X-Gradify-Token` como guarda blanda (extraíble del APK) en `backend/README.md`; el rate-limit es la defensa real.
- Checkpoint 2: `gradle test` backend + unit tests app; llamada real al proxy local OK.

## Fase 3 — Base de texto y eventos (habilita ES/EN real)
- 3.1 `UiText` (StringRes/Raw) en ViewModels; migrar ~60 mensajes (Detail, Home, Edit, Wizard, Calendar, Export, Settings, Login). Nunca mostrar `e.message`/clase de excepción.
- 3.2 `Channel<UiEvent>` para snackbars y navegación (Login, Wizard, Export, Recomendaciones); sin `clearError()`.
- 3.3 Reutilizar las ~120 claves existentes: cambiar los ~110 literales de Kotlin a `stringResource`; borrar claves realmente huérfanas; añadir las que falten en `values-en`.
- 3.4 `SavedStateHandle.getStateFlow` para wizard y búsqueda de Home.
- Checkpoint 3: app en inglés completa (emulador `es` vs `en`), rotar pantalla no repite snackbars.

## Fase 4 — Sistema de diseño completo
- 4.1 Añadir a `DesignSystem.kt`: `GradifySheet`, `GradifyDialog`, `ResultCard`, `EmptyState`, `ErrorBanner`, `LoadingBlock`. Reutilizar `SurfaceCard`, `SectionLabel`, `Tone`, `ShimmerLoading`.
- 4.2 Migrar: Calculadora, Simulador, Entrada rápida, `PermissionDialogs`, diálogos de Detail/Settings/Calendar.
- 4.3 Exportar, Recomendaciones (quitar `Color(0xFF…)` y elevación, usar `Tone`), wizard pasos 2-3, Login/Onboarding a tipografía del tema.
- 4.4 Partir `CalendarScreen` (855), `EstadisticasScreen` (670), `RecomendacionesScreen` (663) en archivos por sección con `@Preview`.
- 4.5 Accesibilidad: `role`+`onClickLabel` en clicables, `minimumInteractiveComponentSize` en celdas del calendario y fila Meta, `semantics` en gráficos, `contentDescription` traducible en los iconos listados.
- Checkpoint 4: capturas claro/oscuro de las 12 pantallas sin estilos ad-hoc; revisión con skill `design:accessibility-review`; `lintDebug` sin errores nuevos.

## Fase 5 — Producto (dar vida)
- 5.1 Plantillas de escala por país en el wizard (CO 0-5, MX/ES 0-10, CL 1.0-7.0, PE/VE 0-20, AR 1-10, BR 0-10, CR 0-100) + tabla "Escalas soportadas" en README. Verificar mínimo≠0 con test en `GradeCalculatorTest`.
- 5.2 "Qué necesito" visible desde la tarjeta de materia y simulador (1.1).
- 5.3 Widgets con acción directa (ya refrescan).
- 5.4 Tests Compose de 3 flujos: crear materia, ingresar nota, ver promedio (justifica conservar deps UI-test).
- Checkpoint 5: 3 tests UI pasan en emulador.

## Fase 6 — Comunidad y lanzamiento open source
- 6.1 `CONTRIBUTING.md` (JDK 17 o 21 — no 25; quickstart sin Firebase copiando `google-services.example.json`; `local.properties` con ejemplo Linux/macOS/Windows; correr tests; convención de commits y ramas; idioma principal; firma release con debug key si no hay `key.properties`).
- 6.2 `CODE_OF_CONDUCT.md` (Contributor Covenant), `SECURITY.md`, `PRIVACY.md` (Gemini/Drive/Google Sign-In envían datos a terceros).
- 6.3 `.github/`: `ISSUE_TEMPLATE/{bug,feature}.md` + `config.yml`, `PULL_REQUEST_TEMPLATE.md`, `CODEOWNERS`, `dependabot.yml` (gradle app+backend, github-actions), `.editorconfig`.
- 6.4 CI: `permissions: contents: read`, `concurrency`, subir reportes y APK debug como artifact, job de emulador que ejecute `androidTest` (migraciones hoy solo compilan).
- 6.5 README: quitar afirmaciones falsas (MockK/JUnit5/Detekt/ktlint/WorkManager-Sheets/árbol viejo), regenerar árbol, sincronizar `README.en.md`, capturas reales, enlazar ROADMAP en vez de duplicar "mejoras futuras", explicar `com.notasapp` vs Gradify.
- 6.6 `docs/RELEASING.md` (4 secretos de keystore, `GOOGLE_SERVICES_JSON_BASE64`), `backend/README.md` actualizado (no tiene wrapper → Gradle 8.9).
- 6.7 **ROADMAP.md reescrito**: solo pendiente y futuro, con casillas ligadas a issues; historial a CHANGELOG. Etiquetar 8-10 issues `good first issue` / `help wanted` (plantillas de país, traducciones, tests de ViewModel, accesibilidad, un widget).
- 6.8 Fastlane metadata (`es-CO`, `en-US`) y evaluar F-Droid (Google Sign-In/Drive pueden requerir flavor sin Play services); tag `v2.3.0` y Release con APK.
- Checkpoint final: clon limpio compila siguiendo solo CONTRIBUTING; Release publicado; checklist de comunidad de GitHub al 100 %.

## Riesgos
| Riesgo | Impacto | Mitigación |
|---|---|---|
| Borrar algo que se usa por reflexión/Hilt | Alto | Lint + build + abrir cada pantalla; falsos positivos listados (módulos Hilt, receivers, migraciones) |
| Migración de strings rompe formatos | Medio | Una pantalla por commit, revisar plurales/args |
| Reescribir historial | Alto | Solo con tu OK; si no, rotar key y documentar |
| F-Droid vs Google Sign-In | Medio | Flavor `foss` opcional, posponer |

## Verificación end-to-end
`./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest` (JDK 17); `cd backend && gradle test`; emulador ES/EN, claro/oscuro, rotación; `connectedDebugAndroidTest` en emulador; release firmado verificado con `apksigner`; revisión de `git grep` de secretos.

## Preguntas abiertas
1. Público objetivo completo (el mensaje se cortó).
2. Simulador: ¿conectarlo (recomendado) o borrarlo?
3. Historial git: ¿reescribir con filter-repo o solo rotar la key?
4. Idioma principal de la documentación (ES con EN secundario, como ahora).

## Alcance de esta aprobación: SOLO DOCUMENTAR (no se toca código)
Pediste dejarlo todo documentado para seguirlo después. Al aprobar haré únicamente:
1. `tasks/plan.md`: este plan completo (contexto, hallazgos, fases, riesgos, verificación).
2. `tasks/todo.md`: una casilla `- [ ]` por tarea (0.1 … 6.8) con criterio de aceptación, comando de verificación, dependencias y archivos probables, más los checkpoints 0-5 y final.
3. `tasks/audit.md`: los resultados crudos de las 3 auditorías (lista de código sin uso con rutas, lista de strings/errores por archivo, brechas OSS) y los enlaces de la investigación web, para no repetir el análisis.
4. Nota en `ROADMAP.md` solo si lo pides (por ahora no se modifica).

Ningún cambio en `app/`, `backend/`, README ni CI. Sin commit ni push; eso solo cuando lo indiques, a tu nombre y sin menciones a IA.
