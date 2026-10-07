package teste;

import service.RestauracaoService;

import java.nio.file.Path;
import java.util.Scanner;

public class TesteRestauracao {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("===============================================");
        System.out.println("TESTE CONTROLADO DE RESTAURAÇÃO");
        System.out.println("===============================================");
        System.out.println();

        System.out.print("Caminho do backup (.zip ou .sql): ");
        String arquivo = scanner.nextLine().trim();

        System.out.print("Host: ");
        String host = scanner.nextLine().trim();

        System.out.print("Porta: ");
        String porta = scanner.nextLine().trim();

        System.out.print("Banco de destino: ");
        String banco = scanner.nextLine().trim();

        System.out.print("Usuário: ");
        String usuario = scanner.nextLine().trim();

        System.out.print("Senha do banco de destino: ");
        String senhaBanco = scanner.nextLine();

        String senhaZip = null;

        if (arquivo.toLowerCase().endsWith(".zip")) {
            System.out.print("Senha do ZIP: ");
            senhaZip = scanner.nextLine();
        }

        try {

            RestauracaoService.ResultadoRestauracao resultado =
                    RestauracaoService.restaurar(
                            Path.of(arquivo),
                            host,
                            porta,
                            banco,
                            usuario,
                            senhaBanco,
                            senhaZip
                    );

            System.out.println();
            System.out.println("===============================================");
            System.out.println("RESTAURAÇÃO CONCLUÍDA COM SUCESSO");
            System.out.println("===============================================");
            System.out.println(resultado.getResumo());

        } catch (Exception ex) {

            System.err.println();
            System.err.println("===============================================");
            System.err.println("FALHA NA RESTAURAÇÃO");
            System.err.println("===============================================");
            System.err.println(ex.getMessage());
        }
    }
}
