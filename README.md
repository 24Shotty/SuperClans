# SuperClans

Plugin completo e robusto per Bukkit/Spigot per la gestione di clan con sistema di territori in Minecraft. SuperClans fornisce una soluzione completa per gli amministratori di server che desiderano implementare un gameplay basato sui clan con funzionalità avanzate di protezione e gestione dei territori.

## Caratteristiche

### Gestione Clan
- **Creazione e gestione clan** con nomi e tag personalizzati
- **Gerarchia basata su ruoli** (Leader, Officer, Member) con permessi differenziati
- **Sistema di inviti** con scadenza e conferma
- **Gestione membri** con funzionalità di promozione/retrocessione ed espulsione
- **Chat clan privata** separata dalla chat globale
- **Sistema home clan** con ritardo di teletrasporto configurabile

### Sistema Territori
- **Sistema di claim flessibile** che supporta claim singoli o multipli di chunk
- **Doppia modalità di protezione**: integrazione WorldGuard o sistema di protezione custom
- **Perimetro visivo del territorio** con materiali e durata personalizzabili
- **Protezioni automatiche** per:
  - Rottura/posizionamento blocchi
  - PvP (configurabile)
  - Spawn dei mob (configurabile)
  - Esplosioni
  - Propagazione del fuoco
  - Operazioni con pistoni
  - Flusso di fluidi
- **Ripristino territorio** su unclaim (ripristino blocchi stile undo)

### Caratteristiche Tecniche
- **Database MariaDB/MySQL** con connection pooling (HikariCP)
- **Integrazione PlaceholderAPI** per placeholder dinamici
- **Compatibilità WorldGuard** per gestione avanzata delle regioni
- **Architettura modulare** con separazione pulita delle responsabilità
- **Configurazione completa** tramite file YAML
- **Supporto multi-lingua** pronto (messages.yml)

## Requisiti

- **Server Minecraft**: Spigot/Paper 1.20.1 o superiore
- **Java**: 17 o superiore
- **Database**: MariaDB 10.3+ o MySQL 8.0+
- **Dipendenze Opzionali**:
  - PlaceholderAPI (per i placeholder)
  - WorldGuard 7.0+ (per protezione basata su WorldGuard)

## Installazione

1. **Scarica il plugin**
   - Scarica l'ultima release dalla pagina [Releases](https://github.com/yourusername/SuperClans/releases)
   - Oppure compila dal sorgente (vedi [Compilazione dal Sorgente](#compilazione-dal-sorgente))

2. **Installa il plugin**
   - Posiziona `SuperClans.jar` nella cartella `plugins` del tuo server
   - Avvia o riavvia il server
   - Il plugin genererà automaticamente i file di configurazione

3. **Configura il database**
   - Crea un nuovo database nel tuo server MariaDB/MySQL
   - Modifica `plugins/SuperClans/config.yml` e configura la connessione al database:
     ```yaml
     database:
       type: mariadb  # o mysql
       host: localhost
       port: 3306
       name: superclans
       username: tuo_username
       password: tua_password
       table-prefix: sc_
     ```

4. **Riavvia il server**
   - Il plugin creerà automaticamente le tabelle del database necessarie
   - Controlla la console per i messaggi di inizializzazione

## Configurazione

### Configurazione Database

Il plugin utilizza MariaDB/MySQL per la persistenza dei dati. Configura la connessione al database in `config.yml`:

```yaml
database:
  type: mariadb              # mariadb o mysql
  host: localhost            # Host del database
  port: 3306                 # Porta del database
  name: superclans           # Nome del database
  username: root             # Username del database
  password: password         # Password del database
  table-prefix: sc_         # Prefisso tabelle (utile per database condivisi)
  pool-size: 10              # Dimensione del pool di connessioni
  connection-timeout: 30000  # Timeout connessione in millisecondi
```

### Impostazioni Clan

```yaml
clan:
  min-name-length: 3         # Lunghezza minima nome clan
  max-name-length: 20        # Lunghezza massima nome clan
  min-tag-length: 2          # Lunghezza minima tag
  max-tag-length: 5          # Lunghezza massima tag
  max-members: 20            # Massimo numero di membri per clan (0 = illimitato)
  max-claims: 10             # Massimo numero di territori per clan
  allowed-name-chars: "[a-zA-Z0-9_]"  # Caratteri permessi regex
  allowed-tag-chars: "[a-zA-Z0-9]"    # Caratteri permessi per tag regex
```

### Impostazioni Territorio

```yaml
territory:
  use-worldguard: true       # Usa WorldGuard per la protezione (richiede WorldGuard)
  claim-radius-chunks: 0     # Raggio claim: 0 = 1 chunk, 1 = 3x3, 2 = 5x5, ecc.
  admin-bypass: true         # Permetti agli admin di bypassare le protezioni
  outline:
    enabled: true            # Abilita perimetro visivo territorio
    material: GREEN_CONCRETE # Materiale del perimetro
    duration-seconds: 0      # Durata perimetro (0 = permanente)
  protections:
    block-break: true        # Proteggi dalla rottura blocchi
    block-place: true        # Proteggi dal posizionamento blocchi
    pvp: false               # Proteggi dal PvP
    mob-spawning: false      # Previeni spawn mob
    explosions: true         # Proteggi dalle esplosioni
    fire-spread: true        # Previeni propagazione fuoco
```

### Configurazione Chat

```yaml
chat:
  format: "&8[&6{tag}&8] &7[{role}] &f{player}&8: &a{message}"
  global-prefix: true
  global-format: "&8[&6{tag}&8] &f{player}&7: &f{message}"
```

## Comandi

### Gestione Clan

| Comando | Descrizione | Permesso | Ruolo Richiesto |
|---------|-------------|----------|-----------------|
| `/clan create <nome> <tag>` | Crea un nuovo clan | `superclans.clan.create` | Nessuno |
| `/clan disband [confirm]` | Sciogli il tuo clan | `superclans.clan.disband` | Leader |
| `/clan invite <giocatore>` | Invita un giocatore nel tuo clan | `superclans.clan.invite` | Officer+ |
| `/clan accept` | Accetta un invito al clan | `superclans.clan.invite` | Nessuno |
| `/clan deny` | Rifiuta un invito al clan | `superclans.clan.invite` | Nessuno |
| `/clan kick <giocatore>` | Espelli un membro dal tuo clan | `superclans.clan.kick` | Officer+ |
| `/clan promote <giocatore>` | Promuovi un membro a Officer | `superclans.clan.promote` | Leader |
| `/clan demote <giocatore>` | Retrocedi un Officer a Member | `superclans.clan.demote` | Leader |
| `/clan info [clan]` | Visualizza informazioni del clan | `superclans.clan.info` | Nessuno |

### Gestione Territori

| Comando | Descrizione | Permesso | Ruolo Richiesto |
|---------|-------------|----------|-----------------|
| `/clan claim` | Reclama territorio nella tua posizione | `superclans.clan.claim` | Officer+ |
| `/clan unclaim` | Libera territorio nella tua posizione | `superclans.clan.unclaim` | Officer+ |

### Sistema Home

| Comando | Descrizione | Permesso | Ruolo Richiesto |
|---------|-------------|----------|-----------------|
| `/clan home` | Teletrasportati alla home del clan | `superclans.clan.home` | Member+ |
| `/clan sethome` | Imposta la posizione home del clan | `superclans.clan.sethome` | Leader |

### Chat

| Comando | Descrizione | Permesso | Ruolo Richiesto |
|---------|-------------|----------|-----------------|
| `/clan chat <messaggio>` | Invia messaggio nella chat clan | `superclans.clan.chat` | Member+ |
| `/clan c <messaggio>` | Alias breve per chat clan | `superclans.clan.chat` | Member+ |

### Alias

- `/clans` - Alias per `/clan`
- `/c` - Alias per `/clan`

## Placeholders (PlaceholderAPI)

Se PlaceholderAPI è installato, sono disponibili i seguenti placeholder:

| Placeholder | Descrizione | Esempio |
|-------------|-------------|---------|
| `%clans_player_clan%` | Nome del clan del giocatore | `MyClan` |
| `%clans_player_tag%` | Tag del clan del giocatore | `MC` |
| `%clans_player_role%` | Ruolo del giocatore nel clan | `Leader` |
| `%clans_clan_members_online%` | Numero di membri online del clan | `5` |
| `%clans_clan_members_total%` | Numero totale di membri del clan | `12` |
| `%clans_clan_territories%` | Numero di territori reclamati | `8` |
| `%clans_has_clan%` | Se il giocatore è in un clan | `true` |

### Esempio di Utilizzo

```
/clan info %clans_player_clan%
```

## Permessi

Tutti i permessi sono impostati di default su `true` (tutti i giocatori possono usarli). Usa un permission manager come LuckPerms per personalizzare l'accesso.

### Permessi Base

- `superclans.clan.create` - Creare un clan
- `superclans.clan.disband` - Sciogliere un clan
- `superclans.clan.invite` - Invitare giocatori
- `superclans.clan.kick` - Espellere membri
- `superclans.clan.promote` - Promuovere membri
- `superclans.clan.demote` - Retrocedere membri
- `superclans.clan.chat` - Usare la chat clan
- `superclans.clan.claim` - Reclamare territori
- `superclans.clan.unclaim` - Liberare territori
- `superclans.clan.home` - Teletrasportarsi alla home clan
- `superclans.clan.sethome` - Impostare la home clan
- `superclans.clan.info` - Visualizzare info clan

### Permesso Admin

- `superclans.admin` - Concede tutti i permessi e bypassa le protezioni dei territori (default: op)

## Schema Database

Il plugin crea tre tabelle principali:

### `sc_clans`
Memorizza le informazioni del clan (ID, nome, tag, leader, posizione home, data di creazione).

### `sc_members`
Memorizza l'appartenenza al clan (UUID giocatore, ID clan, ruolo, data di ingresso).

### `sc_territories`
Memorizza i territori reclamati (ID territorio, ID clan, mondo, coordinate chunk, nome regione WorldGuard).

Tutte le tabelle utilizzano foreign key con eliminazione CASCADE per l'integrità dei dati.

## Compilazione dal Sorgente

### Prerequisiti

- Java 17 o superiore
- Maven 3.6+

### Passaggi di Compilazione

1. **Clona il repository**
   ```bash
   git clone https://github.com/yourusername/SuperClans.git
   cd SuperClans
   ```

2. **Compila il progetto**
   ```bash
   mvn clean package
   ```

3. **Trova il JAR compilato**
   - Il plugin compilato sarà in `target/SuperClans-1.0.jar`

### Setup Sviluppo

1. **Configura Spigot API**
   - Il progetto utilizza Spigot API 1.20.1
   - Maven scaricherà automaticamente le dipendenze

2. **Configura IDE**
   - Importa come progetto Maven
   - Imposta Java SDK a 17
   - Configura il build path se necessario

## Struttura del Progetto

```
SuperClans/
├── src/
│   └── main/
│       ├── java/
│       │   └── it/
│       │       └── shottydeveloper/
│       │           └── superclans/
│       │               ├── command/          # Gestori comandi
│       │               ├── config/            # Gestione configurazione
│       │               ├── core/              # Manager principali (Clan, Territory, Chat)
│       │               ├── database/          # Livello database (repository, migrazioni)
│       │               ├── hook/              # Hook plugin esterni
│       │               ├── listener/          # Listener eventi
│       │               ├── model/             # Modelli dati
│       │               └── util/              # Classi utility
│       └── resources/
│           ├── config.yml                    # Configurazione principale
│           ├── messages.yml                  # Traduzioni messaggi
│           └── plugin.yml                    # Metadati plugin
├── pom.xml                                   # Configurazione Maven
└── README.md                                 # Questo file
```

## Architettura

SuperClans segue un'architettura modulare:

- **Pattern Manager**: Funzionalità core organizzate in classi manager (ClanManager, TerritoryManager, ChatManager)
- **Pattern Repository**: Operazioni database astratte in classi repository
- **Pattern Command**: Comandi implementati come subcomandi con interfaccia unificata
- **Event-Driven**: Eventi Bukkit gestiti da listener dedicati
- **Dependency Injection**: Manager e dipendenze iniettate tramite costruttore

## Risoluzione Problemi

### Problemi di Connessione Database

- Verifica le credenziali del database in `config.yml`
- Assicurati che il server database sia in esecuzione e accessibile
- Controlla le impostazioni del firewall
- Verifica che il database esista e l'utente abbia i permessi corretti

### Integrazione WorldGuard

- Assicurati che WorldGuard sia installato e abilitato
- Controlla la compatibilità della versione WorldGuard (7.0+)
- Verifica che le regioni WorldGuard vengano create (controlla i log)

### PlaceholderAPI Non Funziona

- Verifica che PlaceholderAPI sia installato e abilitato
- Controlla l'ordine di caricamento dei plugin (PlaceholderAPI dovrebbe caricarsi prima di SuperClans)
- Usa `/papi parse me %clans_player_clan%` per testare i placeholder

### Protezione Territorio Non Funziona

- Controlla se `use-worldguard` è impostato correttamente nel config
- Verifica le impostazioni di protezione in `config.yml`
- Controlla i log del server per errori
- Assicurati di testare in un territorio reclamato

## Contribuire

I contributi sono benvenuti! Segui queste linee guida:

1. Fai un fork del repository
2. Crea un branch per la feature (`git checkout -b feature/feature-fantastica`)
3. Committa le tue modifiche (`git commit -m 'Aggiungi feature fantastica'`)
4. Pusha sul branch (`git push origin feature/feature-fantastica`)
5. Apri una Pull Request

### Stile del Codice

- Segui le convenzioni di naming Java
- Usa nomi di variabili e metodi significativi
- Aggiungi commenti per logica complessa
- Mantieni coerenza con lo stile del codice esistente

## Licenza

Questo progetto è licenziato sotto la MIT License - vedi il file LICENSE per i dettagli.

## Supporto

Per problemi, domande o suggerimenti:

- Apri un issue su [GitHub Issues](https://github.com/yourusername/SuperClans/issues)
- Controlla gli issue esistenti prima di crearne uno nuovo
- Fornisci informazioni dettagliate sul tuo problema

## Crediti

- **Autore**: 24Shotty (ShottyDeveloper)
- **Versione**: 1.0
- **Versione Minecraft**: 1.20.1+

## Ringraziamenti

- SpigotMC per la Spigot API
- Team PlaceholderAPI per il supporto placeholder
- Team WorldGuard per l'integrazione gestione regioni
- HikariCP per il connection pooling
- MariaDB Foundation per il supporto database

---

**Nota**: Questo plugin richiede un server database. Assicurati di avere MariaDB o MySQL installato e configurato prima di utilizzare SuperClans.
