/**
 * painel.js
 * Lógica exclusiva da página painel.html.
 * Depende das funções definidas em utilitarios.js.
 */

document.addEventListener("DOMContentLoaded", () => {
  carregarUsuarioLogado();
  inicializarMenuUsuario();
});

/** Busca os dados do usuário logado (mesmo endpoint já usado em perfil.js). */
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
      if (!corpo) return; // já foi redirecionado
      preencherAvatarEDropdown(corpo.dados);
    })
    .catch((erro) => mostrarAlerta(erro.message, "erro"));
}

function preencherAvatarEDropdown(usuario) {
  document.querySelector("[data-elemento='iniciais']").textContent = obterIniciais(usuario.nome);
  document.querySelector("[data-elemento='nome-usuario']").textContent = usuario.nome;
  document.querySelector("[data-elemento='email-usuario']").textContent = usuario.email;
  document.querySelector("[data-elemento='nome-boas-vindas']").textContent = usuario.nome.split(" ")[0];
}

/** "Sabri Teste" -> "ST" | "Maria" -> "M" */
function obterIniciais(nomeCompleto) {
  const partes = nomeCompleto.trim().split(/\s+/);
  const primeira = partes[0]?.charAt(0) || "";
  const ultima = partes.length > 1 ? partes[partes.length - 1].charAt(0) : "";
  return (primeira + ultima).toUpperCase();
}

/* ============================================================
 * MENU SUSPENSO DO AVATAR
 * ============================================================ */
function inicializarMenuUsuario() {
  const botaoAvatar = document.querySelector("[data-acao='abrir-menu-usuario']");
  const dropdown = document.querySelector("[data-elemento='dropdown-usuario']");

  botaoAvatar.addEventListener("click", (evento) => {
    evento.stopPropagation();
    const estaAberto = !dropdown.hidden;
    dropdown.hidden = estaAberto;
    botaoAvatar.setAttribute("aria-expanded", String(!estaAberto));
  });

  // Fecha o menu ao clicar em qualquer lugar fora dele
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