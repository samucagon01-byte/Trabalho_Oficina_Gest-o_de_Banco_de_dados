package view;

import model.ConfiguracaoBanco;
import service.ManutencaoService;
import service.SessaoAplicacao;

import javax.swing.*;
import java.awt.*;

public class TelaManutencao extends JFrame {

    private JLabel lblUltima;
    private JLabel lblDecisao;
    private JLabel lblRegra;
    private JLabel lblStatus;

    private JRadioButton rbAutomatica;
    private JRadioButton rbNenhuma;
    private JRadioButton rbVacuum;
    private JRadioButton rbFullAnalyze;

    private ManutencaoService.Analise ultimaAnalise;

    public TelaManutencao() {

        setTitle("Manutenção do Banco");

        setSize(600, 500);

        setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        setLocationRelativeTo(null);
        setResizable(false);

        criarInterface();
    }

    private void criarInterface() {

        JPanel painel =
                new JPanel(
                        new BorderLayout(10, 10)
                );

        painel.setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        15,
                        15,
                        15
                )
        );

        // ======================================
        // TÍTULO
        // ======================================

        JLabel titulo =
                new JLabel(
                        "MANUTENÇÃO DO BANCO",
                        SwingConstants.CENTER
                );

        titulo.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        20
                )
        );

        painel.add(
                titulo,
                BorderLayout.NORTH
        );

        // ======================================
        // INFORMAÇÕES
        // ======================================

        JPanel info =
                new JPanel();

        info.setLayout(
                new BoxLayout(
                        info,
                        BoxLayout.Y_AXIS
                )
        );

        lblUltima =
                new JLabel(
                        "Última manutenção: não consultada"
                );

        lblDecisao =
                new JLabel(
                        "Decisão automática: não analisada"
                );

        lblRegra =
                new JLabel(
                        "Regra: -"
                );

        lblUltima.setFont(
                new Font("Arial", Font.PLAIN, 15)
        );

        lblDecisao.setFont(
                new Font("Arial", Font.BOLD, 15)
        );

        lblRegra.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        info.add(lblUltima);
        info.add(Box.createVerticalStrut(8));
        info.add(lblDecisao);
        info.add(Box.createVerticalStrut(8));
        info.add(lblRegra);
        info.add(Box.createVerticalStrut(20));

        // ======================================
        // OPÇÕES MANUAIS
        // ======================================

        info.add(
                new JLabel("Escolha manual:")
        );

        rbAutomatica =
                new JRadioButton(
                        "Usar decisão automática",
                        true
                );

        rbNenhuma =
                new JRadioButton(
                        "Não executar"
                );

        rbVacuum =
                new JRadioButton(
                        "VACUUM"
                );

        rbFullAnalyze =
                new JRadioButton(
                        "VACUUM FULL ANALYZE"
                );

        ButtonGroup grupo =
                new ButtonGroup();

        grupo.add(rbAutomatica);
        grupo.add(rbNenhuma);
        grupo.add(rbVacuum);
        grupo.add(rbFullAnalyze);

        info.add(rbAutomatica);
        info.add(rbNenhuma);
        info.add(rbVacuum);
        info.add(rbFullAnalyze);

        painel.add(
                info,
                BorderLayout.CENTER
        );

        // ======================================
        // BOTÕES
        // ======================================

        JButton btnAnalisar =
                new JButton("ANALISAR");

        JButton btnExecutar =
                new JButton("EXECUTAR MANUTENÇÃO");

        JButton btnVoltar =
                new JButton("VOLTAR");

        JPanel botoes =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.CENTER,
                                10,
                                10
                        )
                );

        botoes.add(btnAnalisar);
        botoes.add(btnExecutar);
        botoes.add(btnVoltar);

        lblStatus =
                new JLabel(
                        "Status: aguardando..."
                );

        JPanel rodape =
                new JPanel(
                        new BorderLayout()
                );

        rodape.add(
                botoes,
                BorderLayout.NORTH
        );

        rodape.add(
                lblStatus,
                BorderLayout.SOUTH
        );

        painel.add(
                rodape,
                BorderLayout.SOUTH
        );

        add(painel);

        // ======================================
        // AÇÕES
        // ======================================

        btnAnalisar.addActionListener(
                e -> analisar()
        );

        btnExecutar.addActionListener(
                e -> executar()
        );

        btnVoltar.addActionListener(
                e -> dispose()
        );
    }

    // ==========================================
    // ANALISAR
    // ==========================================

    private void analisar() {

        if (!SessaoAplicacao.temConfiguracao()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Configure o banco primeiro."
            );

            return;
        }

        ConfiguracaoBanco config =
                SessaoAplicacao
                        .getConfiguracaoBanco();

        lblStatus.setText(
                "Status: analisando..."
        );

        new SwingWorker<
                ManutencaoService.Analise,
                Void>() {

            @Override
            protected ManutencaoService.Analise
            doInBackground() throws Exception {

                return ManutencaoService
                        .analisar(config);
            }

            @Override
            protected void done() {

                try {

                    ultimaAnalise = get();

                    if (
                            ultimaAnalise
                                    .getUltimaManutencao()
                                    == null
                    ) {

                        lblUltima.setText(
                                "Última manutenção: nenhuma"
                        );

                    } else {

                        lblUltima.setText(
                                "Última manutenção: "
                                        + ultimaAnalise
                                        .getDias()
                                        + " dias atrás"
                        );
                    }

                    lblDecisao.setText(
                            "Decisão automática: "
                                    + ultimaAnalise
                                    .getDecisao()
                    );

                    lblRegra.setText(
                            "Regra: "
                                    + ultimaAnalise
                                    .getRegra()
                    );

                    lblStatus.setText(
                            "Status: análise concluída."
                    );

                } catch (Exception ex) {

                    lblStatus.setText(
                            "Status: erro na análise."
                    );

                    JOptionPane.showMessageDialog(
                            TelaManutencao.this,
                            "Erro:\n"
                                    + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        }.execute();
    }

    // ==========================================
    // EXECUTAR
    // ==========================================

    private void executar() {

        if (!SessaoAplicacao.temConfiguracao()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Configure o banco primeiro."
            );

            return;
        }

        String decisao;
        String origem;
        String regra;

        // MANUAL
        if (rbNenhuma.isSelected()) {

            decisao = "NENHUMA";
            origem = "MANUAL";
            regra = "Escolha manual do usuário.";

        } else if (rbVacuum.isSelected()) {

            decisao = "VACUUM";
            origem = "MANUAL";
            regra =
                    "Escolha manual do usuário; "
                            + "prevalece sobre a decisão automática.";

        } else if (rbFullAnalyze.isSelected()) {

            decisao = "VACUUM FULL ANALYZE";
            origem = "MANUAL";
            regra =
                    "Escolha manual do usuário; "
                            + "prevalece sobre a decisão automática.";

        } else {

            // AUTOMÁTICA

            if (ultimaAnalise == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Clique em ANALISAR primeiro."
                );

                return;
            }

            decisao =
                    ultimaAnalise.getDecisao();

            origem = "AUTOMATICA";

            regra =
                    ultimaAnalise.getRegra();
        }

        int resposta =
                JOptionPane.showConfirmDialog(
                        this,
                        "Executar manutenção:\n\n"
                                + decisao
                                + "\n\nContinuar?",
                        "Confirmar",
                        JOptionPane.YES_NO_OPTION
                );

        if (
                resposta != JOptionPane.YES_OPTION
        ) {
            return;
        }

        ConfiguracaoBanco config =
                SessaoAplicacao
                        .getConfiguracaoBanco();

        lblStatus.setText(
                "Status: executando..."
        );

        String decisaoFinal = decisao;
        String origemFinal = origem;
        String regraFinal = regra;

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
                    throws Exception {

                ManutencaoService.executar(
                        config,
                        decisaoFinal,
                        origemFinal,
                        regraFinal
                );

                return null;
            }

            @Override
            protected void done() {

                try {

                    get();

                    lblStatus.setText(
                            "Status: "
                                    + decisaoFinal
                                    + " concluído."
                    );

                    JOptionPane.showMessageDialog(
                            TelaManutencao.this,
                            "Manutenção concluída com sucesso!",
                            "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (Exception ex) {

                    lblStatus.setText(
                            "Status: falha."
                    );

                    JOptionPane.showMessageDialog(
                            TelaManutencao.this,
                            "Falha na manutenção:\n"
                                    + ex.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        }.execute();
    }
}