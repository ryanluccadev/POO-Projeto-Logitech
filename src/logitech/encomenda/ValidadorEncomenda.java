package logitech.encomenda;

import java.util.Optional;

/** Regras de validacao, na ordem do enunciado. */
public class ValidadorEncomenda {
    /** Retorna o motivo da rejeicao, ou vazio se a encomenda e valida. */
    public Optional<String> validar(Encomenda e) {
        if (vazio(e.getOrigem()) || vazio(e.getDestino())) return Optional.of("rota invalida");
        if (!(e.getPesoKg() > 0)) return Optional.of("peso invalido");
        if (e.getDistanciaKm() < 0) return Optional.of("distancia invalida");
        if (e.getPesoKg() > e.getModal().getCapacidadeMaxima()) return Optional.of("capacidade excedida");
        return Optional.empty();
    }

    private boolean vazio(String s) {
        return s == null || s.trim().isEmpty();
    }
}
