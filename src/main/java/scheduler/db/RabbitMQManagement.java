package scheduler.db;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQManagement {

    private final String EXCHANGE_NAME = "sft-device-scheduler";
    private static Exchange exchange = null;
    
    @Value("${spring.rabbitmq.host}")
    private String host;  
  
    @Value("${spring.rabbitmq.apiport}")
    private String port;
  
    @Value("${spring.rabbitmq.username}")
    private String username;  
  
    @Value("${spring.rabbitmq.password}")
    private String password;
    
    
    @Autowired
    RabbitTemplate rabbitTemplate;
    
    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
    ConnectionFactory connectionFactory;
    
    private RabbitManagementTemplate rabbitManagementTemplate = null;
    
    private RabbitAdmin rabbitAdmin = null;
    


    public RabbitMQManagement() {
        
    }
    
    public void initialize() {
    	if(rabbitManagementTemplate == null) {
    		String uri = "http://" + host + ":" + port + "/api/";
    		rabbitManagementTemplate = new RabbitManagementTemplate(uri, username, password);
    	}
    	
    	if(rabbitAdmin == null) {
    		ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();
    		rabbitAdmin = new RabbitAdmin(connectionFactory);
    		
    	}
    	if(exchange == null) {
	        exchange = ExchangeBuilder.directExchange(EXCHANGE_NAME).durable(true).build();
	        rabbitAdmin.declareExchange(exchange);
	          
	    }

    }
    
    
    public List<Queue> getQueueList(){
        List<Queue> queues = rabbitManagementTemplate.getQueues();
        return queues;
    	
    }

    void addQueue(String queueName) {
        Queue queue = QueueBuilder.durable(queueName).build();
        BindingBuilder.bind(queue).to(exchange).with(queueName);
        rabbitAdmin.declareQueue(queue);
    }	
    
}
