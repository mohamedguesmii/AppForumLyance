package tn.esprit.devoir.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.devoir.entite.FileDB;


@Repository
public interface FileDBRepository extends JpaRepository<FileDB, String> {

}
