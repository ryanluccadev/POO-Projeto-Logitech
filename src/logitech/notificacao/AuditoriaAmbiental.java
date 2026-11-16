package logitech.notificacao;

import java.math.BigDecimal;
import logitech.calculo.CalculadoraFrete;
import logitech.calculo.ResultadoCalculo;
import logitech.rastreamento.Interessado;
import logitech.rastreamento.StatusTransporte;
import logitech.rastreamento.TipoEvento;
import logitech.rastreamento.Transporte;

/** Acumula o CO2 das encomendas entregues, com o perfil vigente na entrega. */
public class AuditoriaAmbiental implements Interessado {
    private BigDecimal co2Acumulado = BigDecimal.ZERO;
    private final CalculadoraFrete calculadora;

    public AuditoriaAmbiental(CalculadoraFrete calculadora) {
        this.calculadora = calculadora;
    }

    @Override
    public void atualizar(Transporte t, TipoEvento evento) {
        if (evento != TipoEvento.STATUS || t.getStatus() != StatusTransporte.ENTREGUE) return;
        ResultadoCalculo r = calculadora.calcular(t.getEncomenda(), t.getPerfilAtivo());
        co2Acumulado = co2Acumulado.add(BigDecimal.valueOf(r.getCo2()));
    }

    public double getCo2Acumulado() { return co2Acumulado.doubleValue(); }
}
