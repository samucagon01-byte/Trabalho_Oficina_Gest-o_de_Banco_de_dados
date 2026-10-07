package view;

import connection.ConnectionFactory;
import model.ConfiguracaoBanco;
import service.SessaoAplicacao;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class TelaConfiguracao extends JFrame {

    private JTextField txtHost;
    private JTextField txtPorta;
    private JTextField txtBanco;
    private JTextField txtUsuario;
    private JPasswordField txtSenha;

    private JTextField txtCaminhoBackup;
    private JTextField txtQuantidadeBackups;
    private JTextField txtCaminhoCopiaAdicional;

    private JCheckBox chkCompactar;
    private JPasswordField txtSenhaZip;

    private JLabel lblStatus;

    private TelaPrincipal telaPrincipal;

    public TelaConfiguracao() {
        this(null);
    }

    public TelaConfiguracao(TelaPrincipal telaPrincipal) {

        this.telaPrincipal = telaPrincipal;

        setTitle("Configuração do Sistema");
        setSize(650, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        criarInterface();
        carregarConfiguracaoAtual();
    }

    private void criarInterface() {

        JPanel painel =
                new JPanel(new GridBagLayout());

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo =
                new JLabel(
                        "CONFIGURAÇÃO DO SISTEMA",
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

        adicionarCampo(
                painel, gbc, 1,
                "Host:",
                txtHost = new JTextField(
                        "dpg-daq55uflot8c73fctnu0-a.oregon-postgres.render.com"
                )
        );

        adicionarCampo(
                painel, gbc, 2,
                "Porta:",
                txtPorta = new JTextField("5432")
        );

        adicionarCampo(
                painel, gbc, 3,
                "Banco:",
                txtBanco = new JTextField(
                        "oficina_db_gry8"
                )
        );

        adicionarCampo(
                painel, gbc, 4,
                "Usuário:",
                txtUsuario = new JTextField(
                        "oficina_db_gry8_user"
                )
        );

        // Senha banco
        gbc.gridx = 0;
        gbc.gridy = 5;
        painel.add(new JLabel("Senha do banco:"), gbc);

        txtSenha = new JPasswordField(
                "e1ORkXgf1UcwoNEyOpoxiLWiupPr8PxG"
        );

        gbc.gridx = 1;
        painel.add(txtSenha, gbc);

        adicionarCampo(
                painel, gbc, 6,
                "Caminho backup:",
                txtCaminhoBackup =
                        new JTextField("C:\\Backups\\Oficina")
        );

        adicionarCampo(
                painel, gbc, 7,
                "Manter backups:",
                txtQuantidadeBackups =
                        new JTextField("3")
        );

        adicionarCampo(
                painel, gbc, 8,
                "Cópia adicional:",
                txtCaminhoCopiaAdicional =
                        new JTextField("C:\\Backups\\Copia")
        );

        // Compactação
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;

        chkCompactar =
                new JCheckBox(
                        "Compactar backup e proteger automaticamente com AES-256"
                );

        chkCompactar.setSelected(true);

        painel.add(
                chkCompactar,
                gbc
        );

        gbc.gridwidth = 1;

        // Senha ZIP
        gbc.gridx = 0;
        gbc.gridy = 10;
        painel.add(
                new JLabel("Senha do arquivo compactado:"),
                gbc
        );

        txtSenhaZip =
                new JPasswordField();

        gbc.gridx = 1;
        painel.add(
                txtSenhaZip,
                gbc
        );

        JLabel lblInfo =
                new JLabel(
                        "<html>Quando a compactação estiver habilitada, "
                                + "o arquivo será protegido automaticamente com a senha definida aqui.</html>"
                );

        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 2;

        painel.add(lblInfo, gbc);

        // Botão testar
        JButton btnTestar =
                new JButton("TESTAR CONEXÃO");

        gbc.gridy = 12;
        painel.add(btnTestar, gbc);

        // Botão salvar
        JButton btnSalvar =
                new JButton("SALVAR CONFIGURAÇÃO");

        gbc.gridy = 13;
        painel.add(btnSalvar, gbc);

        lblStatus =
                new JLabel(
                        "Status: aguardando...",
                        SwingConstants.CENTER
                );

        gbc.gridy = 14;
        painel.add(lblStatus, gbc);

        add(painel);

        chkCompactar.addActionListener(
                e -> atualizarEstadoSenhaZip()
        );

        btnTestar.addActionListener(
                e -> testarConexao()
        );

        btnSalvar.addActionListener(
                e -> salvarConfiguracao()
        );

        atualizarEstadoSenhaZip();
    }

    private void adicionarCampo(
            JPanel painel,
            GridBagConstraints gbc,
            int linha,
            String label,
            JTextField campo) {

        gbc.gridx = 0;
        gbc.gridy = linha;
        gbc.gridwidth = 1;

        painel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        painel.add(campo, gbc);
    }

    private void atualizarEstadoSenhaZip() {

        boolean ativo = chkCompactar.isSelected();

        txtSenhaZip.setEnabled(ativo);

        if (!ativo) {
            txtSenhaZip.setText("");
        }
    }

    private void carregarConfiguracaoAtual() {

        if (!SessaoAplicacao.temConfiguracao()) {
            return;
        }

        ConfiguracaoBanco config =
                SessaoAplicacao.getConfiguracaoBanco();

        txtHost.setText(config.getHost());
        txtPorta.setText(config.getPorta());
        txtBanco.setText(config.getBanco());
        txtUsuario.setText(config.getUsuario());
        txtSenha.setText(config.getSenha());
        txtCaminhoBackup.setText(config.getCaminhoBackup());

        txtQuantidadeBackups.setText(
                config.getQuantidadeManter() == null
                        ? ""
                        : String.valueOf(config.getQuantidadeManter())
        );

        txtCaminhoCopiaAdicional.setText(
                config.getCaminhoCopiaAdicional() == null
                        ? ""
                        : config.getCaminhoCopiaAdicional()
        );

        chkCompactar.setSelected(
                config.isCompactar()
        );

        if (config.getSenhaZip() != null) {
            txtSenhaZip.setText(
                    config.getSenhaZip()
            );
        }

        atualizarEstadoSenhaZip();
    }

    private void testarConexao() {

        String host = txtHost.getText().trim();
        String porta = txtPorta.getText().trim();
        String banco = txtBanco.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword());

        if (
                host.isBlank()
                        || porta.isBlank()
                        || banco.isBlank()
                        || usuario.isBlank()
                        || senha.isBlank()
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Preencha os dados da conexão antes de testar.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            try (
                    Connection conexao =
                            ConnectionFactory.conectar(
                                    host,
                                    porta,
                                    banco,
                                    usuario,
                                    senha
                            )
            ) {
            }

            lblStatus.setText(
                    "Status: CONECTADO!"
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Conexão realizada com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException ex) {

            lblStatus.setText(
                    "Status: FALHA"
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao conectar:\n" + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void salvarConfiguracao() {

        String host = txtHost.getText().trim();
        String porta = txtPorta.getText().trim();
        String banco = txtBanco.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String senha = new String(txtSenha.getPassword());
        String caminhoBackup = txtCaminhoBackup.getText().trim();
        String textoQuantidade = txtQuantidadeBackups.getText().trim();
        String caminhoCopia = txtCaminhoCopiaAdicional.getText().trim();
        boolean compactar = chkCompactar.isSelected();
        String senhaZip = new String(txtSenhaZip.getPassword());

        if (
                host.isEmpty()
                        || porta.isEmpty()
                        || banco.isEmpty()
                        || usuario.isEmpty()
                        || senha.isEmpty()
                        || caminhoBackup.isEmpty()
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Preencha todos os campos obrigatórios.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (!porta.matches("\\d{1,5}")) {

            JOptionPane.showMessageDialog(
                    this,
                    "Informe uma porta válida.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int numeroPorta = Integer.parseInt(porta);

        if (numeroPorta < 1 || numeroPorta > 65535) {

            JOptionPane.showMessageDialog(
                    this,
                    "A porta deve estar entre 1 e 65535.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        Integer quantidadeManter = null;

        if (!textoQuantidade.isEmpty()) {

            try {

                quantidadeManter =
                        Integer.parseInt(textoQuantidade);

                if (quantidadeManter < 1) {
                    throw new NumberFormatException();
                }

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Informe uma quantidade de backups válida e maior que zero.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }
        }

        if (compactar && senhaZip.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Informe a senha do arquivo compactado.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        try {

            Path pastaPrincipal = Path.of(caminhoBackup);
            Files.createDirectories(pastaPrincipal);

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível acessar a pasta principal:\n"
                            + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (!caminhoCopia.isEmpty()) {

            try {

                Path pastaAdicional = Path.of(caminhoCopia);
                Files.createDirectories(pastaAdicional);

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Não foi possível acessar a pasta de cópia adicional:\n"
                                + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE
                );

                return;
            }
        }

        ConfiguracaoBanco configuracao =
                new ConfiguracaoBanco(
                        host,
                        porta,
                        banco,
                        usuario,
                        senha,
                        caminhoBackup,
                        quantidadeManter,
                        caminhoCopia.isEmpty()
                                ? null
                                : caminhoCopia,
                        compactar,
                        compactar
                                ? senhaZip
                                : null
                );

        SessaoAplicacao.salvarConfiguracao(
                configuracao
        );

        if (telaPrincipal != null) {
            telaPrincipal.atualizarStatus();
        }

        JOptionPane.showMessageDialog(
                this,
                "Configuração salva com sucesso!",
                "Sucesso",
                JOptionPane.INFORMATION_MESSAGE
        );

        dispose();
    }
}
