# StaffPanel - Plugin Minecraft

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.13--1.21-green)
![License](https://img.shields.io/badge/license-All%20Rights%20Reserved-red)
[![〻ꜱᴜᴘᴘᴏʀᴛ](https://img.shields.io/badge/〻ꜱᴜᴘᴘᴏʀᴛ-purple?style=flat&logoColor=white)](https://discord.gg/c4VV4kgZUk)




> **Plugin developed by Youness**

Plugin professionale per la gestione dello staff su server Minecraft Spigot/Paper.

---

## Caratteristiche

### Comandi Staff Completi
- `/staffpanel ban <player> <reason> [duration]` - Banna un giocatore
- `/staffpanel tempban <player> <reason> <duration>` - Ban temporaneo
- `/staffpanel unban <player>` - Sbanna un giocatore
- `/staffpanel mute <player> [duration]` - Muta un giocatore
- `/staffpanel unmute <player>` - Smuta un giocatore
- `/staffpanel kick <player> <reason>` - Kicka un giocatore
- `/staffpanel warn <player> <reason>` - Avvisa un giocatore
- `/staffpanel warns <player>` - Visualizza avvisi
- `/staffpanel clearwarns <player>` - Cancella avvisi
- `/staffpanel freeze <player>` - Freeza un giocatore
- `/staffpanel unfreeze <player>` - Sfreeza un giocatore
- `/staffpanel vanish` - Diventa invisibile
- `/staffpanel fly` - Attiva/disattiva volo
- `/staffpanel heal [player]` - Cura un giocatore
- `/staffpanel feed [player]` - Sfama un giocatore
- `/staffpanel tp <player>` - Teletrasportati
- `/staffpanel tphere <player>` - Teletrasporta qui
- `/staffpanel back` - Torna alla posizione precedente
- `/staffpanel wild` - Teletrasporto casuale
- `/staffpanel gamemode <0/1/2/3> [player]` - Cambia gamemode
- `/staffpanel ip <player>` - Visualizza IP
- `/staffpanel alts <player>` - Visualizza account alternativi
- `/staffpanel clearchat` - Pulisci la chat
- `/staffpanel slowchat [delay]` - Attiva/disattiva slowchat
- `/staffpanel reload` - Ricarica configurazione

### Comandi Giocatori
- `/report <tipologia> <motivo>` - Invia un report
- `/reports [page]` - Lista report (Staff)
- `/discord` - Mostra link Discord

### Funzionalità
- Tab-completion completa per tutti i comandi
- Sistema permessi personalizzabile
- Messaggi in caratteri speciali (〻ʙᴀɴ-ᴄᴏᴍᴘʟᴇᴛᴇ, 〻ᴍᴜᴛᴇ-ᴀᴘᴘʟɪᴇᴅ, etc.)
- Integrazione Discord Webhook per notifiche
- Sistema di logging completo in JSON
- Gestione report con tipologie configurabili
- Salvataggio persistente di ban, mute, warn
- Rilevamento account alternativi (alts)

---

## Installazione

### Requisiti
- Java 8 o superiore
- Server Spigot/Paper 1.13 - 1.21
- Maven (per compilazione)

### Compilazione

```bash
# Clona/scarica il progetto
cd StaffPanel

# Compila con Maven
mvn clean package

# Il JAR sarà in target/StaffPanel-1.0.0.jar
```

### Installazione sul Server

1. Copia `StaffPanel-1.0.0.jar` nella cartella `plugins/` del tuo server
2. Riavvia il server
3. Configura i file in `plugins/StaffPanele/`

---

## Configurazione

### config.yml
```yaml
prefix: "&8[&c&lStaffPanel&8] "
discord-link: "discord.gg/tuoserver"

commands:
  ban:
    enabled: true
    permission: "staffpanel.ban"
    log: true
    notify-staff: true
```

### messages.yml
Tutti i messaggi sono personalizzabili con placeholder:
- `{player}` - Nome giocatore
- `{staff}` - Nome staff
- `{reason}` - Motivo
- `{duration}` - Durata
- `{discord-link}` - Link Discord

### discord-webhook.yml
```yaml
enabled: true

webhooks:
  ban:
    enabled: true
    url: "https://discord.com/api/webhooks/YOUR_WEBHOOK_URL"
    title: "〻 ʙᴀɴ"
```

---

## Permessi

| Permesso | Descrizione |
|----------|-------------|
| `staffpanel.*` | Accesso completo |
| `staffpanel.use` | Permesso base |
| `staffpanel.ban` | Bannare giocatori |
| `staffpanel.tempban` | Ban temporanei |
| `staffpanel.unban` | Sbannare |
| `staffpanel.mute` | Mutare |
| `staffpanel.unmute` | Smutare |
| `staffpanel.kick` | Kickare |
| `staffpanel.warn` | Avvisare |
| `staffpanel.warns` | Vedere avvisi |
| `staffpanel.clearwarns` | Cancellare avvisi |
| `staffpanel.freeze` | Freezare |
| `staffpanel.unfreeze` | Sfreezare |
| `staffpanel.vanish` | Vanish |
| `staffpanel.fly` | Volare |
| `staffpanel.heal` | Curare |
| `staffpanel.feed` | Sfamare |
| `staffpanel.tp` | Teletrasporto |
| `staffpanel.tphere` | Teletrasporto qui |
| `staffpanel.back` | Tornare indietro |
| `staffpanel.wild` | Wild teleport |
| `staffpanel.gamemode` | Cambiare gamemode |
| `staffpanel.ip` | Vedere IP |
| `staffpanel.alts` | Vedere alts |
| `staffpanel.clearchat` | Pulire chat |
| `staffpanel.slowchat` | Slowchat |
| `staffpanel.report` | Inviare report |
| `staffpanel.reports` | Gestire report |
| `staffpanel.discord` | Vedere Discord |
| `staffpanel.reload` | Ricaricare config |
| `staffpanel.bypass.slowchat` | Bypass slowchat |
| `staffpanel.bypass.freeze` | Bypass freeze |

---

## Formati Durata

| Formato | Significato |
|---------|-------------|
| `1s` | 1 secondo |
| `1m` | 1 minuto |
| `1h` | 1 ora |
| `1d` | 1 giorno |
| `1w` | 1 settimana |
| `1mo` | 1 mese |

Esempio: `1d12h` = 1 giorno e 12 ore

---

## Tipologie Report

- `INFO` - Informazioni generali
- `GENERALE` - Report generale
- `BUG` - Segnalazione bug
- `CHEATER` - Segnalazione cheater
- `CHAT` - Problemi in chat
- `ALTRO` - Altro

---

## Struttura File

```
plugins/StaffPanele/
├── config.yml          # Configurazione principale
├── messages.yml        # Messaggi personalizzabili
├── discord-webhook.yml # Configurazione webhook
├── data/
│   ├── bans.json       # Dati ban
│   ├── mutes.json      # Dati mute
│   ├── warns.json      # Dati warn
│   ├── reports.json    # Dati report
│   └── players.json    # Dati giocatori (IP, ecc.)
└── logs/
    └── log_YYYY-MM-DD.json  # Log giornalieri
```

---

## Supporto

Per supporto o segnalazioni:
- Discord: https://discord.gg/c4VV4kgZUk

---

## Credits

**StaffPanel** - Plugin professionale per la gestione dello staff
Developed by Youness - Tutti i diritti riservati

*Tutti i diritti riservati. Non è permessa la redistribuzione senza autorizzazione.*
