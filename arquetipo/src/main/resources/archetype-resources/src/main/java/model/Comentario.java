package ${package}.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity @Table(name="comentarios")
@Getter @Setter @NoArgsConstructor
public class Comentario {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=2000)
    private String contenido;
    @Column(name="autor_id", nullable=false)
    private Long autorId;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="tema_id", nullable=false)
    private Tema tema;
}
