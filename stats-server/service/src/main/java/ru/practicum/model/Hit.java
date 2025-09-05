package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "hits")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "app", nullable = false)
    String app;
    @Column(name = "uri", nullable = false)
    String uri;
    @Column(name = "ip", nullable = false)
    String ip;
    @Column(name = "timestamp", nullable = false)
    LocalDateTime timestamp;
}

