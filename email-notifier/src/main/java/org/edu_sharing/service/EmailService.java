package org.edu_sharing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.domain.UserSettings;
import org.edu_sharing.kafka.notification.events.NotificationEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    private final UserSettingsService userSettings;
    private final SpringTemplateEngine templateEngine;

    @Value("${mail.settings.from}")
    private String fromEmailAddress;

    public void send(NotificationEventDTO event) throws MessagingException {
        Optional<UserSettings> receiverSetting = userSettings.getUserSetting(event.getReceiver().getId());
        if (receiverSetting.isEmpty()) {
            log.info("User does not exists {}", event.getReceiver());
            return;
        }

        if (receiverSetting.map(UserSettings::getDisabledMessageTypes).map(x->x.contains(getMessageType(event))).orElse(false)) {
            log.info("{} disabled for {}", getMessageType(event), event.getReceiver());
            return;
        }

        String messageType = getMessageType(event);
        String body = createContentFromTemplate(event, "html/" + messageType + ".html");
        //String subject = createContentFromTemplate(event, "txt/" + messageType + ".txt");
        String subject = "edu-sharing notification";

        sendHtmlMessage(receiverSetting.get().getEmailAddress(), subject, body);
    }

    private String createContentFromTemplate(NotificationEventDTO event, String template) {
        Context mailContext = new Context();
        ObjectMapper model = new ObjectMapper();
        mailContext.setVariables(model.convertValue(event, Map.class));
        return templateEngine.process(template, mailContext);
    }

    private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
        log.info("send email to {} with subject {}", to, subject);

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmailAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        emailSender.send(message);
    }

    private static String getMessageType(NotificationEventDTO event) {
        return event.getClass().getSimpleName().replace("Event", "Message");
    }
}
