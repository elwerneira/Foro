# Pruebas para el video de Semana 5

Importar los dos archivos JSON en Postman. Son colecciones distintas: Usuarios tiene 10 solicitudes y Foro tiene 17. Ejecutarlas en su orden, con el laboratorio iniciado y Oracle disponible.

Las variables de cada colección ya apuntan a AWS. Si cambia la IP de EC2, modificar `baseUrl` conservando `/api/usuarios` o `/api/foro`. Para probar los servidores Java locales, usar `http://localhost:8081` en Usuarios y `http://localhost:8082` en Foro. Con Docker local, los puertos son 9081 y 9082, sin el prefijo `/api`.

Basic Auth y los cuerpos JSON están preparados con cuentas académicas. No usar credenciales de Oracle en Postman. El login verifica la cuenta; no entrega un token: cada solicitud protegida incluye Basic Auth.

Los POST guardan sus IDs en variables. Los PUT y DELETE usan esos IDs, sin asumir que son 1 o que tema y categoría tienen el mismo número. Si falla una creación, detenerse y resolver el error antes de continuar. Las solicitudes dependientes se omiten cuando falta un ID. Revisar los resultados en Test Results.

Las colecciones crean registros temporales. Sus solicitudes finales eliminan solamente los registros creados en esa ejecución; no cambiar manualmente los IDs por registros existentes. Ejecutar una colección por vez. Si se interrumpe el video, conservar sus variables para completar la limpieza.

Las colecciones están preparadas; todavía falta realizar la demostración de Postman. El frontend funciona por separado de las API.
