package config;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.ConnectionFactory;

public class JmsConfig {
    // Endereço padrão do ActiveMQ local
    public static final String BROKER_URL = "tcp://localhost:61616";

    // Fila onde os Leitores colocam as linhas
    public static final String QUEUE_LINHAS = "FILA_LINHAS";

    // Tópico onde os Workers publicam os resultados
    public static final String TOPIC_CONTAGEM = "TOPICO_PALAVRAS";

    public static ConnectionFactory getConnectionFactory() {
        return new ActiveMQConnectionFactory(BROKER_URL);
    }
}
