# Dockerfile frontend
FROM node:20 as build
WORKDIR /app

# Copier package.json et package-lock.json
COPY package*.json ./

# Installer les dépendances en ignorant les conflits de peer dependencies
RUN npm install --legacy-peer-deps

# Copier tout le code source
COPY . .

# Builder l'application Angular
RUN npm run build --prod

# Étape Nginx
FROM nginx:alpine
COPY --from=build /app/dist/dashfront /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
