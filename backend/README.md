# Gradify Backend

Proxy ligero que oculta la API key de Gemini del APK.

## Configuración

1. Crear variable de entorno o agregar en `application.yml`:
   ```
   GEMINI_API_KEY=tu_clave_de_gemini
   ```

2. Construir:
   ```bash
   ./gradlew bootJar
   ```

3. Ejecutar:
   ```bash
   java -jar build/libs/gradify-backend-1.0.0.jar
   ```

4. Docker:
   ```bash
   docker build -t gradify-backend .
   docker run -p 8080:8080 -e GEMINI_API_KEY=tu_clave gradify-backend
   ```

## Endpoint

```
POST /api/v1/recomendaciones
Content-Type: application/json

{
  "nombreMateria": "Cálculo Diferencial",
  "periodo": "2025-1",
  "promedio": 3.5,
  "notaAprobacion": 3.0,
  "aprobado": true
}
```

## Seguridad y errores

- `APP_TOKEN` (opcional): si se define, el backend exige el header `X-Gradify-Token` con ese valor. Es una **guarda blanda**: el token viaja dentro del APK, así que solo frena el uso casual, no a quien descompile la app.
- Rate limit por IP: `rate-limit.requests-per-minute` (30) y `rate-limit.burst-capacity` (5) en `application.yml`.
- Errores: JSON `{"error": "...", "message": "...", "retryable": bool}`.

| Estado | `error` | La app |
|---|---|---|
| 401 | `UNAUTHORIZED` | Muestra "token inválido" y **no** cae a claves directas |
| 429 | rate limit | Muestra "demasiadas solicitudes" y **no** cae a claves directas |
| 403 / 503 | `API_KEY_INVALID` / `AI_ERROR` | Intenta con la clave propia del usuario, si la tiene |

## Configuración Android

En `local.properties` del proyecto Android:
```
BACKEND_URL=https://tu-servidor.com
BACKEND_TOKEN=el_mismo_valor_de_APP_TOKEN   # opcional
```

La app usa el backend primero. Con 401/429 se detiene con un mensaje claro; con otros fallos usa la clave de IA del propio usuario (Ajustes) como alternativa.
