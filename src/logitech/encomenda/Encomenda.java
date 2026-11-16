package logitech.encomenda;

import logitech.modal.Modal;
import logitech.perfil.PerfilPrecificacao;

/** Dados imutaveis de uma encomenda. */
public final class Encomenda {
    private final int id;
    private final double pesoKg;
    private final double distanciaKm;
    private final String origem;
    private final String destino;
    private final Modal modal;
    private final PerfilPrecificacao perfilInicial;

    public Encomenda(int id, double pesoKg, double distanciaKm, String origem,
                     String destino, Modal modal, PerfilPrecificacao perfilInicial) {
        this.id = id;
        this.pesoKg = pesoKg;
        this.distanciaKm = distanciaKm;
        this.origem = origem;
        this.destino = destino;
        this.modal = modal;
        this.perfilInicial = perfilInicial;
    }

    public int getId() { return id; }
    public double getPesoKg() { return pesoKg; }
    public double getDistanciaKm() { return distanciaKm; }
    public String getOrigem() { return origem; }
    public String getDestino() { return destino; }
    public Modal getModal() { return modal; }
    public PerfilPrecificacao getPerfilInicial() { return perfilInicial; }
}
