package producers;

import config.JmsConfig;
import gui.DashboardGUI;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LeitorArquivo implements Runnable {
    private final String nomeArquivo;
    private final int restoDivisao;
    private final DashboardGUI dashboardGUI;
    private final String tipoLeitor;

    public LeitorArquivo(String nomeArquivo, int restoDivisao, DashboardGUI dashboardGUI) {
        this.nomeArquivo = nomeArquivo;
        this.restoDivisao = restoDivisao;
        this.dashboardGUI = dashboardGUI;
        this.tipoLeitor = (restoDivisao == 0 ? "Par" : "Ímpar");
    }

    @Override
    public void run() {
        dashboardGUI.appendLog("Leitor de linhas " + tipoLeitor + " iniciando...");
        Connection connection = null;
        try {
            ConnectionFactory factory = JmsConfig.getConnectionFactory();
            connection = factory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createQueue(JmsConfig.QUEUE_LINHAS);
            MessageProducer producer = session.createProducer(destination);

            try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
                String linha;
                int numeroLinha = 1;

                while ((linha = br.readLine()) != null) {
                    if (numeroLinha % 2 == restoDivisao) {
                        producer.send(session.createTextMessage(linha));
                    }
                    numeroLinha++;
                }
            }
            dashboardGUI.appendLog("Leitor de linhas " + tipoLeitor + " finalizou a leitura.");

        } catch (JMSException | IOException e) {
            dashboardGUI.appendLog("ERRO no Leitor " + tipoLeitor + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
