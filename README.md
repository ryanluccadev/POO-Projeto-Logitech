# LogiTech — Simulação Logística

Núcleo de simulação de uma operadora logística fictícia, em **Java puro** (sem dependências externas).
Calcula **frete, tempo de entrega e emissão de CO₂** para encomendas transportadas por três modais
(rodoviário, aéreo e aquaviário) sob três perfis de precificação (econômico, rápido e ecológico),
acompanha o status e a posição de cada transporte e avisa automaticamente os interessados.
Também processa lotes de encomendas lidos da entrada padrão e emite um relatório consolidado.

Projeto acadêmico de **Programação Orientada a Objetos**: o foco é o projeto OO e a facilidade de absorver
mudanças (novos modais, perfis e interessados sem alterar o código existente).

## Início rápido

```sh
./compilar.sh
java -cp out logitech.Main < entrada_exemplo.txt
```

Entrada (`;`, sem cabeçalho): `ID;MODAL;PESO_KG;DISTANCIA_KM;ORIGEM;DESTINO;PERFIL`

```
1;RODOVIARIO;500;1000;Curitiba;Recife;ECONOMICO
2;AEREO;-5;1000;Sao Paulo;Manaus;RAPIDO

abc
3;AQUAVIARIO;2000;3000;Santos;Belem;ECOLOGICO
```

Saída (vírgula decimal, 2 casas, sem milhar):

```
VALIDA;1;1445,00;23,33;31,00
REJEITADA;2;peso invalido
REJEITADA;LINHA4;formato invalido
VALIDA;3;2425,50;136,40;67,20
RELATORIO;3870,50;79,87;98,20;2;2
```

Formatos: `VALIDA;ID;FRETE;TEMPO_H;CO2_KG` · `REJEITADA;ID;MOTIVO` ·
`RELATORIO;CUSTO_TOTAL;TEMPO_MEDIO_H;CO2_TOTAL;VALIDAS;REJEITADAS`.
Linhas inválidas são rejeitadas com o motivo, sem interromper o lote; linhas em branco são ignoradas.
Instruções completas (Linux, macOS, Windows): [COMO_EXECUTAR.md](COMO_EXECUTAR.md).

## Testes

```sh
./testar.sh
```

Compila, executa a bateria automatizada (`test/logitech/TestesLogiTech.java`, 621 verificações) e compara a saída do
programa, byte a byte, com os exemplos do enunciado em `test/dados/`. Cobre cálculos, validação, rastreamento,
avisos, relatório, robustez (entradas aleatórias), desempenho (lotes de centenas de milhares de linhas) e as
restrições de projeto (encapsulamento, ausência de `instanceof`/`switch` sobre modal ou perfil fora da entrada).
A CI executa o mesmo script em Java 11, 17 e 21.

## Arquitetura

| Ponto de variação | Mecanismo | Classes |
|---|---|---|
| Modal de transporte | **Strategy** | `Modal`, `ModalRodoviario`, `ModalAereo`, `ModalAquaviario` |
| Perfil de precificação | **Strategy** | `PerfilPrecificacao`, `PerfilEconomico`, `PerfilRapido`, `PerfilEcologico` |
| Interessados nos avisos | **Observer** | `Transporte` (sujeito), `Interessado`, `CentralControle`, `Cliente`, `AuditoriaAmbiental` |
| Texto → objetos | **Fábrica simples** | `InterpretadorEntrada` (os únicos `switch` do sistema) |

`CalculadoraFrete` é o contexto do Strategy e depende só das interfaces. Um novo modal, perfil ou interessado
entra com classes novas; a única alteração em código existente fica na camada de entrada.

```
src/logitech
├── Main.java                      leitura da entrada padrão e impressão do resultado
├── lote/          ProcessadorLote
├── entrada/       InterpretadorEntrada, ResultadoInterpretacao
├── encomenda/     Encomenda, ValidadorEncomenda
├── modal/         Modal e implementações
├── perfil/        PerfilPrecificacao e implementações
├── calculo/       CalculadoraFrete, ResultadoCalculo
├── rastreamento/  Transporte, StatusTransporte, TipoEvento, Posicao, Interessado
├── notificacao/   CentralControle, Cliente, AuditoriaAmbiental
├── relatorio/     Relatorio
└── saida/         FormatadorSaida
```

| Pacote | Depende de |
|---|---|
| `lote` | `entrada`, `encomenda`, `calculo`, `relatorio`, `saida` |
| `entrada` | `encomenda`, `modal`, `perfil` |
| `encomenda` | `modal`, `perfil` |
| `calculo` | `encomenda`, `modal`, `perfil` |
| `rastreamento` | `encomenda`, `calculo`, `perfil` |
| `notificacao` | `rastreamento`, `calculo` |
| `relatorio` | `calculo` |
| `saida` | `calculo`, `relatorio` |
| `modal`, `perfil` | — |

## Decisões técnicas

- **Aritmética decimal exata (`BigDecimal`)**, com arredondamento HALF_UP para 2 casas só no final, por encomenda.
  Com `double`, empates como 18,5175 podiam arredondar para baixo (≈0,6% das linhas num lote de 1 milhão).
- **Operações rejeitadas** (`alterarStatus`, `atualizarPosicao`, `trocarPerfil`) devolvem `false`, sem alterar o estado
  nem avisar ninguém. Exceção só para violação de pré-condição (`Posicao` inválida, `Transporte` de encomenda inválida).
- **Relatório em uma única passagem** pela lista (O(n)); 1 milhão de linhas em cerca de 7 s.
- **Sobretaxa de carga pesada** (5% acima de 1000 kg) em um único ponto: `CalculadoraFrete.aplicarSobretaxa()`.

## Como estender

- Novo modal: classe em `modal` + um `case` em `InterpretadorEntrada.criarModal()`.
- Novo perfil: classe em `perfil` + um `case` em `InterpretadorEntrada.criarPerfil()`.
- Novo interessado: classe implementando `Interessado`, cadastrada com `Transporte.adicionarInteressado()`.

Os testes demonstram esses cenários com classes escritas só no teste (modal ferroviário, perfis corporativo e
premium, alerta por SMS), sem alterar nenhuma classe do sistema.

## Documentação

- [COMO_EXECUTAR.md](COMO_EXECUTAR.md): JDK, classe principal e comandos de compilação e execução.
- [docs/AUDITORIA.md](docs/AUDITORIA.md): conformidade com o enunciado e pontos não verificados.
- [docs/RELATORIO_USO_IA.md](docs/RELATORIO_USO_IA.md): relatório de uso de IA (contém itens a confirmar pelo autor).

## Licença

[MIT](LICENSE)
