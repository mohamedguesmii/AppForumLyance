package tn.esprit.devoir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.devoir.entite.ForumWorkflowStatus;

import java.util.Optional;

@Repository
public interface ForumWorkflowStatusRepository extends JpaRepository<ForumWorkflowStatus, Long> {

    Optional<ForumWorkflowStatus> findByForumId(Long forumId);

    Optional<ForumWorkflowStatus> findByProcessInstanceId(String processInstanceId);
}
