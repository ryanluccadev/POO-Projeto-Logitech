package logitech.perfil;

import java.math.BigDecimal;

public class PerfilEcologico implements PerfilPrecificacao {
    private static final BigDecimal FRETE = new BigDecimal("1.10");
    private static final BigDecimal TEMPO = new BigDecimal("1.10");
    private static final BigDecimal CO2 = new BigDecimal("0.70");

    @Override public BigDecimal calcularFrete(BigDecimal freteBase) { return freteBase.multiply(FRETE); }
    @Override public BigDecimal calcularTempo(BigDecimal tempoBase) { return tempoBase.multiply(TEMPO); }
    @Override public BigDecimal calcularCo2(BigDecimal co2Base) { return co2Base.multiply(CO2); }
}
