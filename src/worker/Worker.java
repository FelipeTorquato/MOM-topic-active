package worker;

import config.JmsConfig;
import gui.DashboardGUI;

import javax.jms.*;
import java.util.List;

public class Worker implements Runnable {
    private final List<String> palavrasChave;
    private final int workerId;
    private final DashboardGUI dashboardGUI;

    public Worker(int id, List<String> keywords, DashboardGUI dashboardGUI) {
        this.workerId = id;
        this.palavrasChave = keywords;
        this.dashboardGUI = dashboardGUI;
    }

    @Override
    public void run() {
        Connection connection = null;
        dashboardGUI.appendLog("Worker " + workerId + " iniciando e aguardando linhas...");
        try {
            ConnectionFactory factory = JmsConfig.getConnectionFactory();
            connection = factory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination filaDestino = session.createQueue(JmsConfig.QUEUE_LINHAS);
            MessageConsumer consumer = session.createConsumer(filaDestino);

            Destination topicoDestino = session.createTopic(JmsConfig.TOPIC_CONTAGEM);
            MessageProducer publisher = session.createProducer(topicoDestino);

            while (true) {
                Message message = consumer.receive(5000); // Bloqueia com timeout

                if (message == null) {
                    // Timeout, pode indicar que não há mais mensagens
                    continue;
                }

                if (message instanceof TextMessage) {
                    String linha = ((TextMessage) message).getText();

                    for (String chave : palavrasChave) {
                        int ocorrencias = contarOcorrencias(linha, chave.trim());
                        if (ocorrencias > 0) {
                            for (int i = 0; i < ocorrencias; i++) {
                                MapMessage mapMsg = session.createMapMessage();
                                mapMsg.setString("palavra", chave.trim());
                                mapMsg.setInt("qtd", 1); // Envia 1 por ocorrência
                                mapMsg.setInt("workerId", workerId);
                                publisher.send(mapMsg);
                            }
                        }
                    }
                }
            }
        } catch (JMSException e) {
            dashboardGUI.appendLog("ERRO no Worker " + workerId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
            dashboardGUI.appendLog("Worker " + workerId + " finalizado.");
        }
    }

    private int contarOcorrencias(String texto, String palavra) {
        if (palavra == null || palavra.trim().isEmpty()) {
            return 0;
        }
        int count = 0;
        String[] tokens = texto.split("\\W+"); // Split por não-palavras
        for (String t : tokens) {
            if (t.equalsIgnoreCase(palavra.trim())) {
                count++;
            }
        }
        return count;
    }
}
