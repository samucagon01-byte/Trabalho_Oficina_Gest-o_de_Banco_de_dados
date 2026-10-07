package teste;

import service.ManutencaoService;

import java.time.LocalDateTime;

public class TesteRegrasManutencao {

    public static void main(String[] args) {

        LocalDateTime hoje =
                LocalDateTime.of(
                        2026,
                        9,
                        30,
                        12,
                        0
                );

        executarTeste(
                "SEM HISTÓRICO",
                null,
                hoje
        );

        executarTeste(
                "MENOS DE 30 DIAS",
                hoje.minusDays(10),
                hoje
        );

        executarTeste(
                "ENTRE 30 E 60 DIAS",
                hoje.minusDays(45),
                hoje
        );

        executarTeste(
                "MAIS DE 60 DIAS",
                hoje.minusDays(90),
                hoje
        );
    }

    private static void executarTeste(
            String nome,
            LocalDateTime ultimaManutencao,
            LocalDateTime dataReferencia) {

        ManutencaoService.Analise resultado =
                ManutencaoService.analisar(
                        ultimaManutencao,
                        dataReferencia
                );

        System.out.println(
                "=========================================="
        );

        System.out.println("TESTE: " + nome);

        if (ultimaManutencao == null) {
            System.out.println(
                    "Última manutenção: NENHUMA"
            );
        } else {
            System.out.println(
                    "Última manutenção: "
                            + ultimaManutencao
            );

            System.out.println(
                    "Dias decorridos: "
                            + resultado.getDias()
            );
        }

        System.out.println(
                "Decisão: "
                        + resultado.getDecisao()
        );

        System.out.println(
                "Regra: "
                        + resultado.getRegra()
        );

        System.out.println();
    }
}
