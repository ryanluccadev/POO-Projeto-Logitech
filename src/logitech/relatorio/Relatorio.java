package logitech.relatorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import logitech.calculo.ResultadoCalculo;

/** Acumula valores e contagens do lote, em uma unica passagem (O(n)). */
public class Relatorio {
    private BigDecimal custoTotal = BigDecimal.ZERO;
    private BigDecimal tempoAcumulado = BigDecimal.ZERO;
    private BigDecimal co2Total = BigDecimal.ZERO;
    private int validas = 0;
    private int rejeitadas = 0;

    /** Valores consolidados (imutavel). */
    public static final class Resumo {
        private final double custoTotal, tempoMedio, co2Total;
        private final int validas, rejeitadas;

        Resumo(double custoTotal, double tempoMedio, double co2Total, int validas, int rejeitadas) {
            this.custoTotal = custoTotal;
            this.tempoMedio = tempoMedio;
            this.co2Total = co2Total;
            this.validas = validas;
            this.rejeitadas = rejeitadas;
        }
        public double getCustoTotal() { return custoTotal; }
        public double getTempoMedio() { return tempoMedio; }
        public double getCo2Total() { return co2Total; }
        public int getValidas() { return validas; }
        public int getRejeitadas() { return rejeitadas; }
    }

    public void registrarValida(ResultadoCalculo r) {
        custoTotal = custoTotal.add(BigDecimal.valueOf(r.getFrete()));
        tempoAcumulado = tempoAcumulado.add(BigDecimal.valueOf(r.getTempo()));
        co2Total = co2Total.add(BigDecimal.valueOf(r.getCo2()));
        validas++;
    }

    public void registrarRejeitada() {
        rejeitadas++;
    }

    /** A media de tempo so e arredondada aqui, ao final. */
    public Resumo gerarRelatorio() {
        BigDecimal media = validas == 0
                ? BigDecimal.ZERO
                : tempoAcumulado.divide(BigDecimal.valueOf(validas), 2, RoundingMode.HALF_UP);
        return new Resumo(
                custoTotal.setScale(2, RoundingMode.HALF_UP).doubleValue(),
                media.doubleValue(),
                co2Total.setScale(2, RoundingMode.HALF_UP).doubleValue(),
                validas, rejeitadas);
    }
}
