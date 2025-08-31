# Utiliser une image Python officielle
FROM python:3.11-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier requirements
COPY requirements.txt.txt ./requirements.txt

# Installer les dépendances Python
RUN pip install --no-cache-dir -r requirements.txt

# Copier le code source
COPY . .

# Exposer le port si nécessaire (ex: FastAPI, Flask)
EXPOSE 8000

# Commande par défaut pour lancer le service (à adapter selon ton app)
CMD ["python", "matching_service.py"]
