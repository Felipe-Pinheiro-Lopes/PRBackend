# 🚀 Space Defender: Galaxy Core

> **Jogo 2D em Java Puro (Swing / AWT) aplicando os 4 Pilares da Programação Orientada a Objetos (POO).**

Projeto prático desenvolvido para a disciplina **Desenvolvimento Back-End - JAVA** ministrada pelo Prof. Cainã Antunes.

---

## 👥 Equipe & Papéis

| Integrante | Função / Responsabilidade Principal | GitHub |
| :--- | :--- | :--- |
| **Felipe Lopes** | Arquitetura de Core, Game Loop, Janela, Colisões e Máquina de Estados | [@Felipe-Pinheiro-Lopes](https://github.com/Felipe-Pinheiro-Lopes) |
| **Bianca** | Modelagem de Entidades, Hierarquia de Tiros, Inimigos e Boss Final | Integrante da Dupla |

---

## 🎯 Objetivo Pedagógico & 4 Pilares da POO

1. **Abstração:** Classes e métodos abstratos (`GameObject`, `Projectile`, `EnemyShip`) definindo contratos claros de atualização (`update()`) e renderização (`render(g)`).
2. **Encapsulamento:** Atributos protegidos por modificadores `private`/`protected`, manipulados exclusivamente via métodos de acesso e validação (getters/setters).
3. **Herança:** Reutilização de código e especialização de entidades (`PlayerShip`, `LaserPulse`, `PlasmaBall`, `ScoutShip`, `ArmoredCruiser`, `BossShip`).
4. **Polimorfismo:** Game loop genérico iterando sobre coleções polimórficas sem necessidade de verificações condicionais de tipo (`instanceof`).

---

## 📅 Planejamento & Gestão

* 🗺️ **[Cronograma Completo e Prompts para NotebookLM](doc/Cronograma_SpaceDefender_Felipe_Bianca.md)**
* 🛡️ **[Diretrizes de Engenharia, GitFlow e Padrões](doc/Diretrizes_Equipe_SpaceDefender.md)**
* 📋 **[GitHub Projects Board #10](https://github.com/users/Felipe-Pinheiro-Lopes/projects/10/views/1)**
* 🎫 **[Issues do Repositório](https://github.com/Felipe-Pinheiro-Lopes/PRBackend/issues)**

---

## 🐳 Ambiente Padronizado com Docker (Webtop XFCE)

Assim como no projeto **Dockermine**, disponibilizamos um ambiente completo dentro de um container com **Linux (Ubuntu XFCE)**, **Java 21**, **VS Code** e aceleração gráfica, permitindo programar e jogar diretamente pelo navegador:

### Como Iniciar o Ambiente Docker:
```bash
cd docker
docker compose up -d
```

Acesse pelo navegador web:
* **HTTP:** [http://localhost:3000](http://localhost:3000)
* **HTTPS:** [https://localhost:3001](https://localhost:3001)

O código deste repositório estará mapeado automaticamente dentro do container na pasta `/config/workspace`. Qualquer alteração salva no container é refletida instantaneamente na máquina host!

---

## ⌨️ Como Executar Localmente (Sem Docker)

Requisitos:
* **JDK 21** ou superior instalado
* IDE de sua preferência (VS Code com *Extension Pack for Java* ou IntelliJ IDEA)

Compilação e execução via terminal:
```bash
# Compilar todas as classes
javac -d bin -sourcepath src src/br/com/spacedefender/Main.java

# Executar o jogo
java -cp bin br.com.spacedefender.Main
```
