package view;

import model.ConfiguracaoBanco;
import service.ProgressoListener;
import service.ProcessoBackupService;
import service.SessaoAplicacao;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

public class TelaPrincipal extends JFrame {

    private JLabel lblBanco;
    private JLabel lblStatus;
    private JLabel lblDestinoPrincipal;
    private JLabel lblDestinoAdicional;

    private JLabel lblConexao;
    private JLabel lblAnalise;
    private JLabel lblManutencao;
    private JLabel lblBackup;
    private JLabel lblCompactacao;
    private JLabel lblRetencao;
    private JLabel lblCopia;

    private JLabel lblEtapaAtual;
    private JProgressBar barraProgresso;

    private JButton btnBackup;

    public TelaPrincipal() {

        setTitle("Plataforma de Gerenciamento de Backup");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        criarInterface();
        atualizarStatus();
    }

    private void criarInterface() {

        JPanel principal =
                new JPanel(new BorderLayout(10, 10));

        principal.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel titulo = new JLabel(
                "PLATAFORMA DE GERENCIAMENTO DE BACKUP",
                SwingConstants.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 23));
        principal.add(titulo, BorderLayout.NORTH);

        JPanel centro =
                new JPanel(new BorderLayout(10, 10));

        JPanel informacoes =
                new JPanel(new GridLayout(4, 1, 5, 5));

        informacoes.setBorder(
                BorderFactory.createTitledBorder("Configuração atual")
        );

        lblBanco = criarLabel("Banco: não configurado");
        lblStatus = criarLabel("Status: configuração pendente");
        lblDestinoPrincipal = criarLabel("Destino principal: -");
        lblDestinoAdicional = criarLabel("Cópia adicional: -");

        informacoes.add(lblBanco);
        informacoes.add(lblStatus);
        informacoes.add(lblDestinoPrincipal);
        informacoes.add(lblDestinoAdicional);

        centro.add(informacoes, BorderLayout.NORTH);

        JPanel etapas = new JPanel();
        etapas.setLayout(new BoxLayout(etapas, BoxLayout.Y_AXIS));
        etapas.setBorder(
                BorderFactory.createTitledBorder(
                        "Acompanhamento do processo"
                )
        );

        lblConexao = criarEtapa("Conexão");
        lblAnalise = criarEtapa("Análise de manutenção");
        lblManutencao = criarEtapa("Manutenção");
        lblBackup = criarEtapa("Geração do backup");
        lblCompactacao = criarEtapa("Compactação / Criptografia");
        lblRetencao = criarEtapa("Retenção");
        lblCopia = criarEtapa("Cópia adicional");

        etapas.add(lblConexao);
        etapas.add(lblAnalise);
        etapas.add(lblManutencao);
        etapas.add(lblBackup);
        etapas.add(lblCompactacao);
        etapas.add(lblRetencao);
        etapas.add(lblCopia);

        centro.add(etapas, BorderLayout.CENTER);

        JPanel progresso = new JPanel(new BorderLayout(5, 5));
        progresso.setBorder(
                BorderFactory.createTitledBorder("Progresso")
        );

        lblEtapaAtual = new JLabel(
                "Etapa atual: aguardando execução."
        );

        lblEtapaAtual.setFont(
                new Font("Arial", Font.BOLD, 14)
        );

        barraProgresso =
                new JProgressBar(0, 100);

        barraProgresso.setValue(0);
        barraProgresso.setStringPainted(false);

        progresso.add(lblEtapaAtual, BorderLayout.NORTH);
        progresso.add(barraProgresso, BorderLayout.CENTER);

        centro.add(progresso, BorderLayout.SOUTH);

        principal.add(centro, BorderLayout.CENTER);

        JPanel inferior =
                new JPanel(new BorderLayout(10, 10));

        btnBackup =
                new JButton("EXECUTAR BACKUP");

        btnBackup.setFont(
                new Font("Arial", Font.BOLD, 18)
        );

        JPanel painelBackup = new JPanel();
        painelBackup.add(btnBackup);
        inferior.add(painelBackup, BorderLayout.NORTH);

        JButton btnManutencao =
                new JButton("Manutenção");
        JButton btnHistorico =
                new JButton("Histórico");
        JButton btnConfiguracao =
                new JButton("Configurações");

        JPanel menu =
                new JPanel(new GridLayout(1, 3, 8, 8));

        menu.add(btnManutencao);
        menu.add(btnHistorico);
        menu.add(btnConfiguracao);

        inferior.add(menu, BorderLayout.SOUTH);
        principal.add(inferior, BorderLayout.SOUTH);

        add(principal);

        btnConfiguracao.addActionListener(e -> {

            TelaConfiguracao tela =
                    new TelaConfiguracao(this);

            tela.setVisible(true);
        });

        btnManutencao.addActionListener(e -> {

            if (!SessaoAplicacao.temConfiguracao()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Configure o banco primeiro.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            new TelaManutencao().setVisible(true);
        });

        btnHistorico.addActionListener(e ->
                new TelaHistorico().setVisible(true)
        );

        btnBackup.addActionListener(e -> executarBackup());
    }

    private void executarBackup() {

        if (!SessaoAplicacao.temConfiguracao()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Configure o banco primeiro.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        ConfiguracaoBanco config =
                SessaoAplicacao.getConfiguracaoBanco();

        int resposta =
                JOptionPane.showConfirmDialog(
                        this,
                        "O sistema irá analisar a necessidade de manutenção "
                                + "e depois executar o processo de backup.\n\n"
                                + "Deseja continuar?",
                        "Executar Backup",
                        JOptionPane.YES_NO_OPTION
                );

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        prepararNovaExecucao();
        btnBackup.setEnabled(false);

        lblStatus.setText(
                "Status: processo em execução..."
        );

        ProgressoListener listener =
                (etapa, progresso, estado) ->
                        SwingUtilities.invokeLater(
                                () -> atualizarProgresso(
                                        etapa,
                                        progresso,
                                        estado
                                )
                        );

        new SwingWorker<Path, Void>() {

            @Override
            protected Path doInBackground()
                    throws Exception {

                return ProcessoBackupService
                        .executarAutomatico(
                                config,
                                listener
                        );
            }

            @Override
            protected void done() {

                btnBackup.setEnabled(true);

                try {

                    Path arquivo = get();

                    lblStatus.setText(
                            "Status: processo concluído com sucesso."
                    );

                    atualizarProgresso(
                            "PROCESSO",
                            100,
                            "SUCESSO"
                    );

                    String adicional =
                            config.getCaminhoCopiaAdicional();

                    JOptionPane.showMessageDialog(
                            TelaPrincipal.this,
                            "Processo concluído!\n\n"
                                    + "Arquivo final:\n"
                                    + arquivo
                                    + "\n\n"
                                    + "Destino principal:\n"
                                    + config.getCaminhoBackup()
                                    + "\n\n"
                                    + "Cópia adicional:\n"
                                    + (
                                    adicional == null || adicional.isBlank()
                                            ? "Não configurada"
                                            : adicional
                            ),
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (Exception ex) {

                    Throwable causa =
                            ex.getCause() != null
                                    ? ex.getCause()
                                    : ex;

                    lblStatus.setText(
                            "Status: falha no processo."
                    );

                    JOptionPane.showMessageDialog(
                            TelaPrincipal.this,
                            "O processo falhou:\n\n"
                                    + causa.getMessage()
                                    + "\n\n"
                                    + "Consulte a aba Histórico para visualizar "
                                    + "o log desta execução.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        }.execute();
    }

    private void prepararNovaExecucao() {

        barraProgresso.setValue(0);
        lblEtapaAtual.setText(
                "Etapa atual: iniciando processo..."
        );

        lblConexao.setText("○ Conexão");
        lblAnalise.setText("○ Análise de manutenção");
        lblManutencao.setText("○ Manutenção");
        lblBackup.setText("○ Geração do backup");
        lblCompactacao.setText("○ Compactação / Criptografia");
        lblRetencao.setText("○ Retenção");
        lblCopia.setText("○ Cópia adicional");
    }

    private void atualizarProgresso(
            String etapa,
            int progresso,
            String estado) {

        barraProgresso.setValue(progresso);

        lblEtapaAtual.setText(
                "Etapa atual: "
                        + nomeEtapa(etapa)
                        + " - "
                        + nomeEstado(estado)
        );

        atualizarLabelEtapa(etapa, estado);
    }

    private void atualizarLabelEtapa(
            String etapa,
            String estado) {

        String simbolo;

        switch (estado.toUpperCase()) {
            case "SUCESSO":
                simbolo = "✓";
                break;
            case "FALHA":
                simbolo = "✗";
                break;
            case "EM_EXECUCAO":
                simbolo = "●";
                break;
            case "IGNORADA":
                simbolo = "○";
                break;
            default:
                simbolo = "○";
                break;
        }

        String texto =
                simbolo + " " + nomeEtapa(etapa);

        switch (etapa.toUpperCase()) {
            case "CONEXAO":
                lblConexao.setText(texto);
                break;
            case "ANALISE_MANUTENCAO":
                lblAnalise.setText(texto);
                break;
            case "MANUTENCAO":
                lblManutencao.setText(texto);
                break;
            case "PG_DUMP":
            case "BACKUP":
                lblBackup.setText(texto);
                break;
            case "COMPACTACAO":
                lblCompactacao.setText(texto);
                break;
            case "RETENCAO":
                lblRetencao.setText(texto);
                break;
            case "COPIA_ADICIONAL":
                lblCopia.setText(texto);
                break;
        }
    }

    public void atualizarStatus() {

        if (SessaoAplicacao.temConfiguracao()) {

            ConfiguracaoBanco config =
                    SessaoAplicacao.getConfiguracaoBanco();

            lblBanco.setText(
                    "Banco: " + config.getBanco()
            );

            lblStatus.setText(
                    "Status: configurado"
            );

            lblDestinoPrincipal.setText(
                    "Destino principal: "
                            + config.getCaminhoBackup()
            );

            String adicional =
                    config.getCaminhoCopiaAdicional();

            lblDestinoAdicional.setText(
                    "Cópia adicional: "
                            + (
                            adicional == null || adicional.isBlank()
                                    ? "não configurada"
                                    : adicional
                    )
            );

        } else {

            lblBanco.setText("Banco: não configurado");
            lblStatus.setText("Status: configuração pendente");
            lblDestinoPrincipal.setText("Destino principal: -");
            lblDestinoAdicional.setText("Cópia adicional: -");
        }
    }

    private JLabel criarLabel(String texto) {

        JLabel label = new JLabel(texto);

        label.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        return label;
    }

    private JLabel criarEtapa(String texto) {

        JLabel label = new JLabel("○ " + texto);

        label.setFont(
                new Font("Arial", Font.PLAIN, 15)
        );

        label.setBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );

        return label;
    }

    private String nomeEtapa(String etapa) {

        switch (etapa.toUpperCase()) {
            case "CONEXAO":
                return "Conexão";
            case "ANALISE_MANUTENCAO":
                return "Análise de manutenção";
            case "MANUTENCAO":
                return "Manutenção";
            case "PG_DUMP":
                return "Geração do backup";
            case "COMPACTACAO":
                return "Compactação / Criptografia";
            case "RETENCAO":
                return "Retenção";
            case "COPIA_ADICIONAL":
                return "Cópia adicional";
            case "BACKUP":
                return "Finalização do backup";
            case "PROCESSO":
                return "Processo";
            default:
                return etapa;
        }
    }

    private String nomeEstado(String estado) {

        switch (estado.toUpperCase()) {
            case "EM_EXECUCAO":
                return "em execução";
            case "SUCESSO":
                return "concluído";
            case "FALHA":
                return "falhou";
            case "IGNORADA":
                return "não necessária";
            default:
                return estado;
        }
    }
}
