# Gradify – Roadmap de retoma (v2.1.1 → v2.2+)

> Análisis hecho el 2026-10-01 sobre `main` (commit `05bc6ad`). ~19k líneas Kotlin, app Android (Compose + Room + Hilt) + backend Spring Boot proxy de Gemini.

## 0. Hallazgos del análisis

### Claves / APIs (lo roto)
| # | Hallazgo | Severidad |
|---|----------|-----------|
| K1 | **API key de Google (`AIzaSyBjb8…`) y `google-services.json` quedaron en el historial git** (commits `4415198` v1.06 → borrado en `25427de` v1.09). Borrarlo del árbol no lo saca del historial. | Alta |
| K2 | Modelos de IA obsoletos/retirados en `GeminiService.kt`: `gemini-1.5-flash(-latest)`, `gemini-pro`, Groq `mixtral-8x7b-32768`, OpenRouter `…:free` (rotan). En el **backend**, `application.yml` usa `gemini-1.5-flash / 1.5-pro / pro` → **el proxy hoy falla siempre**. | Alta |
| K3 | Claves de IA (`GEMINI/GROQ/OPENROUTER`) se compilan en `BuildConfig` → quedan extraíbles del APK. El backend proxy existe justo para evitarlo, pero `BACKEND_URL` no está desplegado/configurado. | Alta |
| K4 | Sin `local.properties` ni `google-services.json` en el repo → **el proyecto no compila en limpio** (plugin `google-services` exige el json). Falta guía/plantilla real. | Media |
| K5 | Backend declara `rate-limit` en yml pero **no hay código que lo aplique**; sin auth → cualquiera con la URL gasta la cuota de Gemini. | Media |
| K6 | Build release firmado con `debug` keystore; `fallbackToDestructiveMigration()` en prod (puede borrar notas del usuario si falta una migración). `allowBackup=true`. | Media |
| K7 | README dice "Gemini SDK" pero el código usa REST; versión `generativeAi` huérfana en `libs.versions.toml`. | Baja |

### Calidad / UX
- Pantallas gigantes: `MateriaDetailScreen` 1532 líneas, `CalendarScreen` 909, `EstadisticasScreen` 842 → difíciles de mantener y de pulir visualmente.
- Solo 6 tests (dominio). Cero tests de UI/DAO/migraciones.
- Cadena de IA con throttle global y reintentos largos (hasta ~minutos) sin feedback claro al usuario.
- Entorno local: JDK 25 (el proyecto exige 17; AGP 8.7.3 / Kotlin 2.1.0 no soportan 25) → usar JDK 17 para compilar.

## 1. Roadmap

### Fase 0 – Higiene y seguridad (primero, ~1 día)
- [ ] **Rotar/restringir** la API key filtrada en Google Cloud Console (restricción por package + SHA-1) y regenerar `google-services.json`. Opcional: limpiar historial con `git filter-repo` (repo público → asumir la key como comprometida igual).
- [ ] Documentar setup mínimo: `local.properties` + `google-services.json` + JDK 17 (sección en README).
- [ ] Verificar que `./gradlew assembleDebug` y `testDebugUnitTest` pasen en limpio con JDK 17.
- [ ] Quitar `GEMINI/GROQ/OPENROUTER_API_KEY` de `BuildConfig` en release (solo debug), dejando backend como único camino en producción.

### Fase 1 – Arreglar IA y backend (~2-3 días)
- [ ] Actualizar modelos: Gemini `gemini-2.5-flash` / `-flash-lite` (verificar lista actual con `GET /v1beta/models`), Groq `llama-3.3-70b-versatile` + `llama-3.1-8b-instant`, quitar mixtral.
- [ ] Mover lista de modelos a config remota del backend (un solo lugar para cambiar sin lanzar APK).
- [ ] Backend: actualizar `application.yml`, implementar rate-limit real (Bucket4j por IP) y header/token simple de app, health endpoint, deploy (Cloud Run/Render/Fly) y setear `BACKEND_URL`.
- [ ] Cliente: timeouts cortos, un solo mensaje de error claro, caché ya existente (`RecomendacionesCache`) con TTL visible, botón "reintentar".
- [ ] Test de contrato del parseo JSON (respuestas con fences, vacías, malformadas).

### Fase 2 – Estabilidad y bugs (~1 semana)
- [ ] Auditar: migraciones Room 2→6 con test (`MigrationTestHelper`), eliminar `fallbackToDestructiveMigration`.
- [ ] Revisar workers (`SheetsSyncWorker`, `AutoBackupWorker`, `ReminderWorker`) y `ExamAlarmReceiver`: permisos de alarmas exactas (Android 12+/14), reprogramación tras reinicio, errores de red/token expirado de Drive/Sheets/Calendar.
- [ ] Validaciones de porcentajes ya iniciadas en v2.1.0: extender a edición y simulador; casos borde de `GradeCalculator` (0 notas, escala letras, redondeo).
- [ ] Release: keystore propio, `allowBackup` revisado, reglas R8 probadas con APK release real.
- [ ] CI GitHub Actions: build + unit tests + lint en cada PR.

### Fase 3 – Diseño y experiencia (~2 semanas)
- [ ] Partir `MateriaDetailScreen`, `CalendarScreen`, `EstadisticasScreen` en composables pequeños + previews.
- [ ] Sistema de diseño: tokens (color, espaciado, tipografía), Material You (dynamic color) opcional, modo oscuro revisado, estados vacío/carga/error consistentes (ya hay `ShimmerLoading`).
- [ ] Home: resumen del semestre arriba (promedio, en riesgo, próximo examen), tarjetas con progreso y color por estado.
- [ ] Onboarding más corto + datos de ejemplo; wizard de materia con plantillas (Colombia 0-5, México 0-10, etc.).
- [ ] Microinteracciones: transiciones de navegación, haptics, undo en borrado (Snackbar) en vez de diálogos.
- [ ] Accesibilidad: contentDescription, contraste, tamaños táctiles, fuentes escaladas.

### Fase 4 – Calidad de vida (~2 semanas)
- [ ] Widgets más útiles y con acciones directas.
- [ ] Recordatorios inteligentes configurables (ya existe `SmartNotificationBuilder`).
- [ ] Importar/Exportar más fácil (compartir PDF/Excel desde detalle).
- [ ] Metas por materia y "qué necesito" accesible desde la tarjeta.
- [ ] Tests UI (Compose) de los 3 flujos críticos: crear materia, ingresar nota, ver promedio.

### Fase 5 – Release
- [ ] v2.2.0: Fases 0-2 (estable + IA arreglada). v2.3.0: Fase 3. v2.4.0: Fase 4.
- [ ] Play Console / APK firmado, changelog, capturas actualizadas.

## 2. Orden recomendado para empezar
1. Fase 0 (rotar key + que compile con JDK 17).
2. Fase 1 (IA funcionando de verdad vía backend).
3. Migraciones/tests (Fase 2) antes de tocar UI, para no perder datos de usuarios.

---

## 3. Estado de avance (sin commit/push)

**Hecho y verificado** (`testDebugUnitTest`, `lintDebug`, `assembleDebug`, `assembleDebugAndroidTest`, backend `gradle test`):
- Modelos de IA actualizados (app + backend); claves de IA solo en debug; `BACKEND_TOKEN` enviado como `X-Gradify-Token`.
- Backend: compilaba mal (dependencia bucket4j inexistente) → corregido; rate-limit por IP, token opcional, Dockerfile multi-stage, test.
- Room: eliminado `fallbackToDestructiveMigration`; test de migración v2→v6 (compila, **no ejecutado**: requiere dispositivo/emulador).
- **Bug real**: los recordatorios del calendario nunca programaban alarmas (`ExamAlarmScheduler` sin uso). Ahora se programan/cancelan al guardar/borrar, se reprograman tras reinicio (`BootReceiver`) y el receiver usa `goAsync`.
- Firma de release configurable por `local.properties`; CI en GitHub Actions; lint sin errores.
- `MateriaDetailScreen` partida en 5 archivos; borrado de materias con "Deshacer" (antes: swipe + diálogo).
- Tests de parseo de respuestas de IA.

**Correcciones al análisis inicial:** el backend sí tenía rate-limit global y `/health`; Home sí tenía tarjeta resumen (`DashboardCard`).

**Pendiente:** deploy del backend y `BACKEND_URL`/`BACKEND_TOKEN`; verificar nombres de modelo con `ListModels`; partir `CalendarScreen`/`EstadisticasScreen`; sistema de diseño/Material You; onboarding/plantillas; widgets; tests de UI Compose; probar R8 en release; ejecutar el test de migración en dispositivo.

**Decisión (APK público en GitHub):** sin claves dentro del APK. Cada usuario pega su clave gratuita de Gemini en Ajustes (`UserPreferencesRepository.geminiApiKey`). El backend queda opcional. Para que el login con Google funcione en el APK público, el release debe firmarse con una keystore propia cuyo SHA-1 esté registrado en Firebase/Google Cloud.

**Rediseño de UI (verificado en emulador, claro y oscuro):** sistema de diseño propio (`ui/components/DesignSystem.kt`): encabezado único, superficies tonales planas, cifras grandes tabulares, estado solo en color de punto/número. Pantallas: Inicio, Detalle, Estadísticas, Calendario, Recursos, Ajustes, Login, Onboarding, Wizard (cabecera), Editar porcentajes, Exportar (cabecera). Pendiente de pulido fino: pasos 2 y 3 del wizard, Exportar, bottom sheets (calculadora, simulador, entrada rápida) y diálogos.

**Avance del plan de mejora (verificado en emulador, R8 incluido):**
- Fase 1: respaldo seguro (transacción, idempotente, JSON escapado, carpeta elegida por el usuario, eventos/meta/notas incluidos) + reglas de manifiesto.
- Fase 2: 0 real visible, escalas con mínimo ≠ 0, aviso si los pesos no suman 100 %, campo de nota sin reformateo ni valores inválidos, 100 % exigido al guardar.
- Fase 3: widgets se refrescan al cambiar notas (observador de Room) y con `goAsync()`.
- Fase 4: cerrar/reabrir semestre (Room v7, migración probada); el promedio ponderado por créditos y el historial ya existían en Estadísticas.
- Fase 5 (parcial): `collectAsStateWithLifecycle`, animaciones acotadas, `distinctUntilChanged`, 48 dp táctiles. Pendiente: textos fijos/errores de ViewModels a recursos, eventos de una sola vez, SavedStateHandle, pruebas de ViewModels.
- Fase 6 (parcial): worker de Sheets eliminado (la subida a Drive de Exportar se conserva), reglas R8 recortadas, PLANIFICACION.md reemplazado por guía en el README.
