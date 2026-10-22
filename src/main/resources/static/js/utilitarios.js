/**
 * ============================================================
 * utilitarios.js
 * Funções compartilhadas por todas as páginas do projeto.
 * Deve ser carregado ANTES do script específico de cada página
 * (ex: <script th:src="@{/js/utilitarios.js}"></script> depois
 * <script th:src="@{/js/cadastro.js}"></script>).
 * ============================================================ */

/* ============================================================
 * ALTERNADOR DE VISIBILIDADE DE SENHA
 * Reutilizável em qualquer página que tenha campo de senha.
 * ============================================================ */
function inicializarAlternadoresSenha() {
  document.querySelectorAll("[data-alternar-senha]").forEach((botao) => {
    botao.addEventListener("click", () => {
      const idAlvo = botao.dataset.alternarSenha;
      const campo = document.getElementById(idAlvo);
      const estaVisivel = campo.type === "text";

      campo.type = estaVisivel ? "password" : "text";
      botao.setAttribute("aria-pressed", String(!estaVisivel));
      botao.setAttribute("aria-label", estaVisivel ? "Mostrar senha" : "Ocultar senha");
    });
  });
}

/* ============================================================
 * MEDIDOR DE FORÇA DE SENHA (usado na página de cadastro)
 * ============================================================ */
function inicializarMedidorForcaSenha(idCampoSenha) {
  const campoSenha = document.getElementById(idCampoSenha);
  if (!campoSenha) return;

  const barras = document.querySelectorAll("[data-barra]");
  const rotulo = document.querySelector("[data-rotulo-forca]");

  const niveis = [
    { minimo: 0, texto: "Digite uma senha", cor: "var(--cor-borda)", quantidade: 0 },
    { minimo: 1, texto: "Fraca", cor: "var(--cor-forca-fraca)", quantidade: 1 },
    { minimo: 2, texto: "Razoável", cor: "var(--cor-forca-razoavel)", quantidade: 2 },
    { minimo: 3, texto: "Boa", cor: "var(--cor-forca-boa)", quantidade: 3 },
    { minimo: 4, texto: "Forte", cor: "var(--cor-forca-forte)", quantidade: 4 },
  ];

  campoSenha.addEventListener("input", () => {
    const pontuacao = calcularPontuacaoSenha(campoSenha.value);
    const nivel = [...niveis].reverse().find((n) => pontuacao >= n.minimo) || niveis[0];

    barras.forEach((barra, indice) => {
      barra.style.background = indice < nivel.quantidade ? nivel.cor : "var(--cor-borda)";
    });
    rotulo.textContent = nivel.texto;
  });
}

/**
 * Heurística simples de força (0 a 4). Não é criptograficamente
 * rigorosa — serve apenas de feedback de UX no front-end.
 * A validação de segurança real deve sempre ocorrer no backend.
 */
function calcularPontuacaoSenha(valor) {
  let pontuacao = 0;
  if (valor.length >= 8) pontuacao++;
  if (/[A-Z]/.test(valor) && /[a-z]/.test(valor)) pontuacao++;
  if (/\d/.test(valor)) pontuacao++;
  if (/[^A-Za-z0-9]/.test(valor)) pontuacao++;
  return valor.length === 0 ? 0 : pontuacao;
}

/* ============================================================
 * MÁSCARA E VALIDAÇÃO DE CPF (usado na página de administrador)
 * ============================================================ */
function inicializarMascaraCPF(idCampoCPF) {
  const campoCPF = document.getElementById(idCampoCPF);
  if (!campoCPF) return;

  campoCPF.addEventListener("input", () => {
    campoCPF.value = formatarCPF(campoCPF.value);
  });
}

function formatarCPF(valor) {
  const digitos = valor.replace(/\D/g, "").slice(0, 11);
  return digitos
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d)/, "$1.$2")
    .replace(/(\d{3})(\d{1,2})$/, "$1-$2");
}

/**
 * Validação de CPF pelo algoritmo oficial de dígitos verificadores.
 * Rejeita sequências óbvias (ex: 111.111.111-11).
 */
function validarCPF(valor) {
  const digitos = valor.replace(/\D/g, "");
  if (digitos.length !== 11 || /^(\d)\1{10}$/.test(digitos)) return false;

  const calcularDigito = (tamanho) => {
    let soma = 0;
    for (let indice = 0; indice < tamanho; indice++) {
      soma += Number(digitos[indice]) * (tamanho + 1 - indice);
    }
    const resto = (soma * 10) % 11;
    return resto === 10 ? 0 : resto;
  };

  const primeiroDigito = calcularDigito(9);
  const segundoDigito = calcularDigito(10);

  return (
    primeiroDigito === Number(digitos[9]) &&
    segundoDigito === Number(digitos[10])
  );
}

/* ============================================================
 * VALIDAÇÃO — UTILITÁRIOS GERAIS
 * ============================================================ */
function definirErroCampo(idCampo, mensagem) {
  const campo = document.getElementById(idCampo);
  const elementoCampo = campo.closest(".campo");
  const elementoErro = document.querySelector(`[data-erro-para="${idCampo}"]`);

  if (mensagem) {
    elementoCampo.classList.add("esta-invalido");
    elementoCampo.classList.remove("esta-valido");
    elementoErro.textContent = mensagem;
  } else {
    elementoCampo.classList.remove("esta-invalido");
    elementoCampo.classList.add("esta-valido");
    elementoErro.textContent = "";
  }
}

function validarEmail(valor) {
  // Checagem simples o suficiente para o front-end;
  // validação definitiva deve ocorrer no backend.
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor);
}

function calcularIdade(dataNascimento) {
  const nascimento = new Date(dataNascimento);
  const hoje = new Date();
  let idade = hoje.getFullYear() - nascimento.getFullYear();
  const aindaNaoFezAniversario =
    hoje.getMonth() < nascimento.getMonth() ||
    (hoje.getMonth() === nascimento.getMonth() && hoje.getDate() < nascimento.getDate());
  if (aindaNaoFezAniversario) idade--;
  return idade;
}

/* ============================================================
 * ALERTA GLOBAL (sucesso / erro)
 * ============================================================ */
function mostrarAlerta(mensagem, variante = "sucesso") {
  const elementoAlerta = document.querySelector("[data-elemento='alerta']");
  elementoAlerta.textContent = mensagem;
  elementoAlerta.dataset.variante = variante;
  elementoAlerta.hidden = false;

  // Em páginas mais longas (ex: painel administrativo), o alerta pode
  // ficar fora da área visível se o usuário estiver com a tela rolada
  // para baixo (ex: preenchendo um formulário no fim da página). Rolar
  // até ele garante que a mensagem sempre seja vista.
  elementoAlerta.scrollIntoView({ behavior: "smooth", block: "center" });
}

function esconderAlerta() {
  const elementoAlerta = document.querySelector("[data-elemento='alerta']");
  elementoAlerta.hidden = true;
  elementoAlerta.textContent = "";
}

/* ============================================================
 * SIMULAÇÃO DE REQUISIÇÃO ASSÍNCRONA
 * Usada enquanto o backend real ainda não está pronto.
 * Nos próximos arquivos vamos trocar isso por fetch() de verdade.
 * ============================================================ */
function simularRequisicao() {
  return new Promise((resolver) => setTimeout(resolver, 600));
}