
package cz.osu.chatappbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "cz.osu.chatappbe.*" })
public class ChatAppBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatAppBeApplication.class, args);
	}

}
