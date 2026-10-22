/**
 * administrador-painel.js
 * Lógica exclusiva da página administrador-painel.html.
 * Depende das funções definidas em utilitarios.js.
 */

// Guarda o id do administrador logado (usado para não deixar a
// tabela oferecer "Editar"/switch de status na própria linha dele).
let idAdministradorLogado = null;

document.addEventListener("DOMContentLoaded", () => {
  carregarUsuarioLogado();
  carregarListaDeUsuarios();
  inicializarMenuUsuario();
  inicializarFormularioCadastrarFuncionario();
});

/* ============================================================
 * AVATAR / MENU DO USUÁRIO LOGADO (topo da página)
 * ============================================================ */
function carregarUsuarioLogado() {
  fetch("/api/usuarios/me")
    .then(async (resposta) => {
      if (resposta.status === 401) {
        window.location.href = "/entrar";
        return null;
      }
      const corpo = await resposta.json();
      if (!resposta.ok || !corpo.sucesso) {
        throw new Error(corpo.mensagem || "Não foi possível carregar seus dados.");
      }
      return corpo;
    })
    .then((corpo) => {
      if (!corpo) return;
      const usuario = corpo.dados;
      idAdministradorLogado = usuario.id;
      document.querySelector("[data-elemento='iniciais']").textContent = obterIniciais(usuario.nome);
      document.querySelector("[data-elemento='nome-usuario']").textContent = usuario.nome;
      document.querySelector("[data-elemento='email-usuario']").textContent = usuario.email;
    })
    .catch((erro) => mostrarAlerta(erro.message, "erro"));
}

function obterIniciais(nomeCompleto) {
  const partes = nomeCompleto.trim().split(/\s+/);
  const primeira = partes[0]?.charAt(0) || "";
  const ultima = partes.length > 1 ? partes[partes.length - 1].charAt(0) : "";
  return (primeira + ultima).toUpperCase();
}

function inicializarMenuUsuario() {
  const botaoAvatar = document.querySelector("[data-acao='abrir-menu-usuario']");
  const dropdown = document.querySelector("[data-elemento='dropdown-usuario']");

  botaoAvatar.addEventListener("click", (evento) => {
    evento.stopPropagation();
    const estaAberto = !dropdown.hidden;
    dropdown.hidden = estaAberto;
    botaoAvatar.setAttribute("aria-expanded", String(!estaAberto));
  });

  document.addEventListener("click", (evento) => {
    if (!dropdown.hidden && !dropdown.contains(evento.target)) {
      dropdown.hidden = true;
      botaoAvatar.setAttribute("aria-expanded", "false");
    }
  });

  document.querySelector("[data-acao='sair']").addEventListener("click", realizarLogout);
}

function realizarLogout() {
  fetch("/api/auth/sair", { method: "POST" })
    .then(() => {
      window.location.href = "/entrar";
    })
    .catch(() => mostrarAlerta("Não foi possível sair. Tente novamente.", "erro"));
}

/* ============================================================
 * CAIXA 1 — LISTAGEM DE USUÁRIOS
 * Cada linha tem: nome, e-mail, tipo, um switch de status
 * (ativo/bloqueado) e um botão "Editar" que transforma a própria
 * linha num formulário rápido de nome/e-mail.
 * ============================================================ */
function carregarListaDeUsuarios() {
  document.querySelector("[data-elemento='tabela-usuarios']").hidden = true;
  document.querySelector("[data-elemento='carregando-usuarios']").hidden = false;

  fetch("/api/admin/usuarios")
    .then(async (resposta) => {
      if (resposta.status === 401) {
        window.location.href = "/entrar";
        return null;
      }
      if (resposta.status === 403) {
        window.location.href = "/painel";
        return null;
      }
      const corpo = await resposta.json();
      if (!resposta.ok || !corpo.sucesso) {
        throw new Error(corpo.mensagem || "Não foi possível carregar os usuários.");
      }
      return corpo;
    })
    .then((corpo) => {
      if (!corpo) return;
      renderizarTabelaUsuarios(corpo.dados);
    })
    .catch((erro) => {
      mostrarAlerta(erro.message, "erro");
      document.querySelector("[data-elemento='carregando-usuarios']").hidden = true;
    });
}

/** Monta a tabela inteira a partir da lista de usuários vinda da API. */
function renderizarTabelaUsuarios(usuarios) {
  const corpoTabela = document.querySelector("[data-elemento='corpo-tabela-usuarios']");
  corpoTabela.innerHTML = ""; // limpa antes de preencher, evita duplicar linhas em recarregamentos

  usuarios.forEach((usuario) => {
    const linha = document.createElement("tr");
    preencherLinhaModoVisualizacao(linha, usuario);
    corpoTabela.appendChild(linha);
  });

  document.querySelector("[data-elemento='carregando-usuarios']").hidden = true;
  document.querySelector("[data-elemento='tabela-usuarios']").hidden = false;
}

/**
 * Preenche uma <tr> no modo normal (não-edição): nome, e-mail, tipo,
 * switch de status e botão "Editar". A própria linha do administrador
 * logado não recebe nem o switch nem o botão de editar — ele gerencia
 * os próprios dados em /perfil, não por aqui.
 */
function preencherLinhaModoVisualizacao(linha, usuario) {
  const podeGerenciar = usuario.id !== idAdministradorLogado;
  const estaAtivo = usuario.status === "ATIVO";

  linha.innerHTML = `
    <td>${escaparHtml(usuario.nome)}</td>
    <td>${escaparHtml(usuario.email)}</td>
    <td>${escaparHtml(formatarTipoPerfil(usuario.tipoPerfil))}</td>
    <td>
      ${podeGerenciar
        ? `<label class="interruptor" title="Clique para ativar ou bloquear este usuário">
             <input type="checkbox" data-acao="alternar-status" ${estaAtivo ? "checked" : ""} />
             <span class="interruptor__trilho"></span>
           </label>
           <span class="interruptor__rotulo">${estaAtivo ? "Ativo" : "Bloqueado"}</span>`
        : `<span class="texto-suave-tabela">—</span>`}
    </td>
    <td>
      ${podeGerenciar
        ? `<button type="button" class="botao-tabela" data-acao="editar-usuario">Editar</button>`
        : `<span class="texto-suave-tabela">—</span>`}
    </td>
  `;

  if (podeGerenciar) {
    linha.querySelector("[data-acao='alternar-status']")
      .addEventListener("change", () => alternarStatus(usuario, linha));
    linha.querySelector("[data-acao='editar-usuario']")
      .addEventListener("click", () => entrarModoEdicaoLinha(linha, usuario));
  }
}

function formatarTipoPerfil(tipoPerfil) {
  const rotulos = {
    USUARIO: "Usuário comum",
    FUNCIONARIO: "Funcionário",
    ADMINISTRADOR: "Administrador",
    GESTOR: "Gestor",
    PACIENTE: "Paciente",
    ALUNO: "Aluno",
    ESTAGIARIO: "Estagiário",
    NUTRICIONISTA: "Nutricionista",
  };
  return rotulos[tipoPerfil] || tipoPerfil;
}

/** Evita que nome/e-mail vindos do banco quebrem o HTML se contiverem caracteres especiais. */
function escaparHtml(texto) {
  const div = document.createElement("div");
  div.textContent = texto ?? "";
  return div.innerHTML;
}

/** Mesma ideia de escaparHtml, mas segura para uso dentro de atributos value="...". */
function escaparAtributo(texto) {
  return (texto ?? "")
    .replace(/&/g, "&amp;")
    .replace(/"/g, "&quot;")
    .replace(/</g, "&lt;");
}

/* ============================================================
 * SWITCH DE STATUS (ativar / bloquear)
 * ============================================================ */
function alternarStatus(usuario, linha) {
  fetch(`/api/admin/usuarios/${usuario.id}/status`, { method: "PATCH" })
    .then(async (resposta) => {
      const corpo = await resposta.json();
      if (!resposta.ok || !corpo.sucesso) {
        throw new Error(corpo.mensagem || "Não foi possível atualizar o status.");
      }
      return corpo;
    })
    .then((corpo) => {
      mostrarAlerta(corpo.mensagem, "sucesso");
      // Atualiza a linha com o novo status vindo da API, sem precisar
      // recarregar a tabela inteira.
      preencherLinhaModoVisualizacao(linha, corpo.dados);
    })
    .catch((erro) => {
      mostrarAlerta(erro.message, "erro");
      carregarListaDeUsuarios(); // recarrega para desfazer o clique visualmente, se der erro
    });
}

/* ============================================================
 * EDIÇÃO EM LINHA (botão "Editar")
 * Transforma a própria <tr> num mini-formulário de nome/e-mail,
 * sem precisar de um modal ou de sair da página.
 * ============================================================ */
function entrarModoEdicaoLinha(linha, usuario) {
  linha.innerHTML = `
    <td><input type="text" class="entrada-tabela" data-campo="nome" value="${escaparAtributo(usuario.nome)}" /></td>
    <td><input type="email" class="entrada-tabela" data-campo="email" value="${escaparAtributo(usuario.email)}" /></td>
    <td>${escaparHtml(formatarTipoPerfil(usuario.tipoPerfil))}</td>
    <td><span class="texto-suave-tabela">—</span></td>
    <td>
      <button type="button" class="botao-tabela botao-tabela--sucesso" data-acao="salvar-edicao">Salvar</button>
      <button type="button" class="botao-tabela" data-acao="cancelar-edicao-linha">Cancelar</button>
    </td>
  `;

  linha.querySelector("[data-acao='cancelar-edicao-linha']")
    .addEventListener("click", () => preencherLinhaModoVisualizacao(linha, usuario));

  linha.querySelector("[data-acao='salvar-edicao']")
    .addEventListener("click", () => salvarEdicaoLinha(linha, usuario));
}

function salvarEdicaoLinha(linha, usuario) {
  const nome = linha.querySelector("[data-campo='nome']").value.trim();
  const email = linha.querySelector("[data-campo='email']").value.trim();
  const botaoSalvar = linha.querySelector("[data-acao='salvar-edicao']");

  if (!nome) {
    mostrarAlerta("Informe o nome.", "erro");
    return;
  }
  if (!email || !validarEmail(email)) {
    mostrarAlerta("Informe um e-mail válido.", "erro");
    return;
  }

  botaoSalvar.disabled = true;
  botaoSalvar.textContent = "Salvando...";

  fetch(`/api/admin/usuarios/${usuario.id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ nome, email }),
  })
    .then(async (resposta) => {
      const corpo = await resposta.json();
      if (!resposta.ok || !corpo.sucesso) {
        throw new Error(corpo.mensagem || "Não foi possível salvar as alterações.");
      }
      return corpo;
    })
    .then((corpo) => {
      mostrarAlerta(corpo.mensagem, "sucesso");
      carregarListaDeUsuarios(); // recarrega a tabela com os dados já atualizados
    })
    .catch((erro) => {
      mostrarAlerta(erro.message, "erro");
      botaoSalvar.disabled = false;
      botaoSalvar.textContent = "Salvar";
    });
}

/* ============================================================
 * CAIXA 2 — CADASTRAR FUNCIONÁRIO
 * Formulário sempre visível na própria página; ao ter sucesso,
 * recarrega a CAIXA 1 para o novo funcionário aparecer na lista.
 * ============================================================ */
function inicializarFormularioCadastrarFuncionario() {
  const formulario = document.getElementById("formulario-cadastrar-funcionario");

  inicializarAlternadoresSenha();
  inicializarMedidorForcaSenha("funcionario-senha");
  inicializarMascaraCPF("funcionario-cpf");
  inicializarSeletorTipoColaborador();

  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    esconderAlerta();

    const tipoColaborador = document.querySelector('input[name="tipoColaborador"]:checked').value;

    const nome = document.getElementById("funcionario-nome");
    const email = document.getElementById("funcionario-email");
    const cpf = document.getElementById("funcionario-cpf");
    const dataNascimento = document.getElementById("funcionario-data-nascimento");
    const cargo = document.getElementById("funcionario-cargo");
    const setor = document.getElementById("funcionario-setor");
    const registroProfissional = document.getElementById("funcionario-registro-profissional");
    const senha = document.getElementById("funcionario-senha");
    const confirmarSenha = document.getElementById("funcionario-confirmar-senha");
    const botaoSalvar = formulario.querySelector("button[type='submit']");

    let valido = true;

    if (!nome.value.trim()) {
      definirErroCampo("funcionario-nome", "Informe o nome completo.");
      valido = false;
    } else {
      definirErroCampo("funcionario-nome", null);
    }

    if (!email.value.trim()) {
      definirErroCampo("funcionario-email", "Informe o e-mail.");
      valido = false;
    } else if (!validarEmail(email.value)) {
      definirErroCampo("funcionario-email", "E-mail inválido.");
      valido = false;
    } else {
      definirErroCampo("funcionario-email", null);
    }

    if (!validarCPF(cpf.value)) {
      definirErroCampo("funcionario-cpf", "CPF inválido.");
      valido = false;
    } else {
      definirErroCampo("funcionario-cpf", null);
    }

    if (!dataNascimento.value) {
      definirErroCampo("funcionario-data-nascimento", "Informe a data de nascimento.");
      valido = false;
    } else if (new Date(dataNascimento.value) > new Date()) {
      definirErroCampo("funcionario-data-nascimento", "A data de nascimento não pode ser no futuro.");
      valido = false;
    } else {
      definirErroCampo("funcionario-data-nascimento", null);
    }

    // Cargo/Setor só são obrigatórios quando o tipo escolhido é FUNCIONARIO
    // (um Gestor não tem esses campos no diagrama).
    if (tipoColaborador === "FUNCIONARIO") {
      if (!cargo.value) {
        definirErroCampo("funcionario-cargo", "Selecione o cargo.");
        valido = false;
      } else {
        definirErroCampo("funcionario-cargo", null);
      }

      if (!setor.value) {
        definirErroCampo("funcionario-setor", "Selecione o setor.");
        valido = false;
      } else {
        definirErroCampo("funcionario-setor", null);
      }
    } else {
      // Limpa qualquer erro que tenha ficado de uma tentativa anterior
      // como Funcionário, já que esses campos não se aplicam ao Gestor.
      definirErroCampo("funcionario-cargo", null);
      definirErroCampo("funcionario-setor", null);
    }

    if (!registroProfissional.value.trim()) {
      definirErroCampo("funcionario-registro-profissional", "Informe o registro profissional.");
      valido = false;
    } else {
      definirErroCampo("funcionario-registro-profissional", null);
    }

    if (calcularPontuacaoSenha(senha.value) < 2) {
      definirErroCampo("funcionario-senha", "Use ao menos 8 caracteres, com letras e números.");
      valido = false;
    } else {
      definirErroCampo("funcionario-senha", null);
    }

    if (confirmarSenha.value !== senha.value || !confirmarSenha.value) {
      definirErroCampo("funcionario-confirmar-senha", "As senhas não coincidem.");
      valido = false;
    } else {
      definirErroCampo("funcionario-confirmar-senha", null);
    }

    if (!valido) {
      mostrarAlerta("Corrija os campos destacados para continuar.", "erro");
      return;
    }

    botaoSalvar.disabled = true;
    botaoSalvar.textContent = "Cadastrando...";

    fetch("/api/admin/colaboradores", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        nome: nome.value.trim(),
        email: email.value.trim(),
        cpf: cpf.value.replace(/\D/g, ""),
        dataNascimento: dataNascimento.value,
        tipoConta: tipoColaborador,
        // Cargo/setor só fazem sentido para Funcionário; para Gestor,
        // mandamos vazio mesmo (o backend ignora esses campos nesse caso).
        cargo: tipoColaborador === "FUNCIONARIO" ? cargo.value : "",
        setor: tipoColaborador === "FUNCIONARIO" ? setor.value : "",
        registroProfissional: registroProfissional.value.trim(),
        senha: senha.value,
        confirmarSenha: confirmarSenha.value,
      }),
    })
      .then(async (resposta) => {
        const corpo = await resposta.json();
        if (!resposta.ok || !corpo.sucesso) {
          throw new Error(corpo.mensagem || "Não foi possível concluir o cadastro.");
        }
        return corpo;
      })
      .then((corpo) => {
        // mostrarAlerta (utilitarios.js) já rola a tela até a mensagem,
        // então ela aparece mesmo com o formulário lá embaixo da página.
        mostrarAlerta(corpo.mensagem, "sucesso");
        formulario.reset();
        document.querySelectorAll("[data-barra]").forEach((barra) => {
          barra.style.background = "var(--cor-borda)";
        });
        document.querySelector("[data-rotulo-forca]").textContent = "Digite uma senha";
        // Volta a exibir o bloco de Funcionário (opção padrão após o reset)
        document.querySelector('[data-campos-para="FUNCIONARIO"]').hidden = false;
        carregarListaDeUsuarios(); // mostra o colaborador recém-criado na caixa de cima
      })
      .catch((erro) => mostrarAlerta(erro.message, "erro"))
      .finally(() => {
        botaoSalvar.disabled = false;
        botaoSalvar.textContent = "Cadastrar";
      });
  });
}

/**
 * Ao trocar entre Funcionário/Gestor, mostra ou esconde o bloco de
 * Cargo/Setor (só existe para Funcionário no diagrama).
 */
function inicializarSeletorTipoColaborador() {
  const opcoes = document.querySelectorAll('input[name="tipoColaborador"]');
  const blocoCargoSetor = document.querySelector('[data-campos-para="FUNCIONARIO"]');

  opcoes.forEach((opcao) => {
    opcao.addEventListener("change", () => {
      blocoCargoSetor.hidden = opcao.value !== "FUNCIONARIO";
    });
  });
}