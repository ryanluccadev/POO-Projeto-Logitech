package logitech.modal;

public class ModalRodoviario implements Modal {
    @Override public double getCustoPorKm() { return 1.50; }
    @Override public double getCustoPorKg() { return 0.40; }
    @Override public double getVelocidadeMedia() { return 60; }
    @Override public double getTempoManuseio() { return 2; }
    @Override public double getFatorEmissao() { return 0.062; }
    @Override public double getCapacidadeMaxima() { return 20000; }
}
