package config;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.ConnectionFactory;

public class JmsConfig {
    // Endereço padrão do ActiveMQ local
    public static final String BROKER_URL = "tcp://localhost:61616";

    // Fila onde os Leitores (Produtores) colocam as linhas [cite: 31]
    public static final String QUEUE_LINHAS = "FILA_LINHAS";

    // Tópico onde os Workers (Publishers) publicam os resultados [cite: 27, 32]
    public static final String TOPIC_CONTAGEM = "TOPICO_PALAVRAS";

    public static ConnectionFactory getConnectionFactory() {
        return new ActiveMQConnectionFactory(BROKER_URL);
    }
}
