import json
import openai

openai.api_key = "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"

def get_embedding(text, model="text-embedding-ada-002"):
    response = openai.embeddings.create(
        model=model,
        input=text
    )
    return response.data[0].embedding

def main():
    print("Début de la génération des embeddings")

    # Charger les documents extraits
    with open('documents.json', 'r', encoding='utf-8') as f:
        documents = json.load(f)

    embeddings = []
    for doc in documents:
        text = doc["content"]
        emb = get_embedding(text)
        embeddings.append({
            "id": doc["id"],
            "type": doc["type"],
            "embedding": emb
        })
        print(f"Embedding créé pour {doc['type']} ID {doc['id']}")

    # Sauvegarder les embeddings dans un fichier JSON
    with open('embeddings.json', 'w', encoding='utf-8') as f:
        json.dump(embeddings, f, ensure_ascii=False, indent=2)

    print("Embeddings créés et sauvegardés dans embeddings.json")
    print("Fin du script")

if __name__ == "__main__":
    main()
