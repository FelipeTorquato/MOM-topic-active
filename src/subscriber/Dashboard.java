package subscriber;

import config.JmsConfig;
import gui.DashboardGUI;

import javax.jms.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Dashboard implements Runnable {
    private final DashboardGUI dashboardGUI;
    private final Map<String, Integer> estatisticas = new HashMap<>();
    private final CountDownLatch finalizacaoLatch = new CountDownLatch(1);

    public Dashboard(DashboardGUI dashboardGUI, List<String> keywords) {
        this.dashboardGUI = dashboardGUI;
        // Inicializa as estatísticas com 0 para todas as palavras-chave passadas
        for (String keyword : keywords) {
            estatisticas.put(keyword.trim(), 0);
        }
    }

    @Override
    public void run() {
        Connection connection = null;
        try {
            dashboardGUI.appendLog("Dashboard iniciando...");
            ConnectionFactory factory = JmsConfig.getConnectionFactory();
            connection = factory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination topic = session.createTopic(JmsConfig.TOPIC_CONTAGEM);
            MessageConsumer subscriber = session.createConsumer(topic);
            dashboardGUI.appendLog("Dashboard inscrito no tópico '" + JmsConfig.TOPIC_CONTAGEM + "'. Aguardando dados...");

            subscriber.setMessageListener(message -> {
                try {
                    if (message.propertyExists("finalizado") && message.getBooleanProperty("finalizado")) {
                        dashboardGUI.appendLog("Dashboard recebeu sinal de finalização de contagem.");
                        finalizacaoLatch.countDown(); // Libera a thread principal do Dashboard
                        return;
                    }

                    if (message instanceof MapMessage) {
                        MapMessage mapMsg = (MapMessage) message;
                        String palavra = mapMsg.getString("palavra");
                        int qtd = mapMsg.getInt("qtd");
                        int workerId = mapMsg.getInt("workerId");

                        synchronized (estatisticas) {
                            estatisticas.put(palavra, estatisticas.getOrDefault(palavra, 0) + qtd);
                            dashboardGUI.appendLog("Worker " + workerId + " encontrou " + qtd + " ocorrência(s) de '" + palavra + "'.");
                            atualizarGUITotais();
                        }
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            });

            // Aguarda até que a mensagem de finalização seja recebida
            finalizacaoLatch.await();

        } catch (Exception e) {
            dashboardGUI.appendLog("ERRO no Dashboard: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
            dashboardGUI.appendLog("Dashboard finalizado.");
        }
    }

    private void atualizarGUITotais() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- CONTAGEM ATUAL ---\n");
        estatisticas.forEach((palavra, total) ->
                sb.append(String.format("Palavra: %s | Total: %d\n", palavra, total))
        );
        sb.append("----------------------");
        dashboardGUI.setResults(sb.toString());
    }
}
