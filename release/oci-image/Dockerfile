FROM docker.io/eclipse-temurin:17-jre

RUN apt update && apt upgrade -y && apt install -y libraqm0 liblqr-1-0 libdjvulibre21 libwebpmux3 libwebpdemux2 libheif1 libpango-1.0-0 libpangocairo-1.0-0 libraw20 libcgraph6 libgvc6 libopenexr25 libzip4 libzstd1 libtiff5 libxrender1 libxtst6 libxi6 libxxf86vm1 mesa-utils

RUN mkdir -p /opt/tinyworld

WORKDIR /opt/tinyworld

ENV PATH=/opt/tinyworld/tools:$PATH

COPY tmp/magick ./tools/magick
COPY tmp/LICENSE* .
COPY tmp/deps ./deps
COPY tinyworld.sh .
COPY tmp/config ./config
COPY tmp/*.jar .

RUN chmod +x ./tools/magick
RUN chmod +x tinyworld.sh

CMD ["./tinyworld.sh"]
