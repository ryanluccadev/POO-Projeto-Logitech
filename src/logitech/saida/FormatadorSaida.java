package logitech.saida;

import java.math.BigDecimal;
import java.math.RoundingMode;
import logitech.calculo.ResultadoCalculo;
import logitech.relatorio.Relatorio;

/**
 * Unico lugar que conhece o formato textual de saida (secao 5.2 do enunciado):
 * <pre>
 * VALIDA;ID;FRETE;TEMPO_H;CO2_KG
 * REJEITADA;ID;MOTIVO
 * RELATORIO;CUSTO_TOTAL;TEMPO_MEDIO_H;CO2_TOTAL;VALIDAS;REJEITADAS
 * </pre>
 * Numeros: virgula decimal, duas casas, sem separador de milhar.
 */
public class FormatadorSaida {
    public String formatarValida(String id, ResultadoCalculo r) {
        return "VALIDA;" + id + ";" + numero(r.getFrete()) + ";" + numero(r.getTempo()) + ";" + numero(r.getCo2());
    }

    public String formatarRejeitada(String id, String motivo) {
        return "REJEITADA;" + id + ";" + motivo;
    }

    public String formatarRelatorio(Relatorio.Resumo r) {
        return "RELATORIO;" + numero(r.getCustoTotal()) + ";" + numero(r.getTempoMedio()) + ";"
                + numero(r.getCo2Total()) + ";" + r.getValidas() + ";" + r.getRejeitadas();
    }

    /**
     * Duas casas, virgula decimal, sem separador de milhar. BigDecimal.toPlainString() nao depende
     * da configuracao regional da maquina (nem usa notacao cientifica), entao o texto e sempre o mesmo.
     */
    private String numero(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}
