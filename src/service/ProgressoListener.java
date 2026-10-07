package service;

@FunctionalInterface
public interface ProgressoListener {

    void atualizar(
            String etapa,
            int progresso,
            String estado
    );
}
