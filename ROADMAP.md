# Gradify – Roadmap público

> Actualizado: 2026-10-14. Estado real: v2.2.0 en `main`. El historial de análisis anterior está en [CHANGELOG.md](CHANGELOG.md) y en `git log`.
> Plan y razonamiento: [`tasks/plan.md`](tasks/plan.md) · Tareas con criterios de aceptación: [`tasks/todo.md`](tasks/todo.md).

**Misión:** que cualquier estudiante de Latinoamérica lleve sus notas con la escala de su país, sin cuenta obligatoria, sin anuncios y sin enviar sus datos a nadie salvo que él lo decida.

## Ya hecho (resumen)
- Room v7 con migraciones probadas, respaldo JSON a carpeta elegida, cerrar/reabrir semestre.
- Recordatorios de exámenes con alarmas (reprogramación tras reinicio); widgets que se refrescan al cambiar notas.
- IA: Gemini/Groq/OpenRouter con clave del propio usuario; backend proxy opcional (rate-limit por IP, token opcional).
- CI (gitleaks, tests, lint, build, backend) y release firmado por tag.
- Rediseño con sistema de diseño propio en Inicio, Detalle, Estadísticas, Calendario, Recursos, Ajustes, Login y Onboarding.

## Fase 0 – Seguridad y legal (antes de promocionar el repo)
- [ ] Rotar/restringir la API key de Google que quedó en el historial (`4415198`…`25427de`); decidir `git filter-repo` o asumirla comprometida
- [ ] Unificar titular de `LICENSE`, READMEs y CHANGELOG; quitar email y datos personales públicos
- [ ] Dejar de versionar `.vscode/`; limpiar `.gitignore`; borrar rama mergeada `fase-0-1-ia-y-build`

## Fase 1 – Eliminar lo no usado (~1.150 líneas, ~6 %)
- [ ] Decidir simulador "¿y si saco X?": conectarlo (recomendado) o borrarlo (`SimuladorBottomSheet`, `SimularNotaUseCase`, su test)
- [ ] Borrar `PromedioGauge`, `ResponsiveDimens`, `GradeBadges`, helpers y rutas muertas (`Screen.kt`)
- [ ] Decidir `ConversorLetras` (solo lo usa su test) y exportación Excel muerta
- [ ] Quitar ~20 métodos de DAO/repositorio sin llamadores
- [ ] Dependencias `androidx-security-crypto`; permisos `WRITE_EXTERNAL_STORAGE`/`ACCESS_NETWORK_STATE`; colores sin uso

## Fase 2 – Interfaces coherentes
- [ ] La app lee `error`/`retryable` del backend (401/429 con mensaje propio, sin caer en silencio a claves directas)
- [ ] Un solo dueño del prompt de IA (backend si hay proxy)
- [ ] Argumentos de navegación por constante, sin parámetros duplicados

## Fase 3 – Textos y eventos (inglés de verdad)
- [ ] `UiText` en ViewModels (~60 mensajes), nunca `e.message` al usuario
- [ ] Eventos de una sola vez con `Channel` (snackbars, navegación)
- [ ] ~110 textos fijos a `strings.xml` (ES/EN); `SavedStateHandle` para wizard y búsqueda

## Fase 4 – Diseño completo y accesibilidad
- [ ] `GradifySheet`, `GradifyDialog`, `ResultCard`, `EmptyState`, `ErrorBanner`, `LoadingBlock`
- [ ] Migrar calculadora, simulador, entrada rápida, permisos, Exportar, Recomendaciones, wizard pasos 2-3
- [ ] Partir `CalendarScreen`, `EstadisticasScreen`, `RecomendacionesScreen` + previews
- [ ] Roles y `onClickLabel`, objetivos táctiles ≥ 48 dp, semántica en gráficos

## Fase 5 – Producto
- [ ] Plantillas de escala por país: CO 0-5, MX/ES 0-10, CL 1.0-7.0, PE/VE 0-20, AR 1-10, BR 0-10, CR 0-100
- [ ] "¿Qué necesito?" accesible desde la tarjeta de materia
- [ ] Widgets con acciones directas
- [ ] Tests Compose de 3 flujos (crear materia, ingresar nota, ver promedio)

## Fase 6 – Comunidad y lanzamiento
- [ ] `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md`, `PRIVACY.md`
- [ ] Plantillas de issue y PR, `CODEOWNERS`, `dependabot.yml`, `.editorconfig`
- [ ] CI: permisos mínimos, `concurrency`, artifacts, job de emulador con `androidTest`
- [ ] README en inglés sincronizado, árbol de arquitectura regenerado, capturas reales
- [ ] `docs/RELEASING.md` y `backend/README.md` actualizados
- [ ] Metadatos fastlane (`es-CO`, `en-US`); evaluar F-Droid (flavor sin servicios de Google)
- [ ] Etiquetar 8-10 issues `good first issue`; tag `v2.3.0` con Release

## Infraestructura pendiente (no es código)
- [ ] Desplegar backend y fijar `BACKEND_URL`/`BACKEND_TOKEN` (opcional)
- [ ] Verificar nombres de modelos con `ListModels`
- [ ] Ejecutar el test de migración en emulador
- [ ] Keystore propia con SHA-1 registrado en Firebase para que el login funcione en el APK público

## Ideas (sin compromiso)
Pomodoro, metas por materia, multi-estudiante, historial gráfico, colores de nota personalizados, importar CSV, versión web, gamificación.

## Versiones
| Versión | Contenido |
|---|---|
| v2.3.0 | Fases 0-4 y 6 (limpia, coherente, publicable) |
| v2.4.0 | Fase 5 (escalas por país, simulador, tests UI) |
