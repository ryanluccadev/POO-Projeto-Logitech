package logitech.rastreamento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import logitech.calculo.CalculadoraFrete;
import logitech.calculo.ResultadoCalculo;
import logitech.encomenda.Encomenda;
import logitech.encomenda.ValidadorEncomenda;
import logitech.perfil.PerfilPrecificacao;

/**
 * Sujeito do Observer: controla status, posicao, perfil ativo e interessados
 * de uma encomenda. Operacoes rejeitadas devolvem false e nao alteram nada.
 */
public class Transporte {
    private final Encomenda encomenda;
    private final CalculadoraFrete calculadora;
    private StatusTransporte status = StatusTransporte.AGUARDANDO;
    private Posicao posicao = null;
    private PerfilPrecificacao perfilAtivo;
    private final List<Interessado> interessados = new ArrayList<>();

    /**
     * Cria o transporte de uma encomenda valida (status AGUARDANDO, sem posicao).
     * @throws IllegalArgumentException se a encomenda for invalida
     */
    public Transporte(Encomenda encomenda, CalculadoraFrete calculadora) {
        Optional<String> motivo = new ValidadorEncomenda().validar(encomenda);
        if (motivo.isPresent()) {
            throw new IllegalArgumentException("encomenda invalida: " + motivo.get());
        }
        this.encomenda = encomenda;
        this.calculadora = calculadora;
        this.perfilAtivo = encomenda.getPerfilInicial();
    }

    public Encomenda getEncomenda() { return encomenda; }
    public StatusTransporte getStatus() { return status; }
    public Posicao getPosicao() { return posicao; }
    public PerfilPrecificacao getPerfilAtivo() { return perfilAtivo; }

    public List<Interessado> getInteressados() {
        return Collections.unmodifiableList(new ArrayList<>(interessados));
    }

    /** Calculo com o perfil ativo. */
    public ResultadoCalculo calcular() {
        return calculadora.calcular(encomenda, perfilAtivo);
    }

    /** Transicoes validas: AGUARDANDO -> EM_TRANSITO -> ENTREGUE. */
    public boolean alterarStatus(StatusTransporte novo) {
        if (novo == null || !transicaoValida(status, novo)) return false;
        status = novo;
        notificar(TipoEvento.STATUS);
        return true;
    }

    public boolean atualizarPosicao(double latitude, double longitude) {
        if (!Posicao.valida(latitude, longitude)) return false;
        return atualizarPosicao(new Posicao(latitude, longitude));
    }

    public boolean atualizarPosicao(Posicao nova) {
        if (nova == null || nova.equals(posicao)) return false;
        posicao = nova;
        notificar(TipoEvento.POSICAO);
        return true;
    }

    /** Permitida antes da entrega; nao gera notificacao. */
    public boolean trocarPerfil(PerfilPrecificacao novo) {
        if (novo == null || status == StatusTransporte.ENTREGUE) return false;
        perfilAtivo = novo;
        return true;
    }

    /** Calcula com outro perfil sem alterar o transporte nem notificar. */
    public ResultadoCalculo simularPerfil(PerfilPrecificacao perfil) {
        return calculadora.calcular(encomenda, perfil);
    }

    public void adicionarInteressado(Interessado i) {
        if (i != null && !interessados.contains(i)) interessados.add(i);
    }

    public void removerInteressado(Interessado i) {
        interessados.remove(i);
    }

    public void notificar(TipoEvento evento) {
        // copia: interessados podem se cadastrar/remover durante a notificacao
        for (Interessado i : new ArrayList<>(interessados)) {
            i.atualizar(this, evento);
        }
    }

    private static boolean transicaoValida(StatusTransporte de, StatusTransporte para) {
        return (de == StatusTransporte.AGUARDANDO && para == StatusTransporte.EM_TRANSITO)
            || (de == StatusTransporte.EM_TRANSITO && para == StatusTransporte.ENTREGUE);
    }
}
