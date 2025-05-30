package hr.algebra.server.controller;

import hr.algebra.server.model.SportType;
import hr.algebra.server.service.SportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sport")
@RequiredArgsConstructor
public class SportController {

    private final SportService sportRepository;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, SportType> entry: sportRepository.getAllEntries()) {
            Map<String, Object> sportMap = new HashMap<>();
            sportMap.put("id", entry.getKey());
            sportMap.put("name", entry.getValue().getName());
            sportMap.put("slug", entry.getValue().getSlug());
            result.add(sportMap);
        }

        return result;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return sportRepository.findById(id)
                .map(sport -> {
                    Map<String, Object> sportMap = new HashMap<>();
                    sportMap.put("id", id);
                    sportMap.put("name", sport.getName());
                    sportMap.put("slug", sport.getSlug());
                    return ResponseEntity.ok(sportMap);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public SportType save(@RequestBody SportType sportType) {
        return sportRepository.save(sportType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody SportType sportType) {
        if (sportRepository.findById(id).isPresent()) {
            sportRepository.update(id, sportType);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (sportRepository.findById(id).isPresent()) {
            sportRepository.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
