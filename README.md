# Head Ball ⚽

Gioco di calcio 2D "a testoni" in **Java (Swing)**: si gioca da soli contro un bot oppure **in due in rete**, con un'architettura **client/server** scritta da zero.

Progetto universitario (Università degli Studi di Brescia) realizzato da **Davide Ruggeri** e **Simone Musatti**.


<p align="center"><img width="600" alt="Parita" src="https://github.com/user-attachments/assets/6c26e7a3-1db8-43a9-b9c1-4efa83cd71c7"/></p>

## Modalità di gioco

- **Single player**: partita contro un **bot** che insegue la palla e salta per colpirla, con un tempo di reazione per non renderlo imbattibile.
- **Host game**: avvia il server sul proprio computer e si unisce alla partita.
- **Join game**: si collega al server di un altro giocatore inserendo il suo indirizzo IP.

Ogni partita dura **90 secondi** e parte con un conto alla rovescia di 3 secondi.

**Comandi:** frecce per muoversi · `Spazio` per saltare · `Z` per calciare

## Aspetti tecnici

- **Server autoritativo**: tutta la simulazione gira sul server, a circa **60 aggiornamenti al secondo**. I client inviano solo i comandi (movimento, salto, tiro) e ricevono lo stato aggiornato della partita.
- **Rete con socket TCP** e oggetti serializzati (`NetworkMessage`, `ClientCommand`). Il server usa un thread per ogni client connesso.
- **Fisica fatta a mano**: vettori 2D, gravità, rimbalzi e **collisioni con il Separating Axis Theorem** (proiezioni sugli assi e vettore di traslazione minima, `MTV`).
- **Programmazione a oggetti**: gerarchia `Oggetto` → `Giocatore` → `Bot`, con `Ball`, campo di gioco e stato di partita separati dalla grafica.

## Struttura del progetto

```
src/it/unibs/pajc/
├── game/        Logica di gioco, entità (giocatori, palla, bot), grafica e menu
│   └── physic/  Vettori, proiezioni e collisioni (SAT)
├── server/      Server di gioco: game loop, timer, gestione dei client
├── client/      Client di rete e comandi inviati al server
└── network/     Messaggi scambiati tra server e client
src/images/      Sfondo e sprite dei giocatori
```

## Come avviarlo

Serve **Java 22 o superiore**.

**Con IntelliJ IDEA:** apri la cartella, segna `src` come *Sources Root* e avvia `it.unibs.pajc.game.HeadBallApp`.

**Da terminale:**

```bash
javac -d out $(find src -name "*.java")
cp -r src/images out/
java -cp out it.unibs.pajc.game.HeadBallApp
```

Per giocare in rete, entrambi i computer devono essere sulla stessa rete. Il server usa la porta **1234**.
