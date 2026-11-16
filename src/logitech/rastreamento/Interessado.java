package logitech.rastreamento;

/** Observador: contrato de recebimento de avisos de um Transporte. */
public interface Interessado {
    void atualizar(Transporte transporte, TipoEvento evento);
}
