# Dockerfile backend Scrapping
FROM node:20
WORKDIR /app

# Copier package.json et package-lock.json
COPY package*.json ./

# Installer les dépendances
RUN npm install --legacy-peer-deps

# Copier le code source
COPY . .

# Exposer le port utilisé par ton backend
EXPOSE 3000

# Lancer le backend
CMD ["node", "index.js"]
