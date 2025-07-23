import mysql.connector
import json

def main():
    conn = mysql.connector.connect(
        host='127.0.0.1',
        user='root',
        password='',
        database='events'
    )

    cursor = conn.cursor(dictionary=True)

    documents = []

    cursor.execute("SELECT * FROM users")
    for row in cursor.fetchall():
        doc = f"Utilisateur ID {row['id_user']} : Nom complet : {row['first_name']} {row['last_name']}, Username : {row['username']}, Email : {row['email']}, Adresse : {row['address']}, Téléphone : {row['phone_number']}, Compte actif : {'oui' if row['active'] else 'non'}"
        documents.append({"id": row['id_user'], "type": "user", "content": doc})

    cursor.execute("SELECT * FROM offres")
    for row in cursor.fetchall():
        doc = f"Offre ID {row['id']} : Titre : {row['titre']}, Domaine : {row['domaine']}, Lieu : {row['lieu']}, Durée : {row['duree']}, Statut : {row['statut']}, Description : {row['description']}"
        documents.append({"id": row['id'], "type": "offre", "content": doc})

    cursor.execute("SELECT * FROM evenement")
    for row in cursor.fetchall():
        doc = f"Événement ID {row['idevent']} : Titre : {row['title']}, Adresse : {row['adresse']}, Date début : {row['datedebut']}, Date fin : {row['datefin']}, Description : {row['description']}"
        documents.append({"id": row['idevent'], "type": "evenement", "content": doc})

    with open('documents.json', 'w', encoding='utf-8') as f:
        json.dump(documents, f, ensure_ascii=False, indent=2)

    cursor.close()
    conn.close()
    print("Extraction terminée, fichier documents.json créé.")

if __name__ == "__main__":
    main()
import json
import openai

# Mets ta clé API OpenAI ici (ou mieux, utilise une variable d'environnement)
openai.api_key = "ta_clef_api_openai_ici"

def get_embedding(text, model="text-embedding-ada-002"):
    response = openai.Embedding.create(
        input=text,
        model=model
    )
    return response['data'][0]['embedding']

def main():
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

if __name__ == "__main__":
    main()
