package service;

import connection.ConnectionFactory;
import model.ConfiguracaoBanco;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ManutencaoService {

    public static class Analise {

        private LocalDateTime ultimaManutencao;
        private long dias;
        private String decisao;
        private String regra;

        public Analise(
                LocalDateTime ultimaManutencao,
                long dias,
                String decisao,
                String regra) {

            this.ultimaManutencao = ultimaManutencao;
            this.dias = dias;
            this.decisao = decisao;
            this.regra = regra;
        }

        public LocalDateTime getUltimaManutencao() {
            return ultimaManutencao;
        }

        public long getDias() {
            return dias;
        }

        public String getDecisao() {
            return decisao;
        }

        public String getRegra() {
            return regra;
        }
    }

    public static Analise analisar(
            ConfiguracaoBanco config)
            throws Exception {

        LocalDateTime ultima =
                HistoricoManutencaoService.buscarUltima(config);

        return analisar(
                ultima,
                LocalDateTime.now()
        );
    }

    /**
     * Método usado pelos testes controlados das regras.
     */
    public static Analise analisar(
            LocalDateTime ultimaManutencao,
            LocalDateTime dataReferencia) {

        if (ultimaManutencao == null) {

            return new Analise(
                    null,
                    -1,
                    "VACUUM FULL ANALYZE",
                    "Nenhuma manutenção anterior registrada."
            );
        }

        long dias = ChronoUnit.DAYS.between(
                ultimaManutencao,
                dataReferencia
        );

        if (dias < 30) {

            return new Analise(
                    ultimaManutencao,
                    dias,
                    "NENHUMA",
                    "Última manutenção realizada há menos de 30 dias."
            );
        }

        if (dias <= 60) {

            return new Analise(
                    ultimaManutencao,
                    dias,
                    "VACUUM",
                    "Diferença entre 30 e 60 dias."
            );
        }

        return new Analise(
                ultimaManutencao,
                dias,
                "VACUUM FULL ANALYZE",
                "Última manutenção realizada há mais de 60 dias."
        );
    }

    public static void executar(
            ConfiguracaoBanco config,
            String tipo,
            String origem,
            String regra)
            throws Exception {

        LocalDateTime inicio =
                LocalDateTime.now();

        try {

            try (
                    Connection conexao =
                            ConnectionFactory.conectar(
                                    config.getHost(),
                                    config.getPorta(),
                                    config.getBanco(),
                                    config.getUsuario(),
                                    config.getSenha()
                            )
            ) {

                conexao.setAutoCommit(true);

                if (!"NENHUMA".equals(tipo)) {

                    try (Statement stmt = conexao.createStatement()) {
                        stmt.execute(tipo);
                    }
                }
            }

            LocalDateTime fim =
                    LocalDateTime.now();

            String mensagem;

            if ("NENHUMA".equals(tipo)) {
                mensagem = "Nenhuma manutenção executada.";
            } else {
                mensagem = tipo + " executado com sucesso.";
            }

            HistoricoManutencaoService.registrar(
                    config,
                    inicio,
                    fim,
                    tipo,
                    origem,
                    regra,
                    "SUCESSO",
                    mensagem
            );

            LogService.sucesso(
                    "MANUTENCAO",
                    mensagem
                            + " Origem="
                            + origem
                            + "; Regra="
                            + regra
            );

        } catch (Exception ex) {

            LocalDateTime fim =
                    LocalDateTime.now();

            try {
                HistoricoManutencaoService.registrar(
                        config,
                        inicio,
                        fim,
                        tipo,
                        origem,
                        regra,
                        "FALHA",
                        ex.getMessage()
                );

                LogService.falha(
                        "MANUTENCAO",
                        "Falha na manutenção. Origem=" + origem,
                        ex.getMessage()
                );
            } catch (Exception ignorado) {
                // Mantém o erro original.
            }

            throw ex;
        }
    }
}
