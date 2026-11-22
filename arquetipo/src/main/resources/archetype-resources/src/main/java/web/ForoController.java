package ${package}.web;

import ${package}.security.UsuariosClient.Perfil;
import ${package}.service.ForoService;
import ${package}.service.ForoService.*;
import ${package}.model.Categoria;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class ForoController {
    private final ForoService service;
    public ForoController(ForoService service) { this.service = service; }

    @GetMapping("/categorias")
    public List<Categoria> categorias() { return service.categorias(); }
    @GetMapping("/categorias/{id}")
    public Categoria categoria(@PathVariable Long id) { return service.categoria(id); }
    @PostMapping("/categorias")
    public ResponseEntity<Categoria> crearCategoria(@Valid @RequestBody DatosCategoria datos) {
        var c = service.crearCategoria(datos);
        return ResponseEntity.created(URI.create("/categorias/" + c.getId())).body(c);
    }
    @PutMapping("/categorias/{id}")
    public Categoria actualizarCategoria(@PathVariable Long id, @Valid @RequestBody DatosCategoria datos) {
        return service.actualizarCategoria(id, datos);
    }
    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        service.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/temas")
    public List<TemaVista> temas(@RequestParam(required=false) String q) { return service.temas(q); }
    @GetMapping("/temas/{id}")
    public TemaVista tema(@PathVariable Long id) { return service.tema(id); }
    @PostMapping("/temas")
    public ResponseEntity<TemaVista> crearTema(@Valid @RequestBody DatosTema datos, @AuthenticationPrincipal Perfil autor) {
        var t = service.crearTema(datos, autor);
        return ResponseEntity.created(URI.create("/temas/" + t.id())).body(t);
    }
    @PutMapping("/temas/{id}")
    public TemaVista actualizarTema(@PathVariable Long id, @Valid @RequestBody DatosTema datos,
                                   @AuthenticationPrincipal Perfil autor) {
        return service.actualizarTema(id, datos, autor);
    }
    @DeleteMapping("/temas/{id}")
    public ResponseEntity<Void> eliminarTema(@PathVariable Long id, @AuthenticationPrincipal Perfil autor) {
        service.eliminarTema(id, autor);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/temas/{id}/comentarios")
    public List<ComentarioVista> comentarios(@PathVariable Long id) { return service.comentarios(id); }
    @GetMapping("/comentarios/{id}")
    public ComentarioVista comentario(@PathVariable Long id) { return service.comentario(id); }
    @PostMapping("/temas/{id}/comentarios")
    public ResponseEntity<ComentarioVista> crearComentario(@PathVariable Long id,
            @Valid @RequestBody DatosComentario datos, @AuthenticationPrincipal Perfil autor) {
        var c = service.crearComentario(id, datos, autor);
        return ResponseEntity.created(URI.create("/comentarios/" + c.id())).body(c);
    }
    @PutMapping("/comentarios/{id}")
    public ComentarioVista actualizarComentario(@PathVariable Long id, @Valid @RequestBody DatosComentario datos,
                                               @AuthenticationPrincipal Perfil autor) {
        return service.actualizarComentario(id, datos, autor);
    }
    @DeleteMapping("/comentarios/{id}")
    public ResponseEntity<Void> eliminarComentario(@PathVariable Long id, @AuthenticationPrincipal Perfil autor) {
        service.eliminarComentario(id, autor);
        return ResponseEntity.noContent().build();
    }
}
