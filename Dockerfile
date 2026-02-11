FROM eclipse-temurin:21-jre-alpine

# Устанавливаем supervisor
RUN apk add --no-cache supervisor

WORKDIR /app

# Копируем JAR файлы
COPY target/budget-0.0.1-SNAPSHOT-main.jar main-app.jar
COPY target/budget-0.0.1-SNAPSHOT-telegram.jar telegram-bot.jar

# Конфигурация supervisor
COPY supervisord.conf /etc/supervisor.d/supervisord.ini

EXPOSE 8080 8081

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor.d/supervisord.ini"]