package service;

import model.ConfiguracaoBanco;

public class SessaoAplicacao {

    private static ConfiguracaoBanco configuracaoBanco;

    public static void salvarConfiguracao(
            ConfiguracaoBanco configuracao) {

        configuracaoBanco = configuracao;
    }

    public static ConfiguracaoBanco getConfiguracaoBanco() {
        return configuracaoBanco;
    }

    public static boolean temConfiguracao() {
        return configuracaoBanco != null;
    }
}