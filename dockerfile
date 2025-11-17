FROM maven:3.9.9-eclipse-temurin-17

# Diretório de trabalho dentro do container
WORKDIR /app

# Copia todo o projeto para dentro do container
COPY . .

# Baixa dependências para acelerar os próximos builds
RUN mvn -q dependency:go-offline

# Porta usada pelo Javalin (de acordo com o README)
EXPOSE 7000

# Comando para rodar a aplicação
CMD ["mvn", "exec:java"]
