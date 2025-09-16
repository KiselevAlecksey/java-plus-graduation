package ru.practicum.compilation.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.compilation.model.Compilation;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query(value = "select * from compilations as c where c.pinned = (:pin) or (:pin) is null",nativeQuery = true)
    Page<Compilation> findCompilations(@Param("pin") Boolean pin, Pageable pageable);

}
