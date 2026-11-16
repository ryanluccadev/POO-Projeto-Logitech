package logitech.modal;

/** Parametros de um modal de transporte (Strategy). */
public interface Modal {
    double getCustoPorKm();
    double getCustoPorKg();
    double getVelocidadeMedia();
    double getTempoManuseio();
    double getFatorEmissao();
    double getCapacidadeMaxima();
}
