import json
import os
import re
import unicodedata
from sentence_transformers import SentenceTransformer
import faiss
import numpy as np
from datetime import datetime
import sys
import calendar

def normalize_text(text):
    text = unicodedata.normalize('NFD', text).encode('ascii', 'ignore').decode("utf-8")
    return text.lower().strip()

def normalize(vectors):
    norms = np.linalg.norm(vectors, axis=1, keepdims=True)
    norms[norms == 0] = 1
    return vectors / norms

def extract_field_regex(content, key):
    regex = re.compile(rf"{key}\s*:\s*(.+?)(?:,|\n|$)", re.IGNORECASE)
    match = regex.search(content)
    return match.group(1).strip() if match else ""

def enrich_content(doc):
    content = doc.get("content", "")
    type_ = doc.get("type", "").lower()

    if type_ == "offre":
        fields = {
            "Titre": extract_field_regex(content, "Titre"),
            "Lieu": extract_field_regex(content, "Lieu"),
            "Type": extract_field_regex(content, "Type"),
            "Domaine": extract_field_regex(content, "Domaine"),
            "Durée": extract_field_regex(content, "Durée"),
            "Description": extract_field_regex(content, "Description"),
        }
        for k, v in fields.items():
            if not v:
                fields[k] = "Non renseigné"
        return (f"Offre : {fields['Titre']} | Domaine : {fields['Domaine']} | Type : {fields['Type']} | "
                f"Lieu : {fields['Lieu']} | Durée : {fields['Durée']} | Description : {fields['Description']}")

    elif type_ == "evenement":
        titre = doc.get("title", "Titre non disponible").replace("Événement:", "").strip()

        lieu_match = re.search(r'à\s+([^\d\n]+)', content, re.IGNORECASE)
        lieu = lieu_match.group(1).strip() if lieu_match else "Non renseignée"

        dates_match = re.search(r'du\s+(\d{4}-\d{2}-\d{2})\s+au\s+(\d{4}-\d{2}-\d{2})', content, re.IGNORECASE)
        date_debut = dates_match.group(1) if dates_match else "Dates non précisées"
        date_fin = dates_match.group(2) if dates_match else ""

        description = content
        description = re.sub(r'à\s+[^\d\n]+', '', description, flags=re.IGNORECASE)
        description = re.sub(r'du\s+\d{4}-\d{2}-\d{2}\s+au\s+\d{4}-\d{2}-\d{2}', '', description, flags=re.IGNORECASE)
        description = description.strip() or "Aucune description"

        return (f"Événement : {titre} | Adresse : {lieu} | Du {date_debut} au {date_fin} | Description : {description}")

    return normalize_text(content)

def parse_date(text):
    try:
        return datetime.strptime(text, "%Y-%m-%d").date()
    except Exception:
        return None

def event_in_month(doc, month_num):
    """Vérifie si un événement a lieu dans le mois demandé (1-12)."""
    content = doc.get("content", "")
    dates_match = re.search(r'du\s+(\d{4}-\d{2}-\d{2})\s+au\s+(\d{4}-\d{2}-\d{2})', content, re.IGNORECASE)
    if not dates_match:
        return False
    start_date = parse_date(dates_match.group(1))
    end_date = parse_date(dates_match.group(2))
    if not start_date or not end_date:
        return False

    # Vérifie chevauchement avec le mois donné
    month_start = datetime(start_date.year, month_num, 1).date()
    _, last_day = calendar.monthrange(start_date.year, month_num)
    month_end = datetime(start_date.year, month_num, last_day).date()

    # Chevauchement intervalles [start_date, end_date] et [month_start, month_end]
    return not (end_date < month_start or start_date > month_end)

def filter_by_date_range(doc, query_dates):
    if not query_dates:
        return True
    content = doc.get("content", "").lower()
    start_q, end_q = query_dates

    match_debut = re.search(r'date début\s*:\s*([\d\-]+)', content)
    match_fin = re.search(r'date fin\s*:\s*([\d\-]+)', content)
    debut = parse_date(match_debut.group(1)) if match_debut else None
    fin = parse_date(match_fin.group(1)) if match_fin else None

    if debut and fin:
        return not (fin < start_q or debut > end_q)
    return True

def load_documents(path):
    if not os.path.exists(path):
        print(f"❌ Fichier introuvable : {path}")
        sys.exit(1)
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read().strip()
        if not content:
            print(f"❌ Le fichier {path} est vide.")
            sys.exit(1)
        try:
            documents = json.loads(content)
        except json.JSONDecodeError as e:
            print(f"❌ Erreur JSON dans {path} : {e}")
            sys.exit(1)

    for doc in documents:
        t = doc.get("type")
        if not t:
            title = doc.get("title", "").lower()
            if "offre" in title:
                doc["type"] = "offre"
            elif "événement" in title or "evenement" in title or "forum" in title:
                doc["type"] = "evenement"
            else:
                doc["type"] = "autre"
        else:
            doc["type"] = t.lower()

        doc['enriched_content'] = enrich_content(doc)

    return documents

def build_index(documents, model):
    texts = [doc['enriched_content'] for doc in documents]
    embeddings = model.encode(texts, convert_to_numpy=True, show_progress_bar=False)
    embeddings = normalize(embeddings)
    dimension = embeddings.shape[1]
    index = faiss.IndexFlatIP(dimension)
    index.add(embeddings)
    return index, embeddings

def main(query, k=10):
    base_dir = os.path.dirname(os.path.abspath(__file__))
    doc_path = os.path.join(base_dir, 'documents.json')

    documents = load_documents(doc_path)
    model = SentenceTransformer('all-MiniLM-L6-v2')
    index, embeddings = build_index(documents, model)

    query_norm = normalize_text(query)
    query_embedding = model.encode([query_norm], convert_to_numpy=True)
    query_embedding = normalize(query_embedding)

    distances, indices = index.search(query_embedding, k*3)
    query_words = set(re.findall(r'\w+', query_norm))

    # Extraction dates dans la requête (optionnel)
    date_range = None
    date_matches = re.findall(r'(\d{4}-\d{2}-\d{2})', query)
    if len(date_matches) == 2:
        date_range = (parse_date(date_matches[0]), parse_date(date_matches[1]))
    elif len(date_matches) == 1:
        d = parse_date(date_matches[0])
        date_range = (d, d)

    # Recherche mois par nom (ex: juillet)
    mois_pattern = r"(janvier|février|mars|avril|mai|juin|juillet|août|septembre|octobre|novembre|décembre)"
    mois_mention = re.search(mois_pattern, query_norm)

    mois_map = {
        "janvier": 1, "février": 2, "mars": 3, "avril":4, "mai":5, "juin":6,
        "juillet":7, "août":8, "septembre":9, "octobre":10, "novembre":11, "décembre":12
    }

    type_filters = set()
    if 'offre' in query_words or 'stage' in query_words or 'emploi' in query_words:
        type_filters.add('offre')
    if 'événement' in query_words or 'evenement' in query_words or 'forum' in query_words:
        type_filters.add('evenement')

    results = []
    seen_ids = set()

    for i, idx in enumerate(indices[0]):
        doc = documents[idx]
        doc_type = doc.get("type", "autre")

        enriched = normalize_text(doc.get("enriched_content", "") or doc.get("content", ""))

        if type_filters and doc_type not in type_filters:
            continue

        # Si recherche mois précisée et doc est un événement : on filtre
        if mois_mention and doc_type == "evenement":
            mois_num = mois_map.get(mois_mention.group(1))
            if not event_in_month(doc, mois_num):
                continue
        # Si mois mentionné mais doc n'est pas événement, on ignore le filtre mois

        # Filtrage par plage de dates dans contenu (extraction date début/fin explicite)
        if not filter_by_date_range(doc, date_range):
            continue

        # Pertinence textuelle minimale (distance cos)
        if not any(word in enriched for word in query_words) and distances[0][i] < 0.35:
            continue

        doc_id = doc.get('id', idx)
        if hasattr(doc_id, "item"):
            doc_id = int(doc_id)

        if doc_id not in seen_ids:
            results.append({
                "id": doc_id,
                "title": doc.get("title", ""),
                "content": doc.get("content", ""),
                "distance": float(distances[0][i]),
                "enriched": doc.get("enriched_content", ""),
                "type": doc_type
            })
            seen_ids.add(doc_id)

        if len(results) >= k:
            break

    print(json.dumps(results, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python search_documents_advanced.py \"votre requête\" [k]")
    else:
        q = sys.argv[1]
        k = int(sys.argv[2]) if len(sys.argv) > 2 else 10
        main(q, k)
