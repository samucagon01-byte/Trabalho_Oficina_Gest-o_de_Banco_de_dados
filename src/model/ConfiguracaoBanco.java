package model;

public class ConfiguracaoBanco {

    private String host;
    private String porta;
    private String banco;
    private String usuario;
    private String senha;

    private String caminhoBackup;
    private Integer quantidadeManter;
    private String caminhoCopiaAdicional;

    private boolean compactar;
    private String senhaZip;

    public ConfiguracaoBanco(
            String host,
            String porta,
            String banco,
            String usuario,
            String senha,
            String caminhoBackup,
            Integer quantidadeManter,
            String caminhoCopiaAdicional,
            boolean compactar,
            String senhaZip) {

        this.host = host;
        this.porta = porta;
        this.banco = banco;
        this.usuario = usuario;
        this.senha = senha;
        this.caminhoBackup = caminhoBackup;
        this.quantidadeManter = quantidadeManter;
        this.caminhoCopiaAdicional = caminhoCopiaAdicional;
        this.compactar = compactar;
        this.senhaZip = senhaZip;
    }

    public ConfiguracaoBanco(
            String host,
            String porta,
            String banco,
            String usuario,
            String senha,
            String caminhoBackup,
            Integer quantidadeManter,
            String caminhoCopiaAdicional) {

        this(
                host,
                porta,
                banco,
                usuario,
                senha,
                caminhoBackup,
                quantidadeManter,
                caminhoCopiaAdicional,
                true,
                null
        );
    }

    public ConfiguracaoBanco(
            String host,
            String porta,
            String banco,
            String usuario,
            String senha,
            String caminhoBackup,
            Integer quantidadeManter) {

        this(
                host,
                porta,
                banco,
                usuario,
                senha,
                caminhoBackup,
                quantidadeManter,
                null,
                true,
                null
        );
    }

    public ConfiguracaoBanco(
            String host,
            String porta,
            String banco,
            String usuario,
            String senha,
            String caminhoBackup) {

        this(
                host,
                porta,
                banco,
                usuario,
                senha,
                caminhoBackup,
                null,
                null,
                true,
                null
        );
    }

    public String getHost() {
        return host;
    }

    public String getPorta() {
        return porta;
    }

    public String getBanco() {
        return banco;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getSenha() {
        return senha;
    }

    public String getCaminhoBackup() {
        return caminhoBackup;
    }

    public Integer getQuantidadeManter() {
        return quantidadeManter;
    }

    public String getCaminhoCopiaAdicional() {
        return caminhoCopiaAdicional;
    }

    public boolean isCompactar() {
        return compactar;
    }

    public String getSenhaZip() {
        return senhaZip;
    }
}
