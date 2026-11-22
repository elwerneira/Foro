package foro.service.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Builder;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity @Table(name="temas")
@Getter @Setter @NoArgsConstructor
public class Tema {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=200)
    private String titulo;
    @Column(nullable=false, length=4000)
    private String contenido;
    @Column(name="autor_id", nullable=false)
    private Long autorId;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="categoria_id", nullable=false)
    private Categoria categoria;

    @Builder
    private Tema(String titulo, String contenido, Categoria categoria, Long autorId) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.categoria = categoria;
        this.autorId = autorId;
    }
}
