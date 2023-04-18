package org.edu_sharing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.kafka.notification.events.NotificationEventDTO;
import org.edu_sharing.kafka.notification.events.data.UserInfo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    private final ObjectMapper objectMapper;

    private final TemplateEngine templateEngine;

    @SuppressWarnings("unchecked")
    public void send(List<NotificationEventDTO> events) throws MessagingException {
        for (NotificationEventDTO event : events) {
            UserInfo receiver = event.getReceiver();
            String messageType = getMessageType(event);

            final Context ctx = new Context(new Locale(receiver.getLocale()));
            ctx.setVariables(objectMapper.convertValue(event, Map.class));
            ctx.setVariable("template", messageType);
            try {
                String content = templateEngine.process("html/baseLayout.html", ctx);
                String subject = templateEngine.process(String.format("text/%s.txt", messageType), ctx);

                sendHtmlMessage(event.getCreator().getEmail(), receiver.getEmail(), subject, content);
            } catch (Exception ex) {
                log.warn(String.format("Event: %s throws: %s", event, ex.getMessage()),ex);
            }
        }
    }

    private void sendHtmlMessage(String from, String to, String subject, String htmlBody) throws MessagingException {
        log.info("send email to {} with subject {}", to, subject);

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        helper.addInline("logo.png", new ClassPathResource("mail/edu-sharing-mail.png"));
        emailSender.send(message);
    }

    private String getMessageType(NotificationEventDTO event) {
        SerializationConfig config = objectMapper.getSerializationConfig();
        AnnotatedClass annotatedClass = AnnotatedClassResolver.resolve(config, config.constructType(event.getClass()), new SimpleMixInResolver(null));
        Collection<NamedType> namedTypes = objectMapper.getSubtypeResolver().collectAndResolveSubtypesByClass(config, annotatedClass);

        return namedTypes.stream().filter(x -> x.getType() == event.getClass())
                .map(NamedType::getName)
                .findFirst()
                .orElse(event.getClass().getSimpleName().replace("Event", "Message").replace("DTO", ""));
    }
}
