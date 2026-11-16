package logitech.lote;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import logitech.calculo.CalculadoraFrete;
import logitech.calculo.ResultadoCalculo;
import logitech.encomenda.Encomenda;
import logitech.encomenda.ValidadorEncomenda;
import logitech.entrada.InterpretadorEntrada;
import logitech.entrada.ResultadoInterpretacao;
import logitech.relatorio.Relatorio;
import logitech.saida.FormatadorSaida;

/** Coordena leitura, validacao, calculo e relatorio de um lote de linhas. */
public class ProcessadorLote {
    private final InterpretadorEntrada interpretador;
    private final ValidadorEncomenda validador;
    private final CalculadoraFrete calculadora;
    private final FormatadorSaida formatador;

    public ProcessadorLote() {
        this(new InterpretadorEntrada(), new ValidadorEncomenda(),
             new CalculadoraFrete(), new FormatadorSaida());
    }

    public ProcessadorLote(InterpretadorEntrada interpretador, ValidadorEncomenda validador,
                           CalculadoraFrete calculadora, FormatadorSaida formatador) {
        this.interpretador = interpretador;
        this.validador = validador;
        this.calculadora = calculadora;
        this.formatador = formatador;
    }

    /** Retorna as linhas de saida: um registro por linha nao vazia, mais o relatorio. */
    public List<String> processarLote(List<String> linhas) {
        Relatorio relatorio = new Relatorio();
        List<String> saida = new ArrayList<>();

        for (int i = 0; i < linhas.size(); i++) {
            ResultadoInterpretacao ri = interpretador.interpretar(linhas.get(i), i + 1);
            if (ri.isIgnorada()) continue;

            if (ri.isRejeitada()) {
                relatorio.registrarRejeitada();
                saida.add(formatador.formatarRejeitada(ri.getIdSaida(), ri.getMotivo()));
                continue;
            }

            Encomenda e = ri.getEncomenda();
            Optional<String> motivo = validador.validar(e);
            if (motivo.isPresent()) {
                relatorio.registrarRejeitada();
                saida.add(formatador.formatarRejeitada(ri.getIdSaida(), motivo.get()));
                continue;
            }

            ResultadoCalculo rc = calculadora.calcular(e);
            relatorio.registrarValida(rc);
            saida.add(formatador.formatarValida(ri.getIdSaida(), rc));
        }

        saida.add(formatador.formatarRelatorio(relatorio.gerarRelatorio()));
        return saida;
    }
}
