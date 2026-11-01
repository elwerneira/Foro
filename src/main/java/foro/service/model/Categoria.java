package foro.service.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity @Table(name="categorias")
@Getter @Setter @NoArgsConstructor
public class Categoria {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=100)
    private String nombre;
    @Column(nullable=false, length=500)
    private String descripcion;
}
