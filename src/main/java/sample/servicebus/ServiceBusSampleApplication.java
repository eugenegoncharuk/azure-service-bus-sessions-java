/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package sample.servicebus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
/**
 * https://gist.github.com/Buildstarted/3354942
 */
public class ServiceBusSampleApplication implements CommandLineRunner {

    private static QueueClient requestClient = null;
    private static QueueClient replyQueueClient = null;

    public static final String REQUEST_CON_STRING = "";

    public static final String REPLY_CON_STRING = "";

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws ServiceBusException, InterruptedException {
        SpringApplication.run(ServiceBusSampleApplication.class);
    }

    public void run(String... var1) throws ServiceBusException, InterruptedException, JsonProcessingException, TimeoutException, ExecutionException {
        sendQueueMessage();
    }

    // NOTE: Please be noted that below are the minimum code for demonstrating the usage of autowired clients.
    // For complete documentation of Service Bus, reference https://azure.microsoft.com/en-us/services/service-bus/
    private void sendQueueMessage() throws ServiceBusException, InterruptedException, JsonProcessingException, ExecutionException, TimeoutException {
        requestClient = new QueueClient(new ConnectionStringBuilder(REQUEST_CON_STRING), ReceiveMode.RECEIVEANDDELETE);
        replyQueueClient = new QueueClient(new ConnectionStringBuilder(REPLY_CON_STRING), ReceiveMode.RECEIVEANDDELETE);

        requestClient.registerMessageHandler(new DestinationMessageHandler());

        int iterations = 0;
        while (iterations != 100) {
            iterations++;

            String sessionId = UUID.randomUUID().toString();
            IMessageSession mySession = ClientFactory.acceptSessionFromConnectionStringBuilder(new ConnectionStringBuilder(REPLY_CON_STRING), sessionId, ReceiveMode.RECEIVEANDDELETE);

            final CommandToSend command = new CommandToSend("data from iteration " + iterations, new Date());
            System.out.println("Sending message: " + command);

            final Message message = new Message(objectMapper.writeValueAsBytes(command));
            message.setReplyTo(replyQueueClient.getEntityPath());
            message.setReplyToSessionId(sessionId);
            message.setSessionId(sessionId);
            message.setTimeToLive(Duration.ofSeconds(10));

            requestClient.send(message);
            IMessage receiovedMessage = mySession.receive(Duration.ofSeconds(10));

            System.out.println("=================================" + new String(receiovedMessage.getBody()));
        }
    }

    static class DestinationMessageHandler implements IMessageHandler {
        @Override
        public CompletableFuture<Void> onMessageAsync(IMessage message) {
            Message messageReply = new Message("Test");
            messageReply.setSessionId(message.getReplyToSessionId());
            messageReply.setTimeToLive(Duration.ofSeconds(10));
            return replyQueueClient.sendAsync(messageReply);
        }

        @Override
        public void notifyException(Throwable exception, ExceptionPhase phase) {
            System.out.println(phase + " encountered exception:" + exception.getMessage());
        }
    }
}
