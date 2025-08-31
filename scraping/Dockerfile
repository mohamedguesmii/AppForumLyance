# Dockerfile pour le service Scraping (Python)
FROM python:3.11-slim

WORKDIR /app

# Copier les fichiers requirements si tu en as un (sinon ignorer)
# COPY requirements.txt ./requirements.txt
# RUN pip install --no-cache-dir -r requirements.txt

# Copier tout le code source
COPY . .

# Commande par défaut pour lancer le script principal
CMD ["python", "main.py"]
