# Gradify: qué pone cada quien

Gradify es open source. El proyecto ofrece **el código y los APK de las releases**. Todo lo demás (claves de API, backend, proyecto de Firebase, firma, bases de datos) lo organiza y paga cada persona que compile o despliegue su propia versión. Nada de eso vive en este repositorio.

| Recurso | ¿Obligatorio? | Dónde se configura | Quién lo pone |
|---|---|---|---|
| Notas, materias, exámenes | Sí | Room local en el teléfono | La app, sin servidor |
| Respaldo JSON | No | Carpeta que elige el usuario en Ajustes | El usuario |
| Clave de IA (Gemini/Groq/OpenRouter) | No | Ajustes → Recomendaciones con IA (release) o `local.properties` (debug) | El usuario |
| Backend proxy de IA (`backend/`) | No | `BACKEND_URL` y `BACKEND_TOKEN` en `local.properties` | Quien lo despliegue |
| Login con Google / Sheets | No | `app/google-services.json` + `GOOGLE_CLIENT_ID` | Quien compile |
| Firma de release | Solo para publicar | `key.properties` (+ keystore) | Quien publique |

## Compilar sin nada propio
Copia `google-services.example.json` a `app/google-services.json` y compila con JDK 17 o 21. La app funciona completa con "Continuar sin cuenta"; solo el login con Google exige valores reales.

## Archivos que nunca se suben
`local.properties`, `key.properties`, `*.jks`, `google-services.json`, `.env*`. Ya están en `.gitignore` y CI corre gitleaks.

## Tu propio backend
Ver [`backend/README.md`](../backend/README.md). Es un proxy opcional para no repartir claves de IA; si no lo despliegas, la app usa la clave que pegue cada usuario.

## Tu propia release firmada
1. Genera un keystore: `keytool -genkeypair -v -keystore gradify-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000`.
2. Crea `key.properties` en la raíz con `storePassword`, `keyPassword`, `keyAlias`, `storeFile`.
3. Para login con Google, registra el SHA-1 de ese keystore en tu proyecto de Firebase.
4. En CI, define los secretos `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD` y, si usas Google, `GOOGLE_SERVICES_JSON_BASE64` y la variable `GOOGLE_CLIENT_ID`. Sin ellos la release se firma con la clave debug y no es publicable.
