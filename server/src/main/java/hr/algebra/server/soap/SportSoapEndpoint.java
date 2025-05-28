package hr.algebra.server.soap;


import hr.algebra.server.factory.ObjectFactory;
import hr.algebra.server.model.SportType;
import hr.algebra.server.service.OddsApiService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.apache.catalina.manager.JspHelper.escapeXml;

@Endpoint
public class SportSoapEndpoint {
    private static final String NAMESPACE_URI = "https://interoperabilnost.hr/sport";
    private final OddsApiService oddsApiService;
    private final ObjectFactory objectFactory = new ObjectFactory();

    public SportSoapEndpoint(OddsApiService oddsApiService) {
        this.oddsApiService = oddsApiService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "SearchRequest")
    @ResponsePayload
    public SearchResponse search(@RequestPayload SearchRequest request) throws Exception {
        String term = request.getTerm().toLowerCase();

        oddsApiService.saveSportsXmlToFile();

        String xml = oddsApiService.fetchSportsXml();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        String expression = "//*[local-name()='Sport'][contains(translate(*[local-name()='name'], 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + term + "')]";
        NodeList nodes = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);

        SearchResponse response = objectFactory.createSearchResponse();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element sportElem = (Element) nodes.item(i);
            Element nameElem = (Element) sportElem.getElementsByTagNameNS(NAMESPACE_URI, "name").item(0);
            Element slugElem = (Element) sportElem.getElementsByTagNameNS(NAMESPACE_URI, "slug").item(0);

            String name = nameElem != null ? escapeXml(nameElem.getTextContent()) : "";
            String slug = slugElem != null ? escapeXml(slugElem.getTextContent()) : "";

            if (name.isBlank() || slug.isBlank()) continue;

            SportType sport = objectFactory.createSportType();
            sport.setName(name);
            sport.setSlug(slug);

            response.getSport().add(sport);
        }

        return response;
    }
}
