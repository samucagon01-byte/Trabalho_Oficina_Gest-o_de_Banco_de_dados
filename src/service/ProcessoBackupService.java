package service;

import connection.ConnectionFactory;
import model.ConfiguracaoBanco;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDateTime;

public class ProcessoBackupService {

    public static Path executarAutomatico(
            ConfiguracaoBanco config)
            throws Exception {

        return executarAutomatico(config, null);
    }

    public static Path executarAutomatico(
            ConfiguracaoBanco config,
            ProgressoListener listener)
            throws Exception {

        LocalDateTime inicio = LocalDateTime.now();
        String decisaoManutencao = "NÃO ANALISADA";
        Path arquivoBackup = null;
        Path arquivoLog = LogService.iniciarExecucao();
        String resultado = "FALHA";

        try {

            atualizar(listener, "CONEXAO", 5, "EM_EXECUCAO");

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
                // A abertura da conexão valida o acesso.
            }

            LogService.sucesso(
                    "CONEXAO",
                    "Conexão com o banco validada com sucesso."
            );

            atualizar(listener, "CONEXAO", 10, "SUCESSO");

            // =================================================
            // ANÁLISE
            // =================================================

            atualizar(
                    listener,
                    "ANALISE_MANUTENCAO",
                    15,
                    "EM_EXECUCAO"
            );

            ManutencaoService.Analise analise =
                    ManutencaoService.analisar(config);

            decisaoManutencao =
                    analise.getDecisao();

            LogService.sucesso(
                    "ANALISE_MANUTENCAO",
                    "Decisão automática: "
                            + analise.getDecisao()
                            + "; Regra: "
                            + analise.getRegra()
                            + "; Dias desde última manutenção: "
                            + analise.getDias()
            );

            atualizar(
                    listener,
                    "ANALISE_MANUTENCAO",
                    25,
                    "SUCESSO"
            );

            // =================================================
            // MANUTENÇÃO
            // =================================================

            String estado =
                    "NENHUMA".equalsIgnoreCase(
                            analise.getDecisao()
                    )
                            ? "IGNORADA"
                            : "EM_EXECUCAO";

            atualizar(
                    listener,
                    "MANUTENCAO",
                    30,
                    estado
            );

            ManutencaoService.executar(
                    config,
                    analise.getDecisao(),
                    "AUTOMATICA",
                    analise.getRegra()
            );

            LogService.sucesso(
                    "MANUTENCAO",
                    "Etapa de manutenção concluída. Decisão aplicada: "
                            + analise.getDecisao()
            );

            atualizar(
                    listener,
                    "MANUTENCAO",
                    40,
                    "NENHUMA".equalsIgnoreCase(
                            analise.getDecisao()
                    )
                            ? "IGNORADA"
                            : "SUCESSO"
            );

            // =================================================
            // BACKUP
            // =================================================

            arquivoBackup =
                    BackupService.gerarBackup(
                            config,
                            listener
                    );

            resultado = "SUCESSO";

            atualizar(
                    listener,
                    "PROCESSO",
                    100,
                    "SUCESSO"
            );

            return arquivoBackup;

        } catch (Exception ex) {

            LogService.falha(
                    "PROCESSO",
                    "Processo de backup finalizado com falha.",
                    ex.getMessage()
            );

            atualizar(
                    listener,
                    "PROCESSO",
                    100,
                    "FALHA"
            );

            throw ex;

        } finally {

            LocalDateTime fim = LocalDateTime.now();

            HistoricoExecucaoService.registrar(
                    inicio,
                    fim,
                    config.getBanco(),
                    decisaoManutencao,
                    resultado,
                    arquivoBackup == null
                            ? ""
                            : arquivoBackup.toString(),
                    arquivoLog
            );

            LogService.finalizarExecucao();
        }
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
