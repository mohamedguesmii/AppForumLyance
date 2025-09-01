# Dockerfile backend RAG
FROM python:3.11-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers requirements et installer les dépendances
COPY requirements.txt .
RUN pip install --upgrade pip \
    && pip install --no-cache-dir -r requirements.txt

# Copier tout le code source
COPY . .

# Exposer le port de l'application
EXPOSE 8000

# S'assurer que uvicorn est disponible
RUN pip install --no-cache-dir uvicorn fastapi

# Lancer l'application
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
