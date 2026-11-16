package logitech.modal;

public class ModalAquaviario implements Modal {
    @Override public double getCustoPorKm() { return 0.60; }
    @Override public double getCustoPorKg() { return 0.15; }
    @Override public double getVelocidadeMedia() { return 30; }
    @Override public double getTempoManuseio() { return 24; }
    @Override public double getFatorEmissao() { return 0.016; }
    @Override public double getCapacidadeMaxima() { return 100000; }
}
