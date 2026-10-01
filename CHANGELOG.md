# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/). Contacto: kelequel@gmail.com

## [2.2.0] - 2026-10-01

> **Importante al actualizar:** esta es la primera versión firmada con la llave oficial de Gradify. Si tienes instalada una versión anterior (firmada con otra llave), Android no permitirá actualizar encima. Antes de desinstalarla, entra a *Ajustes → Exportar backup* y guarda el archivo; después de instalar esta versión usa *Restaurar backup*. Desde ahora las actualizaciones se instalan encima sin perder nada.

### Añadido
- **Modo sin cuenta:** "Continuar sin cuenta" en la pantalla de inicio; iniciar sesión con Google es opcional.
- **Respaldo automático en una carpeta que tú eliges** (Ajustes → Carpeta de respaldos automáticos). Sobrevive a desinstalar la app.
- **Cerrar semestre:** los semestres cerrados salen de Inicio y siguen contando en las estadísticas (chip «Archivados»).
- Renombrar materias, cortes y notas; editar nombre, periodo, profesor y créditos de una materia.
- Deshacer al borrar una materia.
- Clave de Gemini propia en Ajustes (se guarda solo en el teléfono) y colores del sistema (Material You) opcionales.
- Los widgets se actualizan al guardar una nota.

### Cambiado
- Rediseño completo de la interfaz (modo claro y oscuro) con tipografía Plus Jakarta Sans.
- Restaurar un backup dos veces ya no duplica materias; la importación es todo o nada.
- El respaldo incluye metas, notas, semestres cerrados y eventos del calendario.

### Corregido
- Cierre de la app al tocar «Iniciar sesión con Google» sin configuración.
- Los recordatorios del calendario nunca sonaban; ahora se programan y se restauran tras reiniciar el teléfono.
- Una nota de 0 se mostraba como «--»; el campo de nota aceptaba valores inválidos y se reformateaba al escribir.
- La calculadora «qué nota necesito» no respetaba escalas con mínimo distinto de 0 ni avisaba si los cortes no sumaban 100 %.
- Botón «Saltar» del onboarding inaccesible bajo la barra de estado.

### Eliminado
- Sincronización periódica con Google Sheets (nunca estuvo activa).

## [2.1.1]
- Correcciones y mejoras menores.
