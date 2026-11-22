package ${package}.security;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UsuariosClient {
    private final RestClient client;
    public record Perfil(Long id, String nombre, String correo, String rol) {}

    public UsuariosClient(@Value("${usuarios.url}") String url) {
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(5));
        client = RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }

    public Perfil autenticar(String correo, String clave) {
        try {
            var perfil = client.post().uri("/auth/login")
                    .headers(h -> h.setBasicAuth(correo, clave)).retrieve().body(Perfil.class);
            if (perfil == null || perfil.id() == null || perfil.id() <= 0
                    || perfil.rol() == null || !java.util.Set.of("ADMIN", "USUARIO").contains(perfil.rol())) {
                throw new AuthenticationServiceException("Respuesta de Usuarios invalida");
            }
            return perfil;
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new BadCredentialsException("Credenciales incorrectas");
        } catch (RestClientException ex) {
            throw new AuthenticationServiceException("Servicio de Usuarios no disponible", ex);
        }
    }
}
