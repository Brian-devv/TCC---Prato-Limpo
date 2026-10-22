/**
 * perfil.js
 * Lógica exclusiva da página perfil.html.
 * Depende das funções definidas em utilitarios.js.
 */

// Guarda os dados atuais do usuário em memória, pra poder preencher
// o formulário de edição e reverter a exibição sem precisar buscar
// tudo de novo na API.
let usuarioAtual = null;

document.addEventListener("DOMContentLoaded", () => {
  carregarPerfil();
  inicializarBotoesDeAcao();
  inicializarFormularioEdicao();
});

/** Busca os dados do usuário logado. Se não houver sessão válida, volta para /entrar. */
function carregarPerfil() {
  fetch("/api/usuarios/me")
    .then(async (resposta) => {
      if (resposta.status === 401) {
        window.location.href = "/entrar";
        return null;
      }

      const corpo = await resposta.json();
      if (!resposta.ok || !corpo.sucesso) {
        throw new Error(corpo.mensagem || "Não foi possível carregar seu perfil.");
      }
      return corpo;
    })
    .then((corpo) => {
      if (!corpo) return; // já foi redirecionado para /entrar
      usuarioAtual = corpo.dados;
      exibirDadosPerfil(usuarioAtual);
    })
    .catch((erro) => {
      mostrarAlerta(erro.message, "erro");
      document.querySelector("[data-elemento='carregando']").hidden = true;
    });
}

function exibirDadosPerfil(usuario) {
  document.querySelector("[data-campo='nome']").textContent = usuario.nome;
  document.querySelector("[data-campo='cpf']").textContent = formatarCPF(usuario.cpf);
  document.querySelector("[data-campo='dataNascimento']").textContent = formatarData(usuario.dataNascimento);
  document.querySelector("[data-campo='tipoPerfil']").textContent = formatarTipoPerfil(usuario.tipoPerfil);
  document.querySelector("[data-campo='dataCriacaoConta']").textContent = formatarData(usuario.dataCriacaoConta);

  // O botão "Voltar ao painel" precisa levar para o painel certo:
  // administradores voltam para /administrador/painel (com a listagem
  // de usuários); qualquer outro tipo volta para o /painel comum.
  const linkVoltar = document.querySelector("[data-elemento='link-voltar']");
  linkVoltar.href = usuario.tipoPerfil === "ADMINISTRADOR" ? "/administrador/painel" : "/painel";

  document.querySelector("[data-elemento='carregando']").hidden = true;
  document.querySelector("[data-elemento='conteudo-perfil']").hidden = false;
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

function formatarData(dataIso) {
  if (!dataIso) return "-";
  const [ano, mes, dia] = dataIso.split("-");
  return `${dia}/${mes}/${ano}`;
}

/* ============================================================
 * ALTERNÂNCIA ENTRE VISUALIZAÇÃO E EDIÇÃO
 * (CPF e data de nascimento não entram no formulário de edição,
 * de propósito — são dados que normalmente não mudam depois do
 * cadastro; só nome e e-mail são editáveis por enquanto)
 * ============================================================ */
function entrarModoEdicao() {
  document.getElementById("perfil-nome").value = usuarioAtual.nome;
  document.getElementById("perfil-email").value = usuarioAtual.email;

  document.querySelector("[data-elemento='visualizacao-perfil']").hidden = true;
  document.getElementById("formulario-editar-perfil").hidden = false;
}

function sairDoModoEdicao() {
  document.getElementById("formulario-editar-perfil").hidden = true;
  document.querySelector("[data-elemento='visualizacao-perfil']").hidden = false;
}

/* ============================================================
 * BOTÕES DA TELA DE VISUALIZAÇÃO
 * ============================================================ */
function inicializarBotoesDeAcao() {
  document.querySelector("[data-acao='editar-perfil']").addEventListener("click", entrarModoEdicao);
  document.querySelector("[data-acao='cancelar-edicao']").addEventListener("click", () => {
    esconderAlerta();
    sairDoModoEdicao();
  });
  document.querySelector("[data-acao='sair']").addEventListener("click", realizarLogout);
  document.querySelector("[data-acao='excluir-conta']").addEventListener("click", confirmarExclusaoDeConta);
}

/* ============================================================
 * FORMULÁRIO DE EDIÇÃO
 * ============================================================ */
function inicializarFormularioEdicao() {
  const formulario = document.getElementById("formulario-editar-perfil");

  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    esconderAlerta();

    const nome = document.getElementById("perfil-nome");
    const email = document.getElementById("perfil-email");
    const botaoSalvar = formulario.querySelector("button[type='submit']");

    let valido = true;

    if (!nome.value.trim()) {
      definirErroCampo("perfil-nome", "Informe seu nome completo.");
      valido = false;
    } else {
      definirErroCampo("perfil-nome", null);
    }

    if (!email.value.trim()) {
      definirErroCampo("perfil-email", "Informe seu e-mail.");
      valido = false;
    } else if (!validarEmail(email.value)) {
      definirErroCampo("perfil-email", "E-mail inválido.");
      valido = false;
    } else {
      definirErroCampo("perfil-email", null);
    }

    if (!valido) {
      mostrarAlerta("Corrija os campos destacados para continuar.", "erro");
      return;
    }

    botaoSalvar.disabled = true;
    botaoSalvar.textContent = "Salvando...";

    fetch("/api/usuarios/me", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        nome: nome.value.trim(),
        email: email.value.trim(),
      }),
    })
      .then(async (resposta) => {
        if (resposta.status === 401) {
          window.location.href = "/entrar";
          return null;
        }
        const corpo = await resposta.json();
        if (!resposta.ok || !corpo.sucesso) {
          throw new Error(corpo.mensagem || "Não foi possível salvar as alterações.");
        }
        return corpo;
      })
      .then((corpo) => {
        if (!corpo) return; // já foi redirecionado para /entrar
        usuarioAtual = corpo.dados;
        exibirDadosPerfil(usuarioAtual);
        sairDoModoEdicao();
        mostrarAlerta(corpo.mensagem, "sucesso");
      })
      .catch((erro) => mostrarAlerta(erro.message, "erro"))
      .finally(() => {
        botaoSalvar.disabled = false;
        botaoSalvar.textContent = "Salvar alterações";
      });
  });
}

/* ============================================================
 * SAIR / EXCLUIR CONTA
 * ============================================================ */
function realizarLogout() {
  fetch("/api/auth/sair", { method: "POST" })
    .then(() => {
      window.location.href = "/entrar";
    })
    .catch(() => mostrarAlerta("Não foi possível sair. Tente novamente.", "erro"));
}

function confirmarExclusaoDeConta() {
  const confirmou = window.confirm(
    "Tem certeza que deseja excluir sua conta? Essa ação não pode ser desfeita."
  );
  if (!confirmou) return;

  fetch("/api/usuarios/me", { method: "DELETE" })
    .then(async (resposta) => {
      const corpo = await resposta.json();
      if (!resposta.ok || !corpo.sucesso) {
        throw new Error(corpo.mensagem || "Não foi possível excluir sua conta.");
      }
      return corpo;
    })
    .then(() => {
      window.location.href = "/entrar";
    })
    .catch((erro) => mostrarAlerta(erro.message, "erro"));
}