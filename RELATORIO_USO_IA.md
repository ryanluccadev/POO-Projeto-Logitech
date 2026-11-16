# Relatório de uso de IA — LogiTech (Etapa 2)

**Ferramenta:** Claude (Anthropic), pelo chat claude.ai. **Aluno:** [NOME] — **Turma/Data:** [CONFIRMAR]

> **Atenção, aluno:** os trechos marcados **[CONFIRMAR]** dependem do seu histórico real e **não podem ser
> inventados**. O restante foi extraído da conversa em que o código foi gerado e corrigido. Revise tudo antes
> de entregar: você responde por cada linha deste relatório na defesa oral (seção 10.3 do enunciado).

## 1. Principais prompts utilizados

| # | Prompt (resumo fiel) | Objetivo |
|---|---|---|
| 1 | **[CONFIRMAR]** Prompt(s) usados para gerar o Documento de Projeto da Etapa 1 e como você descreveu o problema à ferramenta (cole o texto real). | Etapa 1 |
| 2 | "Com base nessa documentação, faça o projeto." (anexei o link do Documento de Projeto). | Gerar a implementação a partir do documento. **O enunciado não foi enviado junto.** |
| 3 | "Crie o projeto com pacotes para ficar melhor organizado, adicione na documentação também." | Organização em pacotes e seção no documento. |
| 4 | Prompt de auditoria: anexei o projeto (`.zip`) e o `enunciado-logitech-v2.pdf` e pedi uma auditoria completa — o enunciado como autoridade máxima, diagnóstico em tabela, correções, testes executados de verdade, documentação revisada, relatório de IA sem inventar experiências e "não declare 100% concluído enquanto existir requisito não verificado". Listei quatro problemas já suspeitos: ordem dos campos, formato de saída, tipo do ID e testes que validavam o formato errado. | Corrigir e provar a conformidade com o enunciado. |

**Como descrevi o projeto à ferramenta:** nos prompts 2 e 3 pelo Documento de Projeto (UML, catálogo de classes,
padrões); no prompt 4 pelo enunciado oficial. A diferença entre os dois momentos explica o erro 1 abaixo.

## 2. Casos em que a IA errou

**Erro 1 — Formato de entrada e de saída inventados (violou o enunciado).**
Sem ter o enunciado, a IA assumiu a ordem `ID;peso;distância;origem;destino;modal;perfil` e mensagens do tipo
`ENCOMENDA 1 | Frete: R$ …`. O enunciado (5.1 e 5.2) exige `ID;MODAL;PESO;DISTANCIA;ORIGEM;DESTINO;PERFIL` e
`VALIDA;ID;FRETE;TEMPO_H;CO2_KG`. A IA chegou a declarar a suposição como "premissa", mas mesmo assim entregou o código.
*Como identifiquei:* ao comparar com o enunciado, o exemplo 5.2 passou a ser executado: o programa rejeitou as 4 linhas
com `formato invalido`. *Correção:* reescrita do `InterpretadorEntrada` e do `FormatadorSaida`, relatório em uma só
linha, ID `int` (`Integer.parseInt`) e testes que comparam o texto exato.

**Erro 2 — Testes circulares que davam falsa confiança.**
Os 101 testes da primeira versão passavam porque validavam os formatos que a própria IA tinha suposto, não os do
enunciado. *Como identifiquei:* na auditoria, ao confrontar os testes com as seções 4 e 5 do enunciado.
*Correção:* nova bateria cujos valores esperados vêm do enunciado (exemplo 5.2, gabarito 5.3, bordas), arquivos
`test/dados/*.esperado` comparados byte a byte e **testes de mutação**: 19 bugs injetados de propósito no código
(sobretaxa, arredondamento, ordem dos campos, locale, transições…), todos detectados pela bateria.

**Erro 3 — Código de cálculo incorreto em empates de arredondamento (`double`).**
A IA calculou com `double`. Ao rodar um lote sintético de 1 milhão de linhas e compará-lo com um oráculo independente
(Python, `Decimal`), cerca de 0,6% das linhas divergiam em ±0,01 (por exemplo 18,5175 virando 18,51) e o custo total
do relatório divergia em R$ 51. Cada linha estava dentro da tolerância, mas o erro se acumulava no relatório.
*Correção:* `CalculadoraFrete` e `PerfilPrecificacao` passaram a usar `BigDecimal`; depois disso as 970.121 linhas de saída
ficaram idênticas ao oráculo.

**Erro 4 — Requisito do enunciado não garantido.**
O enunciado (4.2) pede criar o transporte *a partir de uma encomenda válida*; o código aceitava qualquer `Encomenda`.
*Como identifiquei:* releitura do enunciado na auditoria. *Correção:* o construtor de `Transporte` valida a encomenda
(`IllegalArgumentException`) e há teste.

*Erros menores, também reais:* um teste com valor esperado sem arredondar (55,384 em vez de 55,38), detectado ao
executar; e três falhas na nova bateria que eram erros do teste (contagem de avisos; `instanceof` legítimo em
`Posicao.equals`), não do código — corrigidas sem enfraquecer a verificação.

## 3. Sugestão da IA rejeitada

**[CONFIRMAR — obrigatório pelo enunciado: pelo menos 1 caso real.]**
A conversa em que o código foi gerado e auditado **não contém** nenhuma sugestão de projeto que você tenha rejeitado,
então este item não pode ser preenchido por mim sem inventar. Procure no seu histórico, **principalmente na conversa
que gerou o Documento de Projeto da Etapa 1**, um caso real em que a IA propôs algo e você manteve a sua decisão.
Registre: (a) o que a IA sugeriu, (b) o que você manteve e (c) o motivo técnico. Verifique também se a decisão de
descartar algum padrão da seção 5.4 do Documento de Projeto (State, Decorator, Command, Singleton…) partiu de uma
sugestão da IA que você recusou: se sim, é um candidato legítimo. **Se nada disso aconteceu, não escreva um caso
fictício**: avise o professor e descreva o que de fato houve.

## 4. Como validei o que a IA produziu
Executei de verdade: compilação, 621 verificações automatizadas, comparação byte a byte com os exemplos do
enunciado, execução com `< arquivo` e por pipe, lote de 1 milhão de linhas (≈7 s, 256 MB de heap) e oráculo
decimal independente. Limites conhecidos: ver "Pontos não verificados" em `AUDITORIA.md`.
