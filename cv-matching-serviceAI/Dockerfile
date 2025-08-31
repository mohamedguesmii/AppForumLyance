# Dockerfile frontend Angular
FROM node:20 AS build
WORKDIR /app

# Copier package.json et package-lock.json
COPY package*.json ./

# Installer les dépendances en ignorant les conflits
RUN npm install --legacy-peer-deps

# Copier tout le code source
COPY . .

# Builder Angular
RUN npm run build --prod

# Étape Nginx pour servir le frontend
FROM nginx:alpine
COPY --from=build /app/dist/ /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
