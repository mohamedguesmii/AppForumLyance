# Dockerfile cv-matching-serviceAI
FROM python:3.11-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier requirements et installer les dépendances
COPY requirements.txt ./requirements.txt
RUN pip install --upgrade pip \
    && pip install --no-cache-dir -r requirements.txt

# Copier tout le code source
COPY . .

# Exposer le port si nécessaire (ex: FastAPI, Flask)
EXPOSE 8000

# Lancer le service
CMD ["python", "matching_service.py"]
