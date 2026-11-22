# Microservicio de Foro

Proyecto de la Semana 5 de Desarrollo Full Stack III, continuando el trabajo de Semana 2. Es parte de un sistema de foros en línea y permite trabajar con categorías, temas y comentarios.

El objetivo es practicar las operaciones de una API, guardar los datos en Oracle y probar sus respuestas desde Postman. Este servicio utiliza el puerto **8082** y se comunica con Usuarios, que funciona en el **8081**, para validar las cuentas.

## Qué permite hacer

- Consultar categorías, temas y comentarios sin iniciar sesión.
- Buscar temas por palabras del título o del contenido.
- Publicar temas y agregar comentarios con una cuenta autenticada.
- Modificar y eliminar contenido propio, o gestionarlo como ADMIN.
- Crear, modificar y eliminar categorías con el rol ADMIN.

No se permite eliminar un tema con comentarios ni una categoría con temas relacionados. Así se evita perder información asociada.

## Tecnologías y organización

Se utiliza Java 21 como versión de compilación, Spring Boot, Spring Web MVC, Spring Data JPA, Spring Security, Validation y Oracle Cloud. Maven Wrapper viene incluido.

| Carpeta | Contenido |
| --- | --- |
| `src/main/java/foro/service/model` | Categoría, Tema y Comentario |
| `src/main/java/foro/service/repository` | Acceso a los datos y búsqueda |
| `src/main/java/foro/service/service` | Reglas del foro |
| `src/main/java/foro/service/web` | Rutas HTTP y manejo de errores |
| `src/main/java/foro/service/security` | Permisos y comunicación con Usuarios |
| `sql` | Creación de tablas y datos de demostración |
| `src/test` | Pruebas automatizadas |

## Configurar Oracle

Se necesita un JDK compatible con Java 21, acceso a Oracle y la wallet del Foro. Se reutiliza la base Biblioteca de Semana 1, conservando la tabla `LIBROS`. Las tablas de este servicio son `CATEGORIAS`, `TEMAS` y `COMENTARIOS`.

1. Dejar `Wallet_Foro` junto a `pom.xml` y `mvnw.cmd`.
2. Copiar `config/application-local.example.properties` como `config/application-local.properties`, si el archivo privado todavía no existe.
3. Completar los accesos de la base siguiendo este formato:

```properties
spring.datasource.username=ADMIN
spring.datasource.password=CLAVE_DE_LA_BASE
spring.datasource.url=jdbc:oracle:thin:@biblioteca_low?TNS_ADMIN=./Wallet_Foro
```

La contraseña anterior es un ejemplo para reemplazar. Es la contraseña de la base, no la de la wallet ni la de Postman. La wallet y el archivo privado están excluidos de Git y se entregan por separado.

En una base sin las tablas del foro, ejecutar `sql/schema.sql` y después `sql/datos-demo.sql`, una sola vez. Si ya están preparadas, no repetirlos. Los datos iniciales incluyen cinco categorías, tres temas y tres comentarios. Se mantiene `ddl-auto=validate` para comprobar las tablas existentes al iniciar.

Los autores corresponden a cuentas de Usuarios. No hay una clave foránea entre ambas bases: el servicio obtiene la identidad por HTTP.

## Iniciar desde Visual Studio Code

Primero iniciar Usuarios en su propia terminal. Después abrir otra terminal PowerShell para Foro:

```powershell
cd "C:\Users\Elwer\Documents\Duoc\FullStack lll\Semana 5\foro"
.\mvnw.cmd spring-boot:run
```

Si Java no está configurado, establecer `JAVA_HOME` antes de iniciar. Ajustar la ruta al JDK instalado:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-27.0.1.0.3"
```

Esperar `Started ServiceApplication` y mantener ambas terminales abiertas. La dirección base del Foro es `http://localhost:8082`. Para detenerlo, presionar **Ctrl + C** en su terminal.

En `src/main/resources/application.properties`, la propiedad `usuarios.url=http://localhost:8081` indica dónde se consulta la autenticación. Si Usuarios está detenido, las operaciones protegidas del Foro pueden responder **503**.

## Cuentas y permisos en Postman

Para las escrituras seleccionar **Authorization > Basic Auth**. Son cuentas de demostración cargadas en la base de Usuarios:

| Correo | Contraseña de demostración | Uso |
| --- | --- | --- |
| `admin@foro.cl` | `AdminDemo2026!` | Administrar categorías y gestionar contenido |
| `ana@foro.cl` | `AnaDemo2026!` | Publicar temas y gestionar contenido propio |
| `luis@foro.cl` | `LuisDemo2026!` | Comentar y gestionar contenido propio |

Usar **No Auth** para las consultas públicas. Basic Auth se envía en cada solicitud protegida; no se utiliza un token. Los servicios están configurados para ejecutarse en el equipo local.

## Rutas principales

Agregar estas rutas a `http://localhost:8082` y reemplazar `{id}` por el ID del recurso correspondiente:

| Método | Ruta | Uso |
| --- | --- | --- |
| GET / POST | `/categorias` | Consultar / crear categorías |
| GET / PUT / DELETE | `/categorias/{id}` | Consultar / modificar / eliminar categoría |
| GET | `/temas` | Listar temas |
| GET | `/temas?q=java` | Buscar por título o contenido |
| POST | `/temas` | Publicar un tema |
| GET / PUT / DELETE | `/temas/{id}` | Consultar / modificar / eliminar tema |
| GET / POST | `/temas/{id}/comentarios` | Consultar / agregar comentarios al tema |
| GET / PUT / DELETE | `/comentarios/{id}` | Consultar / modificar / eliminar comentario |

Las rutas usan **`/temas` en plural**. El ID de un tema y el de su categoría son distintos datos; no asumir que tienen el mismo número.


## Semana 5: patrón, arquetipo y AWS

Aplicamos Builder al crear temas. El servicio construye una entidad nueva con los campos permitidos; el ID lo genera la base. Los cambios de un registro existente siguen usando PUT y verifican sus permisos.

El arquetipo Maven está en `arquetipo`. La [guía de generación](arquetipo/README.md) permite instalar la plantilla y crear un proyecto con otro nombre y paquete. Se generó y probó un proyecto independiente: 6 pruebas H2 aprobadas. La plantilla contiene SQL, pruebas y Dockerfile; excluye wallets y accesos privados.

Dirección AWS: http://3.86.216.48/api/foro/. La IP puede cambiar al reiniciar el laboratorio y el acceso HTTP está restringido a la IP autorizada. Usuarios debe estar iniciado para las operaciones protegidas de Foro.

El workflow `.github/workflows/semana5.yml` está preparado para generar el proyecto desde el arquetipo, probarlo y construir su imagen Docker desde esas fuentes. Todavía debe publicarse y ejecutarse en GitHub para demostrar el flujo completo. Los commits requieren autorización; mensaje acordado: `Sumativa semana 5`.
