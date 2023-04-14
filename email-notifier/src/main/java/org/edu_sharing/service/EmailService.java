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
import org.edu_sharing.component.MailTemplate;
import org.edu_sharing.component.TemplateException;
import org.edu_sharing.kafka.notification.events.NotificationEventDTO;
import org.edu_sharing.kafka.notification.events.data.UserInfo;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    private final MailTemplate mailTemplate;
    private final ObjectMapper objectMapper;

    public void send(List<NotificationEventDTO> events) throws MessagingException {
        for (NotificationEventDTO event : events) {
            UserInfo receiver = event.getReceiver();
            String messageType = getMessageType(event);

            try {
                String subject = mailTemplate.getSubject(messageType, receiver.getLocale());
                String content = mailTemplate.getContent(messageType, receiver.getLocale(), true);
                sendHtmlMessage(event.getCreator().getEmail(), receiver.getEmail(), subject, content);
            } catch (TemplateException ex) {
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
