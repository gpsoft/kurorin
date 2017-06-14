FROM openjdk:8-jre-alpine

# You've got to make some decisions:
#   8-jdk-alpine?         ...failed because of musl libc problem
#   java7?                ...some libs(like boot-cljs) need java8
#   wget from github?     ...couldn't verify cert
#   curl instead of wget? ...crashed with option -L

# RUN apk update && apk add --no-cache wget openssl

# Kindlegen
RUN mkdir -p /tmp/kin
WORKDIR /tmp/kin
RUN wget http://kindlegen.s3.amazonaws.com/kindlegen_linux_2.6_i386_v2_9.tar.gz \
         -O kindlegen.tar.gz \
    && tar xvzf kindlegen.tar.gz \
    && cp kindlegen /usr/local/bin/ \
    && rm -rf *

# Boot
# RUN wget https://github.com/boot-clj/boot-bin/releases/download/latest/boot.sh \
#          -O /usr/local/bin/boot --no-check-certificate \
#     && chmod 755 /usr/local/bin/boot
# ENV BOOT_HOME /.boot
# ENV BOOT_AS_ROOT yes
# ENV BOOT_LOCAL_REPO /m2

# Kurorin App
RUN mkdir -p /tmp/kurorinapp
WORKDIR /tmp/kurorinapp
COPY app-config.edn .
COPY kurorin.jar .

# Kurorin work dir
RUN mkdir -p /tmp/kurorin

CMD ["java", "-Dconf=app-config.edn", "-jar", "kurorin.jar"]

