package teste;

import service.LogService;

public class TesteLog {

    public static void main(String[] args) {

        System.out.println(
                "Iniciando teste do sistema de logs..."
        );

        LogService.sucesso(
                "CONEXAO",
                "Conexão com banco validada"
        );

        LogService.sucesso(
                "MANUTENCAO",
                "VACUUM executado com sucesso"
        );

        LogService.sucesso(
                "BACKUP",
                "Backup gerado com sucesso"
        );

        LogService.falha(
                "BACKUP",
                "Falha controlada durante geração do backup",
                "Teste de falha proposital"
        );

        System.out.println(
                "Logs registrados com sucesso."
        );

        System.out.println(
                "Arquivo: "
                        + LogService.getArquivoLog()
                        .toAbsolutePath()
        );
    }
}
