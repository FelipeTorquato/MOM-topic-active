package main;

import config.JmsConfig;
import gui.ClienteGUI;
import gui.DashboardGUI;
import producers.LeitorArquivo;
import subscriber.Dashboard;
import worker.Worker;

import javax.jms.*;
import javax.swing.*;
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

            // Ler o arquivo grande
            String arquivoNome = "wiki_active_texto_grande.txt";

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

        } catch (InterruptedException | JMSException e) {
            e.printStackTrace();
            dashboardGUI.appendLog("ERRO: " + e.getMessage());
        }
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClienteGUI().setVisible(true);
        });
    }
}
