# Dockerfile backend RAG
FROM python:3.11-slim
WORKDIR /app

# Copier requirements
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# Copier tout le code source
COPY . .

# Exposer le port de l'application
EXPOSE 8000

# Lancer l'application (FastAPI par exemple)
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
