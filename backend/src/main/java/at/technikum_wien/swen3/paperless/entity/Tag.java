package at.technikum_wien.swen3.paperless.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
