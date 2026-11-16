package logitech.modal;

public class ModalAereo implements Modal {
    @Override public double getCustoPorKm() { return 4.00; }
    @Override public double getCustoPorKg() { return 2.50; }
    @Override public double getVelocidadeMedia() { return 800; }
    @Override public double getTempoManuseio() { return 6; }
    @Override public double getFatorEmissao() { return 0.602; }
    @Override public double getCapacidadeMaxima() { return 5000; }
}
