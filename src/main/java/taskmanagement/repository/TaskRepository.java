package taskmanagement.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import taskmanagement.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t JOIN FETCH t.project JOIN FETCH t.assignee WHERE t.id = :id")
    Optional<Task> findByIdWithRelations(@Param("id") Long id);

    Page<Task> findByProject_Id(Long projectId, Pageable pageable);

    List<Task> findByDueDate(LocalDate dueDate);
}
