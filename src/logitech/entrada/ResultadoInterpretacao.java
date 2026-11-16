package logitech.entrada;

import logitech.encomenda.Encomenda;

/** Resultado da interpretacao de uma linha: interpretada, rejeitada ou ignorada. */
public final class ResultadoInterpretacao {
    private final Encomenda encomenda;
    private final String motivo;
    private final String idSaida;
    private final boolean ignorada;

    private ResultadoInterpretacao(Encomenda encomenda, String motivo, String idSaida, boolean ignorada) {
        this.encomenda = encomenda;
        this.motivo = motivo;
        this.idSaida = idSaida;
        this.ignorada = ignorada;
    }

    public static ResultadoInterpretacao interpretada(Encomenda e) {
        return new ResultadoInterpretacao(e, null, String.valueOf(e.getId()), false);
    }

    public static ResultadoInterpretacao rejeitada(String motivo, String idSaida) {
        return new ResultadoInterpretacao(null, motivo, idSaida, false);
    }

    public static ResultadoInterpretacao ignorada() {
        return new ResultadoInterpretacao(null, null, null, true);
    }

    public boolean isIgnorada() { return ignorada; }
    public boolean isRejeitada() { return !ignorada && encomenda == null; }
    public boolean isInterpretada() { return encomenda != null; }
    public Encomenda getEncomenda() { return encomenda; }
    public String getMotivo() { return motivo; }
    public String getIdSaida() { return idSaida; }
}
