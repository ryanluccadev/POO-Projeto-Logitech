package logitech.entrada;

import logitech.encomenda.Encomenda;
import logitech.modal.Modal;
import logitech.modal.ModalAereo;
import logitech.modal.ModalAquaviario;
import logitech.modal.ModalRodoviario;
import logitech.perfil.PerfilEcologico;
import logitech.perfil.PerfilEconomico;
import logitech.perfil.PerfilPrecificacao;
import logitech.perfil.PerfilRapido;

/**
 * Camada de entrada: converte linhas de texto
 * {@code ID;MODAL;PESO_KG;DISTANCIA_KM;ORIGEM;DESTINO;PERFIL} em objetos do dominio.
 * Tambem e a fabrica simples de Modal e PerfilPrecificacao: unico lugar do sistema
 * onde codigos de texto sao mapeados com switch.
 */
public class InterpretadorEntrada {
    private static final String SEPARADOR = ";";
    private static final int NUM_CAMPOS = 7;

    // posicao de cada campo na linha
    private static final int CAMPO_ID = 0;
    private static final int CAMPO_MODAL = 1;
    private static final int CAMPO_PESO = 2;
    private static final int CAMPO_DISTANCIA = 3;
    private static final int CAMPO_ORIGEM = 4;
    private static final int CAMPO_DESTINO = 5;
    private static final int CAMPO_PERFIL = 6;

    public ResultadoInterpretacao interpretar(String linha, int numeroLinha) {
        if (linha == null || linha.trim().isEmpty()) {
            return ResultadoInterpretacao.ignorada();
        }

        String[] campos = linha.split(SEPARADOR, -1);
        for (int i = 0; i < campos.length; i++) campos[i] = campos[i].trim();

        String idSaida = identificacao(campos[CAMPO_ID], numeroLinha);

        if (campos.length != NUM_CAMPOS) {
            return ResultadoInterpretacao.rejeitada("formato invalido", idSaida);
        }

        int id;
        double peso;
        double distancia;
        try {
            id = Integer.parseInt(campos[CAMPO_ID]);
            peso = Double.parseDouble(campos[CAMPO_PESO]);
            distancia = Double.parseDouble(campos[CAMPO_DISTANCIA]);
        } catch (NumberFormatException ex) {
            return ResultadoInterpretacao.rejeitada("formato invalido", idSaida);
        }
        if (!Double.isFinite(peso) || !Double.isFinite(distancia)) {
            return ResultadoInterpretacao.rejeitada("formato invalido", idSaida);
        }

        Modal modal = criarModal(campos[CAMPO_MODAL]);
        PerfilPrecificacao perfil = criarPerfil(campos[CAMPO_PERFIL]);
        if (modal == null || perfil == null) {
            return ResultadoInterpretacao.rejeitada("codigo inexistente", idSaida);
        }

        return ResultadoInterpretacao.interpretada(
                new Encomenda(id, peso, distancia, campos[CAMPO_ORIGEM], campos[CAMPO_DESTINO], modal, perfil));
    }

    /** Codigos diferenciam maiusculas de minusculas. Retorna null se desconhecido. */
    public Modal criarModal(String codigo) {
        switch (codigo) {
            case "RODOVIARIO": return new ModalRodoviario();
            case "AEREO":      return new ModalAereo();
            case "AQUAVIARIO": return new ModalAquaviario();
            default:           return null;
        }
    }

    /** Codigos diferenciam maiusculas de minusculas. Retorna null se desconhecido. */
    public PerfilPrecificacao criarPerfil(String codigo) {
        switch (codigo) {
            case "ECONOMICO": return new PerfilEconomico();
            case "RAPIDO":    return new PerfilRapido();
            case "ECOLOGICO": return new PerfilEcologico();
            default:          return null;
        }
    }

    /** ID legivel (inteiro) ou LINHA&lt;n&gt;, com n = numero fisico da linha (a primeira e 1). */
    private String identificacao(String campoId, int numeroLinha) {
        try {
            return String.valueOf(Integer.parseInt(campoId));
        } catch (NumberFormatException ex) {
            return "LINHA" + numeroLinha;
        }
    }
}
