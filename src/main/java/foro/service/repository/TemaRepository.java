package foro.service.repository;
import java.util.List;
import foro.service.model.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TemaRepository extends JpaRepository<Tema, Long> {
    List<Tema> findByTituloContainingIgnoreCaseOrContenidoContainingIgnoreCase(String titulo, String contenido);
}
