package logitech.notificacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import logitech.encomenda.Encomenda;
import logitech.rastreamento.Interessado;
import logitech.rastreamento.TipoEvento;
import logitech.rastreamento.Transporte;

/** Recebe mensagens somente quando o status muda. */
public class Cliente implements Interessado {
    private final List<String> mensagens = new ArrayList<>();

    @Override
    public void atualizar(Transporte t, TipoEvento evento) {
        if (evento != TipoEvento.STATUS) return;
        mensagens.add("Encomenda " + t.getEncomenda().getId()
                + ": status alterado para " + t.getStatus());
    }

    public List<String> getMensagens() {
        return Collections.unmodifiableList(new ArrayList<>(mensagens));
    }
}
