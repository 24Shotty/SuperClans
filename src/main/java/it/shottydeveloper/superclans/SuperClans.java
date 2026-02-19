package it.shottydeveloper.superclans;

import it.shottydeveloper.superclans.command.ClanCommand;
import it.shottydeveloper.superclans.config.ConfigManager;
import it.shottydeveloper.superclans.config.MessagesConfig;
import it.shottydeveloper.superclans.config.SettingsConfig;
import it.shottydeveloper.superclans.core.ChatManager;
import it.shottydeveloper.superclans.core.ClanManager;
import it.shottydeveloper.superclans.core.InviteManager;
import it.shottydeveloper.superclans.core.ItemManager;
import it.shottydeveloper.superclans.core.TerritoryManager;
import it.shottydeveloper.superclans.database.DatabaseManager;
import it.shottydeveloper.superclans.hook.PlaceHolderHook;
import it.shottydeveloper.superclans.hook.WorldGuardHook;
import it.shottydeveloper.superclans.listener.ChatListener;
import it.shottydeveloper.superclans.listener.PlayerJoinListener;
import it.shottydeveloper.superclans.listener.TerritoryProtectionListener;
import org.bukkit.plugin.java.JavaPlugin;

// classe principale

public class SuperClans extends JavaPlugin {

    // instanza singola del plugin
    private static SuperClans instance;

    // manager principali del plugin
    private ConfigManager configManager;
    private MessagesConfig messagesConfig;
    private SettingsConfig settingsConfig;
    private DatabaseManager databaseManager;
    private ClanManager clanManager;
    private InviteManager inviteManager;
    private TerritoryManager territoryManager;
    private ChatManager chatManager;
    private ItemManager itemManager;

    // hook per i plugin opzionali
    private WorldGuardHook worldGuardHook;
    private PlaceHolderHook placeHolderHook;

    // flag per capire quali plugin opzionali sono disponibili
    private boolean worldGuardEnabled = false;
    private boolean placeholderApiEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("╔═══════════════════════════════╗");
        getLogger().info("║      SuperClans v 1.0         ║");
        getLogger().info("║   Sviluppato da 24Shotty      ║");
        getLogger().info("╚═══════════════════════════════╝");

        // carichiamo i file di configurazione
        if (!loadConfigurations()) {
            getLogger().severe("Errore nel caricamento delle configurazioni! Disabilitando il plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // inizializziamo il database se fallisce il plugin non worka
        if (!initDatabase()) {
            getLogger().severe("Impossibile connettersi al database! Controlla le impostazioni in config.yml");
            getLogger().severe("Disabilitando il plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // inizializziamo i manager principali
        initManagers();

        // controlliamo i plugin opzionali e li hookkiamo se presenti
        checkOptionalPlugins();

        // registriamo i listener degli eventi
        registerListeners();

        // registriamo i comandi
        registerCommands();

        getLogger().info("Plugin avviato con successo!");
        getLogger().info("WorldGuard: " + (worldGuardEnabled ? "✓ Attivo" : "✗ Non trovato"));
        getLogger().info("PlaceholderAPI: " + (placeholderApiEnabled ? "✓ Attivo" : "✗ Non trovato"));
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabilitando SuperClans...");

        // salviamo i dati pendenti prima di chiudere
        if (clanManager != null) {
            clanManager.saveAll();
        }

        // chiudiamo la connessione al database
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("SuperClans disabilitato correttamente. A presto!");
    }

    // carico i files di configurazione
    private boolean loadConfigurations() {
        try {
            // salviamo i file di default se non esistono
            saveDefaultConfig();

            configManager = new ConfigManager(this);
            messagesConfig = new MessagesConfig(this);
            settingsConfig = new SettingsConfig(this);

            getLogger().info("Configurazioni caricate correttamente!");
            return true;
        } catch (Exception e) {
            getLogger().severe("Errore nel caricamento configurazioni: " + e.getMessage());
            return false;
        }
    }

    // inizializzo il database con le varie tabelle necessarie
    private boolean initDatabase() {
        try {
            databaseManager = new DatabaseManager(this);
            return databaseManager.initialize();
        } catch (Exception e) {
            getLogger().severe("Errore inizializzazione database: " + e.getMessage());
            if (settingsConfig.isDebug()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    // inizializza tutti i manager
    private void initManagers() {
        // ItemManager prima perchè altri manager potrebbero usarlo
        itemManager = new ItemManager(this);

        // ClanManager è il core del plugin
        clanManager = new ClanManager(this);

        // InviteManager per gestire gli inviti tra giocatori
        inviteManager = new InviteManager(this);

        // TerritoryManager per la gestione dei territori
        territoryManager = new TerritoryManager(this);

        // ChatManager per la chat del clan
        chatManager = new ChatManager(this);

        getLogger().info("Manager inizializzati correttamente!");
    }

    // controlla se i plugin opzionali sono presenti e disponibili poi li hookka se possibile
    private void checkOptionalPlugins() {
        // controlliamo WorldGuard
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                worldGuardHook = new WorldGuardHook(this);
                worldGuardEnabled = true;
                getLogger().info("WorldGuard hook inizializzato con successo!");
            } catch (Exception e) {
                getLogger().warning("Errore nell'inizializzazione di WorldGuard: " + e.getMessage());
                worldGuardEnabled = false;
            }
        } else {
            getLogger().info("WorldGuard non trovato, usando il sistema di protezione custom.");
        }

        // controlliamo PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                placeHolderHook = new PlaceHolderHook(this);
                placeHolderHook.register();
                placeholderApiEnabled = true;
                getLogger().info("PlaceholderAPI hook registrato con successo!");
            } catch (Exception e) {
                getLogger().warning("Errore nell'inizializzazione di PlaceholderAPI: " + e.getMessage());
                placeholderApiEnabled = false;
            }
        } else {
            getLogger().info("PlaceholderAPI non trovato, i placeholder non saranno disponibili.");
        }
    }

    // registra tutti i listener degli eventi Bukkit.
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new TerritoryProtectionListener(this), this);

        getLogger().info("Listener registrati correttamente!");
    }

    // registra i comandi del plugin nel plugin.yml.
    private void registerCommands() {
        ClanCommand clanCmd = new ClanCommand(this);
        // getCommand può restituire null se il comando non è nel plugin.yml
        // quindi facciamo un controllo per sicurezza
        if (getCommand("clan") != null) {
            getCommand("clan").setExecutor(clanCmd);
            getCommand("clan").setTabCompleter(clanCmd);
        } else {
            getLogger().severe("Comando 'clan' non trovato nel plugin.yml! Qualcosa è andato storto.");
        }

        getLogger().info("Comandi registrati correttamente!");
    }


    // metodi getter per accedere ai manager dagli altri classi
    public static SuperClans getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public SettingsConfig getSettingsConfig() {
        return settingsConfig;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public InviteManager getInviteManager() {
        return inviteManager;
    }

    public TerritoryManager getTerritoryManager() {
        return territoryManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }

    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }

    public boolean isPlaceholderApiEnabled() {
        return placeholderApiEnabled;
    }
}
