import requests
import json

# Lien de ton backend Spring Boot
BASE_URL = "http://localhost:8089/api"

# Fichier de sortie
OUTPUT_FILE = "documents.json"

def get_data(endpoint):
    response = requests.get(f"{BASE_URL}/{endpoint}")
    if response.status_code == 200:
        return response.json()
    else:
        print(f"Erreur lors de la récupération des données : {endpoint} (status code: {response.status_code})")
        return []

def build_documents():
    documents = []

    # Récupérer événements (avec le bon endpoint)
    evenements = get_data("evenements/all")
    for event in evenements:
        doc = {
            "title": f"Événement: {event.get('title', 'Sans titre')}",
            "content": f"{event.get('description', '')} à {event.get('adresse', '')} du {event.get('datedebut', '')} au {event.get('datefin', '')}."
        }
        documents.append(doc)

    # Récupérer offres (je suppose que ton endpoint est /offres)
    offres = get_data("offres")
    for offre in offres:
        doc = {
            "title": f"Offre: {offre.get('titre', 'Sans titre')}",
            "content": f"{offre.get('description', '')} ({offre.get('domaine', '')}, {offre.get('type', '')}) à {offre.get('lieu', '')}"
        }
        documents.append(doc)

    return documents

def save_documents(documents):
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(documents, f, ensure_ascii=False, indent=2)
    print(f"✅ {len(documents)} documents enregistrés dans {OUTPUT_FILE}")

if __name__ == "__main__":
    docs = build_documents()
    save_documents(docs)
