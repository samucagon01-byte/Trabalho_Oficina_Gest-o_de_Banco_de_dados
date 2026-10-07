package model;

public class HistoricoExecucao {

    private final int id;
    private final String data;
    private final String banco;
    private final long duracaoSegundos;
    private final String manutencao;
    private final String resultado;
    private final String arquivoBackup;
    private final String arquivoLog;

    public HistoricoExecucao(
            int id,
            String data,
            String banco,
            long duracaoSegundos,
            String manutencao,
            String resultado,
            String arquivoBackup,
            String arquivoLog) {

        this.id = id;
        this.data = data;
        this.banco = banco;
        this.duracaoSegundos = duracaoSegundos;
        this.manutencao = manutencao;
        this.resultado = resultado;
        this.arquivoBackup = arquivoBackup;
        this.arquivoLog = arquivoLog;
    }

    public int getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public String getBanco() {
        return banco;
    }

    public long getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public String getManutencao() {
        return manutencao;
    }

    public String getResultado() {
        return resultado;
    }

    public String getArquivoBackup() {
        return arquivoBackup;
    }

    public String getArquivoLog() {
        return arquivoLog;
    }
}
