package foro.service;

import com.sun.net.httpserver.HttpServer;
import foro.service.security.UsuariosClient;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import static org.assertj.core.api.Assertions.*;

class UsuariosClientTests {
    @Test void validaCredencialesPorHttpContraUsuarios() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        String autorizado = "Basic " + Base64.getEncoder()
                .encodeToString("ana@foro.cl:ClaveDemo2026!".getBytes(StandardCharsets.ISO_8859_1));
        server.createContext("/auth/login", exchange -> {
            boolean correcto = autorizado.equals(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = (correcto
                    ? "{\"id\":2,\"nombre\":\"Ana\",\"correo\":\"ana@foro.cl\",\"rol\":\"USUARIO\"}"
                    : "{}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type","application/json");
            exchange.sendResponseHeaders(correcto ? 200 : 401, body.length);
            try (var output = exchange.getResponseBody()) { output.write(body); }
        });
        server.start();
        try {
            var client = new UsuariosClient("http://127.0.0.1:" + server.getAddress().getPort());
            assertThat(client.autenticar("ana@foro.cl","ClaveDemo2026!").id()).isEqualTo(2);
            assertThatThrownBy(() -> client.autenticar("ana@foro.cl","incorrecta"))
                    .isInstanceOf(BadCredentialsException.class);
        } finally { server.stop(0); }
    }
}
