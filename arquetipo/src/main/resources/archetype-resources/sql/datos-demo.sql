-- Ejecutar una sola vez en tablas nuevas.
-- Los autores 1, 2 y 3 corresponden a los datos-demo de Usuarios.
INSERT INTO categorias (id,nombre,descripcion) VALUES (1,'Tecnologia','Desarrollo y computacion');
INSERT INTO categorias (id,nombre,descripcion) VALUES (2,'Videojuegos','Juegos y experiencias');
INSERT INTO categorias (id,nombre,descripcion) VALUES (3,'Musica','Artistas e instrumentos');
INSERT INTO categorias (id,nombre,descripcion) VALUES (4,'Deportes','Actividad fisica y competencias');
INSERT INTO categorias (id,nombre,descripcion) VALUES (5,'Cine','Peliculas y series');
INSERT INTO temas (id,titulo,contenido,autor_id,categoria_id) VALUES (1,'Aprender Java','Que recursos usan para aprender Java y Spring Boot',2,1);
INSERT INTO temas (id,titulo,contenido,autor_id,categoria_id) VALUES (2,'Juegos cooperativos','Compartan sus juegos cooperativos favoritos',3,2);
INSERT INTO temas (id,titulo,contenido,autor_id,categoria_id) VALUES (3,'Musica para estudiar','Que musica escuchan al estudiar',2,3);
INSERT INTO comentarios (id,contenido,autor_id,tema_id) VALUES (1,'Practicar con proyectos ayuda mucho',3,1);
INSERT INTO comentarios (id,contenido,autor_id,tema_id) VALUES (2,'Prefiero los juegos para equipos pequenos',2,2);
INSERT INTO comentarios (id,contenido,autor_id,tema_id) VALUES (3,'Escucho musica instrumental',1,3);
COMMIT;
