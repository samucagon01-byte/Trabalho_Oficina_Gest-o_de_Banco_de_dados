package service;

import model.ConfiguracaoBanco;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BackupService {

    private static final String PG_DUMP =
            "C:\\Program Files\\PostgreSQL\\18\\bin\\pg_dump.exe";

    public static Path gerarBackup(
            ConfiguracaoBanco config)
            throws Exception {

        return gerarBackup(config, null);
    }

    public static Path gerarBackup(
            ConfiguracaoBanco config,
            ProgressoListener listener)
            throws Exception {

        atualizar(listener, "PG_DUMP", 45, "EM_EXECUCAO");

        Path pastaBackup = Paths.get(config.getCaminhoBackup());
        Files.createDirectories(pastaBackup);

        String data =
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                );

        String nomeBanco =
                limparNomeArquivo(config.getBanco());

        String nomeBase =
                "backup_" + nomeBanco + "_" + data;

        Path arquivoSql =
                pastaBackup.resolve(nomeBase + ".sql");

        // =====================================================
        // 1. GERAR SQL
        // =====================================================

        LogService.registrar(
                "PG_DUMP",
                "EM_EXECUCAO",
                "Iniciando geração do arquivo SQL.",
                "Banco=" + config.getBanco()
                        + "; Destino=" + arquivoSql
        );

        ProcessBuilder processo =
                new ProcessBuilder(
                        PG_DUMP,
                        "--host=" + config.getHost(),
                        "--port=" + config.getPorta(),
                        "--username=" + config.getUsuario(),
                        "--dbname=" + config.getBanco(),
                        "--format=plain",
                        "--no-owner",
                        "--no-privileges",
                        "--no-password",
                        "--file=" + arquivoSql
                );

        Map<String, String> ambiente =
                processo.environment();

        ambiente.put("PGPASSWORD", config.getSenha());
        ambiente.put("PGSSLMODE", "prefer");
        processo.redirectErrorStream(true);

        try {

            Process executando = processo.start();

            String saida =
                    new String(
                            executando.getInputStream().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

            int codigo = executando.waitFor();

            if (codigo != 0) {

                RuntimeException ex =
                        new RuntimeException(
                                "O pg_dump falhou:\n\n" + saida
                        );

                LogService.falha(
                        "PG_DUMP",
                        "O pg_dump retornou erro.",
                        "Código=" + codigo + "; Saída técnica=" + saida
                );

                atualizar(listener, "PG_DUMP", 100, "FALHA");
                throw ex;
            }

            if (!Files.exists(arquivoSql) || Files.size(arquivoSql) == 0) {

                throw new IOException(
                        "O arquivo SQL não foi criado corretamente."
                );
            }

            LogService.sucesso(
                    "PG_DUMP",
                    "Arquivo SQL gerado com sucesso."
            );

            atualizar(listener, "PG_DUMP", 55, "SUCESSO");

        } catch (IOException ex) {

            LogService.falha(
                    "PG_DUMP",
                    "Não foi possível executar o pg_dump.",
                    ex.getMessage()
            );

            atualizar(listener, "PG_DUMP", 100, "FALHA");

            throw new RuntimeException(
                    "Não foi possível executar o pg_dump.\n"
                            + "Verifique se o PostgreSQL está instalado "
                            + "e se o pg_dump.exe existe no caminho configurado.",
                    ex
            );
        }

        // =====================================================
        // 2. COMPACTAR E CRIPTOGRAFAR, SE HABILITADO
        // =====================================================

        Path arquivoFinal = arquivoSql;

        if (config.isCompactar()) {

            atualizar(listener, "COMPACTACAO", 60, "EM_EXECUCAO");

            String senhaZip = config.getSenhaZip();

            if (senhaZip == null || senhaZip.isBlank()) {

                throw new IllegalStateException(
                        "Informe uma senha para o arquivo compactado."
                );
            }

            Path arquivoZip =
                    pastaBackup.resolve(nomeBase + ".zip");

            try {

                ZipParameters parametros =
                        new ZipParameters();

                parametros.setCompressionMethod(
                        CompressionMethod.DEFLATE
                );

                parametros.setEncryptFiles(true);
                parametros.setEncryptionMethod(EncryptionMethod.AES);
                parametros.setAesKeyStrength(
                        AesKeyStrength.KEY_STRENGTH_256
                );

                ZipFile zipFile =
                        new ZipFile(
                                arquivoZip.toFile(),
                                senhaZip.toCharArray()
                        );

                zipFile.addFile(
                        arquivoSql.toFile(),
                        parametros
                );

                if (!Files.exists(arquivoZip) || Files.size(arquivoZip) == 0) {
                    throw new IOException(
                            "O arquivo ZIP não foi criado corretamente."
                    );
                }

                Files.deleteIfExists(arquivoSql);

                arquivoFinal = arquivoZip;

                LogService.sucesso(
                        "COMPACTACAO",
                        "Backup compactado e protegido com AES-256."
                );

                atualizar(listener, "COMPACTACAO", 70, "SUCESSO");

            } catch (Exception ex) {

                LogService.falha(
                        "COMPACTACAO",
                        "Falha ao criar o arquivo ZIP protegido.",
                        ex.getMessage()
                );

                atualizar(listener, "COMPACTACAO", 100, "FALHA");
                throw ex;
            }

        } else {

            atualizar(listener, "COMPACTACAO", 70, "IGNORADA");

            LogService.registrar(
                    "COMPACTACAO",
                    "IGNORADA",
                    "Compactação não habilitada. O backup permanecerá em formato SQL.",
                    "Arquivo=" + arquivoSql
            );
        }

        // =====================================================
        // 3. RETENÇÃO
        // =====================================================

        atualizar(listener, "RETENCAO", 75, "EM_EXECUCAO");

        try {

            aplicarRetencao(config);

            LogService.sucesso(
                    "RETENCAO",
                    "Política de retenção aplicada."
            );

            atualizar(listener, "RETENCAO", 82, "SUCESSO");

        } catch (Exception ex) {

            LogService.falha(
                    "RETENCAO",
                    "Falha na aplicação da política de retenção.",
                    ex.getMessage()
            );

            atualizar(listener, "RETENCAO", 100, "FALHA");
            throw ex;
        }

        // =====================================================
        // 4. CÓPIA ADICIONAL
        // =====================================================

        String caminhoAdicional =
                config.getCaminhoCopiaAdicional();

        if (caminhoAdicional == null || caminhoAdicional.isBlank()) {

            atualizar(listener, "COPIA_ADICIONAL", 92, "IGNORADA");

        } else {

            atualizar(listener, "COPIA_ADICIONAL", 86, "EM_EXECUCAO");

            try {

                copiarDestinoAdicional(
                        arquivoFinal,
                        config
                );

                atualizar(listener, "COPIA_ADICIONAL", 94, "SUCESSO");

            } catch (Exception ex) {

                LogService.falha(
                        "COPIA_ADICIONAL",
                        "Falha na cópia para o destino adicional.",
                        ex.getMessage()
                );

                atualizar(listener, "COPIA_ADICIONAL", 100, "FALHA");
                throw ex;
            }
        }

        atualizar(listener, "BACKUP", 100, "SUCESSO");

        return arquivoFinal;
    }


    // =====================================================
    // RETENÇÃO
    // =====================================================

    private static void aplicarRetencao(
            ConfiguracaoBanco config)
            throws IOException {

        Integer quantidade =
                config.getQuantidadeManter();

        if (quantidade == null) {
            return;
        }

        if (quantidade < 1) {
            throw new IllegalArgumentException(
                    "A quantidade de backups deve ser maior que zero."
            );
        }

        Path pastaBackup =
                Paths.get(config.getCaminhoBackup());

        if (!Files.exists(pastaBackup)) {
            return;
        }

        String prefixo =
                "backup_"
                        + limparNomeArquivo(config.getBanco())
                        + "_";

        List<Path> backups;

        try (var arquivos = Files.list(pastaBackup)) {

            backups =
                    arquivos
                            .filter(Files::isRegularFile)
                            .filter(arquivo ->
                                    arquivo.getFileName()
                                            .toString()
                                            .startsWith(prefixo)
                            )
                            .filter(arquivo -> {
                                String nome = arquivo.getFileName().toString().toLowerCase();
                                return nome.endsWith(".zip") || nome.endsWith(".sql");
                            })
                            .sorted(
                                    Comparator
                                            .comparing(
                                                    BackupService::dataModificacao
                                            )
                                            .reversed()
                            )
                            .collect(Collectors.toList());
        }

        for (int i = quantidade; i < backups.size(); i++) {

            Path antigo = backups.get(i);

            Files.deleteIfExists(antigo);

            LogService.registrar(
                    "RETENCAO",
                    "SUCESSO",
                    "Backup antigo removido.",
                    "Arquivo=" + antigo
            );
        }
    }

    // =====================================================
    // CÓPIA ADICIONAL
    // =====================================================

    private static void copiarDestinoAdicional(
            Path arquivoFinal,
            ConfiguracaoBanco config)
            throws IOException {

        String caminhoAdicional =
                config.getCaminhoCopiaAdicional();

        if (caminhoAdicional == null || caminhoAdicional.isBlank()) {
            return;
        }

        Path pastaAdicional =
                Paths.get(caminhoAdicional);

        Files.createDirectories(pastaAdicional);

        Path destino =
                pastaAdicional.resolve(
                        arquivoFinal.getFileName()
                );

        if (
                arquivoFinal.toAbsolutePath()
                        .normalize()
                        .equals(
                                destino.toAbsolutePath()
                                        .normalize()
                        )
        ) {

            throw new IOException(
                    "O destino adicional não pode ser igual ao destino principal."
            );
        }

        Files.copy(
                arquivoFinal,
                destino,
                StandardCopyOption.REPLACE_EXISTING
        );

        if (!Files.exists(destino)) {
            throw new IOException(
                    "A cópia adicional não foi criada."
            );
        }

        LogService.sucesso(
                "COPIA_ADICIONAL",
                "Cópia adicional criada em: " + destino
        );
    }

    private static Instant dataModificacao(Path arquivo) {

        try {
            return Files.getLastModifiedTime(arquivo).toInstant();
        } catch (IOException ex) {
            return Instant.EPOCH;
        }
    }

    private static String limparNomeArquivo(String nome) {
        return nome.replaceAll(
                "[^a-zA-Z0-9_-]",
                "_"
        );
    }

    private static void atualizar(
            ProgressoListener listener,
            String etapa,
            int progresso,
            String estado) {

        if (listener != null) {
            listener.atualizar(
                    etapa,
                    progresso,
                    estado
            );
        }
    }
}
