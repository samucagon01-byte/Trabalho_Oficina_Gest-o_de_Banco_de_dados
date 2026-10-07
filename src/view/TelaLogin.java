package view;

import javax.swing.*;
import java.awt.*;

public class TelaLogin extends JFrame {

    // Login provisório da aplicação
    // Depois podemos trocar por usuários vindos do banco
    private static final String USUARIO_VALIDO = "admin";
    private static final String SENHA_VALIDA = "1234";

    private JTextField txtUsuario;
    private JPasswordField txtSenha;

    public TelaLogin() {

        setTitle("Login - Gerenciador de Backup");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel painel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel(
                "GERENCIADOR DE BACKUP",
                SwingConstants.CENTER
        );

        titulo.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        painel.add(titulo, gbc);

        gbc.gridwidth = 1;

        // USUÁRIO
        gbc.gridx = 0;
        gbc.gridy = 1;

        painel.add(
                new JLabel("Usuário:"),
                gbc
        );

        txtUsuario = new JTextField();

        gbc.gridx = 1;

        painel.add(
                txtUsuario,
                gbc
        );

        // SENHA
        gbc.gridx = 0;
        gbc.gridy = 2;

        painel.add(
                new JLabel("Senha:"),
                gbc
        );

        txtSenha = new JPasswordField();

        gbc.gridx = 1;

        painel.add(
                txtSenha,
                gbc
        );

        // BOTÃO
        JButton btnEntrar = new JButton(
                "ENTRAR"
        );

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        painel.add(
                btnEntrar,
                gbc
        );

        add(painel);

        btnEntrar.addActionListener(e -> fazerLogin());
    }

    private void fazerLogin() {

        String usuario =
                txtUsuario.getText();

        String senha =
                new String(txtSenha.getPassword());

        if (usuario.equals(USUARIO_VALIDO)
                && senha.equals(SENHA_VALIDA)) {

            TelaPrincipal tela =
                    new TelaPrincipal();

            tela.setVisible(true);

            dispose();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Usuário ou senha inválidos.",
                    "Erro de Login",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}