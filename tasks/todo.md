# Gradify v2.3 – Lista de tareas

Plan completo: [plan.md](plan.md). Verificación base tras cada tarea (JDK 17 o 21, nunca 25):
`./gradlew testDebugUnitTest lintDebug assembleDebug` · backend: `cd backend && gradle test`.

## Fase 0 – Seguridad y legal
- [x] 0.1 Rotar/restringir key de Google (paquete + SHA-1); decidir `filter-repo`. *Manual.* AC: gitleaks en CI pasa o historial en allowlist.
- [x] 0.2 Un solo titular en `LICENSE`/READMEs/CHANGELOG; quitar email y línea informal. AC: `git grep` sin email.
- [ ] 0.3 `git rm --cached .vscode`, limpiar `.gitignore`, borrar rama `fase-0-1-ia-y-build`.
- [ ] **Checkpoint 0**: licencia coherente, sin datos personales.

## Fase 1 – Eliminar lo no usado (dep: 0)
- [ ] 1.1 Decidir simulador (conectar vs borrar). AC: sin código huérfano; si se conecta, abre desde Detalle.
- [ ] 1.2 Borrar `PromedioGauge.kt` (+6 imports), `ResponsiveDimens.kt`, `GradeBadges`, `RowSpacer`, `FullWidthColumn`, `RenameableText`, 3 rutas de `Screen.kt`.
- [ ] 1.3 `ConversorLetras` + test (revisar `TipoEscala` antes).
- [ ] 1.4 `ExportViewModel.exportarExcel`, `ExcelExporter.exportar(materia)`, `sugerirNombreResumen`.
- [ ] 1.5 DAO: `ComponenteDao` (5), `ExamenEventDao.getEventsPendingReminder`, `MateriaDao` (2), `SubNotaDao` (3), `SubNotaDetailDao` (2), `UsuarioDao` (2); repo: `updateMateria`, `insertComponentes`, `deleteComponentesByMateria`, `insertSubNotas`, `deleteSubNotasByComponente`; luego `relations/*` si quedan huérfanas.
- [ ] 1.6 Deps `androidx-security-crypto` (y espresso/ui-test si no se escriben tests UI); permisos `WRITE_EXTERNAL_STORAGE`, `ACCESS_NETWORK_STATE`; 9 colores de `colors.xml`.
- [ ] 1.7 `duplicarMateria`, `updateEscalaMin/Max`, `RecomendacionesCache.clearAll`, `touchLastSync`, getters de modelo sin lectores.
- [ ] **Checkpoint 1**: build+tests+lint verdes, app abre en emulador, ≈ −800 líneas.

## Fase 2 – Interfaces (dep: 1)
- [ ] 2.1 `GeminiService` (~l.441-497) lee `error`/`retryable`; 401/429 con mensaje propio. Test en `GeminiServiceParseTest`.
- [ ] 2.2 Prompt solo en backend cuando hay proxy; `buildPrompt` solo para clave directa.
- [ ] 2.3 `RecomendacionesViewModel` usa `ARG_MATERIA_ID`; quitar `materiaId` duplicado en pantallas.
- [ ] 2.4 Documentar `X-Gradify-Token` como guarda blanda en `backend/README.md`.
- [ ] **Checkpoint 2**: tests app+backend verdes; llamada local al proxy OK.

## Fase 3 – Textos y eventos (dep: 1)
- [ ] 3.1 `UiText` y migrar ~60 mensajes de ViewModels (Detail, Home, Edit, Wizard, Calendar, Export, Settings, Login). AC: ninguna excepción cruda en UI.
- [ ] 3.2 `Channel<UiEvent>` en Login, Wizard, Export, Recomendaciones.
- [ ] 3.3 ~110 literales Kotlin → `stringResource`; reutilizar ~120 claves ya existentes; completar `values-en`.
- [ ] 3.4 `SavedStateHandle.getStateFlow` para wizard y búsqueda de Home.
- [ ] **Checkpoint 3**: emulador en `en` sin texto español; rotar no repite snackbars.

## Fase 4 – Diseño y accesibilidad (dep: 3)
- [ ] 4.1 Añadir a `DesignSystem.kt`: `GradifySheet`, `GradifyDialog`, `ResultCard`, `EmptyState`, `ErrorBanner`, `LoadingBlock`.
- [ ] 4.2 Migrar Calculadora, Simulador, Entrada rápida, `PermissionDialogs`, diálogos de Detail/Settings/Calendar.
- [ ] 4.3 Exportar, Recomendaciones (sin `Color(0xFF…)` ni elevación), wizard pasos 2-3, Login/Onboarding.
- [ ] 4.4 Partir `CalendarScreen` (855), `EstadisticasScreen` (670), `RecomendacionesScreen` (663) + `@Preview`.
- [ ] 4.5 `role`/`onClickLabel`, `minimumInteractiveComponentSize` (calendario, fila Meta), `semantics` en gráficos.
- [ ] **Checkpoint 4**: capturas claro/oscuro sin estilos ad-hoc; revisión de accesibilidad.

## Fase 5 – Producto (dep: 1.1, 4)
- [ ] 5.1 Plantillas de escala por país en wizard + tabla en README + test de mínimo ≠ 0 en `GradeCalculatorTest`.
- [ ] 5.2 "¿Qué necesito?" en la tarjeta de materia.
- [ ] 5.3 Widgets con acción directa.
- [ ] 5.4 Tests Compose: crear materia, ingresar nota, ver promedio.
- [ ] **Checkpoint 5**: 3 tests UI pasan en emulador.

## Fase 6 – Comunidad y lanzamiento (puede empezar tras Fase 0)
- [ ] 6.1 `CONTRIBUTING.md` (JDK, quickstart con `google-services.example.json`, tests, commits, idioma, firma release).
- [ ] 6.2 `CODE_OF_CONDUCT.md`, `SECURITY.md`, `PRIVACY.md`.
- [ ] 6.3 `.github/`: `ISSUE_TEMPLATE/*`, `PULL_REQUEST_TEMPLATE.md`, `CODEOWNERS`, `dependabot.yml`; `.editorconfig`.
- [ ] 6.4 CI: `permissions`, `concurrency`, artifacts, job emulador `androidTest`.
- [ ] 6.5 README: regenerar árbol, sincronizar `README.en.md`, capturas reales, explicar `com.notasapp`.
- [ ] 6.6 `docs/RELEASING.md`; `backend/README.md` (sin wrapper → Gradle 8.9, token, rate-limit, `/health`).
- [ ] 6.7 Etiquetar 8-10 issues `good first issue`/`help wanted`.
- [ ] 6.8 Fastlane `es-CO`/`en-US`; evaluar F-Droid; tag `v2.3.0` + Release.
- [ ] **Checkpoint final**: clon limpio compila con solo CONTRIBUTING; Release publicado.

## Decisiones abiertas
1. Público objetivo (el mensaje original quedó cortado; se asume estudiantes LATAM).
2. Simulador: conectar (recomendado) o borrar.
3. Historial git: `filter-repo` o solo rotar key.
4. Idioma principal de la documentación.
