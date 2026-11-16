package logitech.calculo;

/** Frete, tempo e CO2 finais (ja arredondados) de um calculo. Imutavel. */
public final class ResultadoCalculo {
    private final double frete;
    private final double tempo;
    private final double co2;

    public ResultadoCalculo(double frete, double tempo, double co2) {
        this.frete = frete;
        this.tempo = tempo;
        this.co2 = co2;
    }

    public double getFrete() { return frete; }
    public double getTempo() { return tempo; }
    public double getCo2() { return co2; }
}
