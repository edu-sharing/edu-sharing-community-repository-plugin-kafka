package org.edu_sharing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

@Configuration
public class EMailConfig {
    @Bean
    public XPathFactory xPathFactory(){
        return XPathFactory.newInstance();
    }

    @Bean
    public DocumentBuilderFactory documentBuilderFactory(){
        return DocumentBuilderFactory.newInstance();
    }

}
