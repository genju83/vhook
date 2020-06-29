FROM openjdk:8u212-alpine

RUN apk update && \
    apk add tzdata ca-certificates && \
    mkdir /app && \
    rm -rf /var/cache/apk/* && \
    update-ca-certificates

ADD ./build/libs/vhook-all.jar /app/vhook.jar
WORKDIR /app

EXPOSE 8506

CMD ["java", "-jar", "/app/vhook.jar"]
