# 📋 Planejamento de Engenharia: Melhorias de Gameplay & Sprint 4
## Space Defender: Galaxy Core
**Equipe:** Felipe Lopes & Bianca  
**Orientação:** Tech Lead Sênior em Java & Professor Orientador  
**Data:** 24 de Novembro de 2026  
**Status:** Em Planejamento / Execução

---

## 🎯 1. Parecer Técnico: Verificação do Boss da Bianca (`BossShip`)

Avaliamos a implementação realizada pela Bianca no commit `4d42847` (PR #22 integrado na `develop`):

### 🌟 Pontos Fortes:
1. **Herança & Polimorfismo Exemplares:** A classe `BossShip extends EnemyShip` honra os 4 pilares da POO, sobrescrevendo `update()` e `draw(Graphics2D g2d)`.
2. **Máquina de Estados de 2 Fases (Modo Fúria):** Transição fluida ativada ao atingir 50% de HP (`getHealth() <= getMaxHealth() / 2`), acelerando a velocidade horizontal de 2.0 para 5.0 e alterando a aura de roxo para vermelho pulsante.
3. **Identidade Visual Imponente:** Casco com polígonos customizados, garras frontais, olhos alienígenas e barra de vida cinematográfica centralizada no topo da tela.
4. **Integração no `WaveManager`:** Configurado para surgir automaticamente na Onda 5 (`BOSS_WAVE = 5`).

### ⚙️ Ajustes de Calibração Necessários:
* **Equilíbrio de HP:** A vida foi configurada em `6000 HP`. Com disparos de 10 (Laser) ou 25 (Plasma), o jogador precisaria acertar entre 240 a 600 tiros. Recomendamos calibrar para **1200 a 1500 HP** para garantir um combate dinâmico, tenso e justo.
* **Ataque Ativo do Boss:** Integrar disparos de projéteis inimigos partindo das garras do Boss.
* **Gatilho de Vitória:** Conectar a destruição do Boss ao estado `VICTORY` do jogo.

---

## 🗺️ 2. Mapa de Melhorias & Estrutura de Branches (GitFlow)

Todas as branches serão geradas a partir da `develop` e integradas via Pull Requests semânticos:

```mermaid
gitGraph
   commit id: "develop (Sprint 3 + Boss)"
   branch fix/gameplay-controls-movement
   checkout fix/gameplay-controls-movement
   commit id: "fix(controls): permite atirar enquanto se move"
   checkout develop
   merge fix/gameplay-controls-movement
   branch feat/gameplay-enemy-pacing
   checkout feat/gameplay-enemy-pacing
   commit id: "feat(balance): inimigos mais lentos e maior densidade"
   checkout develop
   merge feat/gameplay-enemy-pacing
   branch feat/visuals-parallax-background
   checkout feat/visuals-parallax-background
   commit id: "feat(visuals): fundo estelar animado com parallax"
   checkout develop
   merge feat/visuals-parallax-background
   branch feat/enemies-mechanics-expansion
   checkout feat/enemies-mechanics-expansion
   commit id: "feat(enemies): novas classes e mecanicas variadas"
   checkout develop
   merge feat/enemies-mechanics-expansion
   branch feat/powerups-system
   checkout feat/powerups-system
   commit id: "feat(powerups): 5 power-ups e dois modos de obtencao"
   checkout develop
   merge feat/powerups-system
   branch feat/audio-sound-effects
   checkout feat/audio-sound-effects
   commit id: "feat(audio): motor sonoro com controle de volume"
   checkout develop
   merge feat/audio-sound-effects
   branch feat/ui-menu-gameover-arcade
   checkout feat/ui-menu-gameover-arcade
   commit id: "feat(ui): menu principal, continue e arcade high scores"
   checkout develop
   merge feat/ui-menu-gameover-arcade
```

---

## 🛠️ 3. Detalhamento Técnico das Tarefas

### 🎮 Tarefa A: Correção de Movimentação e Disparo Simultâneo
* **Branch:** `fix/gameplay-controls-movement`
* **Diagnóstico da Causa Raiz:** O disparo estava atrelado diretamente ao evento atômico `keyPressed` do Swing `KeyEvent.VK_SPACE`. Ao segurar setas de direção, o envio de eventos de teclado do sistema operacional pode atrasar ou cancelar o envio repetido de teclas secundárias.
* **Solução:**
  1. Adicionar a flag booleana `spacePressed` em `PlayerShip`.
  2. Ativar `spacePressed = true` no `keyPressed` e `false` no `keyReleased`.
  3. Desacoplar a chamada `shoot()` para o método `update()` da `PlayerShip`, garantindo disparos perfeitamente sincronizados aos 60 FPS enquanto a nave se desloca livremente em qualquer direção.

---

### ⚖️ Tarefa B: Rebalanceamento de Cadência e Densidade de Inimigos
* **Branch:** `feat/gameplay-enemy-pacing`
* **Objetivo:** Inimigos descem de forma mais compassada, porém em esquadrões mais numerosos.
* **Implementação:**
  1. Reduzir a velocidade de descida:
     - `ScoutShip`: de 3 para 1.8 px/frame.
     - `ArmoredCruiser`: de 1 para 0.8 px/frame.
  2. Aumentar a densidade de spawn no `WaveManager`:
     - Reduzir o intervalo entre surgimentos para 1000ms a 1400ms.
     - Aumentar a quantidade de inimigos simultâneos por onda para gerar combate de enxame espacial.

---

### 🌌 Tarefa C: Design Visual — Fundo Espacial com Parallax Multi-camadas
* **Branch:** `feat/visuals-parallax-background`
* **Objetivo:** Eliminar o fundo preto estático e criar profundidade cinematográfica em movimento contínuo.
* **Arquitetura (`core/SpaceBackground.java`):**
  1. **Camada 1 (Profundidade Lenta - 0.3 px/frame):** Nebulosas espaciais com gradientes radiais suaves e planetas distantes desenhados proceduralmente.
  2. **Camada 2 (Estrelas Médias - 1.2 px/frame):** Conjunto de 80 estrelas pequenas com cintilação de brilho.
  3. **Camada 3 (Estrelas Próximas - 2.8 px/frame):** 30 estrelas brilhantes com feixe de arrasto dando sensação de velocidade da nave.
  4. Redesenho automático no topo da tela ao atingir o limite inferior (`y > 600`), garantindo zero vazamento de memória.

---

### 👾 Tarefa D: Expansão de Mecânicas dos Inimigos (Mínimo de 5 Mecânicas)
* **Branch:** `feat/enemies-mechanics-expansion`
* **Novas Mecânicas e Subclasses Polimórficas:**
  1. **Mecânica 1 - Mergulho Agressivo (`KamikazeDrone`):** Inimigo triangular veloz que trava a mira na posição X da nave do jogador e acelera em mergulho vertical.
  2. **Mecânica 2 - Disparo Inimigo (`SniperVessel` / `EnemyProjectile`):** Inimigo de suporte que mantém distância na metade superior da tela e dispara pulsos de energia contra o jogador.
  3. **Mecânica 3 - Escudo Defletor Temporário:** Naves blindadas que ativam um campo de força temporário por 2 segundos, absorvendo disparos sem sofrer dano.
  4. **Mecânica 4 - Esquiva Evasiva Lateral:** Naves ágeis que detectam projéteis próximos e saltam lateralmente para desviar do tiro.
  5. **Mecânica 5 - Detonação em Estilhaços:** Ao ser destruída, a nave projeta pequenos fragmentos que desaparecem após curto alcance.

---

### 💊 Tarefa E: Sistema de Power-Ups (5 Tipos Distintos & 2 Vias de Obtenção)
* **Branch:** `feat/powerups-system`
* **Superclasse Abstrata:** `PowerUp extends GameObject` no pacote `br.com.spacedefender.powerups`.
* **5 Power-Ups Distintos:**
  1. 💊 **DoubleScorePill (Pílula 2x):** Dobra todos os pontos acumulados durante 15 segundos.
  2. 💖 **HealthRepairPack (Reparo de Escudo):** Recupera instantaneamente 50% de HP.
  3. 🅰️ **TripleShotItem (Tiro Triplo 'A'):** Dispara 3 feixes de laser/plasma em leque simultâneos por 12 segundos.
  4. 🛡️ **ShieldBarrier (Escudo Protetor):** Cria uma bolha de proteção ao redor da nave que absorve o próximo impacto direto.
  5. 💣 **NovaBomb (Bomba Nuclear):** Aniquila instantaneamente todos os inimigos normais na tela visível.
* **Dois Modos Distintos de Obtenção:**
  * **Origem 1 (Morte de Inimigos):** Pílula 2x e Pacote de Vida caem (*drop*) do ponto exato onde o inimigo foi destruído (chance percentual equilibrada).
  * **Origem 2 (Descida Espacial):** Tiro Triplo, Escudo e Bomba Nuclear surgem do topo da tela e flutuam suavemente para baixo junto às ondas invasoras.

---

### 🔊 Tarefa F: Efeitos Sonoros Imersivos (`SoundManager`)
* **Branch:** `feat/audio-sound-effects`
* **Tecnologia:** `javax.sound.sampled` (API nativa da biblioteca padrão Java, garantindo portabilidade sem dependências externas instáveis).
* **Mapeamento de Áudios:**
  * `shoot_laser.wav`: Disparo agudo veloz.
  * `shoot_plasma.wav`: Disparo grave e ressonante.
  * `enemy_hit.wav`: Impacto metálico.
  * `explosion.wav`: Detonação de naves inimigas.
  * `powerup_collect.wav`: Efeito agudo ascendente retro.
  * `player_damage.wav`: Alerta sonoro de alarme de dano.
  * `game_over.wav`: Jingle dramático de encerramento.
* **Controle de Volume:**
  * Implementação de ganho em decibéis via `FloatControl.Type.MASTER_GAIN`, permitindo controle em escala de 0% a 100%.

---

### 🕹️ Tarefa G: Menu Principal, "CONTINUE?" e Arcade High Scores Retro
* **Branch:** `feat/ui-menu-gameover-arcade`
* **Máquina de Estados (`GameState`):**
  `MENU`, `PLAYING`, `CONTINUE_SCREEN`, `GAME_OVER_ARCADE`, `HIGH_SCORES`, `SETTINGS`, `VICTORY`.
* **Menu Principal:**
  1. **Novo Jogo:** Inicia a partida zerando pontuação e resetando ondas.
  2. **Score (Hall da Fama):** Tabela retro listando os 10 melhores recordes registrados.
  3. **Configuração:** Ajuste de volume de som (0 a 100%) com barra interativa.
* **Fluxo da Morte e "CONTINUE?":**
  1. Ao zerar a barra de HP:
     * Se `lives > 0`: Pausa o jogo e exibe a tela **"CONTINUE?"** com contagem regressiva de 10 segundos e opções **[SIM]** ou **[NÃO]**.
       * Escolhendo **[SIM]**: Consome 1 vida, regenera 100% do HP, limpa a área de colisão imediata e **mantém a pontuação acumulada**!
       * Escolhendo **[NÃO]**: Encerra a partida e direciona para a tela de recordes.
     * Se `lives == 0`: Vai direto para o encerramento da partida.
  2. **Arcade High Scores (3 Letras):**
     * Caso o jogador atinja uma pontuação relevante, exibe a interface clássica de fliperama:
       * Seleção da tag de 3 letras (ex: `FEL`, `BIA`, `ACE`) usando setas Cima/Baixo para trocar letras e Direita/Enter para confirmar.
       * Persistência dos dados em arquivo local (`highscores.dat`) para promover competição permanente entre os jogadores.
       * Ao concluir, redireciona suavemente de volta para o **Menu Principal**.

---

## 🚀 4. Cronograma de Execução Sequencial

| Ordem | Branch | Escopo Principal |
| :--- | :--- | :--- |
| **1** | `fix/gameplay-controls-movement` | Correção do tiro simultâneo ao movimento |
| **2** | `feat/gameplay-enemy-pacing` | Inimigos mais lentos e aumento de quantidade/densidade |
| **3** | `feat/visuals-parallax-background` | Fundo espacial em movimento com estrelas em camadas |
| **4** | `feat/enemies-mechanics-expansion` | 5+ mecânicas progressivas e novos tipos de inimigos |
| **5** | `feat/powerups-system` | 5 Power-ups (drop de inimigos e descida do topo) |
| **6** | `feat/audio-sound-effects` | Motor de áudio `SoundManager` com controle de volume |
| **7** | `feat/ui-menu-gameover-arcade` | Menu principal, Continue, Arcade High Scores e Game Over |

