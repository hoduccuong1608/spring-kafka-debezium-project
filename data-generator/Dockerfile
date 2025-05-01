FROM python:3.11-slim
WORKDIR /app
COPY generator.py .
RUN pip install psycopg2-binary
CMD ["python", "generator.py"]