package main;

import config.JmsConfig;
import gui.ClienteGUI;
import gui.DashboardGUI;
import producers.LeitorArquivo;
import subscriber.Dashboard;
import worker.Worker;

import javax.jms.*;
import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ClienteApp {

    public static final int NUM_WORKERS = 3;

    public void startApplication(String keywordInput, DashboardGUI dashboardGUI) {
        try {
            dashboardGUI.appendLog("Iniciando processo...");
            dashboardGUI.setQueueTopicInfo(
                    "FILA_LINHAS: Pronta para receber as linhas do arquivo.\n" +
                            "TOPICO_CONTAGEM: Aguardando publicação da contagem de palavras."
            );

            // Criar um arquivo para teste
            String arquivoNome = "arquivo_grande.txt";
            criarArquivoTeste(arquivoNome, dashboardGUI);

            // Obter palavras-chave da GUI
            List<String> keywords = Arrays.asList(keywordInput.split(","));
            dashboardGUI.appendLog("Palavras-chave a serem contadas: " + keywords);

            // Iniciar Dashboard (Subscriber)
            Thread dashboardThread = new Thread(new Dashboard(dashboardGUI, keywords));
            dashboardThread.start();

            // Iniciar Workers (Consumidores da Fila / Produtores do Tópico)
            Thread[] workerThreads = new Thread[NUM_WORKERS];
            for (int i = 0; i < NUM_WORKERS; i++) {
                workerThreads[i] = new Thread(new Worker(i + 1, keywords, dashboardGUI));
                workerThreads[i].start();
            }

            // Aguarda para garantir que todos conectaram ao Broker
            Thread.sleep(1500);

            // Iniciar Leitores (Produtores da Fila)
            Thread leitor1 = new Thread(new LeitorArquivo(arquivoNome, 1, dashboardGUI)); // Ímpar
            Thread leitor2 = new Thread(new LeitorArquivo(arquivoNome, 0, dashboardGUI)); // Par
            leitor1.start();
            leitor2.start();

            // Aguardar finalização dos leitores
            leitor1.join();
            leitor2.join();
            dashboardGUI.appendLog("Leitores de arquivo finalizaram o trabalho.");

            // Enviar "Poison Pill" para os workers
//            enviarPoisonPills(dashboardGUI);
//            dashboardGUI.appendLog("Sinal de finalização enviado para os Workers.");

            // Aguardar finalização dos workers
            for (Thread workerThread : workerThreads) {
                workerThread.join();
            }
            dashboardGUI.appendLog("Todos os Workers finalizaram.");
            dashboardGUI.setQueueTopicInfo(
                    "FILA_LINHAS: Processamento concluído. Mensagens consumidas.\n" +
                            "TOPICO_CONTAGEM: Contagem final publicada."
            );

            // Enviar mensagem de finalização para o Dashboard
            enviarMensagemFinalizacaoDashboard();
            dashboardGUI.appendLog("Processo concluído com sucesso!");

        } catch (IOException | InterruptedException | JMSException e) {
            e.printStackTrace();
            dashboardGUI.appendLog("ERRO: " + e.getMessage());
        }
    }
//
//    private void enviarPoisonPills(DashboardGUI dashboardGUI) throws JMSException {
//        ConnectionFactory factory = JmsConfig.getConnectionFactory();
//        try (Connection connection = factory.createConnection();
//             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
//            Destination destination = session.createQueue(JmsConfig.QUEUE_LINHAS);
//            MessageProducer producer = session.createProducer(destination);
//            for (int i = 0; i < NUM_WORKERS; i++) {
//                producer.send(session.createTextMessage("POISON_PILL"));
//            }
//        }
//    }

    private void enviarMensagemFinalizacaoDashboard() throws JMSException {
        ConnectionFactory factory = JmsConfig.getConnectionFactory();
        try (Connection connection = factory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            Destination destination = session.createTopic(JmsConfig.TOPIC_CONTAGEM);
            MessageProducer producer = session.createProducer(destination);
            Message finalizacaoMessage = session.createMessage();
            finalizacaoMessage.setBooleanProperty("finalizado", true);
            producer.send(finalizacaoMessage);
        }
    }

    private void criarArquivoTeste(String nome, DashboardGUI dashboardGUI) throws IOException {
        dashboardGUI.appendLog("Gerando arquivo de teste '" + nome + "'...");
        try (FileWriter fw = new FileWriter(nome)) {
            for (int i = 0; i < 1000; i++) {
                fw.write("Esta é a linha " + i + " contendo java e talvez mom.\n");
                fw.write("Outra linha com teste de mom e java.\n");
            }
        }
        dashboardGUI.appendLog("Arquivo de teste gerado.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClienteGUI().setVisible(true);
        });
    }
}
