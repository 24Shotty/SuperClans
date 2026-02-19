package it.shottydeveloper.superclans.command;

import it.shottydeveloper.superclans.SuperClans;
import it.shottydeveloper.superclans.command.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class ClanCommand implements CommandExecutor, TabCompleter {

    private final SuperClans plugin;
    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public ClanCommand(SuperClans plugin) {
        this.plugin = plugin;
        registerSubcommands();
    }

    private void registerSubcommands() {
        CreateCommand createCmd = new CreateCommand(plugin);
        DisbandCommand disbandCmd = new DisbandCommand(plugin);
        InviteCommand inviteCmd = new InviteCommand(plugin);
        KickCommand kickCmd = new KickCommand(plugin);
        PromoteCommand promoteCmd = new PromoteCommand(plugin);
        DemoteCommand demoteCmd = new DemoteCommand(plugin);
        ChatCommand chatCmd = new ChatCommand(plugin);
        ClaimCommand claimCmd = new ClaimCommand(plugin);
        UnclaimCommand unclaimCmd = new UnclaimCommand(plugin);
        HomeCommand homeCmd = new HomeCommand(plugin);
        SetHomeCommand setHomeCmd = new SetHomeCommand(plugin);
        InfoCommand infoCmd = new InfoCommand(plugin);

        subcommands.put("create", createCmd);
        subcommands.put("disband", disbandCmd);
        subcommands.put("invite", inviteCmd);
        subcommands.put("accept", inviteCmd);
        subcommands.put("deny", inviteCmd);
        subcommands.put("kick", kickCmd);
        subcommands.put("promote", promoteCmd);
        subcommands.put("demote", demoteCmd);
        subcommands.put("chat", chatCmd);
        subcommands.put("c", chatCmd);
        subcommands.put("claim", claimCmd);
        subcommands.put("unclaim", unclaimCmd);
        subcommands.put("home", homeCmd);
        subcommands.put("sethome", setHomeCmd);
        subcommands.put("info", infoCmd);
        subcommands.put("i", infoCmd);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessagesConfig().get("general.players-only"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getMessagesConfig().get("general.usage"));
            return true;
        }

        String subCmdName = args[0].toLowerCase();
        SubCommand subCmd = subcommands.get(subCmdName);

        if (subCmd == null) {
            player.sendMessage(plugin.getMessagesConfig().get("general.unknown-command"));
            return true;
        }

        String[] subArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        subCmd.execute(player, subCmdName, subArgs);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String subName : subcommands.keySet()) {
                if (subName.startsWith(partial)) {
                    completions.add(subName);
                }
            }
            Collections.sort(completions);
            return completions;
        }

        String subCmdName = args[0].toLowerCase();
        SubCommand subCmd = subcommands.get(subCmdName);
        if (subCmd == null) {
            return Collections.emptyList();
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return subCmd.tabComplete(player, subCmdName, subArgs);
    }

    public interface SubCommand {
        void execute(Player player, String subCommand, String[] args);
        List<String> tabComplete(Player player, String subCommand, String[] args);
    }
}
