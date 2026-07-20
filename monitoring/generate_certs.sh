#!/usr/bin/env bash

if [[ ! $(command -v openssl) ]]; then
    echo "[!] install openssl"
    exit 1
fi

entities=( "ca" "victoriametrics" )

for entity in "${entities[@]}"; do
    echo "[...] generating certificate for \"$entity\""

    if [[ -f "$entity.crt" && -f "$entity.key" ]]; then
        continue
    fi

    if [[ $entity = "ca" ]]; then
        openssl req -new -newkey rsa:4096 -nodes -x509 -config openssl.conf -section ca -out ca.crt -keyout ca.key
        continue
    fi

    openssl req -new -newkey rsa:4096 -nodes -x509 -CA ca.crt -CAkey ca.key -config openssl.conf -section "$entity" -out "$entity.crt" -keyout "$entity.key"
done

echo "[+] done!"
