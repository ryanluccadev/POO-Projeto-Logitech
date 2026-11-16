# LogiTech — Sistema de Simulação Logística (Java)

Implementação do Documento de Projeto (Etapa 1), conforme o enunciado v2.
Compilação, execução e testes: veja **COMO_EXECUTAR.md**. Auditoria de conformidade: **AUDITORIA.md**.
Relatório de uso de IA: **RELATORIO_USO_IA.md**.

## Formato
Entrada (`;`, sem cabeçalho): `ID;MODAL;PESO_KG;DISTANCIA_KM;ORIGEM;DESTINO;PERFIL`
(MODAL: RODOVIARIO | AEREO | AQUAVIARIO — PERFIL: ECONOMICO | RAPIDO | ECOLOGICO; códigos diferenciam maiúsculas).

Saída (vírgula decimal, 2 casas, sem milhar):
```
VALIDA;ID;FRETE;TEMPO_H;CO2_KG
REJEITADA;ID;MOTIVO            (ID ou LINHA<n> quando o ID não é um inteiro legível)
RELATORIO;CUSTO_TOTAL;TEMPO_MEDIO_H;CO2_TOTAL;VALIDAS;REJEITADAS
```

## Estrutura de pacotes
```
src/logitech
├── Main.java                      leitura da entrada padrão e impressão do resultado
├── lote/          ProcessadorLote
├── entrada/       InterpretadorEntrada (fábrica simples), ResultadoInterpretacao
├── encomenda/     Encomenda, ValidadorEncomenda
├── modal/         Modal (interface), ModalRodoviario, ModalAereo, ModalAquaviario
├── perfil/        PerfilPrecificacao (interface), PerfilEconomico, PerfilRapido, PerfilEcologico
├── calculo/       CalculadoraFrete, ResultadoCalculo
├── rastreamento/  Transporte, StatusTransporte, TipoEvento, Posicao, Interessado (interface)
├── notificacao/   CentralControle, Cliente, AuditoriaAmbiental (implementam Interessado)
├── relatorio/     Relatorio
└── saida/         FormatadorSaida
test/logitech/     TestesLogiTech        test/dados/  exemplos e gabaritos do enunciado
```

| Pacote | Responsabilidade | Depende de |
|---|---|---|
| `logitech` | Ler a entrada padrão e imprimir o resultado | `lote` |
| `lote` | Coordenar o processamento das linhas e gerar o relatório | `entrada`, `encomenda`, `calculo`, `relatorio`, `saida` |
| `entrada` | Converter texto e códigos em objetos do domínio | `encomenda`, `modal`, `perfil` |
| `encomenda` | Dados imutáveis da encomenda e regras de validação | `modal`, `perfil` |
| `modal` | Parâmetros de cada modal (Strategy) | — |
| `perfil` | Multiplicadores de cada perfil (Strategy) | — |
| `calculo` | Fórmulas de frete, tempo e CO₂ | `encomenda`, `modal`, `perfil` |
| `rastreamento` | Status, posição, perfil ativo e sujeito do Observer | `encomenda`, `calculo`, `perfil` |
| `notificacao` | Observadores concretos | `rastreamento`, `calculo` |
| `relatorio` | Acúmulo de valores e contagens do lote | `calculo` |
| `saida` | Formato textual da saída | `calculo`, `relatorio` |

`modal` e `perfil` não dependem de nenhum outro pacote; `calculo` usa só as interfaces.
`Interessado` fica em `rastreamento` (junto do sujeito), evitando dependência circular com `notificacao`.

## Padrões
- **Strategy:** `modal` e `perfil`; contexto em `calculo.CalculadoraFrete`.
- **Observer:** `rastreamento.Transporte` (sujeito) + `rastreamento.Interessado` + `notificacao`.
- **Fábrica simples:** `entrada.InterpretadorEntrada` — únicos `switch` do sistema; nenhum outro código testa modal ou perfil.

## Decisões técnicas
- **Aritmética decimal exata (`BigDecimal`)** nos cálculos; arredondamento HALF_UP para 2 casas só no final, por encomenda.
  Com `double`, empates como 18,5175 podiam arredondar para baixo (≈0,6% das linhas num lote de 1 milhão, e viés no total).
- **Operações rejeitadas** (`alterarStatus`, `atualizarPosicao`, `trocarPerfil`) devolvem `false`, sem alterar o estado nem avisar ninguém.
  Exceção (`IllegalArgumentException`) só para violação de pré-condição: `Posicao` inválida e `Transporte` de encomenda inválida.
- **Relatório em uma única passagem** pela lista (O(n)); soma de valores já arredondados (BigDecimal).

## Como estender
- Novo modal: classe em `modal` + um `case` em `InterpretadorEntrada.criarModal()`.
- Novo perfil: classe em `perfil` + um `case` em `InterpretadorEntrada.criarPerfil()`.
- Novo interessado: classe implementando `Interessado`, cadastrada com `Transporte.adicionarInteressado()`.
- Sobretaxa de carga pesada: um único ponto, `CalculadoraFrete.aplicarSobretaxa()`.
Os testes provam os cenários com classes novas escritas só no teste (modal ferroviário, perfis corporativo e premium, alerta SMS).
