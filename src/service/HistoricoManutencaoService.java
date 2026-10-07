package service;

import model.ConfiguracaoBanco;
import model.HistoricoManutencao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HistoricoManutencaoService {

    private static final Path PASTA_DADOS =
            Paths.get("dados");

    private static final Path ARQUIVO_HISTORICO =
            PASTA_DADOS.resolve("historico_manutencao.csv");

    private static final String CABECALHO =
            "chave_banco;data_inicio;data_fim;tipo_manutencao;origem;regra_aplicada;status;mensagem";

    public static LocalDateTime buscarUltima(ConfiguracaoBanco config)
            throws IOException {

        List<HistoricoManutencao> historicos = lerTodos();

        String chave = gerarChave(config);

        return historicos.stream()
                .filter(h -> chave.equals(h.getChaveBanco()))
                .filter(h -> "SUCESSO".equalsIgnoreCase(h.getStatus()))
                .filter(h -> !"NENHUMA".equalsIgnoreCase(h.getTipoManutencao()))
                .map(HistoricoManutencao::getDataFim)
                .filter(data -> data != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    public static void registrar(
            ConfiguracaoBanco config,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            String tipoManutencao,
            String origem,
            String regraAplicada,
            String status,
            String mensagem) throws IOException {

        Files.createDirectories(PASTA_DADOS);

        if (!Files.exists(ARQUIVO_HISTORICO)) {
            Files.writeString(
                    ARQUIVO_HISTORICO,
                    CABECALHO + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE
            );
        }

        String linha = String.join(";",
                sanitizar(gerarChave(config)),
                sanitizar(String.valueOf(dataInicio)),
                sanitizar(String.valueOf(dataFim)),
                sanitizar(tipoManutencao),
                sanitizar(origem),
                sanitizar(regraAplicada),
                sanitizar(status),
                sanitizar(mensagem)
        );

        Files.writeString(
                ARQUIVO_HISTORICO,
                linha + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    public static List<HistoricoManutencao> lerTodos()
            throws IOException {

        List<HistoricoManutencao> historicos =
                new ArrayList<>();

        if (!Files.exists(ARQUIVO_HISTORICO)) {
            return historicos;
        }

        List<String> linhas = Files.readAllLines(
                ARQUIVO_HISTORICO,
                StandardCharsets.UTF_8
        );

        for (String linha : linhas) {

            if (linha.isBlank() || linha.startsWith("chave_banco;")) {
                continue;
            }

            String[] partes = linha.split(";", -1);

            if (partes.length < 8) {
                continue;
            }

            try {
                historicos.add(
                        new HistoricoManutencao(
                                partes[0],
                                LocalDateTime.parse(partes[1]),
                                LocalDateTime.parse(partes[2]),
                                partes[3],
                                partes[4],
                                partes[5],
                                partes[6],
                                partes[7]
                        )
                );
            } catch (Exception ignorado) {
                // Ignora apenas linhas inválidas do arquivo.
            }
        }

        return historicos;
    }

    public static Path getArquivoHistorico() {
        return ARQUIVO_HISTORICO;
    }

    private static String gerarChave(ConfiguracaoBanco config) {
        return config.getHost()
                + ":"
                + config.getPorta()
                + "/"
                + config.getBanco();
    }

    private static String sanitizar(String valor) {
        if (valor == null) {
            return "";
        }

        return valor
                .replace("\r", " ")
                .replace("\n", " ")
                .replace(";", ",");
    }
}
