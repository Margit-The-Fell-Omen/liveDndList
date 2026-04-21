FROM node:20-alpine

# Set the working directory inside the container
# All subsequent commands will run from here
WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

EXPOSE 80

CMD ["npm", "run", "dev"]
