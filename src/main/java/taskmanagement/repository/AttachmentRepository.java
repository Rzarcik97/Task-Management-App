package taskmanagement.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import taskmanagement.model.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Page<Attachment> findByTask_Id(Long taskId, Pageable pageable);

    List<Attachment> findByPath(String path);
}
