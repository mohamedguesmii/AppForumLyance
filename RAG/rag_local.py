import json
from sentence_transformers import SentenceTransformer
import faiss
import numpy as np

def normalize(vectors):
    norms = np.linalg.norm(vectors, axis=1, keepdims=True)
    return vectors / norms

def main():
    # Charger les documents extraits
    with open('documents.json', 'r', encoding='utf-8') as f:
        documents = json.load(f)

    # Charger un modèle d'embeddings local
    model = SentenceTransformer('all-MiniLM-L6-v2')

    # Extraire les textes
    texts = [doc['content'] for doc in documents]

    # Générer les embeddings
    embeddings = model.encode(texts, convert_to_numpy=True)
    embeddings = normalize(embeddings)

    # Construire l'index FAISS
    dimension = embeddings.shape[1]
    index = faiss.IndexFlatIP(dimension)  # IP = inner product, avec vecteurs normalisés = cosine similarity
    index.add(embeddings)

    # Saisie de la requête
    query = input("Tape ta requête : ")
    query_embedding = model.encode([query])
    query_embedding = normalize(query_embedding)

    # Recherche les 3 documents les plus proches
    distances, indices = index.search(query_embedding, k=3)

    print("\nDocuments proches trouvés :")
    for i, idx in enumerate(indices[0]):
        print(f"{i+1}. [{documents[idx]['type']}] ID {documents[idx].get('id', 'N/A')} - {documents[idx]['content']}")
        print(f"Distance (cosine similarity) : {distances[0][i]:.4f}")
        print("-" * 40)

if __name__ == "__main__":
    main()
