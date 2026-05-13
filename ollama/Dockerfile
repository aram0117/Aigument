FROM ollama/ollama:latest

ENV OLLAMA_HOST=0.0.0.0

COPY Modelfile /etc/ollama/Modelfile

RUN nohup ollama serve & \
    sleep 5 && \
    ollama pull llama3 && \
    ollama create judge-llama3 -f /etc/ollama/Modelfile

ENTRYPOINT ["ollama", "serve"]