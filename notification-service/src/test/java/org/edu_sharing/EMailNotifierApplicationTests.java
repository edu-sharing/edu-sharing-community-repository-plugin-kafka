package org.edu_sharing;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.edu_sharing.notification.data.Collection;
import org.edu_sharing.notification.event.*;
import org.edu_sharing.notification.data.*;
import org.edu_sharing.service.EmailService;
import org.edu_sharing.userData.UserData;
import org.edu_sharing.userData.UserDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EMailNotifierApplicationTests {

    @Value("${spring.mail.send.address}")
    private String mailSendAddress;

    @Value("${spring.mail.report.address}")
    private String mailReportAddress;

    @Value("${spring.application.name}")
    private String applicationName;

    @MockBean
    private UserDataRepository userDataRepository;

    @Autowired
    private EmailService underTest;

//    @Mock
//    JavaMailSender javaMailSender;
//    @BeforeEach
//    void setUp() {
//        underTest = new EmailService(javaMailSender, );
//    }

    Map<String, UserData> userDataDB = Map.of(
            "receiver", new UserData("receiver", "Lenny", "Linux", "lenny.linux@example.com", "de-DE"),
            "william", new UserData("william", "William", "Windows", "william.windows@example.com", "de-DE"),
            "johansson", new UserData("johansson", "Scala", "Johansson", "scala.johansson@example.com", "de-DE")
            );

    @BeforeEach
    public void init(){
        lenient().when(userDataRepository.findById("receiver")).thenReturn(Optional.ofNullable(userDataDB.get("receiver")));
        lenient().when(userDataRepository.findById("william")).thenReturn(Optional.ofNullable(userDataDB.get("william")));
        lenient().when(userDataRepository.findById("johansson")).thenReturn(Optional.ofNullable(userDataDB.get("johansson")));
    }

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("user", "admin"));

    public static List<List<NotificationEvent>> provideSingleEventMessages() {
        NodeData nodeData = NodeData.builder()
                .property("cm_name", "Some Material")
                .property("link", "www.example.de/some_material")
                .build();

        return Arrays.asList(
                Collections.singletonList(InviteEvent.builder()
                        .type("invited")
                        .name("test")
                        .creatorId("william")
                        .receiverId("receiver")
                        .permission(new Permission("read", "lesen"))
                        .node(nodeData)
                        .build()),
                Collections.singletonList(InviteEvent.builder()
                        .type("invited_safe")
                        .name("test")
                        .creatorId("william")
                        .receiverId("receiver")
                        .permission(new Permission("read", "schreiben"))
                        .node(nodeData)
                        .build()),
                Collections.singletonList(InviteEvent.builder()
                        .type("invited_collection")
                        .name("test")
                        .creatorId("william")
                        .receiverId("receiver")
                        .permission(new Permission("read", "lesen"))
                        .node(nodeData)
                        .build()),
                Collections.singletonList(NodeIssueEvent.builder()
                        .creatorId("william")
                        .receiverId("receiver")
                        .reason("Some reason")
                        .userComment("Some comment")
                        .node(nodeData)
                        .build()),
                Collections.singletonList(WorkflowEvent.builder()
                        .creatorId("william")
                        .receiverId("receiver")
                        .node(nodeData)
                        .workflowStatus("Some status")
                        .userComment("Some comment")
                        .build()),
                Collections.singletonList(MetadataSuggestionEvent.builder()
                        .creatorId("william")
                        .receiverId("receiver")
                        .node(nodeData)
                        .captionId("123")
                        .caption("Some Caption")
                        .parentCaption("Some Parent Caption")
                        .parentId("456")
                        .widget(WidgetData.builder()
                                .id("789")
                                .caption("Some Widget Caption")
                                .build())
                        .build()),
                Collections.singletonList(CommentEvent.builder()
                        .creatorId("william")
                        .receiverId("receiver")
                        .node(nodeData)
                        .id("0815")
                        .commentContent("Some comment")
                        .commentReference(null)
                        .event("added")
                        .build()),
                Collections.singletonList(AddToCollectionEvent.builder()
                        .creatorId("william")
                        .receiverId("receiver")
                        .node(nodeData)
                        .collection(Collection.builder()
                                .property("cm_name", "Some Collection")
                                .property("link", "www.example.de/some_collection")
                                .build())
                        .build()),
                Collections.singletonList(RatingEvent.builder()
                        .creatorId("william")
                        .receiverId("receiver")
                        .node(nodeData)
                        .newRating(4)
                        .ratingSum(45)
                        .ratingCount(10)
                        .build())
        );
    }

    public static List<List<NotificationEvent>> provideMultiEventMessages() {
        NodeData nodeData1 = NodeData.builder()
                .property("cm_name", "Some Material")
                .property("link", "www.example.de/some_material")
                .build();

        NodeData nodeData2 = NodeData.builder()
                .property("cm_name", "Some other Material")
                .property("link", "www.example.de/some_other_material")
                .build();

        return Arrays.asList(
                Arrays.asList(
                        InviteEvent.builder()
                                .type("invited")
                                .name("test")
                                .creatorId("william")
                                .receiverId("receiver")
                                .permission(new Permission("read", "lesen"))
                                .node(nodeData1)
                                .build(),
                        InviteEvent.builder()
                                .type("invited_collection")
                                .name("test")
                                .creatorId("johansson")
                                .receiverId("receiver")
                                .permission(new Permission("write", "schreiben"))
                                .node(nodeData2)
                                .build()),

                Arrays.asList(
                        InviteEvent.builder()
                                .type("invited_safe")
                                .name("test")
                                .creatorId("william")
                                .receiverId("receiver")
                                .permission(new Permission("read", "lesen"))
                                .node(nodeData1)
                                .build(),
                        InviteEvent.builder()
                                .type("invited_safe")
                                .name("test")
                                .creatorId("johansson")
                                .receiverId("receiver")
                                .permission(new Permission("write", "schreiben"))
                                .node(nodeData2)
                                .build()),

                Arrays.asList(
                        NodeIssueEvent.builder()
                                .creatorId("william")
                                .receiverId("receiver")
                                .reason("Some reason")
                                .userComment("Some comment")
                                .node(nodeData1)
                                .build(),
                        NodeIssueEvent.builder()
                                .creatorId("johansson")
                                .receiverId("receiver")
                                .reason("Some other reason")
                                .userComment("Some other comment")
                                .node(nodeData2)
                                .build()),
                Arrays.asList(
                        WorkflowEvent.builder()
                                .creatorId("william")
                                .receiverId("receiver")
                                .node(nodeData1)
                                .workflowStatus("Some status")
                                .userComment("Some comment")
                                .build(),
                        WorkflowEvent.builder()
                                .creatorId("johansson")
                                .receiverId("receiver")
                                .node(nodeData2)
                                .workflowStatus("Some other status")
                                .userComment("Some other comment")
                                .build()),
                Arrays.asList(
                        MetadataSuggestionEvent.builder()
                                .creatorId("william")
                                .receiverId("receiver")
                                .node(nodeData1)
                                .captionId("123")
                                .caption("Some Caption")
                                .parentCaption("Some Parent Caption")
                                .parentId("456")
                                .widget(WidgetData.builder()
                                        .id("789")
                                        .caption("Some Widget Caption")
                                        .build())
                                .build(),
                        MetadataSuggestionEvent.builder()
                                .creatorId("johansson")
                                .receiverId("receiver")
                                .node(nodeData1)
                                .captionId("987")
                                .caption("Some Other Caption")
                                .parentCaption("Some Other Parent Caption")
                                .parentId("654")
                                .widget(WidgetData.builder()
                                        .id("321")
                                        .caption("Some Other Widget Caption")
                                        .build())
                                .build()),
                Arrays.asList(
                        CommentEvent.builder()
                                .creatorId("william")
                                .receiverId("receiver")
                                .node(nodeData1)
                                .commentContent("Some comment")
                                .commentReference(null)
                                .event("added")
                                .build(),

                        CommentEvent.builder()
                                .creatorId("johansson")
                                .receiverId("receiver")
                                .node(nodeData2)
                                .commentContent("Some other comment")
                                .commentReference(null)
                                .event("added")
                                .build()),
                Arrays.asList(
                        AddToCollectionEvent.builder()
                                .creatorId("william")
                                .receiverId("receiver")
                                .node(nodeData1)
                                .collection(Collection.builder()
                                        .property("cm_name", "Some collection")
                                        .property("link", "www.example.de/some_collection")
                                        .build())
                                .build(),

                        AddToCollectionEvent.builder()
                                .creatorId("johansson")
                                .receiverId("receiver")
                                .node(nodeData2)
                                .collection(Collection.builder()
                                        .property("cm_name", "Some other collection")
                                        .property("link", "www.example.de/some_other_collection")
                                        .build())
                                .build()),
                Arrays.asList(
                        RatingEvent.builder()
                                .creatorId("william")
                                .receiverId("receiver")
                                .node(nodeData1)
                                .newRating(4)
                                .ratingSum(45)
                                .ratingCount(10)
                                .build(),
                        RatingEvent.builder()
                                .creatorId("johansson")
                                .receiverId("receiver")
                                .node(nodeData2)
                                .newRating(3)
                                .ratingSum(35)
                                .ratingCount(10)
                                .build())
        );
    }


    @ParameterizedTest
    @MethodSource({"provideSingleEventMessages", "provideMultiEventMessages"})
    void doesThymeleafCompileAndSendMail(List<NotificationEvent> data) throws MessagingException {
        underTest.send(data);
        assertEquals(1, greenMail.getReceivedMessages().length);
        Iterator<MimeMessage> iterator = Arrays.stream(greenMail.getReceivedMessages()).iterator();
        MimeMessage receivedMessage = iterator.next();
        assertEquals(1, receivedMessage.getAllRecipients().length);
        assertEquals(userDataDB.get(data.get(0).getReceiverId()).getEmail(), receivedMessage.getAllRecipients()[0].toString());
        assertEquals(mailSendAddress, receivedMessage.getFrom()[0].toString());
        log.info("Subject: {}", receivedMessage.getSubject());
        log.info("Body: {}", GreenMailUtil.getBody(receivedMessage));
        //assertEquals("Message from Java Mail Sender", receivedMessage.getSubject());
        //assertEquals("Hello this is a simple email message", GreenMailUtil.getBody(receivedMessage));
    }
}
