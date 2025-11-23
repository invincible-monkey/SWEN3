package at.technikum_wien.swen3.paperless.controller;

import at.technikum_wien.swen3.paperless.entity.Tag;
import at.technikum_wien.swen3.paperless.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    private final TagRepository tagRepository;

    @GetMapping
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @PostMapping
    public Tag createTag(@RequestBody Tag tag) {
        // Simple check to return existing tag if it exists
        return tagRepository.findByName(tag.getName())
                .orElseGet(() -> tagRepository.save(tag));
    }
}
