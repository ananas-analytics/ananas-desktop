FROM flink:1.8.2

COPY ./flink-conf.yaml /opt/flink/conf

EXPOSE 8081 6123

CMD ["/bin/bash", "/opt/fink/bin/jobmanager.sh", "start", "0.0.0.0", "6123"]
