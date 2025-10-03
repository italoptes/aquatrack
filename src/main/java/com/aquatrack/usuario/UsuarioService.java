package com.aquatrack.usuario;

import com.aquatrack.UsuarioRepository;
import com.aquatrack.fazenda.ResumoFazendaDTO;
import com.aquatrack.viveiro.Viveiro;
import io.javalin.http.UploadedFile;
import org.mindrot.jbcrypt.BCrypt;
import com.aquatrack.exceptions.EntidadeExcluidaException;
import com.aquatrack.fazenda.Fazenda;
import com.aquatrack.fazenda.FazendaService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class UsuarioService {

    private final List<String> idExistentesFazendas = new ArrayList<>();
    private final UsuarioRepository usuarioRepository;
    private FazendaService fazendaService;

    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepository();
    }

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "usuarioRepository não pode ser nulo");
    }

    // ================= CRUD USUÁRIO =================

    public Usuario cadastrarUsuario(Usuario usuario) {
        if (usuario == null) throw new IllegalArgumentException("Usuário inválido (null).");
        if (isBlank(usuario.getLogin()) || isBlank(usuario.getSenha())) {
            throw new IllegalArgumentException("Login e senha são obrigatórios.");
        }

        // login único
        Usuario existente = usuarioRepository.buscarUsuarioPorLogin(usuario.getLogin());
        if (existente != null && !existente.isDeletado()) {
            throw new IllegalArgumentException("Já existe usuário ativo com este login.");
        }

        // gera id se vier null
        if (usuario.getId() == null) {
            usuario.setId(UUID.randomUUID().toString());
        }

        usuarioRepository.salvarUsuario(usuario);
        return usuario;
    }

    public List<Usuario> listarUsuarios() {
        List<Usuario> ativos = new ArrayList<>();
        for (Usuario usuario : usuarioRepository.listarUsuarios()) {
            if (!usuario.isDeletado()) ativos.add(usuario);
        }
        return ativos;
    }

    public Usuario buscarUsuarioPorId(String id) {
        return listarUsuarios().stream()
                .filter(u -> Objects.equals(id, u.getId()))
                .findFirst()
                .orElse(null);
    }

    public Usuario buscarUsuarioPorLogin(String login) {
        if (isBlank(login)) throw new IllegalArgumentException("Login inválido.");
        return usuarioRepository.buscarUsuarioPorLogin(login);
    }

    public void removerUsuario(String id) {
        Usuario usuario = buscarUsuarioObrigatorio(id);
        if (usuario.isDeletado()) {
            throw new EntidadeExcluidaException("Usuário já excluído.");
        }
        usuario.deletar();
        usuarioRepository.salvarUsuario(usuario);
    }

    public boolean verificaUsuarioDeletado(String id) {
        Usuario usuario = buscarUsuarioObrigatorio(id);
        return usuario.isDeletado();
    }

    public void editarNome(Usuario usuario, String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome não pode ser vazio.");
        }
        usuario.setNome(nome);
        usuarioRepository.salvarUsuario(usuario);
    }

    public void editarEmail(Usuario usuario, String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email não pode ser vazio.");
        }
        if (buscarUsuarioPorLogin(email) != null) {
            throw new IllegalArgumentException("Já existe um usuário com esse email.");
        }
        usuario.setLogin(email);
        usuarioRepository.salvarUsuario(usuario);
    }

    public void editarFoto(Usuario usuario, UploadedFile foto) throws IOException {
        if (foto == null) {
            throw new IllegalArgumentException("Nenhum arquivo enviado.");
        }

        // Caminho da pasta de uploads
        Path pastaUploads = Paths.get("src/main/resources/public/uploads");

        // Cria a pasta se não existir
        if (!Files.exists(pastaUploads)) {
            Files.createDirectories(pastaUploads);
        }


        // salva o arquivo
        String nomeArquivo = UUID.randomUUID() + "-" + foto.filename();
        Path caminho = pastaUploads.resolve(nomeArquivo);
        Files.copy(foto.content(), caminho, StandardCopyOption.REPLACE_EXISTING);

        // seta a URL pública
        usuario.setUrlFoto("/uploads/" + nomeArquivo);

        usuarioRepository.salvarUsuario(usuario);
    }


    public void editarSenha(Usuario usuario, String senha, String confirmarSenha) {
        if (senha == null || senha.isBlank() || confirmarSenha == null) {
            throw new IllegalArgumentException("Senha e confirmação não podem ser vazias.");
        }
        if (!senha.equals(confirmarSenha)) {
            throw new IllegalArgumentException("As senhas não coincidem.");
        }

        String senhaHash = BCrypt.hashpw(senha, BCrypt.gensalt());
        usuario.setSenha(senhaHash);
        usuarioRepository.salvarUsuario(usuario);
    }


    // ================= AUTENTICAÇÃO =================

    public Usuario autenticar(String login, String senha) {
        if (isBlank(login) || isBlank(senha)) {
            throw new IllegalArgumentException("Login e senha são obrigatórios.");
        }
        Usuario u = usuarioRepository.buscarUsuarioPorLogin(login);
        if (u == null || u.isDeletado()) return null;
        return BCrypt.checkpw(senha, u.getSenha()) ? u : null;
    }

    // Cria usuário master padrão (apenas se não existir)
    public void criaUsuarioMaster() {
        if (usuarioRepository.buscarUsuarioPorTipo(TipoUsuario.MASTER) == null) {
            String senha = BCrypt.hashpw("track", BCrypt.gensalt());
            Usuario usuarioMaster = new Usuario("master@aqua.com", "Master", senha, TipoUsuario.MASTER);
            cadastrarUsuario(usuarioMaster);
        }
    }

    // ================= FAZENDAS DO USUÁRIO =================

    public void adicionarFazendaAoUsuario(Usuario usuario, Fazenda fazenda) { //Fazenda é criada no Controller e passada para o service
        gerarId(fazenda);
        if (fazenda == null || isBlank(fazenda.getId())) {
            throw new IllegalArgumentException("Fazenda inválida.");
        }
        usuario.addFazenda(fazenda);
        usuarioRepository.salvarUsuario(usuario);
    }

    public void removerFazendaDoUsuario(String usuarioId, String fazendaId) {
        Usuario usuario = buscarUsuarioObrigatorio(usuarioId);
        if (isBlank(fazendaId)) throw new IllegalArgumentException("Id da fazenda inválido.");
        usuario.removerFazenda(fazendaId);
        usuarioRepository.salvarUsuario(usuario);
    }

    public ArrayList<Fazenda> listarFazendasDoUsuario(String usuarioId) {
        Usuario usuario = buscarUsuarioObrigatorio(usuarioId);
        ArrayList<Fazenda> fazendas = usuario.listarFazendasAtivas();
        return fazendas;
    }

    public Fazenda buscarFazendaPorId(String idUsuario,String idFazenda) {
        Usuario usuario = buscarUsuarioObrigatorio(idUsuario);
        Fazenda fazenda = usuario.getFazendaPorId(idFazenda);
        return fazenda;
    }

    public int contaFazendasUsuarios(Usuario usuario) {
        return usuario.contaFazendasUsuarios(usuario);
    }

    // ================= RESUMO =================
    public List<ResumoFazendaDTO> gerarResumoFazendas(Usuario usuario) {
        List<ResumoFazendaDTO> resumo = new ArrayList<>();

        for (Fazenda fazenda : usuario.listarFazendasAtivas()) {
            int totalViveiros = fazenda.getViveiros().size();
            int ciclosAtivos = 0;
            List<String> ativos = new ArrayList<>();

            for (Viveiro viveiro : fazenda.listarViveiros()) {
                if (!viveiro.isDeletado() && viveiro.isCicloAtivo()) {
                    ciclosAtivos++;
                    ativos.add(viveiro.getId());
                }
            }

            ResumoFazendaDTO dto = new ResumoFazendaDTO();
            dto.setNome(fazenda.getNome());
            dto.setTotalViveiros(totalViveiros);
            dto.setCiclosAtivos(ciclosAtivos);
            dto.setViveirosAtivos(ativos);

            resumo.add(dto);
        }

        return resumo;
    }

    // ================= HELPERS =================

    private Usuario buscarUsuarioObrigatorio(String usuarioId) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        return usuario;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void gerarId(Fazenda fazenda) {
        while (true) {
            int numero = ThreadLocalRandom.current().nextInt(100, 1000);
            String id = String.format("F-%04d", numero);
            if (!idExistentesFazendas.contains(id)) {
                idExistentesFazendas.add(id);
                fazenda.setId(id);
                return;
            }
        }
    }

}
