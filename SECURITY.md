# Política de seguridad

## Reportar una vulnerabilidad
No abras un issue público. Usa **GitHub → Security → Report a vulnerability** en este repositorio. Responderemos lo antes posible.

## Alcance
App Android y backend opcional (`backend/`). Notas importantes:
- `BACKEND_TOKEN` compilado en el APK es una guarda blanda y puede extraerse; la defensa real del proxy es el rate-limit.
- Las claves de IA se compilan solo en builds de debug; en release cada usuario pone la suya en Ajustes.
- Si encuentras una clave o credencial en el repo o su historial, repórtala por el mismo canal.
