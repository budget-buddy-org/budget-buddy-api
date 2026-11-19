FROM cgr.dev/chainguard/jre:latest
WORKDIR /app
COPY --chown=65532:65532 build/libs/app.jar /app/app.jar
EXPOSE 8080
CMD ["-jar", "/app/app.jar"]
