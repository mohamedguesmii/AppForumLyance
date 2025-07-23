import json
import sys
import os
import re
from sentence_transformers import SentenceTransformer
import faiss
import numpy as np
import io
from datetime import datetime

def normalize(vectors):
    """Normalise un tableau de vecteurs par leur norme L2."""
    norms = np.linalg.norm(vectors, axis=1, keepdims=True)
    norms[norms == 0] = 1
    return vectors / norms

def enrich_content(doc):
    """Enrichit et reformate le contenu selon le type du document."""
    type_ = doc.get("type", "").lower()
    content = doc.get("content", "")

    def extract_field(key):
        key_lower = key.lower()
        pos = content.lower().find(key_lower)
        if pos != -1:
            start = pos + len(key)
            colon_pos = content.find(":", start)
            if colon_pos != -1:
                end_pos = content.find(",", colon_pos)
                if end_pos == -1:
                    end_pos = len(content)
                return content[colon_pos+1:end_pos].strip()
        return ""

    if type_ == "offre":
        fields = {
            "Titre": extract_field("Titre"),
            "Lieu": extract_field("Lieu"),
            "Type": extract_field("Type"),
            "Domaine": extract_field("Domaine"),
            "Durée": extract_field("Durée"),
            "Description": extract_field("Description"),
        }
        return (
            f"Offre d'emploi : {fields['Titre']}. "
            f"Domaine : {fields['Domaine']}. "
            f"Type : {fields['Type']}. "
            f"Lieu : {fields['Lieu']}. "
            f"Durée : {fields['Durée']}. "
            f"Description : {fields['Description']}."
        )

    elif type_ == "evenement":
        fields = {
            "Titre": extract_field("Titre"),
            "Adresse": extract_field("Adresse"),
            "Date début": extract_field("Date début"),
            "Date fin": extract_field("Date fin"),
            "Description": extract_field("Description"),
        }
        return (
            f"Événement : {fields['Titre']}. "
            f"Adresse : {fields['Adresse']}. "
            f"Dates : du {fields['Date début']} au {fields['Date fin']}. "
            f"Description : {fields['Description']}."
        )

    elif type_ == "user":
        fields = {
            "Nom": extract_field("Nom"),
            "Email": extract_field("Email"),
            "Rôle": extract_field("Rôle"),
        }
        return (
            f"Utilisateur : {fields['Nom']}. "
            f"Email : {fields['Email']}. "
            f"Rôle : {fields['Rôle']}."
        )

    elif type_ == "faq":
        return content.strip()

    return content.strip()

def parse_date(text):
    """Essaie d'extraire une date au format YYYY-MM-DD depuis une chaîne."""
    try:
        match = re.search(r'(\d{4}-\d{2}-\d{2})', text)
        if match:
            return datetime.strptime(match.group(1), "%Y-%m-%d").date()
    except:
        return None
    return None

def filter_by_date_range(doc, query_dates):
    """Filtre document selon plage de dates."""
    if not query_dates:
        return True

    start_q, end_q = query_dates
    type_ = doc.get("type", "").lower()
    content = doc.get("content", "").lower()

    if type_ == "evenement":
        debut = None
        fin = None
        match_debut = re.search(r'date début\s*:\s*([\d\-]+)', content)
        match_fin = re.search(r'date fin\s*:\s*([\d\-]+)', content)
        if match_debut:
            debut = parse_date(match_debut.group(1))
        if match_fin:
            fin = parse_date(match_fin.group(1))
        if debut and fin:
            # Intersecte si vrai
            return not (fin < start_q or debut > end_q)
        return False

    if type_ == "offre":
        # Essayer d'extraire une date de début / mention de mois dans la description
        # Simple détection mois (ex: "août") dans contenu
        mois_map = {
            "janvier": 1, "février": 2, "mars": 3, "avril": 4, "mai": 5, "juin": 6,
            "juillet": 7, "août": 8, "septembre": 9, "octobre": 10, "novembre": 11, "décembre": 12
        }
        for mois_name, mois_num in mois_map.items():
            if mois_name in content:
                # Vérifie si ce mois est dans la plage demandée
                if start_q.month <= mois_num <= end_q.month:
                    return True
                else:
                    return False
        # Sinon aucune info date => accepte
        return True

    return True

def main(query, k=20):
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

    base_dir = os.path.dirname(os.path.abspath(__file__))
    doc_path = os.path.join(base_dir, 'documents.json')

    if not os.path.exists(doc_path):
        print(f"[ERROR] Fichier documents.json non trouvé dans {base_dir}", file=sys.stderr)
        sys.exit(1)

    with open(doc_path, 'r', encoding='utf-8') as f:
        documents = json.load(f)

    for doc in documents:
        doc['enriched_content'] = enrich_content(doc)

    model = SentenceTransformer('all-MiniLM-L6-v2')
    texts = [doc['enriched_content'] for doc in documents]
    embeddings = model.encode(texts, convert_to_numpy=True, show_progress_bar=False)
    embeddings = normalize(embeddings)

    dimension = embeddings.shape[1]
    index = faiss.IndexFlatIP(dimension)
    index.add(embeddings)

    query_embedding = model.encode([query], convert_to_numpy=True)
    query_embedding = normalize(query_embedding)

    distances, indices = index.search(query_embedding, k*3)

    query_words = re.findall(r'\w+', query.lower())

    filter_types = set()
    if any(w in query_words for w in ['offre', 'offres']):
        filter_types.add('offre')
    if any(w in query_words for w in ['evenement', 'événement', 'evenements', 'événements']):
        filter_types.add('evenement')
    if any(w in query_words for w in ['user', 'utilisateur', 'users', 'utilisateurs']):
        filter_types.add('user')

    # Gestion des dates dans la requête
    date_range = None
    date_matches = re.findall(r'(\d{4}-\d{2}-\d{2})', query)
    if len(date_matches) == 2:
        start_date = datetime.strptime(date_matches[0], "%Y-%m-%d").date()
        end_date = datetime.strptime(date_matches[1], "%Y-%m-%d").date()
        date_range = (start_date, end_date)
    elif len(date_matches) == 1:
        d = datetime.strptime(date_matches[0], "%Y-%m-%d").date()
        date_range = (d, d)

    mois_pattern = r"(janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)"
    mois_mention = re.search(mois_pattern, query.lower())

    results = []
    seen_ids = set()

    for i, idx in enumerate(indices[0]):
        doc = documents[idx]
        doc_type = doc.get("type", "").lower()
        enriched = doc.get("enriched_content", "").lower()

        if filter_types and doc_type not in filter_types:
            continue

        if mois_mention and doc_type == 'offre':
            mois_demande = mois_mention.group(0)
            if mois_demande not in enriched:
                continue

        if not filter_by_date_range(doc, date_range):
            continue

        # Vérifie si au moins un mot de la requête est dans le contenu
        if not any(word in enriched for word in query_words) and distances[0][i] < 0.25:
            continue

        if doc['id'] not in seen_ids:
            results.append({
                "type": doc.get("type", ""),
                "id": doc.get("id", ""),
                "content": doc.get("content", ""),
                "enriched_content": doc.get("enriched_content", ""),
                "distance": float(distances[0][i])
            })
            seen_ids.add(doc['id'])
        if len(results) >= k:
            break

    print(json.dumps(results, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python rag_local_search.py \"ta requête ici\" [k=10]", file=sys.stderr)
        sys.exit(1)
    query = sys.argv[1]
    k = int(sys.argv[2]) if len(sys.argv) > 2 else 10
    main(query, k)
