import io
import fitz  # PyMuPDF
import requests
import unicodedata
import difflib
from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

SPRING_BOOT_BASE_URL = "http://localhost:8089"
THRESHOLD = 50.0
PORT = 5051

# ----------------- Utils -----------------
def normalize_text(text):
    """Met tout en minuscules et supprime accents."""
    if not text:
        return ""
    text = text.lower()
    text = unicodedata.normalize("NFD", text)
    text = "".join(c for c in text if unicodedata.category(c) != "Mn")
    return text

def extract_text_from_pdf_url(pdf_url):
    """Télécharge et extrait le texte d'un PDF depuis une URL"""
    resp = requests.get(pdf_url)
    resp.raise_for_status()
    pdf_bytes = io.BytesIO(resp.content)
    doc = fitz.open(stream=pdf_bytes, filetype="pdf")
    all_text = [page.get_text() for page in doc]
    doc.close()
    return "\n".join(all_text)

def fetch_offre(offre_id):
    """Récupère l'offre depuis Spring Boot"""
    url = f"{SPRING_BOOT_BASE_URL}/api/offres/{offre_id}"
    resp = requests.get(url)
    if resp.status_code == 200:
        return resp.json()
    else:
        raise Exception(f"Impossible de récupérer l'offre (HTTP {resp.status_code})")

def update_candidature_score(candidature_id, score):
    """Met à jour le score de la candidature côté Spring Boot"""
    url = f"{SPRING_BOOT_BASE_URL}/api/candidatures/{candidature_id}/score"
    payload = {"score": score}
    headers = {"Content-Type": "application/json"}
    resp = requests.put(url, json=payload, headers=headers)
    if resp.status_code not in (200, 204):
        raise Exception(f"Erreur lors de la mise à jour du score (HTTP {resp.status_code})")

# ----------------- Matching tolérant -----------------
def compute_matching_score(text_cv, liste_competences_offre):
    """Calcule un score en % avec tolérance aux variations et mots partiels"""
    if not liste_competences_offre:
        return 0.0

    text_norm = normalize_text(text_cv)
    total = len(liste_competences_offre)
    matched = 0

    for comp in liste_competences_offre:
        comp_norm = normalize_text(comp)
        # Correspondance exacte ou incluse
        if comp_norm in text_norm:
            matched += 1
        else:
            # Correspondance partielle par mot
            comp_words = comp_norm.split()
            if any(word in text_norm for word in comp_words):
                matched += 0.5
            else:
                # correspondance approximative via difflib
                ratio = difflib.SequenceMatcher(None, comp_norm, text_norm).ratio()
                if ratio > 0.6:  # seuil 60%
                    matched += 1

    score = (matched / total) * 100.0
    return round(score, 2)

# ----------------- Endpoint principal -----------------
@app.route('/analyze', methods=['POST'])
def match_candidature():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Requête JSON attendue"}), 400

    candidature_id = data.get("candidatureId")
    offre_id = data.get("offreId")
    cv_url = data.get("cvUrl")

    if candidature_id is None or offre_id is None or not cv_url:
        return jsonify({"error": "Missing fields"}), 400

    try:
        texte_cv = extract_text_from_pdf_url(cv_url)
        print("=== TEXTE CV ===\n", texte_cv[:1000])  # debug

        offre = fetch_offre(offre_id)
        competences_offre = offre.get("competences", [])
        if not isinstance(competences_offre, list):
            competences_offre = []

        print("=== COMPÉTENCES OFFRE ===", competences_offre)  # debug

        score = compute_matching_score(texte_cv, competences_offre)
        matched = score >= THRESHOLD

        try:
            update_candidature_score(candidature_id, score)
        except Exception as e:
            print(f"Warning: impossible de mettre à jour le score → {e}")

        return jsonify({
            "candidatureId": candidature_id,
            "offreId": offre_id,
            "score": score,
            "matched": matched
        }), 200

    except requests.exceptions.RequestException as req_err:
        return jsonify({"error": f"Erreur téléchargement PDF : {req_err}"}), 400
    except Exception as exc:
        return jsonify({"error": f"Erreur interne : {exc}"}), 500

# ----------------- Endpoint test -----------------
@app.route('/test-offre/<int:offre_id>', methods=['GET'])
def test_offre(offre_id):
    try:
        offre = fetch_offre(offre_id)
        return jsonify(offre), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

# ----------------- Démarrage -----------------
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=PORT, debug=True)
