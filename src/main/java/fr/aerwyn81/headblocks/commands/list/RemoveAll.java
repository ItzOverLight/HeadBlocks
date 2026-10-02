package fr.aerwyn81.headblocks.commands.list;

import fr.aerwyn81.headblocks.ServiceRegistry;
import fr.aerwyn81.headblocks.commands.Cmd;
import fr.aerwyn81.headblocks.commands.HBAnnotations;
import fr.aerwyn81.headblocks.data.HeadLocation;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;

@HBAnnotations(command = "removeall", permission = "headblocks.admin")
public class RemoveAll implements Cmd {
    private final ServiceRegistry registry;

    public RemoveAll(ServiceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        ArrayList<HeadLocation> headLocations = new ArrayList<>(registry.getHeadService().getChargedHeadLocations());
        int headCount = headLocations.size();

        if (headLocations.isEmpty()) {
            sender.sendMessage(registry.getLanguageService().message("Messages.ListHeadEmpty"));
            return true;
        }

        boolean hasConfirmInCommand = args.length > 1 && args[1].equals("--confirm");
        if (hasConfirmInCommand) {
            sender.sendMessage(registry.getLanguageService().message("Messages.RemoveAllInProgress")
                    .replace("%headCount%", String.valueOf(headCount)));

            registry.getHeadService().removeAllHeadLocationsAsync(headLocations, registry.getConfigService().resetPlayerData(), (headRemoved) -> {
                if (headRemoved == 0) {
                    tell(sender, registry.getLanguageService().message("Messages.RemoveAllError")
                            .replace("%headCount%", String.valueOf(headCount)));
                    return;
                }

                tell(sender, registry.getLanguageService().message("Messages.RemoveAllSuccess")
                        .replace("%headCount%", String.valueOf(headRemoved)));
            });

            return true;
        }

        sender.sendMessage(registry.getLanguageService().message("Messages.RemoveAllConfirm")
                .replace("%headCount%", String.valueOf(headCount)));

        return true;
    }

    private void tell(CommandSender sender, String message) {
        // Callbacks run on the global region thread: Player.sendMessage must hop to its entity thread on Folia.
        if (sender instanceof org.bukkit.entity.Player player) {
            registry.getScheduler().runTask(player, () -> sender.sendMessage(message));
        } else {
            sender.sendMessage(message);
        }
    }

    @Override
    public ArrayList<String> tabComplete(CommandSender sender, String[] args) {
        return args.length == 2 ? new ArrayList<>(Collections.singleton("--confirm")) : new ArrayList<>();
    }
}
