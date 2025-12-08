package gui;

import main.ClienteApp;

import javax.swing.*;
import java.awt.*;

public class ClienteGUI extends JFrame {

    private JTextField keywordField;
    private JButton startButton;

    public ClienteGUI() {
        setTitle("Cliente");
        setSize(500, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Painel para o formulário
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JLabel infoLabel = new JLabel("Digite as palavras-chave separadas por vírgula (ex: java,mom,teste):");
        keywordField = new JTextField(30);
        startButton = new JButton("Iniciar Processamento");

        formPanel.add(infoLabel);
        formPanel.add(keywordField);

        // Painel para o botão
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Ação do botão
        startButton.addActionListener(e -> {
            String keywords = keywordField.getText();
            if (keywords == null || keywords.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, insira pelo menos uma palavra-chave.", "Entrada Inválida", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Desabilita o botão para não iniciar duas vezes
            startButton.setEnabled(false);
            keywordField.setEnabled(false);

            // Inicia o Dashboard e o processo em background
            startApplication(keywords);
        });
    }

    private void startApplication(String keywords) {
        // A GUI do Dashboard é criada e mostrada
        DashboardGUI dashboardGUI = new DashboardGUI();
        dashboardGUI.setVisible(true);

        // A lógica da aplicação principal é iniciada em uma nova thread
        // para não congelar a GUI do Cliente.
        ClienteApp clienteApp = new ClienteApp();
        new Thread(() -> clienteApp.startApplication(keywords, dashboardGUI)).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClienteGUI().setVisible(true);
        });
    }
}
