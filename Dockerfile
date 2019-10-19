FROM ananasanalytics/ananas-ci-base:1.0.0

WORKDIR /ananas-desktop

COPY . .

RUN git clone https://github.com/ananas-analytics/ananas-examples
RUN cd ui && yarn install

# this command will be overrided in docker-compose
CMD echo 'ananas'
