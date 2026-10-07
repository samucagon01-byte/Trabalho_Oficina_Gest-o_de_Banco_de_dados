package model;

import java.time.LocalDateTime;

public class HistoricoManutencao {

    private String chaveBanco;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String tipoManutencao;
    private String origem;
    private String regraAplicada;
    private String status;
    private String mensagem;

    public HistoricoManutencao(
            String chaveBanco,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            String tipoManutencao,
            String origem,
            String regraAplicada,
            String status,
            String mensagem) {

        this.chaveBanco = chaveBanco;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.tipoManutencao = tipoManutencao;
        this.origem = origem;
        this.regraAplicada = regraAplicada;
        this.status = status;
        this.mensagem = mensagem;
    }

    public String getChaveBanco() {
        return chaveBanco;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public String getTipoManutencao() {
        return tipoManutencao;
    }

    public String getOrigem() {
        return origem;
    }

    public String getRegraAplicada() {
        return regraAplicada;
    }

    public String getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }
}
