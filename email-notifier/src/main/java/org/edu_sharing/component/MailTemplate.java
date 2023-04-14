package org.edu_sharing.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class MailTemplate {

    private final DocumentBuilderFactory documentBuilderFactory;
    private final XPathFactory xPathFactory;

    public String getSubject(String template, String locale) {
        return getTemplateContent(getTemplates(locale), template, "subject");
    }

    public String getContent(String template, String locale, boolean addFooter) {
        Map<String, Node> templates = getTemplates(locale);
        StringBuilder stringBuilder = new StringBuilder()
                .append("<style>")
                .append(getTemplateContent(getTemplates(null), "stylesheet", "style"))
                .append("</style>")
                .append(getTemplateContent(templates, "header", "message"))
                .append("<div class='content'>")
                .append(getTemplateContent(templates, template, "message"))
                .append("</div>");

        if (addFooter) {
            stringBuilder
                    .append("<div class='footer'>")
                    .append(getTemplateContent(templates, "footer", "message"))
                    .append("</div>");
        }

        return stringBuilder.toString();
    }


    private String getTemplateContent(Map<String, Node> templates, String template, String name) {
        return Optional.of(templates)
                .map(x -> x.get(template))
                .map(Node::getChildNodes)
                .flatMap(x -> IntStream.range(0, x.getLength())
                        .mapToObj(x::item)
                        .filter(y -> y.getNodeName().equals(name))
                        .map(Node::getTextContent)
                        .findFirst())
                .orElseThrow(() -> new TemplateException(String.format("Template %s doesn't contains %s", template, name)));
    }

    private Map<String, Node> getTemplates(String locale) {
        Document base = getTemplateResource(locale);
        try {
            NodeList templates = (NodeList) xPathFactory.newXPath().evaluate("/templates/template", base, XPathConstants.NODESET);
            return IntStream.range(0, templates.getLength())
                    .mapToObj(templates::item)
                    .collect(Collectors.toMap(
                            x -> x.getAttributes().getNamedItem("name").getTextContent(),
                            x -> x));
        } catch (XPathExpressionException e) {
            throw new TemplateException(e);
        }
    }

    private Document getTemplateResource(String locale) {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream in = Optional.ofNullable(classLoader.getResourceAsStream(String.format("mailtemplates/templates_%s.xml", locale)))
                .orElse(classLoader.getResourceAsStream("mailtemplates/templates.xml"));

        try {
            return documentBuilderFactory.newDocumentBuilder().parse(in);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new TemplateException(e);
        }
    }
}
