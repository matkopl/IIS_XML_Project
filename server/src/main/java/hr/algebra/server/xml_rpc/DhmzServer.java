package hr.algebra.server.xml_rpc;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.List;

public class DhmzServer {
    public String[] getTemperatures(String cityFragment) throws Exception {
        URL url = new URL("https://vrijeme.hr/hrvatska_n.xml");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());

        NodeList gradnodes = doc.getElementsByTagName("Grad");
        NodeList tempNodes = doc.getElementsByTagName("Temp");
        List<String> results = new java.util.ArrayList<>();
        for (int i = 0; i < gradnodes.getLength(); i++) {
            String grad = gradnodes.item(i).getTextContent();
            if (grad.toLowerCase().contains(cityFragment.toLowerCase())) {
                String temp = tempNodes.item(i).getTextContent();
                results.add(grad + ": " + temp + " °C");
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