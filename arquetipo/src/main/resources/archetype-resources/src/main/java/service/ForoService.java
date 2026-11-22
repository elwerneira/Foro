package ${package}.service;

import ${package}.model.*;
import ${package}.repository.*;
import ${package}.security.UsuariosClient.Perfil;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ForoService {
    private final CategoriaRepository categorias;
    private final TemaRepository temas;
    private final ComentarioRepository comentarios;

    public ForoService(CategoriaRepository categorias, TemaRepository temas, ComentarioRepository comentarios) {
        this.categorias = categorias;
        this.temas = temas;
        this.comentarios = comentarios;
    }

    public record DatosCategoria(@NotBlank @Size(max=100) String nombre,
                                 @NotBlank @Size(max=500) String descripcion) {}
    public record DatosTema(@NotBlank @Size(max=200) String titulo,
                            @NotBlank @Size(max=4000) String contenido,
                            @NotNull @Positive Long categoriaId) {}
    public record DatosComentario(@NotBlank @Size(max=2000) String contenido) {}
    public record TemaVista(Long id, String titulo, String contenido, Long categoriaId, Long autorId) {}
    public record ComentarioVista(Long id, String contenido, Long temaId, Long autorId) {}

    @Transactional(readOnly=true)
    public List<Categoria> categorias() { return categorias.findAll(); }

    @Transactional(readOnly=true)
    public Categoria categoria(Long id) {
        return categorias.findById(id).orElseThrow(() -> ausente("Categoria"));
    }

    public Categoria crearCategoria(DatosCategoria datos) {
        var categoria = new Categoria();
        categoria.setNombre(datos.nombre().trim());
        categoria.setDescripcion(datos.descripcion().trim());
        return categorias.saveAndFlush(categoria);
    }

    public Categoria actualizarCategoria(Long id, DatosCategoria datos) {
        var categoria = categoria(id);
        categoria.setNombre(datos.nombre().trim());
        categoria.setDescripcion(datos.descripcion().trim());
        return categorias.saveAndFlush(categoria);
    }

    public void eliminarCategoria(Long id) { categorias.delete(categoria(id)); }

    @Transactional(readOnly=true)
    public List<TemaVista> temas(String busqueda) {
        var encontrados = busqueda == null || busqueda.isBlank() ? temas.findAll()
                : temas.findByTituloContainingIgnoreCaseOrContenidoContainingIgnoreCase(busqueda.trim(), busqueda.trim());
        return encontrados.stream().map(this::vista).toList();
    }

    @Transactional(readOnly=true)
    public TemaVista tema(Long id) { return vista(buscarTema(id)); }

    public TemaVista crearTema(DatosTema datos, Perfil autor) {
        var tema = Tema.builder()
                .titulo(datos.titulo().trim())
                .contenido(datos.contenido().trim())
                .categoria(categoria(datos.categoriaId()))
                .autorId(autor.id())
                .build();
        return vista(temas.saveAndFlush(tema));
    }

    public TemaVista actualizarTema(Long id, DatosTema datos, Perfil autor) {
        var tema = buscarTema(id);
        propietario(tema.getAutorId(), autor);
        tema.setTitulo(datos.titulo().trim());
        tema.setContenido(datos.contenido().trim());
        tema.setCategoria(categoria(datos.categoriaId()));
        return vista(temas.saveAndFlush(tema));
    }

    public void eliminarTema(Long id, Perfil autor) {
        var tema = buscarTema(id);
        propietario(tema.getAutorId(), autor);
        if (comentarios.existsByTemaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El tema tiene comentarios; eliminelos antes");
        }
        temas.delete(tema);
    }

    @Transactional(readOnly=true)
    public List<ComentarioVista> comentarios(Long temaId) {
        buscarTema(temaId);
        return comentarios.findByTemaId(temaId).stream().map(this::vista).toList();
    }

    @Transactional(readOnly=true)
    public ComentarioVista comentario(Long id) { return vista(buscarComentario(id)); }

    public ComentarioVista crearComentario(Long temaId, DatosComentario datos, Perfil autor) {
        var comentario = new Comentario();
        comentario.setTema(buscarTema(temaId));
        comentario.setContenido(datos.contenido().trim());
        comentario.setAutorId(autor.id());
        return vista(comentarios.saveAndFlush(comentario));
    }

    public ComentarioVista actualizarComentario(Long id, DatosComentario datos, Perfil autor) {
        var comentario = buscarComentario(id);
        propietario(comentario.getAutorId(), autor);
        comentario.setContenido(datos.contenido().trim());
        return vista(comentarios.saveAndFlush(comentario));
    }

    public void eliminarComentario(Long id, Perfil autor) {
        var comentario = buscarComentario(id);
        propietario(comentario.getAutorId(), autor);
        comentarios.delete(comentario);
    }

    private Tema buscarTema(Long id) {
        return temas.findById(id).orElseThrow(() -> ausente("Tema"));
    }

    private Comentario buscarComentario(Long id) {
        return comentarios.findById(id).orElseThrow(() -> ausente("Comentario"));
    }

    private ResponseStatusException ausente(String entidad) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, entidad + " no encontrada");
    }

    private void propietario(Long autorId, Perfil perfil) {
        if (!autorId.equals(perfil.id()) && !perfil.rol().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor o ADMIN puede modificar este registro");
        }
    }

    private TemaVista vista(Tema t) {
        return new TemaVista(t.getId(), t.getTitulo(), t.getContenido(), t.getCategoria().getId(), t.getAutorId());
    }

    private ComentarioVista vista(Comentario c) {
        return new ComentarioVista(c.getId(), c.getContenido(), c.getTema().getId(), c.getAutorId());
    }
}
