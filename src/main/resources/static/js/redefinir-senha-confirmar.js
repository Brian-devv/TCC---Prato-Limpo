/**
 * redefinir-senha-confirmar.js
 * Lógica exclusiva da página redefinir-senha-confirmar.html.
 * Depende das funções definidas em utilitarios.js.
 */

document.addEventListener("DOMContentLoaded", () => {
  inicializarAlternadoresSenha();
  inicializarMedidorForcaSenha("nova-senha");
  inicializarFormularioNovaSenha();
});

/** Lê o token da URL, ex: /redefinir-senha/confirmar?token=abc123 */
function obterTokenDaUrl() {
  const parametros = new URLSearchParams(window.location.search);
  return parametros.get("token");
}

function inicializarFormularioNovaSenha() {
  const formulario = document.getElementById("formulario-nova-senha");
  const botaoEnviar = formulario.querySelector("button[type='submit']");
  const token = obterTokenDaUrl();

  if (!token) {
    mostrarAlerta("Link inválido. Solicite uma nova redefinição de senha.", "erro");
    botaoEnviar.disabled = true;
    return;
  }

  formulario.addEventListener("submit", (evento) => {
    evento.preventDefault();
    esconderAlerta();

    const novaSenha = document.getElementById("nova-senha");
    const confirmarNovaSenha = document.getElementById("confirmar-nova-senha");
    let valido = true;

    if (calcularPontuacaoSenha(novaSenha.value) < 2) {
      definirErroCampo("nova-senha", "Use ao menos 8 caracteres, com letras e números.");
      valido = false;
    } else {
      definirErroCampo("nova-senha", null);
    }

    if (confirmarNovaSenha.value !== novaSenha.value || !confirmarNovaSenha.value) {
      definirErroCampo("confirmar-nova-senha", "As senhas não coincidem.");
      valido = false;
    } else {
      definirErroCampo("confirmar-nova-senha", null);
    }

    if (!valido) {
      mostrarAlerta("Corrija os campos destacados para continuar.", "erro");
      return;
    }

    botaoEnviar.disabled = true;
    botaoEnviar.textContent = "Salvando...";

    fetch("/api/auth/redefinir-senha/confirmar", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        token: token,
        novaSenha: novaSenha.value,
        confirmarNovaSenha: confirmarNovaSenha.value,
      }),
    })
      .then(async (resposta) => {
        const corpo = await resposta.json();
        if (!resposta.ok || !corpo.sucesso) {
          throw new Error(corpo.mensagem || "Não foi possível redefinir sua senha.");
        }
        return corpo;
      })
      .then((corpo) => {
        mostrarAlerta(corpo.mensagem, "sucesso");
        formulario.reset();
        setTimeout(() => {
          window.location.href = "/entrar";
        }, 2000);
      })
      .catch((erro) => {
        mostrarAlerta(erro.message, "erro");
        botaoEnviar.disabled = false;
        botaoEnviar.textContent = "Salvar nova senha";
      });
  });
}