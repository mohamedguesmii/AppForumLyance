import json
import os
from sentence_transformers import SentenceTransformer
import faiss
import numpy as np
import openai
from dotenv import load_dotenv

# Charger les variables d'environnement depuis rag.env
load_dotenv("rag.env")
openai.api_key = os.getenv("OPENAI_API_KEY")

if not openai.api_key:
    raise ValueError("❌ Clé API OpenAI manquante. Vérifie que 'OPENAI_API_KEY' est bien défini dans rag.env.")

# Nom du fichier JSON contenant les données exportées (offres + événements)
DATA_JSON_FILE = 'data.json'

def charger_donnees(json_file):
    with open(json_file, 'r', encoding='utf-8') as f:
        return json.load(f)

def preparer_documents(data):
    docs = []
    for item in data:
        if item["type"] == "offre":
            txt = (f"Offre: {item.get('titre','')}. Description: {item.get('description','')}. "
                   f"Lieu: {item.get('lieu','')}. Date: {item.get('date_publication','')}. "
                   f"Type: {item.get('offre_type','')}. Domaine: {item.get('domaine','')}")
        else:  # evenement
            txt = (f"Événement: {item.get('titre','')}. Description: {item.get('description','')}. "
                   f"Lieu: {item.get('lieu','')}. Dates: {item.get('date_debut','')} au {item.get('date_fin','')}")
        docs.append(txt)
    return docs

def creer_index_faiss(embeddings):
    dimension = embeddings.shape[1]
    index = faiss.IndexFlatL2(dimension)
    index.add(np.array(embeddings))
    return index

def rechercher(question, model, index, documents, k=3):
    question_embedding = model.encode([question])
    distances, indices = index.search(np.array(question_embedding), k)
    return [documents[idx] for idx in indices[0]]

def generer_reponse(question, documents):
    context = "\n\n".join(documents)
    messages = [
        {"role": "system", "content": "Tu es un assistant utile qui répond de manière claire et concise."},
        {"role": "user", "content": f"Voici des informations extraites de la base :\n{context}\n\nQuestion : {question}\nRéponse courte et précise :"}
    ]
    
    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=messages,
        max_tokens=150,
        temperature=0.3
    )
    return response.choices[0].message.content.strip()

def main():
    print("Chargement des données...")
    data = charger_donnees(DATA_JSON_FILE)

    print("Préparation des documents...")
    documents = preparer_documents(data)

    print("Chargement du modèle d'embeddings...")
    model = SentenceTransformer('all-MiniLM-L6-v2')

    print("Création des embeddings...")
    embeddings = model.encode(documents)

    print("Création de l'index FAISS...")
    index = creer_index_faiss(embeddings)

    print("✅ Système RAG prêt. Pose ta question (tape 'exit' pour quitter).")

    while True:
        question = input("\n❓ Question : ")
        if question.lower() == 'exit':
            print("👋 Fin du programme.")
            break

        print("\n🔍 Recherche des documents pertinents...")
        docs_trouves = rechercher(question, model, index, documents)

        print("\n📄 Documents pertinents trouvés :")
        for d in docs_trouves:
            print("-", d)

        print("\n🤖 Génération de la réponse...")
        reponse = generer_reponse(question, docs_trouves)

        print("\n✅ Réponse :")
        print(reponse)

if __name__ == "__main__":
    main()
