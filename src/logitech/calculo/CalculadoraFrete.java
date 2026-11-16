package logitech.calculo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import logitech.encomenda.Encomenda;
import logitech.modal.Modal;
import logitech.perfil.PerfilPrecificacao;

/**
 * Contexto do Strategy: calcula os valores base a partir do Modal (formulas da secao 3.2),
 * aplica o PerfilPrecificacao informado e arredonda para 2 casas (HALF_UP) so no final.
 * Nao conhece nenhum modal nem perfil concreto. Aritmetica decimal exata (BigDecimal), para
 * que empates como 18,5175 arredondem sempre para cima, sem ruido de ponto flutuante.
 */
public class CalculadoraFrete {
    private static final BigDecimal LIMITE_CARGA_PESADA_KG = new BigDecimal("1000");
    private static final BigDecimal FATOR_CARGA_PESADA = new BigDecimal("1.05");
    private static final BigDecimal KG_POR_TONELADA = new BigDecimal("1000");

    public ResultadoCalculo calcular(Encomenda e) {
        return calcular(e, e.getPerfilInicial());
    }

    public ResultadoCalculo calcular(Encomenda e, PerfilPrecificacao perfil) {
        Modal m = e.getModal();
        BigDecimal km = BigDecimal.valueOf(e.getDistanciaKm());
        BigDecimal kg = BigDecimal.valueOf(e.getPesoKg());

        BigDecimal freteBase = aplicarSobretaxa(
                km.multiply(decimal(m.getCustoPorKm())).add(kg.multiply(decimal(m.getCustoPorKg()))), kg);
        BigDecimal tempoBase = km.divide(decimal(m.getVelocidadeMedia()), MathContext.DECIMAL128)
                .add(decimal(m.getTempoManuseio()));
        BigDecimal co2Base = kg.divide(KG_POR_TONELADA).multiply(km).multiply(decimal(m.getFatorEmissao()));

        return new ResultadoCalculo(
                arredondar(perfil.calcularFrete(freteBase)),
                arredondar(perfil.calcularTempo(tempoBase)),
                arredondar(perfil.calcularCo2(co2Base)));
    }

    /** Unico ponto da regra de sobretaxa de carga pesada (vale para todos os modais). */
    private BigDecimal aplicarSobretaxa(BigDecimal freteBase, BigDecimal pesoKg) {
        return pesoKg.compareTo(LIMITE_CARGA_PESADA_KG) > 0 ? freteBase.multiply(FATOR_CARGA_PESADA) : freteBase;
    }

    private BigDecimal decimal(double parametro) {
        return BigDecimal.valueOf(parametro);
    }

    private double arredondar(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
