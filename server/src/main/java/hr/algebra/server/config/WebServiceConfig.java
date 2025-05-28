package hr.algebra.server.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "sports")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema sportsSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("SportPort");
        wsdl11Definition.setLocationUri("/ws");

        wsdl11Definition.setTargetNamespace("https://interoperabilnost.hr/sport");
        wsdl11Definition.setSchema(sportsSchema);
        return wsdl11Definition;
    }
    @Bean
    public XsdSchema sportsSchema() {
        return new SimpleXsdSchema(new ClassPathResource("schemes/sports_soap.xsd"));
    }
}
