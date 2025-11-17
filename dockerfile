FROM maven:3.9.9-eclipse-temurin-17

# Diretório de trabalho dentro do container
WORKDIR /app

# Copia só o pom primeiro (melhora o cache)
COPY pom.xml .

# Baixa dependências para acelerar os próximos builds
RUN mvn -q dependency:go-offline

# Agora copia o código fonte
COPY src ./src

# Porta usada pelo Javalin
EXPOSE 8000

# Comando para rodar a aplicação
# -> usando a sua classe main: com.aquatrack.App
CMD ["mvn", "-q", "exec:java", "-Dexec.mainClass=com.aquatrack.App"]
