package hr.algebra.server.handler;

import lombok.Getter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

@Getter
public class XmlErrorHandler implements ErrorHandler {
    private final List<SAXParseException> exceptions = new ArrayList<>();

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        exceptions.add(exception);
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        exceptions.add(exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        exceptions.add(exception);
    }
}
