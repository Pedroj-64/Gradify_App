# Personalidad de Gradify

## Axo, la mascota

Axo es un ajolote. Lo elegimos porque **el ajolote regenera lo que pierde**: una mala nota no es el final, se puede remontar. Esa es la idea de toda la app.

| Cara | Archivo | Cuándo |
|---|---|---|
| Feliz | `axo_feliz` | Aprobó, cumplió la meta, todo listo, semestre cerrado |
| Tranqui | `axo_tranqui` | Bienvenida, va pasando, estados neutros |
| Preocupado | `axo_preocupado` | En riesgo, meta imposible, búsqueda sin resultados |
| Estudiando | `axo_estudiando` | Calcular lo que falta, estadísticas vacías, recursos de estudio |

Los dibujos son vectores originales generados por [`docs/assets/axo/gen.py`](assets/axo/gen.py) (`python3 docs/assets/axo/gen.py app/src/main/res/drawable docs/assets/axo`). En Compose se usa `Axo(AxoMood.FELIZ)` o `EmptyState(..., mood = …)` / `ResultCard(..., mood = …)`.

**No se usan memes ni imágenes de internet**: tienen derechos de autor y Gradify es de código abierto y se publica en tiendas. Para las reacciones con humor está Axo.

## Cómo habla

Gradify es **el compa del salón que sí lleva las cuentas**: cercano, honesto con los números y de tu lado.

- **Tutea** y habla corto. Una idea por frase.
- **Honesto**: si la meta es imposible, lo dice, y propone qué hacer ("prueba con una meta más baja").
- **Optimista sin mentir**: "todavía se puede remontar" solo cuando de verdad queda por evaluar.
- **Nunca se burla de una nota baja ni culpa al usuario.** El humor es para celebrar y para los estados vacíos, no para los errores.
- **Español neutro latino**: sin regionalismos fuertes ("parce", "wey", "che"), porque la app es para toda Latinoamérica.
- **Emojis**: como mucho uno, y solo en celebraciones o ánimo (🎉 🏆 💪 🎯). Nunca en errores.
- **Errores**: qué pasó + qué hacer. "No se pudo guardar la nota. Intenta otra vez." Nada de "Ocurrió un error inesperado".

| En vez de | Mejor |
|---|---|
| ¡Bienvenido a Gradify! | ¡Hola! Soy Axo 👋 |
| No se encontraron materias | Busqué por todas partes y nada. ¿Probamos con otro nombre? |
| Meta inalcanzable | Fuera de alcance · Haría falta 5.8, más que la nota máxima. Prueba con una meta más baja. |
| Sin estadísticas | Aquí van a vivir tus gráficas |
| Ocurrió un error inesperado. Por favor intenta nuevamente. | Algo salió mal. Intenta otra vez. |

En inglés, el mismo tono: casual, corto y amable ("Hi! I'm Axo", "Looked everywhere, nothing.").
