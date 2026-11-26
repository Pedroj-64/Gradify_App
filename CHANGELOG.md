# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).

## [3.0.1] - 2026-11-26

> Primera versión publicada de la 3.x (el tag `v3.0.0` no llegó a publicarse por un fallo del CI al verificar la firma).
>
> **Al actualizar:** el número grande de cada materia ahora es tu **rendimiento en lo evaluado** (antes mostraba el acumulado). Si llevas 4.5 en un corte del 20 %, verás 4.5 y no 0.9 «en riesgo». El acumulado sigue a la vista debajo. Se instala encima de la 2.2.0 sin perder nada.

### Añadido
- **Axo, el ajolote:** mascota de Gradify en el inicio, la bienvenida, los estados vacíos, la calculadora y el simulador, con una cara según cómo vas.
- **Color e identidad:** resumen de Inicio con degradado de marca, color propio por materia, estados en color y un color por tipo de evento en el calendario.
- **Aviso de versión nueva:** al abrir la app, Gradify pregunta a GitHub si hay una actualización y la descargas con un toque (se apaga en Ajustes → Actualizaciones).
- **Crear varias materias seguidas:** recuerda el periodo y la escala, «Copiar cortes y pesos de…» otra materia y «Guardar y crear otra».
- **Deshacer** al borrar una nota o una parte de una nota.
- Botón **«Repartir lo que falta»** para completar el 100 % de los porcentajes.
- Atajos visibles en cada materia: poner notas, «¿Y si saco…?» y «¿Qué necesito?».
- Aviso para elegir una carpeta de respaldo cuando tienes 3 o más materias sin respaldo.
- Mantener pulsada una materia abre un menú: abrir, cerrar semestre o eliminar.

### Cambiado
- **Porcentajes y notas se escriben** (con coma o punto) en vez de usar barras deslizantes; los porcentajes admiten decimales como 0,5 % o 33,33 % en todos los campos.
- La calculadora abre ya calculada con tu meta o con la nota para pasar, y el simulador calcula en vivo.
- El calendario usa los selectores de fecha y hora del teléfono (antes eran botones ±1 día y ±1 hora, sin minutos).
- La escala por país es una lista desplegable.
- Textos nuevos con la voz de Axo, en español e inglés; ya no quedan textos fijos en español con la app en inglés.
- Tocar la notificación de un examen o el widget de materia abre esa materia.

### Corregido
- La **meta de una materia** no se podía poner: el diálogo abría directo en «¿Quitar meta?».
- Al agregar una nota o dividirla en partes, el porcentaje **solo aceptaba números enteros**.
- La entrada rápida borraba la fila mientras escribías la nota y no aceptaba coma decimal; la calculadora y el simulador tampoco.
- Un corte con solo parte de sus notas contaba como evaluado entero.
- El widget de resumen usaba umbrales de la escala 0–5 en todas las escalas.
- «1 materias» y otros plurales.
- La etiqueta del peso al agregar una nota o dividirla en partes mostraba «%%» en vez de «%», y el peso de cada parte se mostraba redondeado (20,5 % salía como 20 %).

### Eliminado
- Borrar materias deslizando la tarjeta (se borraban sin querer); ahora es desde el menú de mantener pulsado.

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
