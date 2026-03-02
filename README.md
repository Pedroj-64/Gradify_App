<div align="center">

# 📚 Gradify - Sistema de Gestión Académica

### *Tu compañero inteligente para el éxito académico*

[![Android](https://img.shields.io/badge/Android-26%2B-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2025.02.00-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

**Gradify** es una aplicación Android nativa de última generación diseñada para ayudar a estudiantes universitarios a gestionar sus materias, calificaciones y promedios académicos de forma eficiente, intuitiva y profesional.

[🚀 Características](#-características-principales) • [📸 Capturas](#-capturas-de-pantalla) • [🛠️ Tecnologías](#-stack-tecnológico) • [⚙️ Instalación](#-instalación-y-configuración) • [🏗️ Arquitectura](#-arquitectura-del-proyecto) • [👥 Equipo](#-créditos)

</div>

---

## 🎯 ¿Qué es Gradify?

Gradify es mucho más que una simple calculadora de notas. Es un **ecosistema académico completo** que te permite:

- 📊 **Gestionar materias** con sistemas de calificación personalizados (0-5, 0-10, 0-100, letras A-D)
- 🧮 **Calcular promedios** ponderados automáticamente en tiempo real
- 🎯 **Planificar estrategias** con la calculadora "¿Qué nota necesito?"
- 📈 **Visualizar estadísticas** de tu rendimiento académico con gráficos interactivos
- 📅 **Programar exámenes** en un calendario inteligente con recordatorios
- 🤖 **Recibir recomendaciones** de estudio personalizadas con Inteligencia Artificial
- 💾 **Respaldar datos** en Google Sheets y exportar reportes en Excel y PDF
- 🎨 **Disfrutar una UI moderna** con Material Design 3 y tema dinámico

**Dedicado con cariño a todos los estudiantes universitarios y especialmente a "miripili" 💜**

---

## ✨ Características Principales

### 📚 Gestión Académica Completa
- ✅ **Múltiples sistemas de calificación**: Soporte para escalas 0-5 (Colombia), 0-10 (México/España), 0-100 (porcentaje), letras A-D, o escalas personalizadas
- ✅ **Componentes flexibles**: Define parciales, talleres, quices, tareas, proyectos con sus respectivos porcentajes
- ✅ **Sub-notas ilimitadas**: Agrega múltiples intentos, entregas o actividades dentro de cada componente
- ✅ **Cálculo automático**: Promedios ponderados calculados en tiempo real según los porcentajes asignados
- ✅ **Drag & Drop**: Reordena componentes y actividades con gestos táctiles intuitivos

### 🎯 Herramientas Inteligentes
- 🧮 **Calculadora predictiva**: Descubre qué nota necesitas en las próximas actividades para alcanzar tu meta
- 📊 **Dashboard estadístico**: Visualiza tu promedio general, materias aprobadas/en riesgo, progreso del semestre
- 🔔 **Recordatorios personalizados**: Notificaciones antes de exámenes, entregas y eventos importantes
- 🤖 **Recomendaciones con IA**: Sugerencias de estudio basadas en tu rendimiento usando Google Gemini

### ☁️ Sincronización y Respaldo
- 📥 **Backup automático**: Sincronización opcional con Google Sheets en segundo plano
- 📄 **Exportación profesional**: Genera reportes detallados en formato Excel (.xlsx) y PDF
- 🔄 **Trabajo offline**: Toda la funcionalidad disponible sin conexión a internet
- 🔐 **Autenticación segura**: Inicia sesión con tu cuenta de Google

### 🎨 Experiencia de Usuario Premium
- 🌈 **Material You**: Tema dinámico que se adapta al color de tu dispositivo (Android 12+)
- 🌙 **Modo oscuro**: Protege tus ojos con temas claro y oscuro automáticos
- 📱 **Diseño responsive**: Optimizado para teléfonos, phablets y tablets
- 🎭 **Animaciones fluidas**: Transiciones suaves y feedback háptico
- 🌐 **Multiidioma**: Soporte para español e inglés
- 🏠 **Widgets**: Visualiza tus promedios directamente desde la pantalla de inicio

---

## 📸 Capturas de Pantalla

<!-- Aquí puedes agregar capturas más adelante -->
*Próximamente: capturas de pantalla de la aplicación en acción*

---

## 🛠️ Stack Tecnológico

Gradify está construida con las tecnologías más modernas y robustas del ecosistema Android:

### Core
| Categoría | Tecnología |
|-----------|-----------|
| **Lenguaje** | [Kotlin 2.1.0](https://kotlinlang.org/) - 100% Kotlin, null-safety |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI declarativa moderna |
| **Design System** | [Material Design 3](https://m3.material.io/) - Material You con tema dinámico |
| **Min SDK** | Android 8.0 (API 26) |
| **Target SDK** | Android 15 (API 35) |

### Arquitectura
| Capa | Implementación |
|------|---------------|
| **Patrón** | MVVM (Model-View-ViewModel) |
| **Arquitectura** | Clean Architecture (data / domain / presentation) |
| **Inyección de Dependencias** | [Hilt](https://dagger.dev/hilt/) - DI de Google |
| **Navegación** | [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) |
| **Concurrencia** | Kotlin Coroutines + Flow |

### Persistencia & Backend
| Componente | Tecnología |
|------------|-----------|
| **Base de Datos Local** | [Room](https://developer.android.com/training/data-storage/room) - SQLite con type safety |
| **Preferencias** | DataStore (reemplazo de SharedPreferences) |
| **Autenticación** | Google Sign-In (Credential Manager API) |
| **Cloud Sync** | Google Sheets API v4 + Google Drive API |
| **Tareas en Background** | WorkManager - Sincronización periódica |

### Funcionalidades Avanzadas
| Feature | Librería |
|---------|----------|
| **Inteligencia Artificial** | Google Gemini AI (Generative AI SDK) |
| **Exportación Excel** | [Apache POI](https://poi.apache.org/) - Generación de .xlsx |
| **Exportación PDF** | Android PDF API nativa |
| **Drag & Drop** | [sh.calvin.reorderable](https://github.com/Calvin-LL/Reorderable) |
| **Seguridad** | AndroidX Security Crypto (EncryptedSharedPreferences) |
| **Logging** | [Timber](https://github.com/JakeWharton/timber) |

### Testing & CI/CD
- **Unit Testing**: JUnit 5 + MockK
- **UI Testing**: Compose UI Test
- **Code Quality**: Detekt + ktlint

---

## 🏗️ Arquitectura del Proyecto

Gradify implementa **Clean Architecture** con separación estricta de responsabilidades:

```
app/src/main/java/com/notasapp/
├── 📊 data/                           # Capa de Datos
│   ├── local/
│   │   ├── AppDatabase.kt             # Base de datos Room
│   │   ├── dao/                       # Data Access Objects
│   │   │   ├── UsuarioDao.kt
│   │   │   ├── MateriaDao.kt
│   │   │   ├── ComponenteDao.kt
│   │   │   ├── SubNotaDao.kt
│   │   │   └── ExamenEventDao.kt
│   │   ├── entities/                  # Entidades Room (@Entity)
│   │   └── relations/                 # Relaciones 1:N (@Relation)
│   ├── mapper/
│   │   └── EntityMapper.kt            # Entity ↔ Domain Model
│   └── repository/
│       └── MateriaRepositoryImpl.kt   # Implementación del repositorio
│
├── 🎯 domain/                         # Capa de Dominio (lógica de negocio)
│   ├── model/                         # Modelos puros (sin Room)
│   │   ├── Materia.kt
│   │   ├── Componente.kt
│   │   ├── SubNota.kt
│   │   └── TipoEscala.kt
│   ├── repository/
│   │   └── MateriaRepository.kt       # Interfaz del repositorio
│   └── usecase/                       # Casos de uso
│       ├── CalcularPromedioUseCase.kt
│       └── ValidarPorcentajesUseCase.kt
│
├── 🎨 ui/                             # Capa de Presentación
│   ├── auth/                          # Login con Google
│   ├── home/                          # Lista de materias
│   ├── materia/
│   │   ├── create/                    # Wizard de creación (3 pasos)
│   │   ├── detail/                    # Detalle + ingreso de notas
│   │   └── edit/                      # Editar porcentajes (drag & drop)
│   ├── stats/                         # Estadísticas del semestre
│   ├── calendar/                      # Calendario de exámenes
│   ├── recomendaciones/               # Recomendaciones con IA
│   ├── export/                        # Exportar Excel/PDF/Sheets
│   ├── settings/                      # Configuración de la app
│   ├── onboarding/                    # Tutorial de primer uso
│   ├── components/                    # Componentes reutilizables
│   └── theme/                         # Tema Material You
│
├── 🔧 di/                             # Inyección de Dependencias (Hilt)
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── NetworkModule.kt
│
├── 🧭 navigation/                     # Navegación
│   ├── Screen.kt                      # Rutas (sealed class)
│   └── NotasNavGraph.kt               # Grafo de navegación
│
├── 🛠️ utils/                          # Utilidades
│   ├── GradeCalculator.kt             # Lógica de cálculo de promedios
│   ├── ExcelExporter.kt               # Exportación a Excel
│   ├── PdfExporter.kt                 # Exportación a PDF
│   ├── BackupManager.kt               # Backup/Restore JSON
│   └── NotificationHelper.kt          # Notificaciones locales
│
└── 📱 widget/                         # Home Screen Widgets
    └── GradeWidget.kt
```

### Flujo de Datos
```
[UI Layer - Compose] 
       ↕️ StateFlow/LiveData
[ViewModel Layer] 
       ↕️ Casos de Uso
[Domain Layer] 
       ↕️ Repository Interface
[Data Layer - Room DB]
```

---

## ⚙️ Instalación y Configuración

### 📋 Requisitos Previos

- **Android Studio**: Ladybug (2024.3) o superior
- **JDK**: Version 17 o superior
- **Android SDK**: API 35 (Android 15)
- **Cuenta de Google**: Para autenticación y sincronización (opcional)

### 🔧 Configuración del Proyecto

#### 1️⃣ Clonar el Repositorio
```bash
git clone https://github.com/tu-usuario/GradeApp-Android.git
cd GradeApp-Android
```

#### 2️⃣ Abrir en Android Studio
- Abre Android Studio
- Selecciona `File > Open` y elige la carpeta del proyecto
- Espera a que Gradle sincronice las dependencias

#### 3️⃣ Configurar Google Sign-In

Para habilitar la autenticación con Google, necesitas configurar OAuth 2.0:

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto o selecciona uno existente
3. Habilita las siguientes APIs:
   - **Google Sign-In API**
   - **Google Sheets API** (opcional, para sincronización)
   - **Google Drive API** (opcional, para backup)
4. Ve a `Credentials > Create Credentials > OAuth 2.0 Client ID`
5. Selecciona **Android** como tipo de aplicación
6. Ingresa:
   - **Package name**: `com.notasapp`
   - **SHA-1**: Obtenlo con `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
7. Copia el **Web Client ID** generado

#### 4️⃣ Configurar Variables de Entorno

Crea un archivo `local.properties` en la raíz del proyecto (si no existe):

```properties
## Android SDK (auto-generado por Android Studio)
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk

## Google OAuth 2.0 Web Client ID (REQUERIDO para login)
GOOGLE_CLIENT_ID=TU_WEB_CLIENT_ID.apps.googleusercontent.com

## APIs opcionales (dejar vacío si no se usan)
GEMINI_API_KEY=tu_api_key_de_gemini_opcional
BACKEND_URL=
GROQ_API_KEY=
OPENROUTER_API_KEY=
```

> ⚠️ **Importante**: El archivo `local.properties` está en `.gitignore` y nunca debe subirse a GitHub.

#### 5️⃣ Compilar y Ejecutar

```bash
# Compilar en modo debug
./gradlew assembleDebug

# Instalar en dispositivo conectado
./gradlew installDebug

# Ejecutar tests
./gradlew test
```

O simplemente presiona el botón **▶ Run** en Android Studio.

---

## 🎓 Guía de Uso

### Primer Uso

1. **Autenticación**: Inicia sesión con tu cuenta de Google
2. **Tutorial interactivo**: Sigue el onboarding inicial que te guiará por las funcionalidades
3. **Crear tu primera materia**: 
   - Toca el botón ➕ flotante
   - Define nombre, periodo y profesor
   - Selecciona la escala de calificación (0-5, 0-10, etc.)
   - Agrega los componentes (parciales, talleres, etc.) con sus porcentajes

### Gestionar Calificaciones

1. **Ver materia**: Toca una tarjeta de materia en el inicio
2. **Agregar notas**: Toca el ícono ✏️ junto a cada componente
3. **Ver promedio**: El promedio se actualiza automáticamente en tiempo real
4. **Calculadora**: Usa el botón 🧮 para calcular qué nota necesitas

### Estadísticas y Reportes

- **Dashboard**: Visualiza tu promedio general en la pestaña "Estadísticas"
- **Exportar Excel**: Desde el menú de una materia → Exportar → Excel
- **Exportar PDF**: Genera reportes profesionales en PDF
- **Sincronizar**: Conecta con Google Sheets para respaldo automático

---

## 📊 Roadmap y Estado del Proyecto

### ✅ Fase 1 - MVP (COMPLETADA)
- [x] Estructura MVVM + Clean Architecture
- [x] Base de datos Room con DAOs optimizados
- [x] Modelos de dominio y repositorios
- [x] Inyección de dependencias con Hilt
- [x] Sistema de navegación completo
- [x] Autenticación con Google Sign-In
- [x] Pantalla de inicio con lista de materias
- [x] Wizard de creación (3 pasos)
- [x] Vista de detalle con ingreso de notas
- [x] Cálculo automático de promedios ponderados
- [x] UI con Material Design 3

### ✅ Fase 2 - Experiencia de Usuario (COMPLETADA)
- [x] Animaciones fluidas (stagger, fade, slide)
- [x] Drag & drop con feedback háptico
- [x] Calculadora "¿Qué nota necesito?"
- [x] Modo oscuro y tema dinámico
- [x] Pantalla de estadísticas avanzadas
- [x] Componentes reutilizables (ShimmerLoading, GradeBadge, etc.)
- [x] Skeleton screens para carga

### ✅ Fase 3 - Cloud & Exportación (COMPLETADA)
- [x] Exportación a Excel (.xlsx) con Apache POI
- [x] Exportación a PDF con formato profesional
- [x] Integración con Google Sheets API
- [x] Sincronización automática con WorkManager
- [x] Modo offline-first con sincronización en background

### ✅ Fase 4 - Funcionalidades Avanzadas (COMPLETADA)
- [x] Sistema de notificaciones inteligente
- [x] Calendario de exámenes con recordatorios
- [x] Widget de pantalla de inicio
- [x] Recomendaciones de estudio con IA (Gemini)
- [x] Backup/Restore completo en JSON
- [x] Sistema multiidioma (ES/EN)
- [x] Soporte para tablets y landscape

### 🚀 Fase 5 - Mejoras Futuras (EN PLANIFICACIÓN)
- [ ] Modo de estudio con Pomodoro integrado
- [ ] Compartir progreso en redes sociales
- [ ] Comparación anónima con compañeros
- [ ] Integración con calendarios institucionales
- [ ] App para tablets con vista de múltiples materias
- [ ] Versión web con sincronización en tiempo real
- [ ] Gamificación (logros, racha de estudio)

---

## 🔒 Seguridad y Privacidad

Gradify toma en serio la seguridad de tus datos:

- ✅ **OAuth 2.0**: Autenticación segura con Google sin almacenar contraseñas
- ✅ **Encrypted Storage**: Las credenciales se guardan con `EncryptedSharedPreferences`
- ✅ **HTTPS Only**: Todas las comunicaciones con APIs externas usan TLS 1.3
- ✅ **Local First**: Tus calificaciones se almacenan localmente en Room (SQLite)
- ✅ **Permisos mínimos**: Solo solicita permisos estrictamente necesarios
- ✅ **Sin tracking**: No hay analytics de terceros ni recopilación de datos personales
- ✅ **Código abierto**: Puedes auditar el código completo en GitHub
- ✅ **Proguard**: Ofuscación de código en releases para mayor seguridad

> 💡 **Nota**: La sincronización con Google Sheets es completamente opcional. Puedes usar la app sin conexión a internet.

---

## 🤝 Cómo Contribuir

¡Las contribuciones son bienvenidas! Si quieres mejorar Gradify:

1. **Fork** el repositorio
2. Crea una **rama** para tu feature (`git checkout -b feature/amazing-feature`)
3. **Commit** tus cambios (`git commit -m 'Add: nueva funcionalidad increíble'`)
4. **Push** a la rama (`git push origin feature/amazing-feature`)
5. Abre un **Pull Request**

### Lineamientos de Código
- Usa **Kotlin** idiomático y best practices
- Sigue los principios **SOLID** y **Clean Architecture**
- Documenta funciones públicas con **KDoc**
- Agrega **tests unitarios** para lógica crítica
- Usa **ktlint** para mantener el estilo consistente

---

## 🐛 Reportar Bugs

Si encuentras un error, por favor abre un [Issue en GitHub](https://github.com/Pedroj-64/GradeApp-Android/issues) incluyendo:

- Descripción del problema
- Pasos para reproducirlo
- Comportamiento esperado vs. actual
- Capturas de pantalla (si aplica)
- Versión de Android y modelo de dispositivo

---

## 📱 Soporte y Contacto

- **Email**: pj245668@gmail.com
- **GitHub Issues**: [Abrir ticket](https://github.com/Pedroj-64/GradeApp-Android/issues)
- **Documentación**: [Wiki del proyecto](https://github.com/Pedroj-64/GradeApp-Android/wiki)

---

## 👥 Créditos

### 👨‍💻 Equipo de Desarrollo

Este proyecto fue desarrollado con dedicación y pasión por estudiantes de **Ingeniería de Sistemas y Computación** de la **Universidad del Quindío**, Armenia, Colombia.

#### 🏆 Desarrollador Principal
**Pedro José Soto Rivera**  
*Basic Java, JavaScript, Python, Elixir/Erlang , React, Spring (noob),Kotlin and Gradle, C#. Junior Cybersecurity Analyst*  
Arquitectura, desarrollo frontend & backend, integración de APIs, CI/CD

#### 🌟 Agradecimientos Especiales

Un reconocimiento especial a quienes contribuyeron con ideas, pruebas y apoyo durante el desarrollo:

- **Mateo Gómez Marulanda** - Testing, feedback de UX y diseño
- **Santiago Padilla Ríos** - Testing, ideas de funcionalidades y validación

#### 💜 Dedicatoria

*Este proyecto está dedicado con amor a todos los estudiantes universitarios que luchan cada semestre por alcanzar sus metas académicas, y especialmente a **"miripili"**, quien inspiró la creación de esta herramienta.*

---

### 🎓 Universidad del Quindío

<div align="center">

**Programa de Ingeniería de Sistemas y Computación**  
*Facultad de Ingeniería*  
Armenia, Quindío, Colombia 
**Hecho con mucho love and bareta in the hood, i love linux** 

**"Ciencia, Educación y Cultura"**

</div>

---

## 📄 Licencia

Este proyecto está licenciado bajo la **MIT License** - ve el archivo [LICENSE](LICENSE) para más detalles.

```
MIT License

Copyright (c) 2026 Pedro José Soto Rivera

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 🌟 ¿Te gusta el proyecto?

Si Gradify te ha sido útil, considera:

- ⭐ Dar una **estrella** al repositorio
- 🐛 Reportar **bugs** y sugerir mejoras
- 📢 **Compartir** con otros estudiantes
- 💻 **Contribuir** con código o documentación

---

<div align="center">

### Hecho con ❤️ por estudiantes, para estudiantes

**Gradify** - *Tu aliado académico hacia el éxito* 🎓

[⬆ Volver arriba](#-gradify---sistema-de-gestión-académica)

---

**© 2026 Pedro José Soto Rivera | Universidad del Quindío**

</div>
