package logitech.perfil;

import java.math.BigDecimal;

/**
 * Define como um perfil transforma os valores base de frete, tempo e CO2 nos valores finais
 * (Strategy). Usa BigDecimal para que os multiplicadores sejam aplicados em aritmetica decimal
 * exata; o arredondamento final para 2 casas e feito pela CalculadoraFrete.
 */
public interface PerfilPrecificacao {
    BigDecimal calcularFrete(BigDecimal freteBase);
    BigDecimal calcularTempo(BigDecimal tempoBase);
    BigDecimal calcularCo2(BigDecimal co2Base);
}
