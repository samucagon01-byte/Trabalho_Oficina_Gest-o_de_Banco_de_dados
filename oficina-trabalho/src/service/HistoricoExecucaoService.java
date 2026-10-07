package service;

import model.HistoricoExecucao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoricoExecucaoService {

    private static final Path PASTA_DADOS =
            Paths.get("dados");

    private static final Path ARQUIVO_HISTORICO =
            PASTA_DADOS.resolve("historico_execucoes.csv");

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final String CABECALHO =
            "ID;DATA;BANCO;DURACAO_SEGUNDOS;MANUTENCAO;RESULTADO;ARQUIVO_BACKUP;ARQUIVO_LOG";

    public static synchronized void registrar(
            LocalDateTime inicio,
            LocalDateTime fim,
            String banco,
            String manutencao,
            String resultado,
            String arquivoBackup,
            Path arquivoLog) {

        try {

            Files.createDirectories(PASTA_DADOS);

            criarCabecalhoSeNecessario();

            int id = proximoId();

            long duracao =
                    Duration.between(inicio, fim).getSeconds();

            String linha =
                    id
                            + ";"
                            + limpar(inicio.format(FORMATO_DATA))
                            + ";"
                            + limpar(banco)
                            + ";"
                            + duracao
                            + ";"
                            + limpar(manutencao)
                            + ";"
                            + limpar(resultado)
                            + ";"
                            + limpar(arquivoBackup)
                            + ";"
                            + limpar(
                                    arquivoLog == null
                                            ? ""
                                            : arquivoLog.toString()
                    );

            Files.writeString(
                    ARQUIVO_HISTORICO,
                    linha + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException ex) {

            System.err.println(
                    "Não foi possível registrar o histórico da execução: "
                            + ex.getMessage()
            );
        }
    }

    public static List<HistoricoExecucao> listar() {

        List<HistoricoExecucao> historico =
                new ArrayList<>();

        try {

            if (!Files.exists(ARQUIVO_HISTORICO)) {
                return historico;
            }

            List<String> linhas =
                    Files.readAllLines(
                            ARQUIVO_HISTORICO,
                            StandardCharsets.UTF_8
                    );

            for (String linha : linhas) {

                if (
                        linha == null
                                || linha.isBlank()
                                || linha.startsWith("ID;")
                ) {
                    continue;
                }

                String[] partes =
                        linha.split(";", -1);

                if (partes.length < 8) {
                    continue;
                }

                try {

                    historico.add(
                            new HistoricoExecucao(
                                    Integer.parseInt(partes[0]),
                                    partes[1],
                                    partes[2],
                                    Long.parseLong(partes[3]),
                                    partes[4],
                                    partes[5],
                                    partes[6],
                                    partes[7]
                            )
                    );

                } catch (NumberFormatException ignored) {
                }
            }

            Collections.reverse(historico);

        } catch (IOException ex) {

            System.err.println(
                    "Não foi possível ler o histórico: "
                            + ex.getMessage()
            );
        }

        return historico;
    }

    public static Path getArquivoHistorico() {
        return ARQUIVO_HISTORICO;
    }

    private static void criarCabecalhoSeNecessario()
            throws IOException {

        if (Files.exists(ARQUIVO_HISTORICO)) {
            return;
        }

        Files.writeString(
                ARQUIVO_HISTORICO,
                CABECALHO + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE
        );
    }

    private static int proximoId() throws IOException {

        int maiorId = 0;

        if (!Files.exists(ARQUIVO_HISTORICO)) {
            return 1;
        }

        List<String> linhas =
                Files.readAllLines(
                        ARQUIVO_HISTORICO,
                        StandardCharsets.UTF_8
                );

        for (String linha : linhas) {

            if (linha == null || linha.isBlank() || linha.startsWith("ID;")) {
                continue;
            }

            String[] partes = linha.split(";", -1);

            if (partes.length == 0) {
                continue;
            }

            try {
                maiorId = Math.max(
                        maiorId,
                        Integer.parseInt(partes[0])
                );
            } catch (NumberFormatException ignored) {
            }
        }

        return maiorId + 1;
    }

    private static String limpar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\r", " ")
                .replace("\n", " ")
                .replace(";", ",");
    }
}
