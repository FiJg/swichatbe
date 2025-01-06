package cz.osu.chatappbe.services.utility;

import cz.osu.chatappbe.config.RabbitMQConfig;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQService {
	@Autowired
	private AmqpAdmin admin;
	
	public void createQueue(String queueName) {
		Queue queue = new Queue(queueName, false);
		Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, RabbitMQConfig.exchange, "", null);
		admin.declareQueue(queue);
		admin.declareBinding(binding);
	}
}
