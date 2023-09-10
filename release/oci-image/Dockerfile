FROM docker.io/eclipse-temurin:17-jre AS tools

COPY tmp/*-linux-* /tools/

# Only keep the binaries for the current architecture
RUN for thirdparty in ./tools/*-linux-$(uname -m); \
do \
    cp ${thirdparty} ${thirdparty%-linux-$(uname -m)}; \
done; \
rm -rf /tools/*-linux-*


FROM docker.io/eclipse-temurin:17-jre

RUN apt update && apt upgrade -y && apt install -y libraqm0 liblqr-1-0 libdjvulibre21 libwebpmux3 libwebpdemux2 libjpeg62 libheif1 libpango-1.0-0 libpangocairo-1.0-0 libraw20 libcgraph6 libgvc6 libopenexr25 libzip4 libzstd1 libtiff5 libxrender1 libxtst6 libxi6 libxxf86vm1 mesa-utils

RUN mkdir -p /opt/tinyworld

WORKDIR /opt/tinyworld

ENV PATH=/opt/tinyworld/tools:$PATH

COPY --from=tools /tools ./tools
COPY tmp/LICENSE* .
COPY tmp/deps ./deps
COPY tinyworld.sh .
COPY tmp/config ./config
COPY tmp/*.jar .

RUN chmod +x ./tools/*
RUN chmod +x tinyworld.sh

CMD ["./tinyworld.sh"]
