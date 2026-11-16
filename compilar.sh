#!/bin/sh
# Compila o programa em out/. Uso: ./compilar.sh
set -e
mkdir -p out
javac -d out $(find src -name '*.java')
echo "Compilado em out/ (classe principal: logitech.Main)"
