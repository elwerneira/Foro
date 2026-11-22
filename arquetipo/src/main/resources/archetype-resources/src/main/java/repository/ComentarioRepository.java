package ${package}.repository;
import java.util.List;
import ${package}.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByTemaId(Long temaId);
    boolean existsByTemaId(Long temaId);
}
