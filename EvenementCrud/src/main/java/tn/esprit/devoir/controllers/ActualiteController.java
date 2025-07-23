package tn.esprit.devoir.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.devoir.Cloudinary.CloudinaryService;
import tn.esprit.devoir.Model.ModelActulite;
import tn.esprit.devoir.Model.ModelUrl;
import tn.esprit.devoir.entite.Actualite;
import tn.esprit.devoir.service.ActualiteService;

import java.util.List;
@CrossOrigin(origins = "*")
@RestController

@RequestMapping("/actualites")
public class ActualiteController {

    @Autowired
    private ActualiteService actualiteService;
    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/all")
    public List<Actualite> retrieveAllActualite() {
        return actualiteService.retrieveAllActualite();
    }

    @GetMapping("/{id}")
    public Actualite findById(@PathVariable Long id) {
        return actualiteService.findById(id);
    }

    @PostMapping("/add")
    public Actualite addActualite(
        @RequestParam("file") MultipartFile file,
        @RequestParam("actuality") String actuality,
        @RequestParam("description") String description
    ) {
        // 1. Upload le fichier via Cloudinary
        String imageUrl = cloudinaryService.uploadFile(file, "folder_1");

        // 2. Crée l'entité Actualite
        Actualite actualite = new Actualite();
        actualite.setActuality(actuality);
        actualite.setDescription(description);
        actualite.setImageUrl(imageUrl);

        // 3. Sauvegarde et retourne
        return actualiteService.addActualite(actualite);
    }



    @PostMapping("/update/{id}")
    public Actualite updateActualite(@RequestBody Actualite actualite, @PathVariable Long id) {
        return actualiteService.updateActualite(actualite, id);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteActualiteById(@PathVariable Long id) {
        actualiteService.deleteActualiteById(id);
    }

    @PostMapping("/upload")
    public ModelUrl upload(@RequestBody MultipartFile file){

            ModelUrl m = new ModelUrl();
                    m.setUrl(cloudinaryService.uploadFile(file, "folder_1"));
            return m;

    }

    @GetMapping("/best")
    public Actualite Best(){
        return actualiteService.Best();
    }

}
