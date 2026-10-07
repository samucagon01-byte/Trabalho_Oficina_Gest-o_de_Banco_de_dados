package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmailService {

    private static final Path PASTA_EMAIL =
            Paths.get("dados", "email_simulado");

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    public static Path simularEnvioLog(
            Path arquivoLog,
            String destinatario)
            throws IOException {

        if (arquivoLog == null || !Files.exists(arquivoLog)) {
            throw new IOException("O arquivo de log da execução não foi encontrado.");
        }

        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("Informe o destinatário.");
        }

        Files.createDirectories(PASTA_EMAIL);

        String momento =
                LocalDateTime.now().format(FORMATO);

        Path arquivoEnviado =
                PASTA_EMAIL.resolve(
                        "log_enviado_" + momento + ".txt"
                );

        Files.copy(
                arquivoLog,
                arquivoEnviado,
                StandardCopyOption.REPLACE_EXISTING
        );

        Path comprovante =
                PASTA_EMAIL.resolve(
                        "comprovante_envio_" + momento + ".txt"
                );

        String conteudo =
                "SIMULAÇÃO DE ENVIO DE E-MAIL"
                        + System.lineSeparator()
                        + "Data: " + LocalDateTime.now()
                        + System.lineSeparator()
                        + "Destinatário: " + destinatario
                        + System.lineSeparator()
                        + "Arquivo de origem: " + arquivoLog
                        + System.lineSeparator()
                        + "Arquivo simulado enviado: " + arquivoEnviado
                        + System.lineSeparator()
                        + "Status: ENVIO SIMULADO COM SUCESSO"
                        + System.lineSeparator();

        Files.writeString(
                comprovante,
                conteudo,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        return comprovante;
    }
}
