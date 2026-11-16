package logitech.notificacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import logitech.rastreamento.Interessado;
import logitech.rastreamento.Posicao;
import logitech.rastreamento.TipoEvento;
import logitech.rastreamento.Transporte;

/** Registra eventos de status e de posicao. */
public class CentralControle implements Interessado {
    private final List<String> logs = new ArrayList<>();

    @Override
    public void atualizar(Transporte t, TipoEvento evento) {
        Posicao p = t.getPosicao();
        logs.add("ID=" + t.getEncomenda().getId()
                + " evento=" + evento
                + " status=" + t.getStatus()
                + " posicao=" + (p == null ? "indisponivel" : p.getLatitude() + "," + p.getLongitude()));
    }

    public List<String> getLogs() {
        return Collections.unmodifiableList(new ArrayList<>(logs));
    }
}
