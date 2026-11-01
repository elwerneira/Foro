package foro.service;

import foro.service.repository.*;
import foro.service.security.UsuariosClient;
import foro.service.security.UsuariosClient.Perfil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationServiceException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class ForoApiTests {
    @Autowired MockMvc mvc;
    @Autowired CategoriaRepository categorias;
    @Autowired TemaRepository temas;
    @Autowired ComentarioRepository comentarios;
    @MockitoBean UsuariosClient usuarios;
    private static final String CATEGORIA = "{\"nombre\":\"Tecnologia\",\"descripcion\":\"Computacion\"}";

    @BeforeEach void preparar() {
        comentarios.deleteAll(); temas.deleteAll(); categorias.deleteAll();
        when(usuarios.autenticar(anyString(),anyString())).thenAnswer(call -> {
            String correo = call.getArgument(0), clave = call.getArgument(1);
            if (!clave.equals("ClaveDemo2026!")) throw new BadCredentialsException("Clave incorrecta");
            return new Perfil(correo.equals("admin@foro.cl") ? 1L : correo.equals("ana@foro.cl") ? 2L : 3L,
                    "Prueba", correo, correo.equals("admin@foro.cl") ? "ADMIN" : "USUARIO");
        });
    }

    private Long categoria() throws Exception {
        mvc.perform(post("/categorias").with(httpBasic("admin@foro.cl","ClaveDemo2026!"))
                        .contentType("application/json").content(CATEGORIA))
                .andExpect(status().isCreated());
        return categorias.findAll().getFirst().getId();
    }

    private String datosTema(Long categoriaId) {
        return "{\"titulo\":\"Java y Spring\",\"contenido\":\"Tema de prueba\",\"categoriaId\":" + categoriaId + "}";
    }

    private String crearTema(Long categoriaId) throws Exception {
        return mvc.perform(post("/temas").with(httpBasic("ana@foro.cl","ClaveDemo2026!"))
                        .contentType("application/json").content(datosTema(categoriaId)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.autorId").value(2))
                .andReturn().getResponse().getHeader("Location");
    }

    @Test void crudCategoriasTemasComentariosYBusqueda() throws Exception {
        Long idCategoria = categoria();
        mvc.perform(get("/categorias/" + idCategoria)).andExpect(status().isOk());
        mvc.perform(put("/categorias/" + idCategoria).with(httpBasic("admin@foro.cl","ClaveDemo2026!"))
                .contentType("application/json").content(CATEGORIA.replace("Computacion","Desarrollo")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.descripcion").value("Desarrollo"));
        String tema = crearTema(idCategoria);
        mvc.perform(get("/temas").param("q","spring")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/temas").param("q","inexistente")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(put(tema).with(httpBasic("ana@foro.cl","ClaveDemo2026!"))
                .contentType("application/json").content(datosTema(idCategoria).replace("Java y Spring","Java actualizado")))
                .andExpect(status().isOk());
        String comentario = mvc.perform(post(tema + "/comentarios").with(httpBasic("otro@foro.cl","ClaveDemo2026!"))
                .contentType("application/json").content("{\"contenido\":\"Buen tema\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.autorId").value(3))
                .andReturn().getResponse().getHeader("Location");
        mvc.perform(get(tema + "/comentarios")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(put(comentario).with(httpBasic("otro@foro.cl","ClaveDemo2026!"))
                .contentType("application/json").content("{\"contenido\":\"Comentario actualizado\"}"))
                .andExpect(status().isOk());
        mvc.perform(delete(tema).with(httpBasic("ana@foro.cl","ClaveDemo2026!"))).andExpect(status().isConflict());
        mvc.perform(delete("/categorias/" + idCategoria).with(httpBasic("admin@foro.cl","ClaveDemo2026!")))
                .andExpect(status().isConflict());
        mvc.perform(delete(comentario).with(httpBasic("otro@foro.cl","ClaveDemo2026!")))
                .andExpect(status().isNoContent());
        mvc.perform(get(comentario)).andExpect(status().isNotFound());
        mvc.perform(delete(tema).with(httpBasic("ana@foro.cl","ClaveDemo2026!")))
                .andExpect(status().isNoContent());
        mvc.perform(get(tema)).andExpect(status().isNotFound());
        mvc.perform(delete("/categorias/" + idCategoria).with(httpBasic("admin@foro.cl","ClaveDemo2026!")))
                .andExpect(status().isNoContent());
    }

    @Test void permisosPropiedadYCredenciales() throws Exception {
        Long categoriaId = categoria();
        String tema = crearTema(categoriaId);
        mvc.perform(post("/temas").contentType("application/json").content(datosTema(categoriaId)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/categorias").with(httpBasic("ana@foro.cl","ClaveDemo2026!"))
                .contentType("application/json").content(CATEGORIA)).andExpect(status().isForbidden());
        mvc.perform(put(tema).with(httpBasic("otro@foro.cl","ClaveDemo2026!"))
                .contentType("application/json").content(datosTema(categoriaId))).andExpect(status().isForbidden());
        mvc.perform(delete(tema).with(httpBasic("otro@foro.cl","ClaveDemo2026!")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/temas").with(httpBasic("ana@foro.cl","incorrecta"))
                .contentType("application/json").content(datosTema(categoriaId))).andExpect(status().isUnauthorized());
    }

    @Test void validacionesYServicioNoDisponible() throws Exception {
        mvc.perform(post("/temas").with(httpBasic("ana@foro.cl","ClaveDemo2026!"))
                .contentType("application/json").content("{}")).andExpect(status().isBadRequest());
        mvc.perform(post("/temas").with(httpBasic("ana@foro.cl","ClaveDemo2026!"))
                .contentType("application/json").content(datosTema(999999L))).andExpect(status().isNotFound());
        when(usuarios.autenticar("ana@foro.cl","ClaveDemo2026!"))
                .thenThrow(new AuthenticationServiceException("No disponible"));
        mvc.perform(post("/temas").with(httpBasic("ana@foro.cl","ClaveDemo2026!"))
                .contentType("application/json").content(datosTema(999999L))).andExpect(status().isServiceUnavailable());
        mvc.perform(get("/categorias")).andExpect(status().isOk());
    }
}
