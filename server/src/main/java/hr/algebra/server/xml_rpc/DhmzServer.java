package hr.algebra.server.xml_rpc;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.List;

public class DhmzServer {
    public String[] getTemperatures(String cityFragment) throws Exception {
        URL url = new URL("https://vrijeme.hr/hrvatska_n.xml");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());

        NodeList gradNodes = doc.getElementsByTagName("Grad");
        List<String> results = new java.util.ArrayList<>();
        for (int i = 0; i < gradNodes.getLength(); i++) {
            Element gradElem = (org.w3c.dom.Element) gradNodes.item(i);
            String gradIme = gradElem.getElementsByTagName("GradIme").item(0).getTextContent().trim();
            String lat = gradElem.getElementsByTagName("Lat").item(0).getTextContent().trim();
            String lon = gradElem.getElementsByTagName("Lon").item(0).getTextContent().trim();

            Element podaciElem = (org.w3c.dom.Element) gradElem.getElementsByTagName("Podatci").item(0);
            String temp = podaciElem.getElementsByTagName("Temp").item(0).getTextContent().trim();
            String vlaga = podaciElem.getElementsByTagName("Vlaga").item(0).getTextContent().trim();
            String tlak = podaciElem.getElementsByTagName("Tlak").item(0).getTextContent().trim();
            String vjetarSmjer = podaciElem.getElementsByTagName("VjetarSmjer").item(0).getTextContent().trim();
            String vjetarBrzina = podaciElem.getElementsByTagName("VjetarBrzina").item(0).getTextContent().trim();
            String vjetarSnaga = podaciElem.getElementsByTagName("Vrijeme").item(0).getTextContent().trim();

            if (gradIme.toLowerCase().contains(cityFragment.trim().toLowerCase())) {
                String info = String.format(
                        "%s (Lat: %s, Lon: %s)\n  Temperatura: %s °C\n  Vlaga: %s%%\n  Tlak: %s hPa\n  Vjetar: %s %s m/s %s\n",
                        gradIme, lat, lon, temp, vlaga, tlak, vjetarSmjer, vjetarBrzina, vjetarSnaga
                );

                results.add(info);
            }
        }
        return results.toArray(new String[0]);
    }

    public static void main(String[] args) throws Exception {
        int port = 8081;
        WebServer webServer = new WebServer(port);
        XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();

        PropertyHandlerMapping phm = new PropertyHandlerMapping();
        phm.addHandler("dhmz", DhmzServer.class);

        xmlRpcServer.setHandlerMapping(phm);
        webServer.start();
        System.out.println("XML-RPC DHMZ Server started on port " + port);
    }
}