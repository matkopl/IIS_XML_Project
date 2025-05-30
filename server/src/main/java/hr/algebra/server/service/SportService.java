package hr.algebra.server.service;

import hr.algebra.server.model.SportType;
import hr.algebra.server.model.Sports;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class SportService {
    private final Map<Long, SportType> sports = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong();

    private static final String XML_PATH = "server/src/main/resources/schemes/xml/sports.xml";

    @PostConstruct
    public void init() {
        loadFromXml();
    }

    public List<SportType> findAll() {
        return new ArrayList<>(sports.values());
    }

    public Optional<SportType> findById(Long id) {
        return Optional.ofNullable(sports.get(id));
    }

    public SportType save(SportType sport) {
        Long id = idGen.incrementAndGet();
        sports.put(id, sport);
        saveToXml();
        return sport;
    }

    public void update(Long id, SportType sport) {
        sports.put(id, sport);
        saveToXml();
    }

    public void delete(Long id) {
        sports.remove(id);
        saveToXml();
    }

    private void loadFromXml() {
        try {
            File xmlFile = new File(XML_PATH);
            if (!xmlFile.exists()) return;
            JAXBContext jaxbContext = JAXBContext.newInstance(Sports.class, SportType.class);
            Unmarshaller tito = jaxbContext.createUnmarshaller();
            Sports sportsList = (Sports) tito.unmarshal(xmlFile);
            for (SportType sport : sportsList.getSport()) {
                if (sport.getName() != null && sport.getSlug() != null) {
                    save(sport);
                }
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void saveToXml() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Sports.class, SportType.class);
            Marshaller tito = jaxbContext.createMarshaller();
            tito.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            Sports wrapper = new Sports();
            wrapper.getSport().addAll(sports.values());
            tito.marshal(wrapper, new File(XML_PATH));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public Set<Map.Entry<Long, SportType>> getAllEntries() {
        return sports.entrySet();
    }
}
