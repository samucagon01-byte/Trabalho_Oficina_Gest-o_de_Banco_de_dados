package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogService {

    private static final Path PASTA_LOGS =
            Paths.get("dados", "logs");

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final DateTimeFormatter FORMATO_ARQUIVO =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private static final ThreadLocal<Path> LOG_ATUAL =
            new ThreadLocal<>();

    public static Path iniciarExecucao() {

        try {
            Files.createDirectories(PASTA_LOGS);

            String nome =
                    "execucao_"
                            + LocalDateTime.now().format(FORMATO_ARQUIVO)
                            + ".log";

            Path arquivo =
                    PASTA_LOGS.resolve(nome);

            Files.createFile(arquivo);

            LOG_ATUAL.set(arquivo);

            return arquivo;

        } catch (IOException ex) {

            throw new RuntimeException(
                    "Não foi possível criar o arquivo de log da execução.",
                    ex
            );
        }
    }

    public static void finalizarExecucao() {
        LOG_ATUAL.remove();
    }

    public static Path getArquivoLog() {
        Path atual = LOG_ATUAL.get();

        if (atual != null) {
            return atual;
        }

        return criarLogAvulso();
    }

    private static Path criarLogAvulso() {

        try {
            Files.createDirectories(PASTA_LOGS);

            Path arquivo =
                    PASTA_LOGS.resolve(
                            "avulso_"
                                    + LocalDateTime.now()
                                    .format(FORMATO_ARQUIVO)
                                    + ".log"
                    );

            Files.createFile(arquivo);

            LOG_ATUAL.set(arquivo);

            return arquivo;

        } catch (IOException ex) {
            throw new RuntimeException(
                    "Não foi possível criar um arquivo de log.",
                    ex
            );
        }
    }

    public static void registrar(
            String etapa,
            String resultado,
            String mensagem,
            String detalheTecnico) {

        try {

            Path arquivo = getArquivoLog();

            String data =
                    LocalDateTime.now().format(FORMATO_DATA);

            String linha =
                    data
                            + " | "
                            + limpar(etapa)
                            + " | "
                            + limpar(resultado)
                            + " | "
                            + limpar(mensagem)
                            + " | "
                            + limpar(detalheTecnico);

            Files.writeString(
                    arquivo,
                    linha + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException e) {

            System.err.println(
                    "Não foi possível gravar o log: "
                            + e.getMessage()
            );
        }
    }

    public static void sucesso(
            String etapa,
            String mensagem) {

        registrar(
                etapa,
                "SUCESSO",
                mensagem,
                ""
        );
    }

    public static void falha(
            String etapa,
            String mensagem,
            String detalheTecnico) {

        registrar(
                etapa,
                "FALHA",
                mensagem,
                detalheTecnico
        );
    }

    public static List<String> lerLogs() {
        return lerLog(getArquivoLog());
    }

    public static List<String> lerLog(Path arquivo) {

        try {

            if (
                    arquivo == null
                            || !Files.exists(arquivo)
            ) {
                return new ArrayList<>();
            }

            return Files.readAllLines(
                    arquivo,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            List<String> erro = new ArrayList<>();

            erro.add(
                    "Erro ao ler arquivo de log: "
                            + e.getMessage()
            );

            return erro;
        }
    }

    public static void limparLogs() {

        try {

            if (!Files.exists(PASTA_LOGS)) {
                return;
            }

            try (var arquivos = Files.list(PASTA_LOGS)) {

                arquivos
                        .filter(Files::isRegularFile)
                        .forEach(arquivo -> {
                            try {
                                Files.deleteIfExists(arquivo);
                            } catch (IOException ignored) {
                            }
                        });
            }

        } catch (IOException e) {

            System.err.println(
                    "Não foi possível limpar os logs: "
                            + e.getMessage()
            );
        }
    }

    private static String limpar(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("|", "/");
    }
}
