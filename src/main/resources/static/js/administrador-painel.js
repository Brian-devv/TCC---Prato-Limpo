/**
 * administrador-painel.js
 * Lógica exclusiva da página administrador-painel.html.
 * Depende das funções definidas em utilitarios.js.
 */

// Guarda o id do administrador logado (usado para não deixar a
// tabela oferecer "Excluir" na própria linha dele).
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
 * CAIXA 1 — LISTAGEM DE USUÁRIOS (com botão Excluir)
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
        // Sessão válida, mas não é de administrador: manda para o painel comum.
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

/** Monta as linhas da tabela a partir da lista de usuários vinda da API. */
function renderizarTabelaUsuarios(usuarios) {
  const corpoTabela = document.querySelector("[data-elemento='corpo-tabela-usuarios']");
  corpoTabela.innerHTML = ""; // limpa antes de preencher, evita duplicar linhas em recarregamentos

  usuarios.forEach((usuario) => {
    const linha = document.createElement("tr");
    const classeSelo = usuario.status === "ATIVO" ? "selo-status--ativo" : "selo-status--inativo";

    // A própria linha do administrador logado não recebe botão de
    // excluir (o backend também bloqueia isso, mas escondê-lo aqui
    // evita que a pessoa clique e receba um erro sem necessidade).
    const podeExcluir = usuario.id !== idAdministradorLogado;

    linha.innerHTML = `
      <td>${escaparHtml(usuario.nome)}</td>
      <td>${escaparHtml(usuario.email)}</td>
      <td>${escaparHtml(formatarTipoPerfil(usuario.tipoPerfil))}</td>
      <td><span class="selo-status ${classeSelo}">${escaparHtml(usuario.status)}</span></td>
      <td>
        ${podeExcluir
          ? `<button type="button" class="botao-tabela botao-tabela--perigo" data-acao="excluir-usuario" data-id="${usuario.id}">Excluir</button>`
          : `<span class="texto-suave-tabela">—</span>`}
      </td>
    `;
    corpoTabela.appendChild(linha);
  });

  // Liga o evento de excluir em cada botão recém-criado
  corpoTabela.querySelectorAll("[data-acao='excluir-usuario']").forEach((botao) => {
    botao.addEventListener("click", () => confirmarExclusaoDeUsuario(botao.dataset.id, botao));
  });

  document.querySelector("[data-elemento='carregando-usuarios']").hidden = true;
  document.querySelector("[data-elemento='tabela-usuarios']").hidden = false;
}

function formatarTipoPerfil(tipoPerfil) {
  const rotulos = { USUARIO: "Usuário comum", FUNCIONARIO: "Funcionário", ADMINISTRADOR: "Administrador" };
  return rotulos[tipoPerfil] || tipoPerfil;
}

/** Evita que nome/e-mail vindos do banco quebrem o HTML se contiverem caracteres especiais. */
function escaparHtml(texto) {
  const div = document.createElement("div");
  div.textContent = texto ?? "";
  return div.innerHTML;
}

function confirmarExclusaoDeUsuario(idUsuario, botao) {
  const confirmou = window.confirm("Tem certeza que deseja excluir este usuário? Essa ação não pode ser desfeita.");
  if (!confirmou) return;

  botao.disabled = true;
  botao.textContent = "Excluindo...";

  fetch(`/api/admin/usuarios/${idUsuario}`, { method: "DELETE" })
    .then(async (resposta) => {
      const corpo = await resposta.json();
      if (!resposta.ok || !corpo.sucesso) {
        throw new Error(corpo.mensagem || "Não foi possível excluir o usuário.");
      }
      return corpo;
    })
    .then((corpo) => {
      mostrarAlerta(corpo.mensagem, "sucesso");
      carregarListaDeUsuarios(); // recarrega a tabela sem o usuário excluído
    })
    .catch((erro) => {
      mostrarAlerta(erro.message, "erro");
      botao.disabled = false;
      botao.textContent = "Excluir";
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

  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    esconderAlerta();

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

    if (!cargo.value.trim()) {
      definirErroCampo("funcionario-cargo", "Informe o cargo.");
      valido = false;
    } else {
      definirErroCampo("funcionario-cargo", null);
    }

    if (!setor.value.trim()) {
      definirErroCampo("funcionario-setor", "Informe o setor.");
      valido = false;
    } else {
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

    fetch("/api/admin/funcionarios", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        nome: nome.value.trim(),
        email: email.value.trim(),
        cpf: cpf.value.replace(/\D/g, ""),
        dataNascimento: dataNascimento.value,
        cargo: cargo.value.trim(),
        setor: setor.value.trim(),
        registroProfissional: registroProfissional.value.trim(),
        senha: senha.value,
        confirmarSenha: confirmarSenha.value,
      }),
    })
      .then(async (resposta) => {
        const corpo = await resposta.json();
        if (!resposta.ok || !corpo.sucesso) {
          throw new Error(corpo.mensagem || "Não foi possível cadastrar o funcionário.");
        }
        return corpo;
      })
      .then((corpo) => {
        mostrarAlerta(corpo.mensagem, "sucesso");
        formulario.reset();
        // reseta o medidor de força visualmente, já que o reset() não faz isso sozinho
        document.querySelectorAll("[data-barra]").forEach((barra) => {
          barra.style.background = "var(--cor-borda)";
        });
        document.querySelector("[data-rotulo-forca]").textContent = "Digite uma senha";
        carregarListaDeUsuarios(); // mostra o funcionário recém-criado na caixa de cima
      })
      .catch((erro) => mostrarAlerta(erro.message, "erro"))
      .finally(() => {
        botaoSalvar.disabled = false;
        botaoSalvar.textContent = "Cadastrar funcionário";
      });
  });
}