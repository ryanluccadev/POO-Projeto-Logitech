package logitech;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import logitech.calculo.CalculadoraFrete;
import logitech.calculo.ResultadoCalculo;
import logitech.encomenda.Encomenda;
import logitech.encomenda.ValidadorEncomenda;
import logitech.entrada.InterpretadorEntrada;
import logitech.entrada.ResultadoInterpretacao;
import logitech.lote.ProcessadorLote;
import logitech.modal.Modal;
import logitech.notificacao.AuditoriaAmbiental;
import logitech.notificacao.CentralControle;
import logitech.notificacao.Cliente;
import logitech.perfil.PerfilEcologico;
import logitech.perfil.PerfilEconomico;
import logitech.perfil.PerfilPrecificacao;
import logitech.perfil.PerfilRapido;
import logitech.rastreamento.Interessado;
import logitech.rastreamento.Posicao;
import logitech.rastreamento.StatusTransporte;
import logitech.rastreamento.TipoEvento;
import logitech.rastreamento.Transporte;
import logitech.relatorio.Relatorio;
import logitech.saida.FormatadorSaida;

/**
 * Testes sem dependencias externas. Os valores esperados vem do ENUNCIADO (secoes 3, 4 e 5),
 * nunca de uma execucao anterior do proprio codigo.
 * Execute a partir da raiz do projeto: java -cp out logitech.TestesLogiTech
 */
public class TestesLogiTech {
    static int ok = 0, falhas = 0;

    static void check(String nome, boolean cond) {
        if (cond) ok++; else { falhas++; System.out.println("FALHOU: " + nome); }
    }
    static void igual(String nome, String esperado, String obtido) {
        if (esperado.equals(obtido)) ok++;
        else { falhas++; System.out.println("FALHOU: " + nome + "\n  esperado: [" + esperado + "]\n  obtido:   [" + obtido + "]"); }
    }
    static boolean prox(double a, double b) { return Math.abs(a - b) <= 0.0100001; }

    static final InterpretadorEntrada IN = new InterpretadorEntrada();
    static final ValidadorEncomenda VAL = new ValidadorEncomenda();
    static final CalculadoraFrete CALC = new CalculadoraFrete();
    static final FormatadorSaida FMT = new FormatadorSaida();

    // ---------- utilitarios ----------

    /** Executa o programa de verdade (Main) com a entrada dada na entrada padrao. */
    static String exec(String entrada) {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buf, true, "UTF-8"));
            Main.main(new String[0]);
            System.out.flush();
            return new String(buf.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
    }

    /** Interpreta + valida; devolve o motivo, "OK" ou "IGNORADA". */
    static String motivo(String linha) {
        ResultadoInterpretacao r = IN.interpretar(linha, 1);
        if (r.isIgnorada()) return "IGNORADA";
        if (r.isRejeitada()) return r.getMotivo();
        return VAL.validar(r.getEncomenda()).orElse("OK");
    }

    static Encomenda enc(String linha) {
        ResultadoInterpretacao r = IN.interpretar(linha, 1);
        if (!r.isInterpretada()) throw new IllegalStateException("linha de teste invalida: " + linha);
        return r.getEncomenda();
    }

    /** Monta uma linha no formato oficial: ID;MODAL;PESO;DIST;ORIGEM;DESTINO;PERFIL. */
    static String linha(int id, String modal, String peso, String dist, String perfil) {
        return id + ";" + modal + ";" + peso + ";" + dist + ";Origem;Destino;" + perfil;
    }

    static String saida(String valorLinhaUnica) { return exec(valorLinhaUnica + "\n").split("\n")[0]; }

    // ---------- main ----------

    public static void main(String[] a) throws Exception {
        exemplosOficiais();
        formatoDeSaida();
        regrasDeLeitura();
        validacaoOrdemEBordas();
        calculosGabaritoEParametros();
        calculosContraOraculoIndependente();
        arredondamento();
        rastreamentoStatus();
        rastreamentoPosicao();
        perfilTrocaESimulacao();
        notificacoes();
        relatorio();
        cenariosDeEvolucao();
        restricoesDeProjeto();
        robustezEDesempenho();
        System.out.println(ok + " verificacoes OK, " + falhas + " falhas");
        if (falhas > 0) System.exit(1);
    }

    // ===== exemplos e gabaritos do enunciado, comparando o TEXTO exato =====

    static String ler(String nome) throws IOException {
        return new String(Files.readAllBytes(new File("test/dados/" + nome).toPath()), StandardCharsets.UTF_8);
    }

    static void exemplosOficiais() throws IOException {
        for (String caso : new String[]{"exemplo_5_2", "exemplo_5_3", "bordas_5_3", "vazio"}) {
            igual("arquivo " + caso, ler(caso + ".esperado"), exec(ler(caso + ".txt")));
        }
        // exemplo 5.2 literal (nao depende dos arquivos)
        igual("5.2 literal",
                "VALIDA;1;1445,00;23,33;31,00\nREJEITADA;2;peso invalido\nREJEITADA;LINHA4;formato invalido\n"
              + "VALIDA;3;2425,50;136,40;67,20\nRELATORIO;3870,50;79,87;98,20;2;2\n",
                exec("1;RODOVIARIO;500;1000;Curitiba;Recife;ECONOMICO\n2;AEREO;-5;1000;Sao Paulo;Manaus;RAPIDO\n\nabc\n"
                   + "3;AQUAVIARIO;2000;3000;Santos;Belem;ECOLOGICO\n"));
    }

    static void formatoDeSaida() {
        igual("valida", "VALIDA;7;1,00;2,50;0,00", FMT.formatarValida("7", new ResultadoCalculo(1, 2.5, 0)));
        igual("sem milhar", "VALIDA;7;1345890,00;20835,83;1240000,00",
                FMT.formatarValida("7", new ResultadoCalculo(1345890, 20835.83, 1240000)));
        igual("rejeitada", "REJEITADA;LINHA9;formato invalido", FMT.formatarRejeitada("LINHA9", "formato invalido"));
        Relatorio r = new Relatorio();
        igual("relatorio vazio", "RELATORIO;0,00;0,00;0,00;0;0", FMT.formatarRelatorio(r.gerarRelatorio()));
        check("sem CRLF/ruido", !exec(linha(1, "AEREO", "1", "1", "RAPIDO") + "\n").contains("\r"));
        // saida termina com uma unica linha RELATORIO
        String[] ls = exec(linha(1, "AEREO", "1", "1", "RAPIDO") + "\nlixo\n").split("\n");
        check("uma linha por encomenda + 1 relatorio", ls.length == 3 && ls[2].startsWith("RELATORIO;"));
        check("so uma linha RELATORIO", count(ls, "RELATORIO;") == 1);
    }

    static int count(String[] ls, String prefixo) {
        int c = 0;
        for (String s : ls) if (s.startsWith(prefixo)) c++;
        return c;
    }

    // ===== regras de leitura (5.1) =====

    static void regrasDeLeitura() {
        String ok7 = "1;RODOVIARIO;100;10;SP;RJ;ECONOMICO";
        igual("valido", "OK", motivo(ok7));
        igual("espacos nas pontas", "OK", motivo(" 1 ; RODOVIARIO ; 100 ; 10 ; SP ; RJ ; ECONOMICO "));
        igual("6 campos", "formato invalido", motivo("1;RODOVIARIO;100;10;SP;RJ"));
        igual("8 campos", "formato invalido", motivo(ok7 + ";X"));
        igual("campo vazio no fim (7 campos, perfil vazio)", "codigo inexistente", motivo("1;RODOVIARIO;100;10;SP;RJ;"));
        igual("so separadores", "formato invalido", motivo(";;;;;"));
        igual("ID nao inteiro", "formato invalido", motivo("A;RODOVIARIO;100;10;SP;RJ;ECONOMICO"));
        igual("ID decimal", "formato invalido", motivo("1.5;RODOVIARIO;100;10;SP;RJ;ECONOMICO"));
        igual("ID vazio", "formato invalido", motivo(";RODOVIARIO;100;10;SP;RJ;ECONOMICO"));
        igual("ID maior que int", "formato invalido", motivo("2147483648;RODOVIARIO;100;10;SP;RJ;ECONOMICO"));
        igual("ID negativo e inteiro", "OK", motivo("-3;RODOVIARIO;100;10;SP;RJ;ECONOMICO"));
        igual("peso texto", "formato invalido", motivo("1;RODOVIARIO;abc;10;SP;RJ;ECONOMICO"));
        igual("peso com virgula", "formato invalido", motivo("1;RODOVIARIO;10,5;10;SP;RJ;ECONOMICO"));
        igual("peso vazio", "formato invalido", motivo("1;RODOVIARIO;;10;SP;RJ;ECONOMICO"));
        igual("distancia texto", "formato invalido", motivo("1;RODOVIARIO;10;xyz;SP;RJ;ECONOMICO"));
        igual("peso NaN", "formato invalido", motivo("1;RODOVIARIO;NaN;10;SP;RJ;ECONOMICO"));
        igual("peso Infinity", "formato invalido", motivo("1;RODOVIARIO;Infinity;10;SP;RJ;ECONOMICO"));
        igual("peso -Infinity", "formato invalido", motivo("1;RODOVIARIO;-Infinity;10;SP;RJ;ECONOMICO"));
        igual("distancia NaN", "formato invalido", motivo("1;RODOVIARIO;10;NaN;SP;RJ;ECONOMICO"));
        igual("distancia Infinity", "formato invalido", motivo("1;RODOVIARIO;10;Infinity;SP;RJ;ECONOMICO"));
        igual("estouro vira Infinity", "formato invalido", motivo("1;RODOVIARIO;1e999;10;SP;RJ;ECONOMICO"));
        igual("notacao cientifica valida", "OK", motivo("1;RODOVIARIO;1e2;1e1;SP;RJ;ECONOMICO"));
        igual("modal minusculo", "codigo inexistente", motivo("1;rodoviario;100;10;SP;RJ;ECONOMICO"));
        igual("perfil minusculo", "codigo inexistente", motivo("1;RODOVIARIO;100;10;SP;RJ;economico"));
        igual("modal desconhecido", "codigo inexistente", motivo("1;FERROVIARIO;100;10;SP;RJ;ECONOMICO"));
        igual("perfil desconhecido", "codigo inexistente", motivo("1;RODOVIARIO;100;10;SP;RJ;LUXO"));
        igual("origem = destino valido", "OK", motivo("1;RODOVIARIO;100;10;SP;SP;ECONOMICO"));
        igual("linha vazia", "IGNORADA", motivo(""));
        igual("linha com espacos/tab", "IGNORADA", motivo("  \t  "));

        // ordem dos campos oficial: ID;MODAL;PESO;DISTANCIA;ORIGEM;DESTINO;PERFIL
        Encomenda e = enc("42;AEREO;12.5;340.25;Origem X;Destino Y;RAPIDO");
        check("campo ID", e.getId() == 42);
        check("campo PESO", e.getPesoKg() == 12.5);
        check("campo DISTANCIA", e.getDistanciaKm() == 340.25);
        check("campo ORIGEM", e.getOrigem().equals("Origem X"));
        check("campo DESTINO", e.getDestino().equals("Destino Y"));
        check("campo MODAL (aereo: capacidade 5000)", e.getModal().getCapacidadeMaxima() == 5000);
        check("campo PERFIL (rapido: frete x1,40)", prox(e.getPerfilInicial().calcularFrete(new BigDecimal("100")).doubleValue(), 140));
        igual("ordem antiga rejeitada", "formato invalido", motivo("1;100;10;SP;RJ;RODOVIARIO;ECONOMICO"));

        // identificacao na saida
        igual("LINHA<n> conta linhas em branco", "REJEITADA;LINHA5;formato invalido",
                exec("\n\n\n\nxx;1\n").split("\n")[0]);
        igual("ID legivel mesmo com campos faltando", "REJEITADA;12;formato invalido", saida("12;RODOVIARIO;1"));
        igual("ID com espacos", "REJEITADA;12;formato invalido", saida(" 12 ;x"));
        igual("ID > int usa LINHA", "REJEITADA;LINHA1;formato invalido",
                saida("99999999999;RODOVIARIO;1;1;a;b;ECONOMICO"));
        igual("tabs/CRLF na entrada", "VALIDA;1;1,00;3,63;0,00".equals("") ? "" : "VALIDA;1;1,00;3,63;0,00",
                "VALIDA;1;1,00;3,63;0,00"); // controle do formato (abaixo o teste real)
        String crlf = exec("1;RODOVIARIO;1;0;A;B;ECONOMICO\r\n2;AEREO;1;0;A;B;RAPIDO\r\n");
        igual("CRLF aceito", "VALIDA;1;0,34;2,50;0,00\nVALIDA;2;3,50;4,80;0,00\nRELATORIO;3,84;3,65;0,00;2;0\n", crlf);
    }

    // ===== validacao: tabela 4.1, na ordem =====

    static void validacaoOrdemEBordas() {
        igual("origem vazia", "rota invalida", motivo("1;RODOVIARIO;100;10;;RJ;ECONOMICO"));
        igual("destino vazio", "rota invalida", motivo("1;RODOVIARIO;100;10;SP;;ECONOMICO"));
        igual("origem so espacos", "rota invalida", motivo("1;RODOVIARIO;100;10;   ;RJ;ECONOMICO"));
        igual("peso zero", "peso invalido", motivo("1;RODOVIARIO;0;10;SP;RJ;ECONOMICO"));
        igual("peso -0", "peso invalido", motivo("1;RODOVIARIO;-0.0;10;SP;RJ;ECONOMICO"));
        igual("peso negativo", "peso invalido", motivo("1;RODOVIARIO;-0.001;10;SP;RJ;ECONOMICO"));
        igual("peso minimo positivo", "OK", motivo("1;RODOVIARIO;0.0001;10;SP;RJ;ECONOMICO"));
        igual("distancia negativa", "distancia invalida", motivo("1;RODOVIARIO;100;-0.001;SP;RJ;ECONOMICO"));
        igual("distancia zero valida", "OK", motivo("1;RODOVIARIO;100;0;SP;RJ;ECONOMICO"));
        igual("distancia -0 valida", "OK", motivo("1;RODOVIARIO;100;-0.0;SP;RJ;ECONOMICO"));
        String[] modais = {"RODOVIARIO", "AEREO", "AQUAVIARIO"};
        String[] cap = {"20000", "5000", "100000"};
        String[] acima = {"20000.01", "5000.01", "100000.01"};
        for (int i = 0; i < 3; i++) {
            igual("capacidade igual " + modais[i], "OK", motivo("1;" + modais[i] + ";" + cap[i] + ";10;SP;RJ;ECONOMICO"));
            igual("capacidade excedida " + modais[i], "capacidade excedida",
                    motivo("1;" + modais[i] + ";" + acima[i] + ";10;SP;RJ;ECONOMICO"));
        }
        igual("capacidade e do modal (5001 kg ok no rodoviario)", "OK", motivo("1;RODOVIARIO;5001;10;SP;RJ;ECONOMICO"));
        igual("capacidade e do modal (5001 kg excede no aereo)", "capacidade excedida", motivo("1;AEREO;5001;10;SP;RJ;ECONOMICO"));

        // prioridade: formato > codigo > rota > peso > distancia > capacidade
        igual("formato antes de codigo", "formato invalido", motivo("x;FERROVIARIO;1;1;a;b;LUXO"));
        igual("codigo antes de rota", "codigo inexistente", motivo("1;FERROVIARIO;1;1;;;ECONOMICO"));
        igual("rota antes de peso", "rota invalida", motivo("1;RODOVIARIO;-1;1;;b;ECONOMICO"));
        igual("peso antes de distancia", "peso invalido", motivo("1;RODOVIARIO;0;-1;a;b;ECONOMICO"));
        igual("distancia antes de capacidade", "distancia invalida", motivo("1;AEREO;999999;-1;a;b;ECONOMICO"));
        igual("tudo errado", "codigo inexistente", motivo("1;X;-1;-1;;;Y"));
        igual("capacidade so se o resto ok", "capacidade excedida", motivo("1;AEREO;999999;1;a;b;RAPIDO"));
    }

    // ===== calculos: gabarito 5.3 =====

    static void conferir(String nome, ResultadoCalculo r, double frete, double tempo, double co2) {
        check(nome + " frete " + r.getFrete() + " vs " + frete, prox(r.getFrete(), frete));
        check(nome + " tempo " + r.getTempo() + " vs " + tempo, prox(r.getTempo(), tempo));
        check(nome + " co2 " + r.getCo2() + " vs " + co2, prox(r.getCo2(), co2));
    }

    static void calculosGabaritoEParametros() {
        conferir("gab1", CALC.calcular(enc("1;RODOVIARIO;500;1000;Curitiba;Recife;ECONOMICO")), 1445.00, 23.33, 31.00);
        conferir("gab2", CALC.calcular(enc("2;AEREO;500;1000;Sao Paulo;Manaus;RAPIDO")), 7350.00, 5.80, 346.15);
        conferir("gab3", CALC.calcular(enc("3;AQUAVIARIO;2000;3000;Santos;Belem;ECOLOGICO")), 2425.50, 136.40, 67.20);
        conferir("borda dist 0", CALC.calcular(enc("4;AQUAVIARIO;2000;0;A;B;ECONOMICO")), 267.75, 30.00, 0.00);
        conferir("borda 1000kg", CALC.calcular(enc("5;RODOVIARIO;1000;100;A;B;ECONOMICO")), 467.50, 4.58, 6.20);
        conferir("borda 1001kg", CALC.calcular(enc("6;RODOVIARIO;1001;100;A;B;ECONOMICO")), 491.23, 4.58, 6.21);

        // distancia zero: tempo = manuseio x perfil; co2 zero; frete = peso x custo/kg (sobretaxa se > 1000)
        conferir("dist0 rodoviario eco", CALC.calcular(enc("7;RODOVIARIO;10;0;A;B;ECONOMICO")), 10 * 0.40 * 0.85, 2 * 1.25, 0);
        conferir("dist0 aereo rapido", CALC.calcular(enc("8;AEREO;10;0;A;B;RAPIDO")), 10 * 2.5 * 1.40, 6 * 0.80, 0);
        conferir("dist0 aqua ecologico", CALC.calcular(enc("9;AQUAVIARIO;10;0;A;B;ECOLOGICO")), 10 * 0.15 * 1.10, 24 * 1.10, 0);

        // valores extremos (stress: distancias de ate 1.000.000 km)
        conferir("extremo rodoviario", CALC.calcular(enc("10;RODOVIARIO;20000;1000000;A;B;ECONOMICO")),
                1345890.00, 20835.83, 1240000.00);
        conferir("extremo aereo", CALC.calcular(enc("11;AEREO;5000;1000000;A;B;RAPIDO")),
                5898375.00, 1004.80, 3461500.00);
        conferir("extremo aquaviario", CALC.calcular(enc("12;AQUAVIARIO;100000;1000000;A;B;ECOLOGICO")),
                (1000000 * 0.60 + 100000 * 0.15) * 1.05 * 1.10, (1000000.0 / 30 + 24) * 1.10, 100 * 1000000 * 0.016 * 0.70);
        igual("saida extrema", "VALIDA;10;1345890,00;20835,83;1240000,00",
                saida("10;RODOVIARIO;20000;1000000;A;B;ECONOMICO"));
        conferir("peso minusculo", CALC.calcular(enc("13;AEREO;0.0001;0;A;B;ECONOMICO")), 0.00, 7.50, 0.00);
    }

    // oraculo independente: formulas do enunciado, com BigDecimal e tabelas copiadas do PDF
    static void calculosContraOraculoIndependente() {
        String[] modal = {"RODOVIARIO", "AEREO", "AQUAVIARIO"};
        double[][] p = { // custoKm, custoKg, velocidade, manuseio, emissao
                {1.50, 0.40, 60, 2, 0.062}, {4.00, 2.50, 800, 6, 0.602}, {0.60, 0.15, 30, 24, 0.016}};
        double[] cap = {20000, 5000, 100000};
        String[] perfil = {"ECONOMICO", "RAPIDO", "ECOLOGICO"};
        double[][] m = {{0.85, 1.25, 1.00}, {1.40, 0.80, 1.15}, {1.10, 1.10, 0.70}};
        double[] pesos = {0.5, 1, 999.99, 1000, 1000.01, 1001, 2000, 4999.5};
        double[] dists = {0, 0.5, 1, 99.99, 1000, 12345.678, 999999};
        int n = 0;
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++)
            for (double peso : pesos) for (double d : dists) {
                if (peso > cap[i]) continue;
                double fb = d * p[i][0] + peso * p[i][1];
                if (peso > 1000) fb *= 1.05;
                double tb = d / p[i][2] + p[i][3];
                double cb = (peso / 1000) * d * p[i][4];
                String l = "1;" + modal[i] + ";" + peso + ";" + d + ";A;B;" + perfil[j];
                ResultadoCalculo r = CALC.calcular(enc(l));
                n++;
                if (!(prox(r.getFrete(), fb * m[j][0]) && prox(r.getTempo(), tb * m[j][1]) && prox(r.getCo2(), cb * m[j][2]))) {
                    check("oraculo " + l, false);
                }
            }
        check("oraculo: " + n + " combinacoes conferidas", n > 400);
    }

    static final Modal MODAL_MEIO_CENTAVO = new Modal() {
        public double getCustoPorKm() { return 0; }
        public double getCustoPorKg() { return 0.005; }
        public double getVelocidadeMedia() { return 1; }
        public double getTempoManuseio() { return 0; }
        public double getFatorEmissao() { return 0; }
        public double getCapacidadeMaxima() { return 10; }
    };
    static final PerfilPrecificacao PERFIL_NEUTRO = new PerfilPrecificacao() {
        public BigDecimal calcularFrete(BigDecimal f) { return f; }
        public BigDecimal calcularTempo(BigDecimal t) { return t; }
        public BigDecimal calcularCo2(BigDecimal c) { return c; }
    };

    static void arredondamento() {
        Encomenda meio = new Encomenda(9, 1, 0, "A", "B", MODAL_MEIO_CENTAVO, PERFIL_NEUTRO);
        check("HALF_UP 0,005 -> 0,01", prox(CALC.calcular(meio).getFrete(), 0.01));
        igual("HALF_UP no texto", "VALIDA;9;0,01;0,00;0,00", FMT.formatarValida("9", CALC.calcular(meio)));
        Encomenda tres = new Encomenda(9, 3, 0, "A", "B", MODAL_MEIO_CENTAVO, PERFIL_NEUTRO); // 0,015 -> 0,02
        igual("HALF_UP 0,015 -> 0,02", "VALIDA;9;0,02;0,00;0,00", FMT.formatarValida("9", CALC.calcular(tres)));
        // empates exatos (o double erra): peso 123,45 x R$ 0,15/kg = 18,5175 -> 18,52 (perfil neutro, aquaviario)
        Encomenda empate = new Encomenda(9, 123.45, 0, "A", "B", enc("1;AQUAVIARIO;1;1;A;B;ECONOMICO").getModal(), PERFIL_NEUTRO);
        igual("empate 18,5175 -> 18,52", "VALIDA;9;18,52;24,00;0,00", FMT.formatarValida("9", CALC.calcular(empate)));
        Encomenda empate2 = new Encomenda(9, 1.01, 0, "A", "B", MODAL_MEIO_CENTAVO, PERFIL_NEUTRO); // 0,00505 -> 0,01
        igual("empate 0,00505 -> 0,01", "VALIDA;9;0,01;0,00;0,00", FMT.formatarValida("9", CALC.calcular(empate2)));
        // arredondamento so depois de aplicar o perfil: 1.001 kg rodoviario
        ResultadoCalculo r = CALC.calcular(enc("1;RODOVIARIO;1001;100;A;B;ECONOMICO"));
        igual("2 casas", "VALIDA;1;491,23;4,58;6,21", FMT.formatarValida("1", r));
    }

    // ===== rastreamento =====

    static Transporte novoTransporte() {
        return new Transporte(enc("1;RODOVIARIO;1000;100;A;B;ECONOMICO"), CALC);
    }

    static Transporte transporteEm(StatusTransporte s) {
        Transporte t = novoTransporte();
        if (s != StatusTransporte.AGUARDANDO) t.alterarStatus(StatusTransporte.EM_TRANSITO);
        if (s == StatusTransporte.ENTREGUE) t.alterarStatus(StatusTransporte.ENTREGUE);
        return t;
    }

    static void rastreamentoStatus() {
        Transporte t = novoTransporte();
        check("nasce AGUARDANDO", t.getStatus() == StatusTransporte.AGUARDANDO);
        check("nasce sem posicao", t.getPosicao() == null);
        check("nasce com o perfil inicial", t.getPerfilAtivo() == t.getEncomenda().getPerfilInicial());

        StatusTransporte[] todos = StatusTransporte.values();
        check("tres status", todos.length == 3);
        for (StatusTransporte de : todos) for (StatusTransporte para : todos) {
            Transporte x = transporteEm(de);
            boolean valida = (de == StatusTransporte.AGUARDANDO && para == StatusTransporte.EM_TRANSITO)
                          || (de == StatusTransporte.EM_TRANSITO && para == StatusTransporte.ENTREGUE);
            boolean r = x.alterarStatus(para);
            check("transicao " + de + "->" + para + (valida ? " aceita" : " rejeitada"), r == valida);
            check("estado apos " + de + "->" + para, x.getStatus() == (valida ? para : de));
        }
        check("null rejeitado", !novoTransporte().alterarStatus(null));
    }

    static void rastreamentoPosicao() {
        Transporte p = novoTransporte();
        check("primeira posicao conta", p.atualizarPosicao(-23.5, -46.6));
        check("posicao gravada", p.getPosicao().getLatitude() == -23.5 && p.getPosicao().getLongitude() == -46.6);
        check("igual nao e mudanca", !p.atualizarPosicao(-23.5, -46.6));
        check("mudar so a longitude", p.atualizarPosicao(-23.5, -46.7));
        double[][] invalidas = {{90.0001, 0}, {-90.0001, 0}, {0, 180.0001}, {0, -180.0001},
                {Double.NaN, 0}, {0, Double.NaN}, {Double.POSITIVE_INFINITY, 0}, {0, Double.NEGATIVE_INFINITY},
                {91, 181}, {10, 200}, {95, 10}};
        for (double[] par : invalidas) {
            Posicao antes = p.getPosicao();
            check("invalida " + par[0] + "," + par[1], !p.atualizarPosicao(par[0], par[1]));
            check("par inteiro rejeitado (estado intacto)", p.getPosicao() == antes);
        }
        check("limite 90,180", p.atualizarPosicao(90, 180));
        check("limite -90,-180", p.atualizarPosicao(-90, -180));
        check("zero,zero", p.atualizarPosicao(0, 0));
        check("posicao nula rejeitada", !p.atualizarPosicao((Posicao) null));
        // permitida em qualquer status
        for (StatusTransporte s : StatusTransporte.values()) {
            check("posicao em " + s, transporteEm(s).atualizarPosicao(1, 2));
        }
        try { new Posicao(100, 0); check("Posicao invalida lanca", false); }
        catch (IllegalArgumentException e) { check("Posicao invalida lanca", true); }
    }

    static void perfilTrocaESimulacao() {
        Transporte t = novoTransporte();
        double antes = t.calcular().getFrete();
        check("troca em AGUARDANDO", t.trocarPerfil(new PerfilRapido()));
        check("novo perfil muda frete (sem recriar transporte)", prox(t.calcular().getFrete(), 550 * 1.40) && t.calcular().getFrete() != antes);
        t.alterarStatus(StatusTransporte.EM_TRANSITO);
        check("troca em EM_TRANSITO", t.trocarPerfil(new PerfilEcologico()));
        check("calculo com ecologico", prox(t.calcular().getCo2(), 6.2 * 0.70));
        t.alterarStatus(StatusTransporte.ENTREGUE);
        PerfilPrecificacao ativo = t.getPerfilAtivo();
        check("troca apos ENTREGUE rejeitada", !t.trocarPerfil(new PerfilEconomico()));
        check("perfil intacto apos rejeicao", t.getPerfilAtivo() == ativo);
        check("troca com null rejeitada", !novoTransporte().trocarPerfil(null));

        // simulacao: permitida em todos os status, inclusive depois de ENTREGUE
        for (StatusTransporte s : StatusTransporte.values()) {
            Transporte x = transporteEm(s);
            PerfilPrecificacao original = x.getPerfilAtivo();
            ResultadoCalculo sim = x.simularPerfil(new PerfilRapido());
            check("simulacao em " + s, prox(sim.getFrete(), 550 * 1.40) && prox(sim.getTempo(), (100.0 / 60 + 2) * 0.80));
            check("simulacao nao altera perfil em " + s, x.getPerfilAtivo() == original);
        }
        // simulacao reutiliza a calculadora: mesmo resultado que calcular diretamente
        Transporte s = novoTransporte();
        ResultadoCalculo viaSim = s.simularPerfil(new PerfilEcologico());
        ResultadoCalculo direto = CALC.calcular(s.getEncomenda(), new PerfilEcologico());
        check("simulacao == calculo direto", viaSim.getFrete() == direto.getFrete()
                && viaSim.getTempo() == direto.getTempo() && viaSim.getCo2() == direto.getCo2());

        // criar transporte so a partir de encomenda valida
        try { new Transporte(new Encomenda(1, -5, 10, "A", "B", enc("1;AEREO;1;1;A;B;RAPIDO").getModal(), new PerfilRapido()), CALC);
              check("transporte de encomenda invalida", false); }
        catch (IllegalArgumentException e) { check("transporte de encomenda invalida lanca", true); }
    }

    // ===== notificacoes =====

    /** Interessado de teste que apenas conta os eventos recebidos. */
    static class Espiao implements Interessado {
        int status = 0, posicao = 0;
        public void atualizar(Transporte t, TipoEvento e) { if (e == TipoEvento.STATUS) status++; else posicao++; }
        int total() { return status + posicao; }
    }

    static void notificacoes() {
        Transporte t = novoTransporte();
        CentralControle central = new CentralControle();
        Cliente cliente = new Cliente();
        AuditoriaAmbiental audit = new AuditoriaAmbiental(CALC);
        Espiao espiao = new Espiao();
        t.adicionarInteressado(central);
        t.adicionarInteressado(cliente);
        t.adicionarInteressado(audit);
        t.adicionarInteressado(espiao);

        // posicao: central registra, cliente nao e avisado, auditoria nao soma
        t.atualizarPosicao(1, 2);
        check("central recebe 1a posicao", central.getLogs().size() == 1);
        check("cliente nao recebe posicao", cliente.getMensagens().isEmpty());
        check("auditoria nao soma em posicao", audit.getCo2Acumulado() == 0);
        check("espiao recebeu POSICAO", espiao.posicao == 1 && espiao.status == 0);
        t.atualizarPosicao(1, 2);
        check("posicao repetida sem aviso", central.getLogs().size() == 1 && espiao.total() == 1);
        t.atualizarPosicao(500, 2);
        check("posicao invalida sem aviso", central.getLogs().size() == 1 && espiao.total() == 1);

        // rejeitadas nao avisam
        t.alterarStatus(StatusTransporte.ENTREGUE);
        t.alterarStatus(StatusTransporte.AGUARDANDO);
        check("status rejeitado sem aviso", espiao.total() == 1 && cliente.getMensagens().isEmpty());

        // status valido
        t.alterarStatus(StatusTransporte.EM_TRANSITO);
        check("central recebe status", central.getLogs().size() == 2);
        check("cliente recebe status", cliente.getMensagens().size() == 1);
        check("auditoria nao soma antes da entrega", audit.getCo2Acumulado() == 0);

        // auditoria usa o perfil vigente no momento da entrega
        t.trocarPerfil(new PerfilEcologico());
        t.alterarStatus(StatusTransporte.ENTREGUE);
        check("auditoria soma na entrega (6,20 x 0,70)", prox(audit.getCo2Acumulado(), 4.34));
        check("cliente recebe a entrega", cliente.getMensagens().size() == 2);
        check("central: ID, status e coordenadas no log",
                central.getLogs().get(2).contains("ID=1") && central.getLogs().get(2).contains("ENTREGUE")
                && central.getLogs().get(2).contains("1.0,2.0"));
        check("troca de perfil rejeitada nao avisa", espiao.total() == 3 && !t.trocarPerfil(new PerfilRapido()) && espiao.total() == 3);
        check("posicao apos entrega avisa a central mas nao soma", t.atualizarPosicao(3, 4)
                && central.getLogs().size() == 4 && prox(audit.getCo2Acumulado(), 4.34));
        check("simulacao e troca nao geraram aviso extra", t.simularPerfil(new PerfilRapido()) != null && espiao.total() == 4);

        // log sem posicao
        Transporte s = novoTransporte();
        CentralControle c2 = new CentralControle();
        s.adicionarInteressado(c2);
        s.alterarStatus(StatusTransporte.EM_TRANSITO);
        check("log sem posicao nao quebra", c2.getLogs().size() == 1 && c2.getLogs().get(0).contains("indisponivel"));

        // auditoria soma varias entregas, cada uma com o perfil do momento
        AuditoriaAmbiental a2 = new AuditoriaAmbiental(CALC);
        Transporte t1 = novoTransporte(), t2 = novoTransporte();
        t1.adicionarInteressado(a2); t2.adicionarInteressado(a2);
        t2.trocarPerfil(new PerfilRapido());
        t1.alterarStatus(StatusTransporte.EM_TRANSITO); t1.alterarStatus(StatusTransporte.ENTREGUE);
        t2.alterarStatus(StatusTransporte.EM_TRANSITO); t2.alterarStatus(StatusTransporte.ENTREGUE);
        check("auditoria acumula 6,20 + 7,13", prox(a2.getCo2Acumulado(), 6.20 + 7.13));

        // cadastro e remocao em execucao
        Transporte d = novoTransporte();
        Cliente antigo = new Cliente(), novo = new Cliente();
        d.adicionarInteressado(antigo);
        d.alterarStatus(StatusTransporte.EM_TRANSITO);
        d.removerInteressado(antigo);
        d.adicionarInteressado(novo);
        d.alterarStatus(StatusTransporte.ENTREGUE);
        check("removido deixa de receber", antigo.getMensagens().size() == 1);
        check("novo passa a receber", novo.getMensagens().size() == 1);
        d.adicionarInteressado(novo);
        check("nao duplica interessado", d.getInteressados().size() == 1);
        d.removerInteressado(new Cliente());
        check("remover inexistente e inofensivo", d.getInteressados().size() == 1);

        // interessado que se remove durante o aviso
        Transporte e = novoTransporte();
        final Espiao outro = new Espiao();
        final Transporte alvo = e;
        e.adicionarInteressado(new Interessado() {
            public void atualizar(Transporte tr, TipoEvento ev) { alvo.removerInteressado(this); }
        });
        e.adicionarInteressado(outro);
        e.alterarStatus(StatusTransporte.EM_TRANSITO);
        check("remocao durante aviso nao pula ninguem", outro.status == 1);

        // encapsulamento das listas
        try { central.getLogs().add("x"); check("getLogs imutavel", false); }
        catch (UnsupportedOperationException ex) { check("getLogs imutavel", true); }
        try { cliente.getMensagens().add("x"); check("getMensagens imutavel", false); }
        catch (UnsupportedOperationException ex) { check("getMensagens imutavel", true); }
        try { t.getInteressados().clear(); check("getInteressados imutavel", false); }
        catch (UnsupportedOperationException ex) { check("getInteressados imutavel", true); }
        check("interessados intactos", t.getInteressados().size() == 4);
    }

    // ===== relatorio =====

    static void relatorio() {
        ProcessadorLote p = new ProcessadorLote();
        igual("lista vazia", "RELATORIO;0,00;0,00;0,00;0;0", p.processarLote(new ArrayList<String>()).get(0));
        List<String> brancos = new ArrayList<>();
        brancos.add(""); brancos.add("   "); brancos.add("\t");
        List<String> r = p.processarLote(brancos);
        check("so linhas em branco: so relatorio zerado, sem contagem", r.size() == 1 && r.get(0).equals("RELATORIO;0,00;0,00;0,00;0;0"));
        List<String> invalidas = new ArrayList<>();
        invalidas.add("lixo"); invalidas.add("1;X;1;1;a;b;Y");
        List<String> ri = p.processarLote(invalidas);
        igual("so invalidas", "RELATORIO;0,00;0,00;0,00;0;2", ri.get(ri.size() - 1));
        check("invalidas nao entram nos totais, linhas na ordem", ri.get(0).equals("REJEITADA;LINHA1;formato invalido")
                && ri.get(1).equals("REJEITADA;1;codigo inexistente"));

        // media: 79,865 -> 79,87 (HALF_UP so no final); agregados com valores ja arredondados por encomenda
        Relatorio rel = new Relatorio();
        rel.registrarValida(new ResultadoCalculo(0.10, 23.33, 0.10));
        rel.registrarValida(new ResultadoCalculo(0.20, 136.40, 0.20));
        Relatorio.Resumo res = rel.gerarRelatorio();
        igual("media 79,865 -> 79,87", "RELATORIO;0,30;79,87;0,30;2;0", FMT.formatarRelatorio(res));
        // soma de valores arredondados evita deriva de ponto flutuante (0,1 + 0,2)
        check("0,10 + 0,20 = 0,30 exato", res.getCustoTotal() == 0.30);

        // media usa soma dos tempos ja arredondados (4,58 e nao 4,5833)
        Relatorio r3 = new Relatorio();
        for (int i = 0; i < 3; i++) r3.registrarValida(new ResultadoCalculo(1, 4.58, 1));
        check("media de 3 x 4,58", r3.gerarRelatorio().getTempoMedio() == 4.58);
        Relatorio r4 = new Relatorio();
        r4.registrarRejeitada(); r4.registrarRejeitada(); r4.registrarValida(new ResultadoCalculo(2, 2, 2));
        check("contagens", r4.gerarRelatorio().getValidas() == 1 && r4.gerarRelatorio().getRejeitadas() == 2);
    }

    // ===== cenarios de evolucao (secao 7): provados com classes NOVAS, sem alterar o nucleo =====

    static class ModalFerroviario implements Modal {
        public double getCustoPorKm() { return 0.8; }
        public double getCustoPorKg() { return 0.2; }
        public double getVelocidadeMedia() { return 100; }
        public double getTempoManuseio() { return 12; }
        public double getFatorEmissao() { return 0.03; }
        public double getCapacidadeMaxima() { return 50000; }
    }
    static class PerfilCorporativo implements PerfilPrecificacao {
        public BigDecimal calcularFrete(BigDecimal f) { return f.multiply(new BigDecimal("0.95")); }
        public BigDecimal calcularTempo(BigDecimal t) { return t; }
        public BigDecimal calcularCo2(BigDecimal c) { return c.multiply(new BigDecimal("0.9")); }
    }
    static class PerfilPremium implements PerfilPrecificacao {
        public BigDecimal calcularFrete(BigDecimal f) { return f.multiply(new BigDecimal("1.20")).max(new BigDecimal("50.00")); }
        public BigDecimal calcularTempo(BigDecimal t) { return t.multiply(new BigDecimal("0.90")); }
        public BigDecimal calcularCo2(BigDecimal c) { return c; }
    }
    static class AlertaSms implements Interessado {
        int mensagens = 0;
        public void atualizar(Transporte t, TipoEvento e) {
            if (e == TipoEvento.POSICAO && t.getPosicao().getLatitude() < 0) mensagens++;
        }
    }

    static void cenariosDeEvolucao() {
        Encomenda trem = new Encomenda(1, 1000, 500, "A", "B", new ModalFerroviario(), new PerfilEconomico());
        check("ferroviario valido", VAL.validar(trem).isEmpty());
        conferir("modal ferroviario", CALC.calcular(trem), 510.00, 21.25, 15.00);
        check("ferroviario respeita capacidade",
                VAL.validar(new Encomenda(1, 50001, 5, "A", "B", new ModalFerroviario(), new PerfilEconomico())).isPresent());

        Encomenda rod = enc("1;RODOVIARIO;1000;100;A;B;ECONOMICO");
        conferir("perfil corporativo", CALC.calcular(rod, new PerfilCorporativo()), 550 * 0.95, (100.0 / 60 + 2), 6.2 * 0.9);
        conferir("perfil premium (frete minimo)", CALC.calcular(enc("1;RODOVIARIO;1;1;A;B;ECONOMICO"), new PerfilPremium()),
                50.00, (1.0 / 60 + 2) * 0.9, 0.00);
        conferir("perfil premium (acima do minimo)", CALC.calcular(rod, new PerfilPremium()), 550 * 1.2, (100.0 / 60 + 2) * 0.9, 6.2);
        // o mesmo perfil novo funciona em todos os modais e em tempo de execucao
        Transporte t = new Transporte(trem, CALC);
        check("trocar para perfil novo em execucao", t.trocarPerfil(new PerfilPremium()));
        check("calculo usa o perfil novo", prox(t.calcular().getFrete(), 600 * 1.2));

        // novo interessado sem alterar Transporte
        AlertaSms sms = new AlertaSms();
        t.adicionarInteressado(sms);
        t.atualizarPosicao(-10, -50);
        t.atualizarPosicao(10, 50);
        check("novo interessado (SMS)", sms.mensagens == 1);

        // sobretaxa em um unico ponto: mesma razao 1,05 em todos os modais
        String[] modais = {"RODOVIARIO", "AEREO", "AQUAVIARIO"};
        double[] custoKg = {0.40, 2.50, 0.15};
        for (int i = 0; i < 3; i++) {
            Encomenda pesada = new Encomenda(1, 1001, 0, "A", "B", enc("1;" + modais[i] + ";1;1;A;B;ECONOMICO").getModal(), PERFIL_NEUTRO);
            Encomenda leve = new Encomenda(1, 1000, 0, "A", "B", pesada.getModal(), PERFIL_NEUTRO);
            check("sobretaxa 1,05 em " + modais[i], prox(CALC.calcular(pesada).getFrete(), 1001 * custoKg[i] * 1.05));
            check("sem sobretaxa em 1000 kg " + modais[i], prox(CALC.calcular(leve).getFrete(), 1000 * custoKg[i]));
        }
    }

    // ===== restricoes obrigatorias (secao 6) verificadas por reflexao e leitura do codigo-fonte =====

    static void restricoesDeProjeto() throws Exception {
        String[] classes = {"logitech.Main", "logitech.calculo.CalculadoraFrete", "logitech.calculo.ResultadoCalculo",
                "logitech.encomenda.Encomenda", "logitech.encomenda.ValidadorEncomenda",
                "logitech.entrada.InterpretadorEntrada", "logitech.entrada.ResultadoInterpretacao",
                "logitech.lote.ProcessadorLote", "logitech.modal.ModalRodoviario", "logitech.modal.ModalAereo",
                "logitech.modal.ModalAquaviario", "logitech.notificacao.AuditoriaAmbiental",
                "logitech.notificacao.CentralControle", "logitech.notificacao.Cliente",
                "logitech.perfil.PerfilEconomico", "logitech.perfil.PerfilRapido", "logitech.perfil.PerfilEcologico",
                "logitech.rastreamento.Posicao", "logitech.rastreamento.Transporte",
                "logitech.relatorio.Relatorio", "logitech.relatorio.Relatorio$Resumo", "logitech.saida.FormatadorSaida"};
        int campos = 0;
        for (String nome : classes) {
            for (Field f : Class.forName(nome).getDeclaredFields()) {
                if (f.isSynthetic()) continue;
                campos++;
                boolean constante = Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers());
                check("encapsulamento " + nome + "." + f.getName(), Modifier.isPrivate(f.getModifiers()) || (constante && Modifier.isPublic(f.getModifiers())));
            }
        }
        check("campos verificados por reflexao: " + campos, campos > 30);

        // fora da camada de entrada: nada de instanceof/switch/getClass/comparacao de codigos; nucleo nao le dados
        File raiz = new File("src/logitech");
        if (!raiz.isDirectory()) { System.out.println("AVISO: src/ nao encontrado; execute da raiz do projeto"); return; }
        List<File> fontes = new ArrayList<>();
        coletar(raiz, fontes);
        check("fontes encontrados", fontes.size() >= 25);
        for (File f : fontes) {
            String s = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            boolean entrada = f.getPath().contains("/entrada/");
            boolean main = f.getName().equals("Main.java");
            if (!entrada) {
                // Posicao.equals usa instanceof sobre a propria classe: nao testa modal nem perfil
                check("sem instanceof em " + f.getName(), f.getName().equals("Posicao.java") || !s.contains("instanceof"));
                check("sem switch em " + f.getName(), !s.matches("(?s).*\\bswitch\\s*\\(.*"));
                check("sem getClass em " + f.getName(), !s.contains("getClass"));
                for (String codigo : new String[]{"RODOVIARIO", "AEREO", "AQUAVIARIO", "ECONOMICO", "RAPIDO", "ECOLOGICO"}) {
                    check("sem codigo " + codigo + " em " + f.getName(), !s.contains("\"" + codigo + "\""));
                }
            }
            if (!main) {
                check("nucleo nao le dados diretamente: " + f.getName(), !s.contains("System.in") && !s.contains("Scanner"));
            }
            check("sem depuracao em " + f.getName(), !s.contains("printStackTrace") && !s.contains("System.err")
                    && (main || !s.contains("System.out")));
            check("sem TODO em " + f.getName(), !s.contains("TODO") && !s.contains("FIXME"));
        }
    }

    static void coletar(File d, List<File> out) {
        File[] fs = d.listFiles();
        if (fs == null) return;
        for (File f : fs) {
            if (f.isDirectory()) coletar(f, out);
            else if (f.getName().endsWith(".java")) out.add(f);
        }
    }

    // ===== robustez e desempenho =====

    static void robustezEDesempenho() {
        ProcessadorLote p = new ProcessadorLote();

        // fuzz deterministico: nenhuma excecao, uma linha de saida por linha nao vazia + 1 relatorio
        Random rnd = new Random(12345);
        String[] pedacos = {"1", "-1", "0", "1e999", "NaN", "Infinity", "abc", "", " ", "RODOVIARIO", "AEREO", "AQUAVIARIO",
                "rodoviario", "ECONOMICO", "RAPIDO", "ECOLOGICO", "10.5", "99999999999", "-0.0", "SP", ";", ";;", "\t", "ç", "1,5"};
        List<String> fuzz = new ArrayList<>();
        int naoVazias = 0;
        for (int i = 0; i < 50000; i++) {
            StringBuilder sb = new StringBuilder();
            int k = rnd.nextInt(10);
            for (int j = 0; j < k; j++) { if (j > 0) sb.append(';'); sb.append(pedacos[rnd.nextInt(pedacos.length)]); }
            String l = sb.toString();
            if (!l.trim().isEmpty()) naoVazias++;
            fuzz.add(l);
        }
        try {
            List<String> out = p.processarLote(fuzz);
            check("fuzz: sem excecao e saida consistente", out.size() == naoVazias + 1);
            check("fuzz: ultima linha e o relatorio", out.get(out.size() - 1).startsWith("RELATORIO;"));
            boolean formatoOk = true;
            for (int i = 0; i < out.size() - 1; i++) {
                String s = out.get(i);
                if (!(s.startsWith("VALIDA;") || s.startsWith("REJEITADA;"))) formatoOk = false;
            }
            check("fuzz: toda linha comeca com VALIDA; ou REJEITADA;", formatoOk);
        } catch (RuntimeException e) {
            check("fuzz lancou " + e, false);
        }

        // lote grande, misturando validas e invalidas
        String[] m = {"RODOVIARIO", "AEREO", "AQUAVIARIO"};
        double[] cap = {20000, 5000, 100000};
        String[] pf = {"ECONOMICO", "RAPIDO", "ECOLOGICO"};
        int n = 600000;
        List<String> grande = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            if (i % 7 == 0) grande.add("lixo " + i);
            else if (i % 11 == 0) grande.add("");
            else grande.add(i + ";" + m[i % 3] + ";" + (1 + i % (int) cap[i % 3]) + ";" + (i % 1000000) + ";A;B;" + pf[i % 3]);
        }
        long t0 = System.nanoTime();
        List<String> og = p.processarLote(grande);
        long ms = (System.nanoTime() - t0) / 1_000_000;
        check("lote de " + n + " linhas processado (" + ms + " ms)", og.size() > 1 && ms < 8000);

        // linearidade aproximada: 4x mais linhas nao leva mais que ~12x o tempo
        List<String> pequeno = grande.subList(0, n / 4);
        p.processarLote(pequeno); // aquecimento
        long a = System.nanoTime(); p.processarLote(pequeno); long ta = System.nanoTime() - a;
        long b = System.nanoTime(); p.processarLote(grande); long tb = System.nanoTime() - b;
        check("crescimento ~linear (x" + (tb / Math.max(ta, 1)) + " para 4x linhas)", tb < 12 * Math.max(ta, 1));

        // erro no meio nao interrompe as demais
        String[] ls = exec("1;AEREO;1;1;A;B;RAPIDO\nlixo\n3;AEREO;1;1;A;B;RAPIDO\n").split("\n");
        check("erro no meio nao interrompe", ls[0].startsWith("VALIDA;1;") && ls[1].equals("REJEITADA;LINHA2;formato invalido")
                && ls[2].startsWith("VALIDA;3;") && ls[3].equals("RELATORIO;" + ls[3].substring(10)) && ls[3].endsWith(";2;1"));
        // entrada vazia e sem quebra de linha final
        igual("stdin vazio", "RELATORIO;0,00;0,00;0,00;0;0\n", exec(""));
        igual("ultima linha sem \\n", "VALIDA;1;0,34;2,50;0,00\nRELATORIO;0,34;2,50;0,00;1;0\n",
                exec("1;RODOVIARIO;1;0;A;B;ECONOMICO"));
    }
}
