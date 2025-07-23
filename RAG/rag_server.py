import os
import openai
import faiss
import numpy as np
import json
from flask import Flask, request, jsonify

openai.api_key = os.getenv("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")

app = Flask(__name__)

def load_documents(folder="docs"):
    docs = []
    filenames = []
    for fname in os.listdir(folder):
        if fname.endswith(".txt"):
            path = os.path.join(folder, fname)
            with open(path, "r", encoding="utf-8") as f:
                docs.append(f.read())
            filenames.append(fname)
    return docs, filenames

def get_embedding(text):
    response = openai.embeddings.create(
        model="text-embedding-ada-002",
        input=text
    )
    return np.array(response.data[0].embedding)

print("Chargement des documents...")
documents, filenames = load_documents()

embeddings_file = "embeddings.json"

# Charger les embeddings depuis un fichier si possible
if os.path.exists(embeddings_file):
    print("Chargement des embeddings depuis le fichier...")
    with open(embeddings_file, "r") as f:
        embeddings_list = json.load(f)
    embeddings = np.array(embeddings_list)
else:
    print("Création des embeddings...")
    try:
        embeddings = np.array([get_embedding(doc) for doc in documents])
    except Exception as e:
        print(f"Erreur lors de la création de l'embedding : {e}")
        print("Impossible de continuer sans embeddings valides. Arrêt.")
        exit(1)
    # Sauvegarder embeddings dans un fichier pour éviter de refaire les appels API
    with open(embeddings_file, "w") as f:
        json.dump(embeddings.tolist(), f)

dimension = embeddings.shape[1]
index = faiss.IndexFlatL2(dimension)
index.add(embeddings)

@app.route("/api/rag/query", methods=["POST"])
def query():
    data = request.json
    question = data.get("question", "")
    if not question:
        return jsonify({"error": "Question vide"}), 400

    q_emb = get_embedding(question).reshape(1, -1)
    D, I = index.search(q_emb, k=3)

    context = "\n---\n".join([documents[i] for i in I[0]])

    prompt = f"Utilise les documents suivants pour répondre précisément à la question.\nDocuments:\n{context}\n\nQuestion : {question}\nRéponse :"

    completion = openai.ChatCompletion.create(
        model="gpt-4o",
        messages=[{"role": "user", "content": prompt}],
        max_tokens=500,
        temperature=0.2,
    )
    answer = completion.choices[0].message.content
    return jsonify({"answer": answer})

if __name__ == "__main__":
    print("Lancement du serveur sur http://localhost:5000")
    app.run(port=5000)
