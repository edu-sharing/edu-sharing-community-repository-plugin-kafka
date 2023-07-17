package org.edu_sharing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.edu_sharing.notification.data.Status;
import org.edu_sharing.notification.event.NotificationEvent;
import org.edu_sharing.userData.UserData;
import org.edu_sharing.userData.UserDataRepository;
import org.edu_sharing.userData.UserDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements NotificationService {
    private final JavaMailSender emailSender;
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;
    private final UserDataService userDataService;

    @Value("${spring.mail.send.address}")
    private String mailSendAddress;


    @Override
    public void send(List<NotificationEvent> notificationEvents) {
        Map<Pair<? extends Class<? extends NotificationEvent>, String>, List<NotificationEvent>> groupedNotificationEvents = notificationEvents.stream().collect(groupingBy(x -> new ImmutablePair<>(x.getClass(), x.getReceiverId())));
        groupedNotificationEvents.forEach((group, notifications) -> {
            if (notifications.size() == 1) {
                sendSingleNotification(notifications.get(0));
            } else {
                sendGroupedNotifications(group.getRight(), notifications, group.getLeft());
            }
        });
    }

    @SuppressWarnings("unchecked assignment")
    public void sendGroupedNotifications(String receiverId, List<? extends NotificationEvent> notificationEvents, Class<? extends NotificationEvent> notificationClass) {
        Optional<UserData> userData = userDataService.getUserData(receiverId);
        if (userData.isEmpty()) {
            log.warn("Cant send notification to unknown user id: {}", receiverId);
            return;
        }

        String messageType = getMessageType(notificationClass);
        final Context ctx = new Context(LocaleUtils.toLocale(userData.get().getLocale()));
        ctx.setVariable("events", notificationEvents.stream()
                .map(x -> objectMapper.convertValue(x, Map.class))
                .peek(x -> this.resolveUserId(x, "creatorId"))
                .peek(x -> this.resolveUserId(x, "receiverId"))
                .map(this::replaceInvalidSymbols)
                .collect(Collectors.toList()));
        ctx.setVariable("receiver", userData.get());
        ctx.setVariable("template", "html/multiple/" + messageType);

        try {
            String content = templateEngine.process("html/baseLayout.html", ctx);
            String subject = templateEngine.process(String.format("text/multiple/%s.txt", messageType), ctx);

            sendHtmlMessage(userData.get().getEmail(), subject, content);
            notificationEvents.forEach(x -> x.setStatus(Status.SENT));
        } catch (Exception ex) {
            log.error("Error fail to send {}s emails to user {}, coursed by {}", notificationClass.getSimpleName(), receiverId, ex.getMessage(), ex);
        }
    }

    private Object replaceInvalidSymbols(Object data) {
        if (data instanceof Map) {
            HashMap<String, Object> map = (HashMap<String, Object>) data;
            return map.entrySet().stream()
                    .map(x -> new ImmutablePair<>(x.getKey().replaceAll("[:.]", "_"), replaceInvalidSymbols(x.getValue())))
                    .collect(HashMap::new, (m,v)-> m.put(v.getKey(), v.getValue()), HashMap::putAll); // Collectors.toMap() produces a NullPointerException for values

        } else if (data instanceof List) {
            List<Object> list = (List<Object>) data;
            return list.stream()
                    .map(this::replaceInvalidSymbols)
                    .collect(Collectors.toList());
        } else {
            return data;
        }
    }

    public void sendSingleNotification(NotificationEvent notificationEvent) {
        Optional<UserData> userData = userDataService.getUserData(notificationEvent.getReceiverId());
        if (userData.isEmpty()) {
            log.warn("Cant send notification to unknown user id: {}", notificationEvent.getReceiverId());
            return;
        }

        String messageType = getMessageType(notificationEvent.getClass());
        final Context ctx = new Context(LocaleUtils.toLocale(userData.get().getLocale()));
        Map<String, Object> data = objectMapper.convertValue(notificationEvent, Map.class);
        this.resolveUserId(data, "creatorId");
        this.resolveUserId(data, "receiverId");
        data = (Map<String, Object>)replaceInvalidSymbols(data);

        ctx.setVariables(data);
        ctx.setVariable("template", "html/single/" + messageType);

        try {
            String content = templateEngine.process("html/baseLayout.html", ctx);
            String subject = templateEngine.process(String.format("text/single/%s.txt", messageType), ctx);

            sendHtmlMessage(userData.get().getEmail(), subject, content);
            notificationEvent.setStatus(Status.SENT);
        } catch (Exception ex) {
            log.error("Error fail to send {}s emails to user {}, coursed by {}", notificationEvent.getClass().getSimpleName(), notificationEvent.getReceiverId(), ex.getMessage(), ex);
        }
    }

    private void resolveUserId(Map<String, Object> data, String userId) {
        Optional<UserData> userData = userDataService.getUserData((String) data.get(userId));
        data.remove(userId);
        userData.ifPresent(value -> data.put(userId.replace("Id", ""), objectMapper.convertValue(value, Map.class)));
    }

    private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {
        log.info("send email to {} with subject {}", to, subject);

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailSendAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        helper.addInline("logo.png", new ClassPathResource("mail/edu-sharing-mail.png"));
        emailSender.send(message);
    }

    private String getMessageType(Class<? extends NotificationEvent> event) {
        return event.getSimpleName();
    }
}
