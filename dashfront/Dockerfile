# Dockerfile frontend corrigé
# Étape 1 : build Angular
FROM node:20 AS build
WORKDIR /app

# Copier package.json et package-lock.json
COPY package*.json ./

# Installer les dépendances en ignorant les conflits de peer dependencies
RUN npm install --legacy-peer-deps

# Copier tout le code source
COPY . .

# Builder l'application Angular
RUN npm run build --prod

# Vérifier le contenu du dossier dist
RUN ls -la dist

# Étape 2 : servir avec Nginx
FROM nginx:alpine
# Remplace 'dashfront' par le nom exact de ton projet dans angular.json si nécessaire
COPY --from=build /app/dist/dashfront /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
