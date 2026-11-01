package foro.service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import java.util.List;

@Configuration
public class Seguridad {
    @Bean
    AuthenticationProvider autenticacion(UsuariosClient usuarios) {
        return new AuthenticationProvider() {
            @Override public Authentication authenticate(Authentication auth) {
                var perfil = usuarios.autenticar(auth.getName(), auth.getCredentials().toString());
                return new UsernamePasswordAuthenticationToken(perfil, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + perfil.rol())));
            }
            @Override public boolean supports(Class<?> tipo) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(tipo);
            }
        };
    }

    @Bean
    AuthenticationEntryPoint entrada() {
        return (req, res, ex) -> {
            boolean noDisponible = ex instanceof AuthenticationServiceException;
            res.setStatus(noDisponible ? 503 : 401);
            res.setHeader("WWW-Authenticate", "Basic realm=\"foro\"");
            res.setContentType("application/json");
            res.getWriter().write(noDisponible
                    ? "{\"status\":503,\"mensaje\":\"Servicio de Usuarios no disponible\"}"
                    : "{\"status\":401,\"mensaje\":\"Credenciales requeridas o incorrectas\"}");
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider autenticacion,
                                          AuthenticationEntryPoint entrada) throws Exception {
        return http.authenticationProvider(autenticacion)
                // API local de Postman, sin cookies de sesion ni formularios.
                .csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(r -> r
                        .requestMatchers(HttpMethod.GET, "/categorias", "/categorias/**", "/temas", "/temas/**", "/comentarios/**").permitAll()
                        .requestMatchers("/categorias", "/categorias/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(b -> b.authenticationEntryPoint(entrada))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entrada)
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"status\":403,\"mensaje\":\"Permiso insuficiente\"}");
                        })).build();
    }
}
