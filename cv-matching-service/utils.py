# matching_service/utils.py

import re

def extract_entities(text: str) -> dict:
    """
    Extrait une liste de “compétences” (SKILL) en détectant quelques mots-clés
    parmi un lexique réduit. À adapter selon vos propres besoins.
    """
    # Exemples de mots clés « compétences » (mettez les vôtres, en minuscules)
    lexique_competences = {
        "python", "java", "flask", "sql", "angular", "springboot",
        "docker", "kubernetes", "react", "nodejs", "javascript"
    }

    # On extrait tous les mots de 3 lettres ou plus, en minuscules
    mots = re.findall(r"\b\w{3,}\b", text.lower())

    # On filtre pour ne garder que ceux dans lexique_competences
    skills = sorted({ mot for mot in mots if mot in lexique_competences })

    return {"SKILL": skills}
