package at.technikum_wien.swen3.paperless.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "document-exchange";

    public static final String QUEUE_NAME = "ocr-queue";
    public static final String OCR_RESULT_QUEUE_NAME = "ocr-result-queue";

    public static final String ROUTING_KEY = "document.ocr";
    public static final String OCR_RESULT_ROUTING_KEY = "document.ocr.result";

    public static final String GENAI_QUEUE_NAME = "genai-queue";
    public static final String GENAI_RESULT_QUEUE_NAME = "genai-result-queue";

    public static final String GENAI_ROUTING_KEY = "document.genai";
    public static final String GENAI_RESULT_ROUTING_KEY = "document.genai.result";

    public static final String SEARCH_QUEUE_NAME = "search-queue";
    public static final String SEARCH_ROUTING_KEY = "document.search";

    @Bean
    Queue queue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    Queue ocrResultQueue() {
        return new Queue(OCR_RESULT_QUEUE_NAME, false);
    }

    @Bean
    Queue genaiQueue() {
        return new Queue(GENAI_QUEUE_NAME, false);
    }

    @Bean
    Queue genaiResultQueue() {
        return new Queue(GENAI_RESULT_QUEUE_NAME, false);
    }

    @Bean
    Queue searchQueue() {
        return new Queue(SEARCH_QUEUE_NAME, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    Binding ocrResultBinding(Queue ocrResultQueue, TopicExchange exchange) {
        return BindingBuilder.bind(ocrResultQueue).to(exchange).with(OCR_RESULT_ROUTING_KEY);
    }

    @Bean
    Binding genaiBinding(Queue genaiQueue, TopicExchange exchange) {
        return BindingBuilder.bind(genaiQueue).to(exchange).with(GENAI_ROUTING_KEY);
    }

    @Bean
    Binding genaiResultBinding(Queue genaiResultQueue, TopicExchange exchange) {
        return BindingBuilder.bind(genaiResultQueue).to(exchange).with(GENAI_RESULT_ROUTING_KEY);
    }

    @Bean
    Binding searchBinding(Queue searchQueue, TopicExchange exchange) {
        return BindingBuilder.bind(searchQueue).to(exchange).with(SEARCH_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
