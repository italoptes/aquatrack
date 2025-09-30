package com.aquatrack;

import com.aquatrack.json.GsonProvider;
import com.aquatrack.usuario.TipoUsuario;
import com.aquatrack.usuario.Usuario;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class UsuarioRepository {

    private final String jsonPath;
    private static final String DEFAULT_JSON_PATH = "src/main/resources/json/Usuario.json";
    private final Map<String, Usuario> usuarios = new HashMap<>();

    public UsuarioRepository() {
        this(DEFAULT_JSON_PATH);
    }

    public UsuarioRepository(String jsonPath) {
        this.jsonPath = jsonPath;
        carregarUsuarios();
    }

    private void carregarUsuarios() {
        usuarios.clear();
        Path path = Paths.get(jsonPath);
        if (!Files.exists(path)) {
            try {
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao criar arquivo de usuários.", e);
            }
            return;
        }
        try (FileReader reader = new FileReader(jsonPath)) {
            var gson = GsonProvider.get(); // <-- alterado
            Type tipoListaUsuario = new TypeToken<List<Usuario>>() {}.getType();
            List<Usuario> listaUsuario = gson.fromJson(reader, tipoListaUsuario);
            if (listaUsuario != null) {
                for (Usuario usuario : listaUsuario) {
                    if (usuario.getId() != null) {
                        usuarios.put(usuario.getId(), usuario);
                    }
                }
            } else {
                // Arquivo vazio → lista nula, mantemos 'usuarios' vazio
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo de usuários.", e);
        }
    }

    public void salvarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null || usuario.getLogin() == null) {
            throw new IllegalArgumentException("Usuário ou campos obrigatórios nulos.");
        }
        carregarUsuarios();
        usuarios.put(usuario.getId(), usuario);
        salvarUsuarios();
    }

    private void salvarUsuarios() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(jsonPath))) {
            var gson = GsonProvider.get(); // <-- alterado
            String json = gson.toJson(new ArrayList<>(usuarios.values()));
            writer.write(json);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar usuários.", e);
        }
    }

    public List<Usuario> listarUsuarios() {
        carregarUsuarios();
        return new ArrayList<>(usuarios.values());
    }

    public Usuario buscarUsuarioPorLogin(String login) {
        return usuarios.values().stream()
                .filter(u -> Objects.equals(login, u.getLogin()) && !u.isDeletado())
                .findFirst()
                .orElse(null);
    }

    public Usuario buscarUsuarioPorTipo(TipoUsuario tipoUsuario) {
        carregarUsuarios();
        return usuarios.values().stream()
                .filter(u -> Objects.equals(tipoUsuario, u.getTipo()) && !u.isDeletado())
                .findFirst()
                .orElse(null);
    }
}
