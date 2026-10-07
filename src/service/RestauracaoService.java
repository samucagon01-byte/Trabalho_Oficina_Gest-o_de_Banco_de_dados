package service;

import connection.ConnectionFactory;
import net.lingala.zip4j.ZipFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RestauracaoService {

    private static final String PSQL =
            "C:\\Program Files\\PostgreSQL\\18\\bin\\psql.exe";

    public static ResultadoRestauracao restaurar(
            Path arquivoBackup,
            String host,
            String porta,
            String banco,
            String usuario,
            String senha)
            throws Exception {

        return restaurar(
                arquivoBackup,
                host,
                porta,
                banco,
                usuario,
                senha,
                null
        );
    }

    public static ResultadoRestauracao restaurar(
            Path arquivoBackup,
            String host,
            String porta,
            String banco,
            String usuario,
            String senha,
            String senhaZipInformada)
            throws Exception {

        validar(
                arquivoBackup,
                host,
                porta,
                banco,
                usuario,
                senha
        );

        Path pastaTemp =
                Paths.get(
                        "dados",
                        "restauracao_temp",
                        LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMdd_HHmmss_SSS"
                                )
                        )
                );

        Files.createDirectories(pastaTemp);

        try {

            Path arquivoSql;
            String nome =
                    arquivoBackup.getFileName()
                            .toString()
                            .toLowerCase();

            if (nome.endsWith(".zip")) {
                arquivoSql = null;

            } else if (nome.endsWith(".sql")) {

                arquivoSql = arquivoBackup;

            } else {

                throw new IllegalArgumentException(
                        "O arquivo precisa ser .zip ou .sql."
                );
            }

            if (nome.endsWith(".zip")) {
                String senhaZip = senhaZipInformada;

                if (senhaZip == null || senhaZip.isBlank()) {
                    throw new IllegalStateException(
                            "Informe a senha do arquivo ZIP para realizar a restauração."
                    );
                }

                ZipFile zip =
                        new ZipFile(
                                arquivoBackup.toFile(),
                                senhaZip.toCharArray()
                        );

                if (!zip.isValidZipFile()) {
                    throw new IOException("O arquivo ZIP é inválido ou está corrompido.");
                }

                zip.extractAll(pastaTemp.toString());
                arquivoSql = localizarSql(pastaTemp);

                if (arquivoSql == null) {
                    throw new IOException(
                            "Nenhum arquivo SQL foi encontrado dentro do ZIP."
                    );
                }
            } else {
                arquivoSql = arquivoBackup;
            }

            if (!Files.exists(arquivoSql) || Files.size(arquivoSql) == 0) {
                throw new IOException("O arquivo SQL para restauração está ausente ou vazio.");
            }

            LogService.registrar(
                    "RESTAURACAO",
                    "EM_EXECUCAO",
                    "Iniciando restauração do backup.",
                    "Arquivo=" + arquivoBackup
                            + "; Banco destino=" + banco
            );

            try (Connection conexao =
                         ConnectionFactory.conectar(
                                 host,
                                 porta,
                                 banco,
                                 usuario,
                                 senha
                         )) {
            }

            ProcessBuilder processo =
                    new ProcessBuilder(
                            PSQL,
                            "--host=" + host,
                            "--port=" + porta,
                            "--username=" + usuario,
                            "--dbname=" + banco,
                            "--file=" + arquivoSql,
                            "--set=ON_ERROR_STOP=1"
                    );

            processo.redirectErrorStream(true);
            processo.environment().put("PGPASSWORD", senha);
            processo.environment().put("PGSSLMODE", "prefer");

            Process executando = processo.start();

            String saida =
                    new String(
                            executando.getInputStream().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

            int codigo = executando.waitFor();

            if (codigo != 0) {

                LogService.falha(
                        "RESTAURACAO",
                        "O psql falhou durante a restauração.",
                        "Código=" + codigo + "; Saída técnica=" + saida
                );

                throw new RuntimeException(
                        "Falha na restauração:\n\n" + saida
                );
            }

            ResultadoVerificacao verificacao =
                    verificarBanco(
                            host,
                            porta,
                            banco,
                            usuario,
                            senha
                    );

            String resumo =
                    "Restauração concluída com sucesso.\n\n"
                            + "Banco: " + banco + "\n"
                            + "Tabelas encontradas: "
                            + verificacao.tabelas + "\n"
                            + "Registros encontrados: "
                            + verificacao.registros;

            LogService.sucesso(
                    "RESTAURACAO",
                    resumo.replace("\n", " ")
            );

            return new ResultadoRestauracao(
                    resumo,
                    verificacao.tabelas,
                    verificacao.registros
            );

        } catch (Exception ex) {

            LogService.falha(
                    "RESTAURACAO",
                    "Falha durante o processo de restauração.",
                    ex.getMessage()
            );

            throw ex;

        } finally {

            apagarRecursivamente(pastaTemp);
        }
    }

    private static void validar(
            Path arquivo,
            String host,
            String porta,
            String banco,
            String usuario,
            String senha) {

        if (arquivo == null) {
            throw new IllegalArgumentException("Selecione um backup.");
        }

        if (host == null || host.isBlank()
                || porta == null || porta.isBlank()
                || banco == null || banco.isBlank()
                || usuario == null || usuario.isBlank()
                || senha == null || senha.isBlank()) {

            throw new IllegalArgumentException(
                    "Preencha todos os dados do banco de destino."
            );
        }
    }

    private static Path localizarSql(Path pasta)
            throws IOException {

        try (var arquivos = Files.walk(pasta)) {
            return arquivos
                    .filter(Files::isRegularFile)
                    .filter(arquivo ->
                            arquivo.getFileName()
                                    .toString()
                                    .toLowerCase()
                                    .endsWith(".sql")
                    )
                    .findFirst()
                    .orElse(null);
        }
    }

    private static ResultadoVerificacao verificarBanco(
            String host,
            String porta,
            String banco,
            String usuario,
            String senha)
            throws Exception {

        List<String[]> tabelas = new ArrayList<>();

        try (
                Connection conexao =
                        ConnectionFactory.conectar(
                                host,
                                porta,
                                banco,
                                usuario,
                                senha
                        );
                Statement stmt = conexao.createStatement()
        ) {

            String sql =
                    "SELECT table_schema, table_name "
                            + "FROM information_schema.tables "
                            + "WHERE table_type = 'BASE TABLE' "
                            + "AND table_schema NOT IN ('pg_catalog', 'information_schema') "
                            + "ORDER BY table_schema, table_name";

            try (ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    tabelas.add(
                            new String[]{
                                    rs.getString("table_schema"),
                                    rs.getString("table_name")
                            }
                    );
                }
            }
        }

        long registros = 0;

        try (
                Connection conexao =
                        ConnectionFactory.conectar(
                                host,
                                porta,
                                banco,
                                usuario,
                                senha
                        );
                Statement stmt = conexao.createStatement()
        ) {

            for (String[] tabela : tabelas) {

                String schema =
                        "\""
                                + tabela[0].replace("\"", "\"\"")
                                + "\"";

                String nome =
                        "\""
                                + tabela[1].replace("\"", "\"\"")
                                + "\"";

                String count =
                        "SELECT COUNT(*) FROM " + schema + "." + nome;

                try (ResultSet rs = stmt.executeQuery(count)) {
                    if (rs.next()) {
                        registros += rs.getLong(1);
                    }
                }
            }
        }

        return new ResultadoVerificacao(
                tabelas.size(),
                registros
        );
    }

    private static void apagarRecursivamente(Path caminho) {

        if (caminho == null || !Files.exists(caminho)) {
            return;
        }

        try (var arquivos = Files.walk(caminho)) {

            arquivos
                    .sorted(java.util.Comparator.reverseOrder())
                    .forEach(arquivo -> {
                        try {
                            Files.deleteIfExists(arquivo);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private static class ResultadoVerificacao {

        private final int tabelas;
        private final long registros;

        public ResultadoVerificacao(int tabelas, long registros) {
            this.tabelas = tabelas;
            this.registros = registros;
        }
    }

    public static class ResultadoRestauracao {

        private final String resumo;
        private final int quantidadeTabelas;
        private final long quantidadeRegistros;

        public ResultadoRestauracao(
                String resumo,
                int quantidadeTabelas,
                long quantidadeRegistros) {

            this.resumo = resumo;
            this.quantidadeTabelas = quantidadeTabelas;
            this.quantidadeRegistros = quantidadeRegistros;
        }

        public String getResumo() {
            return resumo;
        }

        public int getQuantidadeTabelas() {
            return quantidadeTabelas;
        }

        public long getQuantidadeRegistros() {
            return quantidadeRegistros;
        }
    }
}
