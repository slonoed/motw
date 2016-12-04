FROM clojure:alpine

# Build sassc
RUN apk --update add curl g++ build-base && \
    curl -L https://github.com/sass/libsass/archive/3.3.6.tar.gz | tar -xvz -C /usr/local && \
    curl -L https://github.com/sass/sassc/archive/3.3.6.tar.gz | tar -xvz -C /usr/local && \
    SASS_LIBSASS_PATH=/usr/local/libsass-3.3.6 make BUILD=static -C /usr/local/sassc-3.3.6 && \
    cp /usr/local/sassc-3.3.6/bin/sassc /usr/local/bin/sassc && \
    rm -rf /usr/local/sassc-3.3.6 /usr/local/libsass-3.3.6

ADD project.clj /src/project.clj
WORKDIR /src
RUN lein deps
ADD . /src
RUN lein with-profile ui cljsbuild once prod
RUN mkdir -p resources/public/css/ && /usr/local/bin/sassc -t compressed scss/core.scss resources/public/css/motw.css

EXPOSE 8080

CMD lein with-profile prod run
