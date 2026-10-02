package fr.aerwyn81.headblocks.commands.list;

import fr.aerwyn81.headblocks.HeadBlocks;
import fr.aerwyn81.headblocks.ServiceRegistry;
import fr.aerwyn81.headblocks.commands.Cmd;
import fr.aerwyn81.headblocks.commands.HBAnnotations;
import fr.aerwyn81.headblocks.databases.EnumTypeDatabase;
import fr.aerwyn81.headblocks.utils.internal.ExportSQLHelper;
import fr.aerwyn81.headblocks.utils.message.MessageUtils;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@HBAnnotations(command = "export", permission = "headblocks.admin", args = {"database"}, alias = "e")
public class Export implements Cmd {
    private final ServiceRegistry registry;

    public Export(ServiceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(registry.getLanguageService().message("Messages.ErrorCommand"));
            return true;
        }

        EnumTypeDatabase typeDatabase = EnumTypeDatabase.of(args[2]);

        if (typeDatabase == null) {
            sender.sendMessage(MessageUtils.colorize(registry.getLanguageService().prefix() + " &cThe SQL type &e" + args[2] + " &cis not supported!"));
            return true;
        }

        String fileName = "export-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".sql";

        sender.sendMessage(MessageUtils.colorize(registry.getLanguageService().message("Messages.ExportInProgress")));

        HeadBlocks.getScheduler().runTaskAsync(() -> {
            try {
                ExportSQLHelper.generateFile(registry, typeDatabase, fileName);
            } catch (Exception ex) {
                sendBackToSender(sender, MessageUtils.colorize(registry.getLanguageService().message("Messages.ExportError") + ex.getMessage()));
                return;
            }

            sendBackToSender(sender, MessageUtils.colorize(registry.getLanguageService().message("Messages.ExportSuccess"))
                    .replace("%fileName%", fileName));
        });

        return true;
    }

    private void sendBackToSender(CommandSender sender, String message) {
        // CommandSender can be a Player: Player.sendMessage must run on its entity thread on Folia.
        if (sender instanceof org.bukkit.entity.Player player) {
            HeadBlocks.getScheduler().runTask(player, () -> sender.sendMessage(message));
        } else {
            HeadBlocks.getScheduler().runTask(() -> sender.sendMessage(message));
        }
    }

    @Override
    public ArrayList<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return new ArrayList<>(Collections.singleton("database"));
        }

        if (args.length == 3) {
            return Stream.of(EnumTypeDatabase.values())
                    .map(Enum::name)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        return new ArrayList<>();
    }
}
