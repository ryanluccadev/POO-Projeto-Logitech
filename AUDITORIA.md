# Auditoria de conformidade com o enunciado v2

Referência: `enunciado-logitech-v2.pdf` (autoridade máxima). Evidências reais obtidas em OpenJDK 21.

## Estado inicial (versão anterior, antes desta auditoria)
O programa **rejeitava todas as linhas** do exemplo 5.2 (campos lidos na ordem errada) e imprimia mensagens
descritivas em vez do formato `VALIDA;…/REJEITADA;…/RELATORIO;…`. Os 101 testes passavam porque validavam
o formato suposto, não o do enunciado.

## Checklist final
| Seção | Requisito | Situação | Evidência |
|---|---|---|---|
| 3.1/3.2 | Parâmetros dos 3 modais; frete, tempo, CO₂ base; sobretaxa 5% > 1000 kg (também com distância 0) | OK | Gabarito 5.3 e 3 bordas; 400+ combinações contra oráculo escrito de novo no teste |
| 3.3 | Perfis (multiplicadores) independentes do modal | OK | Testes dos 9 pares modal×perfil |
| 3.4 | HALF_UP em 2 casas por encomenda; média só no final; agregados com valores arredondados | OK | Empates exatos testados; `79,865 → 79,87`; 1M linhas idênticas ao oráculo decimal |
| 4.1 | Todas as linhas da tabela de validação, ordem de prioridade, fronteiras (peso = capacidade, distância 0) | OK | Testes por motivo e por prioridade |
| 4.1 | Linha em branco ignorada; lista vazia → zeros sem divisão por zero | OK | `vazio.esperado`, stdin vazio |
| 4.2 | Estado inicial; transições (matriz 3×3); posição (limites, NaN, par inteiro rejeitado, 1ª posição conta); troca de perfil (rejeitada após ENTREGUE); simulação (permitida sempre, sem aviso); transporte só de encomenda válida | OK | Testes de rastreamento |
| 4.2 | Operação rejeitada: não altera estado, não avisa, o solicitante sabe | OK | Retorno `boolean`; justificativa no Documento de Projeto (seção 11) |
| 4.3 | Central (log por evento), Auditoria (soma na entrega com o perfil vigente), Cliente (só status); sem avisos em mudanças nulas ou rejeitadas | OK | Testes de notificação |
| 4.4 | Relatório: custo total, tempo médio, CO₂ total, válidas, rejeitadas | OK | Linha `RELATORIO;…` |
| 5.1 | Ordem `ID;MODAL;PESO;DIST;ORIGEM;DESTINO;PERFIL`; `split(";", -1)` + `trim`; 7 campos; `Integer.parseInt`; `Double.parseDouble` finito; códigos com maiúsculas | OK | Testes de leitura (inclui ordem antiga rejeitada) |
| 5.2 | Texto exato, `LINHA<n>` contando linhas em branco, vírgula decimal, 2 casas, sem milhar | OK | Exemplo 5.2 e 5.3 comparados byte a byte (`cmp`) |
| 6 | Sem testar modal/perfil fora da entrada; atributos `private`; getters sem estado mutável | OK | Reflexão sobre 22 classes + varredura do código-fonte nos testes |
| 7 | Cenários de evolução | Parcial (por desenho) | Provados com classes novas nos testes: modal ferroviário, perfil corporativo, perfil PREMIUM, alerta SMS, sobretaxa em ponto único, troca de perfil em execução, adicionar/remover interessado. **Não implementados no código final** (devem ser feitos ao vivo): `CANCELADO`, peso mínimo, sobretaxa 8% > 2000 kg, separador `,` |
| 8 | Documento de Projeto | OK | Seções 10 e 11 (Do projeto ao código) adicionadas |
| 9 | Código compilável, `COMO_EXECUTAR.md`, testes, documento revisado, relatório de IA | Parcial | Relatório de IA tem itens **[CONFIRMAR]** que só o aluno pode preencher |
| 10.1 | O(n), sem exceção não tratada, texto exato | OK | Lote de 1M linhas em ≈7 s; fuzz de 50 mil linhas aleatórias sem exceção |

## Pontos não verificados (honestidade)
- **Compatibilidade com Java 8:** o JDK de desenvolvimento não aceitou `--release 8/11/17`. Revisão textual não achou recursos pós-Java 8, mas **não foi compilado** nessas versões.
- **Coleções "ensinadas em sala":** o código usa `List`, `ArrayList`, `Collections.unmodifiableList` e `Optional`. Confirme com o professor se isso está dentro do que foi ensinado.
- **Base secreta e critérios da banca:** não conhecidos. Valores conferidos contra a aritmética decimal exata, dentro de ±0,01.
- **Relatório de uso de IA:** itens [CONFIRMAR] pendentes (prompts da Etapa 1 e sugestão rejeitada).
- **PDF do Documento de Projeto:** o zip original traz uma exportação antiga; exporte de novo do Claude Docs (a seção 11 foi adicionada depois).
