package gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

// Interface de Dashboard
public class DashboardGUI extends JFrame {

    private final JTextArea logArea;
    private final JTextArea queueTopicInfoArea;
    private final JTextArea resultsArea;

    public DashboardGUI() {
        setTitle("Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Área de log principal
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(new TitledBorder("Logs de Atividade (Workers e Leitores)"));

        // Painel para informações e resultados
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Área de Informações de Fila e Tópico
        queueTopicInfoArea = new JTextArea();
        queueTopicInfoArea.setEditable(false);
        JScrollPane queueTopicScrollPane = new JScrollPane(queueTopicInfoArea);
        queueTopicScrollPane.setBorder(new TitledBorder("Status (FILA_LINHAS e TOPICO_CONTAGEM)"));
        infoPanel.add(queueTopicScrollPane);

        // Área de resultados finais
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        JScrollPane resultsScrollPane = new JScrollPane(resultsArea);
        resultsScrollPane.setBorder(new TitledBorder("Contagem Final"));
        infoPanel.add(resultsScrollPane);

        // Adicionando os componentes ao frame
        add(logScrollPane, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
        infoPanel.setPreferredSize(new Dimension(getWidth(), 200));
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); // Auto-scroll
        });
    }

    public void setQueueTopicInfo(String info) {
        SwingUtilities.invokeLater(() -> queueTopicInfoArea.setText(info));
    }

    public void setResults(String results) {
        SwingUtilities.invokeLater(() -> resultsArea.setText(results));
    }
}
