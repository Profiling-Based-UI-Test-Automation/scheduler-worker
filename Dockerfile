FROM rabbitmq:3.7.8-management-alpine

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8

RUN apk add --no-cache openjdk8

ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin

EXPOSE 8080

RUN mkdir -p /root/scheduler-worker
COPY . /root/scheduler-worker

WORKDIR /root/scheduler-worker

RUN chmod +x /root/scheduler-worker/entry_point.sh
