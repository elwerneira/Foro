# Arquetipo de Foro

Esta plantilla permite generar otro proyecto con la estructura de nuestro microservicio: modelo, repositorio, servicio, seguridad y controladores. Incluye pruebas con H2, SQL y Dockerfile. No contiene la wallet ni la contraseña de Oracle.

Desde la carpeta del proyecto, con Java y Python instalados:

```powershell
python scripts/actualizar-arquetipo.py
.\mvnw.cmd -B -ntp -f arquetipo/pom.xml install
.\mvnw.cmd -B -ntp org.apache.maven.plugins:maven-archetype-plugin:3.4.1:generate "-DarchetypeGroupId=cl.duoc.fullstack" "-DarchetypeArtifactId=foro-arquetipo" "-DarchetypeVersion=1.0.0" "-DgroupId=cl.duoc.entrega" "-DartifactId=foro-generado" "-Dversion=1.0.0" "-Dpackage=cl.duoc.entrega.foro" "-DinteractiveMode=false" "-DoutputDirectory=target/proyectos"
.\mvnw.cmd -B -ntp -f target/proyectos/foro-generado/pom.xml test
```

`python` debe apuntar a una instalación real; si Windows abre la tienda, usar `py` o la ruta del ejecutable instalado.

Las pruebas usan H2 y no alteran Oracle. Para ejecutar la API generada con Oracle hay que agregar su wallet y completar `config/application-local.properties` tomando el archivo de ejemplo.

GitHub Actions genera el proyecto, ejecuta sus pruebas y conserva esas fuentes como un artefacto. La etapa de publicación construye la imagen desde esas mismas fuentes generadas. Hace falta publicar el workflow y comprobar su ejecución para demostrarlo en GitHub.
