package hr.algebra.server.controller;

import hr.algebra.server.model.SportType;
import hr.algebra.server.service.SportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
    public ResponseEntity<?> save(@Valid @RequestBody SportType sportType, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder("Greške pri unosu:\n");
            bindingResult.getAllErrors().forEach(error -> sb.append("-").append(error.getDefaultMessage()).append("\n"));
            return ResponseEntity.badRequest().body(sb.toString());
        }

        boolean nameExists = sportRepository.getAllEntries().stream()
                .anyMatch(entry ->
                    entry.getValue().getName().equalsIgnoreCase(sportType.getName()));

        boolean slugExists = sportRepository.getAllEntries().stream()
                .anyMatch(entry ->
                        entry.getValue().getSlug().equalsIgnoreCase(sportType.getSlug()));

        if (nameExists) {
            return ResponseEntity.badRequest().body("Naziv sporta mora biti jedinstven.");
        }

        if (slugExists) {
            return ResponseEntity.badRequest().body("Slug mora biti jedinstven.");
        }

        return ResponseEntity.ok(sportRepository.save(sportType));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,@Valid @RequestBody SportType sportType, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder sb = new StringBuilder("Greške pri unosu:\n");
            bindingResult.getAllErrors().forEach(error -> sb.append("-").append(error.getDefaultMessage()).append("\n"));
            return ResponseEntity.badRequest().body(sb.toString());
        }

        boolean nameExists = sportRepository.getAllEntries().stream()
                .anyMatch(entry ->
                        !entry.getKey().equals(id) &&
                                entry.getValue().getName().equalsIgnoreCase(sportType.getName()));

        boolean slugExists = sportRepository.getAllEntries().stream()
                .anyMatch(entry ->
                        !entry.getKey().equals(id) &&
                                entry.getValue().getSlug().equalsIgnoreCase(sportType.getSlug()));

        if (nameExists) {
            return ResponseEntity.badRequest().body("Naziv sporta mora biti jedinstven.");
        }

        if (slugExists) {
            return ResponseEntity.badRequest().body("Slug mora biti jedinstven.");
        }

        if (sportRepository.findById(id).isPresent()) {
            sportRepository.update(id, sportType);
            return ResponseEntity.ok().build();
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
