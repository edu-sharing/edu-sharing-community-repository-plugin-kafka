package org.edu_sharing;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.kafka.notification.events.*;
import org.edu_sharing.kafka.notification.events.data.NodeData;
import org.edu_sharing.kafka.notification.events.data.UserInfo;
import org.edu_sharing.kafka.notification.events.data.WidgetData;
import org.edu_sharing.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EMailNotifierApplicationTests {

    @Autowired
    private EmailService underTest;

//    @Mock
//    JavaMailSender javaMailSender;
//    @BeforeEach
//    void setUp() {
//        underTest = new EmailService(javaMailSender, );
//    }


    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("user", "admin"));
    //.withPerMethodLifecycle(false);


    public static List<List<NotificationEventDTO>> provideEventMessages() {
        UserInfo creator = UserInfo.builder()
                .firstName("William")
                .lastName("Windows")
                .email("william.windows@example.com")
                .locale("de-DE")
                .build();

        UserInfo receiver = UserInfo.builder()
                .firstName("Lenny")
                .lastName("Linux")
                .email("lenny.linux@example.com")
                .locale("de-DE")
                .build();

        NodeData nodeData = NodeData.builder()
                .property("cm_name", "Some Node")
                .property("link", "www.example.de")
                .build();

        return Arrays.asList(
                Collections.singletonList(InviteEventDTO.builder()
                        .type("invited")
                        .name("test")
                        .id("0815")
                        .creator(creator)
                        .receiver(receiver)
                        .permission("read")
                        .node(nodeData)
                        .build()),
                Collections.singletonList(InviteEventDTO.builder()
                        .type("invited_safe")
                        .name("test")
                        .id("0815")
                        .creator(creator)
                        .receiver(receiver)
                        .permission("read")
                        .node(nodeData)
                        .build()),
                Collections.singletonList(InviteEventDTO.builder()
                        .type("invited_collection")
                        .name("test")
                        .id("0815")
                        .creator(creator)
                        .receiver(receiver)
                        .permission("read")
                        .node(nodeData)
                        .build()),
                Collections.singletonList(NodeIssueEventDTO.builder()
                        .creator(creator)
                        .receiver(receiver)
                        .reason("Some reason")
                        .userComment("Some comment")
                        .node(nodeData)
                        .build()),
                Collections.singletonList(WorkflowEventDTO.builder()
                        .creator(creator)
                        .receiver(receiver)
                        .node(nodeData)
                        .workflowStatus("Some status")
                        .userComment("Some comment")
                        .build()),
                Collections.singletonList(MetadataSuggestionEventDTO.builder()
                        .creator(creator)
                        .receiver(receiver)
                        .node(nodeData)
                        .id("1234")
                        .caption("Some Caption")
                        .parentCaption("Some Parent Caption")
                        .parentId("0815")
                        .widget(WidgetData.builder()
                                .id("4411")
                                .caption("Some Widget Caption")
                                .build())
                        .build())
        );
    }

    @ParameterizedTest
    @MethodSource("provideEventMessages")
    void doesThymeleafCompileAndSendMail(List<NotificationEventDTO> data) throws MessagingException {
        underTest.send(data);

        assertEquals(data.size(), greenMail.getReceivedMessages().length);
        Iterator<MimeMessage> iterator = Arrays.stream(greenMail.getReceivedMessages()).iterator();
        for (NotificationEventDTO item : data) {
            MimeMessage receivedMessage = iterator.next();
            assertEquals(1, receivedMessage.getAllRecipients().length);
            assertEquals(item.getReceiver().getEmail(), receivedMessage.getAllRecipients()[0].toString());
            assertEquals(item.getCreator().getEmail(), receivedMessage.getFrom()[0].toString());
            log.info("Subject: {}", receivedMessage.getSubject());
            log.info("Body: {}", GreenMailUtil.getBody(receivedMessage));
            //assertEquals("Message from Java Mail Sender", receivedMessage.getSubject());
            //assertEquals("Hello this is a simple email message", GreenMailUtil.getBody(receivedMessage));
        }
    }

}
