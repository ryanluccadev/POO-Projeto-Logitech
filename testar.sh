#!/bin/sh
# Compila, roda os testes automatizados e compara a saida do programa (java Main < arquivo)
# com os arquivos esperados em test/dados. Uso: ./testar.sh (na raiz do projeto)
set -e
./compilar.sh
javac -d out -cp out test/logitech/TestesLogiTech.java
java -cp out logitech.TestesLogiTech
falha=0
for esperado in test/dados/*.esperado; do
    base="${esperado%.esperado}"
    if java -cp out logitech.Main < "$base.txt" | cmp -s - "$esperado"; then
        echo "OK   $(basename "$base")"
    else
        echo "FALHOU $(basename "$base")"; falha=1
    fi
done
exit $falha
