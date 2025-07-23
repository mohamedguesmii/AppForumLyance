package tn.esprit.devoir.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.devoir.entite.Evenement;
import tn.esprit.devoir.entite.FileDB;
import tn.esprit.devoir.repository.EvenementRepo;
import tn.esprit.devoir.repository.FileDBRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class FileStorageService2 {

    @Autowired
    private FileDBRepository fileDBRepository;
    @Autowired
    private EvenementRepo livrableRepository;

    public FileDB store(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        FileDB FileDB = new FileDB(fileName, file.getContentType(), file.getBytes());


        Evenement livrable = new Evenement();
        livrable.setImageUrl(fileName);
        livrableRepository.save(livrable);


        return fileDBRepository.save(FileDB);
    }

    public FileDB getFile(String id) {
        return fileDBRepository.findById(id).get();
    }

    public Stream<FileDB> getAllFiles() {
        return fileDBRepository.findAll().stream();
    }


}
