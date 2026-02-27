# GradeApp - App de Notas Academicas para Android
> Planificacion tecnica y de producto | Fecha: 2026-02-27

---

## 1. RESUMEN DEL PROYECTO

App Android nativa en **Kotlin + Jetpack Compose** que permite al usuario:
- Iniciar sesion con su cuenta Google
- Gestionar materias y sus sistemas de calificacion
- Ingresar notas con porcentajes configurables
- Calcular promedios ponderados automaticamente
- Almacenar todo localmente (Room DB) con sincronizacion opcional a **Google Sheets**

---

## 2. ES POSIBLE? SI, 100%

| Funcionalidad                              | Viable? | Tecnologia                          |
|--------------------------------------------|---------|--------------------------------------|
| Login con Google                           | Si      | Google Identity Services / Firebase  |
| UI interactiva y bonita                    | Si      | Jetpack Compose + Material Design 3  |
| Escalas de notas personalizables           | Si      | Room DB + logica Kotlin              |
| Porcentajes movibles (drag & drop)         | Si      | Compose `LazyColumn` + `reorderable` |
| Calcular promedios ponderados              | Si      | Logica pura Kotlin                   |
| Guardar localmente sin internet            | Si      | Room Database (SQLite local)         |
| Exportar / sincronizar con Google Sheets   | Si      | Google Sheets API v4                 |
| Crear archivos Excel (.xlsx)               | Si      | Apache POI o libreria `xlsx-creator` |
| Funcionar offline + sync cuando hay red    | Si      | WorkManager + Room + Sheets API      |

---

## 3. STACK TECNOLOGICO

```
Lenguaje:       Kotlin
UI:             Jetpack Compose + Material Design 3
Arquitectura:   MVVM + Clean Architecture
Base de datos:  Room Database (SQLite local)
Auth:           Google Sign-In (Credential Manager API)
Cloud sync:     Google Sheets API v4
Excel export:   Apache POI (.xlsx)
Background:     WorkManager (sync automatico)
DI:             Hilt (inyeccion de dependencias)
Nav:            Navigation Compose
```

---

## 4. PANTALLAS Y FLUJO DE USUARIO

### Flujo principal:
```
[Splash] --> [Login Google] --> [Home - Lista de Materias]
                                        |
                          +-------------+-------------+
                          |             |             |
                   [Nueva Materia]  [Ver Materia]  [Configurar]
                          |             |
                  [Configurar      [Ver Notas +
                   escala +         Promedio]
                   componentes]         |
                                  [Editar notas /
                                   porcentajes]
```

### Pantallas detalladas:

#### 4.1 Splash Screen
- Logo de la app, animacion suave
- Verificar si ya hay sesion activa

#### 4.2 Login
- Boton "Iniciar sesion con Google" (Material Design)
- Solicitar permisos: almacenamiento + Google Sheets (opcional)

#### 4.3 Home - Mis Materias
- Lista de cards con cada materia
- Badge con el promedio actual de cada materia
- FAB (+) para agregar materia
- Menu hamburguesa: Perfil / Backup / Configuracion

#### 4.4 Nueva Materia (Wizard en 3 pasos)

**Paso 1 - Info basica:**
```
Nombre de la materia: [Matematicas        ]
Semestre/Periodo:     [2026-1             ]
Profesor (opcional):  [                   ]
```

**Paso 2 - Escala de calificacion:**
```
¿Que escala de notas usa esta materia?

  O  De 0 a 5  (tipica Colombia)
  O  De 0 a 10 (tipica Mexico/Espana)
  O  De A a D  (letras)
  O  De 0 a 100 (porcentaje)
  O  Personalizada --> [min] a [max], aprobacion: [nota minima]

  Nota minima para aprobar: [ 3.0 ]
```

**Paso 3 - Componentes / Cortes:**
```
¿Cuantos componentes/cortes tiene?  [3  v]

  Componente 1: [ Primer corte   ]  Porcentaje: [ 30% ]
  Componente 2: [ Segundo corte  ]  Porcentaje: [ 30% ]
  Componente 3: [ Examen final   ]  Porcentaje: [ 40% ]

  Total: 100%  ✓

  [+ Agregar componente]
  [Guardar materia]
```

#### 4.5 Vista de Materia - Detalle
```
MATEMATICAS                    Promedio: 3.8 / 5.0
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[Primer corte  30%]   Nota: 3.5   Aporte: 1.05
  - Taller 1  (20%)  4.0
  - Parcial   (80%)  3.3

[Segundo corte 30%]   Nota: 4.0   Aporte: 1.20
  - Quiz      (30%)  4.5
  - Parcial   (70%)  3.8

[Examen final  40%]   Nota: --    Aporte: --
  - [Ingresar nota]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Promedio actual:  2.25 / 5.0
Nota necesaria en el final para aprobar: 2.44
```

#### 4.6 Editar Porcentajes (drag & drop)
- Lista reordenable de componentes
- Sliders para ajustar % en tiempo real
- Validacion: total siempre debe sumar 100%

#### 4.7 Calculadora "¿Que necesito?"
- Input: nota meta (ej: quiero sacar 4.0 al final)
- Output: calcula que nota necesita en los componentes faltantes

#### 4.8 Exportar / Sync
```
  [Exportar a Excel (.xlsx)]     --> descarga al dispositivo
  [Abrir en Google Sheets]       --> crea/actualiza hoja en Drive
  [Copiar enlace de la hoja]
  [Sync automatico: ON/OFF]
```

---

## 5. MODELOS DE DATOS (Room DB)

```kotlin
// Entidad: Usuario
@Entity
data class Usuario(
    @PrimaryKey val googleId: String,
    val nombre: String,
    val email: String,
    val fotoUrl: String?
)

// Entidad: Materia
@Entity
data class Materia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: String,
    val nombre: String,
    val periodo: String,
    val profesor: String?,
    val escalaMin: Float,       // ej: 0.0
    val escalaMax: Float,       // ej: 5.0
    val notaAprobacion: Float,  // ej: 3.0
    val tipoEscala: String,     // "NUMERICO" | "LETRAS" | "PORCENTAJE"
    val googleSheetsId: String? // ID de la hoja vinculada
)

// Entidad: Componente (corte/parcial)
@Entity
data class Componente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val materiaId: Long,
    val nombre: String,
    val porcentaje: Float,  // 0.0 a 1.0 (ej: 0.30 = 30%)
    val orden: Int
)

// Entidad: SubNota (nota dentro de un componente)
@Entity
data class SubNota(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val componenteId: Long,
    val descripcion: String,
    val porcentajeDelComponente: Float,
    val valor: Float?  // null si aun no se ha ingresado
)
```

---

## 6. LOGICA DE CALCULO

```kotlin
// Promedio ponderado de un componente
fun calcularPromedioComponente(subNotas: List<SubNota>): Float? {
    val ingresadas = subNotas.filter { it.valor != null }
    if (ingresadas.isEmpty()) return null
    return ingresadas.sumOf { it.valor!! * it.porcentajeDelComponente }.toFloat()
}

// Promedio total de la materia
fun calcularPromedioMateria(componentes: List<ComponenteConSubNotas>): Float? {
    val conNota = componentes.filter { it.promedio != null }
    if (conNota.isEmpty()) return null
    return conNota.sumOf { it.promedio!! * it.componente.porcentaje }.toFloat()
}

// Nota necesaria en el componente faltante para alcanzar meta
fun calcularNotaNecesaria(
    componentesFaltantes: List<Componente>,
    promedioActualPonderado: Float,
    metaFinal: Float,
    escalaMax: Float
): Map<Long, Float> { ... }
```

---

## 7. INTEGRACION GOOGLE SHEETS

### Permisos necesarios (OAuth 2.0 scopes):
```
https://www.googleapis.com/auth/spreadsheets
https://www.googleapis.com/auth/drive.file
```

### Estructura de la hoja generada:
```
Hoja: "Matematicas - 2026-1"

| Componente     | Peso | SubNota        | Peso(comp) | Nota | Aporte |
|----------------|------|----------------|------------|------|--------|
| Primer corte   | 30%  | Taller 1       | 20%        | 4.0  | ...    |
|                |      | Parcial        | 80%        | 3.3  | ...    |
|                |      | SUBTOTAL       |            | 3.5  | 1.05   |
| Segundo corte  | 30%  | ...            | ...        | ...  | ...    |
| PROMEDIO FINAL |      |                |            | 3.8  |        |
```

### Flujo de sync:
```
App (Room) --[WorkManager]--> Google Sheets API
                |
                +-- Si no existe hoja: crear nueva en Drive
                +-- Si existe: actualizar celdas correspondientes
                +-- Manejar conflictos: "La nube tiene cambios, ¿sobreescribir?"
```

---

## 8. FASES DE DESARROLLO

### Fase 1 - MVP (3-4 semanas)
- [ ] Setup proyecto Android + Hilt + Room + Compose
- [ ] Pantalla Login con Google (Credential Manager)
- [ ] CRUD de Materias
- [ ] Wizard de configuracion (escala + componentes + porcentajes)
- [ ] Ingreso de notas y calculo de promedio
- [ ] UI basica pero funcional

### Fase 2 - UX Pulida (2-3 semanas)
- [ ] Animaciones Compose (transiciones, loading states)
- [ ] Drag & drop para reordenar componentes
- [ ] Sliders de porcentaje con validacion en tiempo real
- [ ] Calculadora "¿Que nota necesito?"
- [ ] Modo oscuro
- [ ] Pantalla de estadisticas (promedio acumulado del semestre)

### Fase 3 - Cloud & Export (2-3 semanas)
- [ ] Exportar a .xlsx (Apache POI)
- [ ] Integracion Google Sheets API
- [ ] Sync manual (boton)
- [ ] Sync automatico (WorkManager, cada 24h o al cerrar app)
- [ ] Manejo de errores de red (offline-first)

### Fase 4 - Extras (1-2 semanas)
- [ ] Notificaciones: "Te falta 1 semana para tu parcial"
- [ ] Widget en pantalla principal con promedio
- [ ] Compartir hoja de notas (link de Sheets)
- [ ] Backup/Restore desde Google Drive (archivo JSON)

---

## 9. ESTRUCTURA DEL PROYECTO

```
app/
  src/main/
    java/.../gradeapp/
      data/
        local/
          AppDatabase.kt
          dao/           # UsuarioDao, MateriaDao, ComponenteDao, SubNotaDao
          entities/      # Usuario, Materia, Componente, SubNota
        remote/
          GoogleSheetsService.kt
          SheetsRepository.kt
        repository/
          MateriaRepository.kt
      domain/
        model/           # modelos de dominio (no Room)
        usecase/         # CalcularPromedioUseCase, SyncSheetsUseCase, etc.
      ui/
        auth/            # LoginScreen, LoginViewModel
        home/            # HomeScreen, HomeViewModel
        materia/
          create/        # CreateMateriaWizard, CreateMateriaViewModel
          detail/        # MateriaDetailScreen, MateriaDetailViewModel
          edit/          # EditPorcentajesScreen
        export/          # ExportScreen, ExportViewModel
        components/      # Componentes UI reutilizables
      di/                # Modulos Hilt
      utils/
        ExcelExporter.kt
        PromedioCalculator.kt
    res/
      values/themes.xml  # Material You theming
```

---

## 10. DEPENDENCIAS CLAVE (build.gradle)

```kotlin
// Jetpack Compose BOM
implementation(platform("androidx.compose:compose-bom:2025.xx.xx"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Room
implementation("androidx.room:room-runtime:2.6.x")
implementation("androidx.room:room-ktx:2.6.x")
ksp("androidx.room:room-compiler:2.6.x")

// Google Sign-In (Credential Manager)
implementation("androidx.credentials:credentials:1.3.x")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.x")

// Google Sheets API
implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0")
implementation("com.google.api-client:google-api-client-android:2.x.x")

// Apache POI (Excel)
implementation("org.apache.poi:poi-ooxml:5.2.5") // reducida para Android

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.x")

// Hilt
implementation("com.google.dagger:hilt-android:2.5x")
ksp("com.google.dagger:hilt-compiler:2.5x")

// Navigation
implementation("androidx.navigation:navigation-compose:2.8.x")

// Reorderable (drag & drop)
implementation("sh.calvin.reorderable:reorderable:2.x.x")
```

---

## 11. CONSIDERACIONES IMPORTANTES

### Seguridad:
- El `client_id` de Google OAuth va en `strings.xml` (no hardcodeado en codigo)
- Los tokens de acceso se almacenan en `EncryptedSharedPreferences`
- La base de datos Room puede encriptarse con SQLCipher si se necesita

### Offline-first:
- La app funciona 100% sin internet (Room como fuente de verdad)
- Cuando hay conexion, WorkManager sincroniza en background
- Conflictos: "timestamp" en cada registro para resolver cual es mas reciente

### Apache POI en Android:
- La version completa es muy pesada; usar `poi-ooxml` con exclusion de dependencias innecesarias
- Alternativa mas liviana: libreria `xlsx-creator` o generar CSV y convertir

---

## 12. SIGUIENTE PASO

Con esta planificacion, el siguiente paso es:

1. Crear el proyecto Android base en Android Studio
2. Configurar Hilt + Room + Compose
3. Implementar el Login con Google
4. Construir el wizard de creacion de materia

**¿Empezamos a codear?**
