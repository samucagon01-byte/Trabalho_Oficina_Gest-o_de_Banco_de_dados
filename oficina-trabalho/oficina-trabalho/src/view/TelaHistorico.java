package view;

import model.HistoricoExecucao;
import service.EmailService;
import service.HistoricoExecucaoService;
import service.LogService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

public class TelaHistorico extends JFrame {

    private JTable tabela;
    private DefaultTableModel modelo;
    private JTextArea areaLog;
    private JButton btnVerLog;
    private JButton btnEmail;

    private List<HistoricoExecucao> historicoAtual;

    public TelaHistorico() {

        setTitle("Histórico de Execuções");
        setSize(1150, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        criarInterface();
        carregarHistorico();
    }

    private void criarInterface() {

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel titulo = new JLabel(
                "HISTÓRICO DE EXECUÇÕES",
                SwingConstants.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        principal.add(titulo, BorderLayout.NORTH);

        String[] colunas = {
                "ID",
                "Data",
                "Banco",
                "Duração",
                "Manutenção",
                "Resultado",
                "Arquivo de Backup"
        };

        modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);
        tabela.setRowHeight(28);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(500);

        JScrollPane scrollTabela = new JScrollPane(tabela);

        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setLineWrap(true);
        areaLog.setWrapStyleWord(true);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaLog.setText(
                "Selecione uma execução e clique em VER LOG."
        );

        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setBorder(
                BorderFactory.createTitledBorder("Log da execução selecionada")
        );

        JSplitPane divisao = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                scrollTabela,
                scrollLog
        );

        divisao.setResizeWeight(0.55);
        principal.add(divisao, BorderLayout.CENTER);

        btnVerLog = new JButton("VER LOG");
        btnEmail = new JButton("SIMULAR ENVIO DO LOG");
        JButton btnAtualizar = new JButton("ATUALIZAR");
        JButton btnFechar = new JButton("FECHAR");

        btnVerLog.setEnabled(false);
        btnEmail.setEnabled(false);

        JPanel botoes = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 5)
        );

        botoes.add(btnVerLog);
        botoes.add(btnEmail);
        botoes.add(btnAtualizar);
        botoes.add(btnFechar);

        principal.add(botoes, BorderLayout.SOUTH);

        add(principal);

        tabela.getSelectionModel().addListSelectionListener(
                e -> atualizarEstadoBotoes()
        );

        tabela.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent e) {

                        if (e.getClickCount() == 2) {
                            mostrarLogSelecionado();
                        }
                    }
                }
        );

        btnVerLog.addActionListener(
                e -> mostrarLogSelecionado()
        );

        btnEmail.addActionListener(
                e -> simularEmail()
        );

        btnAtualizar.addActionListener(
                e -> carregarHistorico()
        );

        btnFechar.addActionListener(
                e -> dispose()
        );
    }

    private void carregarHistorico() {

        modelo.setRowCount(0);
        areaLog.setText(
                "Selecione uma execução e clique em VER LOG."
        );

        historicoAtual =
                HistoricoExecucaoService.listar();

        for (HistoricoExecucao execucao : historicoAtual) {

            modelo.addRow(
                    new Object[]{
                            execucao.getId(),
                            execucao.getData(),
                            execucao.getBanco(),
                            formatarDuracao(
                                    execucao.getDuracaoSegundos()
                            ),
                            execucao.getManutencao(),
                            execucao.getResultado(),
                            execucao.getArquivoBackup()
                    }
            );
        }

        atualizarEstadoBotoes();
    }

    private void atualizarEstadoBotoes() {

        boolean selecionado =
                tabela.getSelectedRow() >= 0
                        && tabela.getSelectedRow() < historicoAtual.size();

        btnVerLog.setEnabled(selecionado);
        btnEmail.setEnabled(selecionado);
    }

    private HistoricoExecucao getSelecionado() {

        int linha = tabela.getSelectedRow();

        if (
                linha < 0
                        || linha >= historicoAtual.size()
        ) {
            return null;
        }

        return historicoAtual.get(linha);
    }

    private void mostrarLogSelecionado() {

        HistoricoExecucao execucao = getSelecionado();

        if (execucao == null) {
            return;
        }

        Path arquivoLog = Path.of(
                execucao.getArquivoLog()
        );

        List<String> linhas =
                LogService.lerLog(arquivoLog);

        if (linhas.isEmpty()) {

            areaLog.setText(
                    "Não foi possível localizar o log desta execução.\n\n"
                            + "Arquivo esperado:\n"
                            + arquivoLog
            );

            return;
        }

        areaLog.setText(
                String.join(
                        System.lineSeparator(),
                        linhas
                )
        );

        areaLog.setCaretPosition(0);
    }

    private void simularEmail() {

        HistoricoExecucao execucao = getSelecionado();

        if (execucao == null) {
            return;
        }

        String destinatario =
                JOptionPane.showInputDialog(
                        this,
                        "Informe o destinatário:",
                        "Simular envio do log",
                        JOptionPane.QUESTION_MESSAGE
                );

        if (destinatario == null || destinatario.isBlank()) {
            return;
        }

        try {

            Path comprovante =
                    EmailService.simularEnvioLog(
                            Path.of(execucao.getArquivoLog()),
                            destinatario.trim()
                    );

            JOptionPane.showMessageDialog(
                    this,
                    "Envio simulado com sucesso!\n\n"
                            + "Comprovante:\n"
                            + comprovante.toAbsolutePath(),
                    "E-mail simulado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível simular o envio:\n\n"
                            + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String formatarDuracao(long segundos) {

        long minutos = segundos / 60;
        long restante = segundos % 60;

        if (minutos > 0) {
            return String.format(
                    "%02d:%02d",
                    minutos,
                    restante
            );
        }

        return segundos + " s";
    }
}
