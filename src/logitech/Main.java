package logitech;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import logitech.lote.ProcessadorLote;

/** Camada de entrada/saida: le a entrada padrao e imprime o resultado do lote. */
public class Main {
    public static void main(String[] args) throws IOException {
        List<String> linhas = new ArrayList<>();
        BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String linha;
        while ((linha = entrada.readLine()) != null) {
            linhas.add(linha);
        }

        List<String> saida = new ProcessadorLote().processarLote(linhas);

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        for (String s : saida) {
            out.write(s);
            out.write('\n');
        }
        out.flush();
    }
}
