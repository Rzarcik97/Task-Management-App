package taskmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taskmanagement.model.Label;

public interface LabelRepository extends JpaRepository<Label, Long> {

    boolean existsByName(String name);
}
