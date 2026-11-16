# Como executar — LogiTech

- **JDK:** 8 ou superior (desenvolvido e testado com OpenJDK 21).
  O código usa apenas recursos do Java 8; a compilação com `--release 8` não pôde ser
  verificada no ambiente de desenvolvimento (ver `AUDITORIA.md`).
- **Classe principal:** `logitech.Main` (o projeto usa pacotes).
- **Entrada:** entrada padrão (`System.in`), uma encomenda por linha,
  `ID;MODAL;PESO_KG;DISTANCIA_KM;ORIGEM;DESTINO;PERFIL`.

## Linux / macOS
```sh
cd logitech                      # raiz do projeto (onde está a pasta src)
mkdir -p out
javac -d out $(find src -name '*.java')
java -cp out logitech.Main < entrada.txt
```
Atalhos: `./compilar.sh` (só compila) e `./testar.sh` (compila, roda os testes e compara com `test/dados`).

## Windows (PowerShell)
```powershell
cd logitech
mkdir out
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
cmd /c "java -cp out logitech.Main < entrada.txt"
```

## Windows (Prompt de Comando)
```bat
cd logitech
mkdir out
dir /s /b src\*.java > fontes.txt
javac -d out @fontes.txt
java -cp out logitech.Main < entrada.txt
```

## Testes automatizados (sem dependências externas)
```sh
javac -d out -cp out test/logitech/TestesLogiTech.java
java -cp out logitech.TestesLogiTech     # executar da raiz do projeto
```
Saída esperada: `<N> verificacoes OK, 0 falhas` (código de saída 0).
Os arquivos `test/dados/*.txt` e `*.esperado` são o exemplo do enunciado (seção 5.2), o gabarito
(5.3), os casos de borda e a lista vazia; o `testar.sh` compara a saída do programa com cada `.esperado`.

## Exemplo
```sh
java -cp out logitech.Main < entrada_exemplo.txt
VALIDA;1;1445,00;23,33;31,00
REJEITADA;2;peso invalido
REJEITADA;LINHA4;formato invalido
VALIDA;3;2425,50;136,40;67,20
RELATORIO;3870,50;79,87;98,20;2;2
```
