# Contribuir a Gradify

¡Gracias por ayudar! Gradify es una app Android (Kotlin, Compose, Room, Hilt) para llevar notas con la escala de cada país. Antes de empezar mira el [ROADMAP](ROADMAP.md) y las tareas en [`tasks/todo.md`](tasks/todo.md). Los issues con `good first issue` son buenos para empezar.

Idioma: issues y PR en español o inglés. La documentación principal está en español (`README.md`) con versión en inglés (`README.en.md`).

## Preparar el entorno
1. **JDK 17 o 21** (AGP 8.7.3 no soporta JDK 25).
2. Clona y copia la plantilla de Firebase: `cp google-services.example.json app/google-services.json` (sirve para compilar; el login con Google no funcionará con valores de ejemplo).
3. Crea `local.properties` (no se sube):
   ```properties
   sdk.dir=/ruta/a/Android/Sdk
   # Opcionales, solo para depuración (en release se ignoran):
   GEMINI_API_KEY=
   GOOGLE_CLIENT_ID=
   ```
4. Compila y prueba:
   ```bash
   ./gradlew testDebugUnitTest lintDebug assembleDebug
   cd backend && gradle test   # backend opcional, Gradle 8.9 (sin wrapper)
   ```
   Un `assembleRelease` local sin `key.properties` queda firmado con la clave de debug y no es publicable.

## Flujo de trabajo
1. Abre o comenta un issue antes de cambios grandes.
2. Rama desde `main`: `feature/…`, `fix/…`, `docs/…`.
3. Commits pequeños con mensaje claro (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`).
4. Antes del PR: tests y lint verdes, sin claves ni `google-services.json` reales.
5. Abre el PR con la plantilla y adjunta capturas si tocas UI (claro y oscuro).

## Reglas de código
- Arquitectura `data` / `domain` / `ui`; la lógica de notas va en `domain` con tests unitarios.
- Textos de UI en `res/values/strings.xml` **y** `values-en`, nunca literales en Kotlin; los errores de ViewModel no muestran excepciones crudas.
- Componentes visuales desde `ui/components/DesignSystem.kt`; sin colores `Color(0xFF…)` sueltos.
- Accesibilidad: `contentDescription` traducible, objetivos táctiles ≥ 48 dp.
- Cambios de esquema Room: sube la versión, añade migración y el JSON en `app/schemas/`; nunca `fallbackToDestructiveMigration`.
- No agregues dependencias si la plataforma o las ya instaladas lo resuelven.

## Seguridad
Nunca subas claves ni `google-services.json`. Vulnerabilidades: ver [SECURITY.md](SECURITY.md).

## Conducta
Ver [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
