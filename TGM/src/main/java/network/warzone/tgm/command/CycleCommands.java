package network.warzone.tgm.command;

import com.google.common.base.Joiner;
import com.sk89q.minecraft.util.commands.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.gametype.GameType;
import network.warzone.tgm.map.MapContainer;
import network.warzone.tgm.map.MapInfo;
import network.warzone.tgm.match.MatchManager;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.ChatModule;
import network.warzone.tgm.modules.countdown.Countdown;
import network.warzone.tgm.modules.countdown.CycleCountdown;
import network.warzone.tgm.modules.countdown.StartCountdown;
import network.warzone.tgm.modules.ffa.FFAModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.team.TeamUpdateEvent;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class CycleCommands {

    @Command(aliases = {"maps"}, desc = "View the maps that are on Warzone, although not necessarily in the rotation.", usage = "[page]")
    public static void maps(CommandContext cmd, CommandSender sender) throws CommandException {
        int index = cmd.argsLength() == 0 ? 1 : cmd.getInteger(0);
        List<MapContainer> mapLibrary = TGM.get().getMatchManager().getMapLibrary().getMaps();

        int pagesRemainder = mapLibrary.size() % 9;
        int pagesDivisible = mapLibrary.size() / 9;
        int pages = pagesDivisible;

        if(pagesRemainder >= 1) {
            pages = pagesDivisible + 1;
        }

        if ((index > pages) || (index <= 0)) {
            index = 1;
        }
        sender.sendMessage(ChatColor.YELLOW + "Maps (" + index + "/" + pages + "): ");
        String[] maps = {"", "", "", "", "", "", "", "", ""};

        try {
            for (int i = 0; i <= maps.length - 1; i++) {
                int position = 9 * (index - 1) + i;
                MapContainer map = mapLibrary.get(position);
                maps[i] = maps[i] + ChatColor.GOLD + map.getMapInfo().getName();

                if (map.getMapInfo().equals(TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo())) {
                    maps[i] = ChatColor.GREEN + "" + (position + 1) + ". " + maps[i];
                } else {
                    maps[i] = ChatColor.WHITE + "" + (position + 1) + ". " + maps[i];
                }
                TextComponent message = new TextComponent(maps[i]);
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sn " + mapLibrary.get(position).getMapInfo().getName()));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD + mapLibrary.get(position).getMapInfo().getName()).append("\n\n")
                        .append(ChatColor.GRAY + "Authors: ").append(Joiner.on(",").join(mapLibrary.get(position).getMapInfo().getAuthors())).append("\n")
                        .append(ChatColor.GRAY + "Game Type: ").append(ChatColor.YELLOW + map.getMapInfo().getGametype().toString()).append("\n")
                        .append(ChatColor.GRAY + "Version: ").append(ChatColor.YELLOW + mapLibrary.get(position).getMapInfo().getVersion()).create()));

                sender.spigot().sendMessage(message);
            }
        } catch (IndexOutOfBoundsException ignored) { }
    }

    @Command(aliases = {"rot", "rotation", "rotations"}, desc = "View the maps that are in the rotation.", usage = "[page]")
    public static void rotation(final CommandContext cmd, CommandSender sender) throws CommandException {
        int index = cmd.argsLength() == 0 ? 1 : cmd.getInteger(0);
        List<MapContainer> rotation = TGM.get().getMatchManager().getMapRotation().getMaps();

        int pagesRemainder = rotation.size() % 9;
        int pagesDivisible = rotation.size() / 9;
        int pages = pagesDivisible;

        if(pagesRemainder >= 1) {
            pages = pagesDivisible + 1;
        }

        if ((index > pages) || (index <= 0)) {
            index = 1;
        }
        sender.sendMessage(ChatColor.YELLOW + "Active Rotation (" + index + "/" + pages + "): ");
        String[] maps = {"", "", "", "", "", "", "", "", ""};

        try {
            for (int i = 0; i <= maps.length - 1; i++) {
                int position = 9 * (index - 1) + i;
                MapContainer map = rotation.get(position);
                maps[i] = maps[i] + ChatColor.GOLD + map.getMapInfo().getName();

                if (map.getMapInfo().equals(TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo())) {
                    maps[i] = ChatColor.GREEN + "" + (position + 1) + ". " + maps[i];
                } else {
                    maps[i] = ChatColor.WHITE + "" + (position + 1) + ". " + maps[i];
                }
                TextComponent message = new TextComponent(maps[i]);
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sn " + rotation.get(position).getMapInfo().getName()));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD + rotation.get(position).getMapInfo().getName()).append("\n\n")
                        .append(ChatColor.GRAY + "Authors: ").append(Joiner.on(",").join(rotation.get(position).getMapInfo().getAuthors())).append("\n")
                        .append(ChatColor.GRAY + "Game Type: ").append(ChatColor.YELLOW + map.getMapInfo().getGametype().toString()).append("\n")
                        .append(ChatColor.GRAY + "Version: ").append(ChatColor.YELLOW + rotation.get(position).getMapInfo().getVersion()).create()));

                sender.spigot().sendMessage(message);
            }
        } catch (IndexOutOfBoundsException ignored) { }
    }


    @Command(aliases = {"cycle"}, desc = "Cycle to a new map.")
    @CommandPermissions({"tgm.cycle"})
    public static void cycle(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchStatus != MatchStatus.MID) {
            int time = CycleCountdown.START_TIME;
            if (cmd.argsLength() > 0) {
                try {
                    time = cmd.getInteger(0);
                } catch (CommandNumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Unknown time \"" + cmd.getString(0) + "\"");
                }
            }
            TGM.get().getModule(CycleCountdown.class).start(time);
        } else {
            sender.sendMessage(ChatColor.RED + "A match is currently in progress.");
        }
    }

    @Command(aliases = {"start"}, desc = "Start the match.")
    @CommandPermissions({"tgm.start"})
    public static void start(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.PRE) {
            int time = StartCountdown.START_TIME;
            if (cmd.argsLength() > 0) {
                try {
                    time = cmd.getInteger(0);
                } catch (CommandNumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Unknown time \"" + cmd.getString(0) + "\"");
                }
            }
            TGM.get().getModule(StartCountdown.class).start(time);
        } else {
            sender.sendMessage(ChatColor.RED + "The match cannot be started at this time.");
        }
    }

    @Command(aliases = {"end"}, desc = "End the match.", anyFlags = true, flags = "f")
    @CommandPermissions({"tgm.end"})
    public static void end(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.MID) {
            if (cmd.argsLength() > 0) {
                MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getJoinedStrings(0));
                if (matchTeam == null) {
                    sender.sendMessage(ChatColor.RED + "Unable to find team \"" + cmd.getJoinedStrings(0) + "\"");
                    return;
                }
                TGM.get().getMatchManager().endMatch(matchTeam);
            } else {
                if (cmd.hasFlag('f')) {
                    TGM.get().getMatchManager().endMatch(null);
                } else {
                    TGM.get().getModule(TimeModule.class).endMatch();
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No match in progress.");
        }
    }

    @Command(aliases = {"cancel"}, desc = "Cancel all countdowns.")
    @CommandPermissions({"tgm.cancel"})
    public static void cancel(CommandContext cmd, CommandSender sender) {
        for (Countdown countdown : TGM.get().getModules(Countdown.class)) {
            countdown.cancel();
        }
        sender.sendMessage(ChatColor.GREEN + "Countdowns cancelled.");
    }

    @Command(aliases = {"setnext", "sn"}, desc = "Set the next map.")
    @CommandPermissions({"tgm.setnext"})
    public static void setNext(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            MapContainer found = null;
            for (MapContainer mapContainer : TGM.get().getMatchManager().getMapLibrary().getMaps()) {
                if (mapContainer.getMapInfo().getName().equalsIgnoreCase(cmd.getJoinedStrings(0))) {
                    found = mapContainer;
                }
            }
            for (MapContainer mapContainer : TGM.get().getMatchManager().getMapLibrary().getMaps()) {
                if (mapContainer.getMapInfo().getName().toLowerCase().startsWith(cmd.getJoinedStrings(0).toLowerCase())) {
                    found = mapContainer;
                }
            }

            if (found == null) {
                sender.sendMessage(ChatColor.RED + "Map not found \"" + cmd.getJoinedStrings(0) + "\"");
                return;
            }

            TGM.get().getMatchManager().setForcedNextMap(found);
            sender.sendMessage(ChatColor.GREEN + "Set the next map to " + ChatColor.YELLOW + found.getMapInfo().getName() + ChatColor.GRAY + " (" + found.getMapInfo().getVersion() + ")");
        } else {
            sender.sendMessage(ChatColor.RED + "/sn <map_name>");
        }
    }

    @Command(aliases = {"join", "play"}, desc = "Join a team.")
    public static void join(CommandContext cmd, CommandSender sender) {
        TeamManagerModule teamManager = TGM.get().getModule(TeamManagerModule.class);
        MatchManager matchManager = TGM.get().getMatchManager();
        GameType gameType = matchManager.getMatch().getMapContainer().getMapInfo().getGametype();
        MatchStatus matchStatus = matchManager.getMatch().getMatchStatus();
        if (cmd.argsLength() == 0) {
            if (gameType.equals(GameType.Blitz) || gameType.equals(GameType.FFA) && TGM.get().getModule(FFAModule.class).isBlitzMode()) {
                if (!matchStatus.equals(MatchStatus.PRE)) {
                    sender.sendMessage(ChatColor.RED + "You can't pick a team after the match starts in this gamemode.");
                    return;
                }
            }
            if (teamManager.getTeam((Player) sender).isSpectator() || matchStatus.equals(MatchStatus.PRE)) {
                if (gameType.equals(GameType.Infected)) {
                    if (matchStatus.equals(MatchStatus.MID) || matchStatus.equals(MatchStatus.POST)) {
                        MatchTeam team = teamManager.getTeamById("infected");
                        attemptJoinTeam((Player) sender, team, true);
                        return;
                    }

                    MatchTeam team = teamManager.getTeamById("humans");
                    attemptJoinTeam((Player) sender, team, true);
                    return;
                }
                MatchTeam matchTeam = teamManager.getSmallestTeam();
                attemptJoinTeam((Player) sender, matchTeam, true);
            } else {
                sender.sendMessage(ChatColor.RED + "You have already chosen a team.");
            }
        } else {
            MatchTeam matchTeam = teamManager.getTeamFromInput(cmd.getJoinedStrings(0));

            if (matchTeam == null) {
                sender.sendMessage(ChatColor.RED + "Unable to find team \"" + cmd.getJoinedStrings(0) + "\"");
                return;
            }

            if (gameType.equals(GameType.Infected)) {
                if (matchStatus.equals(MatchStatus.POST)) {
                    if (!matchTeam.isSpectator()) {
                        sender.sendMessage(ChatColor.RED + "The game has already ended.");
                        return;
                    } else {
                        attemptJoinTeam((Player) sender, matchTeam, false);
                        return;
                    }
                } else if (matchStatus.equals(MatchStatus.MID)) {
                    if (!matchTeam.isSpectator()) {
                        sender.sendMessage(ChatColor.RED + "You can't pick a team after the match starts in this gamemode.");
                        return;
                    } else {
                        attemptJoinTeam((Player) sender, matchTeam, false);
                        return;
                    }
                }
            } else if (gameType.equals(GameType.Blitz) || gameType.equals(GameType.FFA) && TGM.get().getModule(FFAModule.class).isBlitzMode()) {
                if (!matchStatus.equals(MatchStatus.PRE)) {
                    sender.sendMessage(ChatColor.RED + "You can't pick a team after the match starts in this gamemode.");
                    return;
                }
            }

            attemptJoinTeam((Player) sender, matchTeam, false);
        }
    }

    @Command(aliases = {"team"}, desc = "Manage teams.")
    @CommandPermissions({"tgm.team"})
    public static void team(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            if (cmd.getString(0).equalsIgnoreCase("alias")) {
                if (cmd.argsLength() == 3) {
                    MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getString(1));
                    if (matchTeam == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown team \"" + cmd.getString(1) + "\"");
                        return;
                    }
                    matchTeam.setAlias(cmd.getString(2));
                    Bukkit.getPluginManager().callEvent(new TeamUpdateEvent(matchTeam));
                } else {
                    sender.sendMessage(ChatColor.RED + "/team alias (team) (name)");
                }
            } else if (cmd.getString(0).equalsIgnoreCase("force")) {
                if (cmd.argsLength() == 3) {
                    MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getString(2));
                    if (matchTeam == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown team \"" + cmd.getString(2) + "\"");
                        return;
                    }
                    Player player = Bukkit.getPlayer(cmd.getString(1));
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown player \"" + cmd.getString(1) + "\"");
                        return;
                    }
                    attemptJoinTeam(player, matchTeam, true, true);
                    sender.sendMessage(ChatColor.GREEN + "Forced " + player.getName() + " into " + matchTeam.getColor() + matchTeam.getAlias());
                } else {
                    sender.sendMessage(ChatColor.RED + "/team force (player) (team)");
                }
            } else if (cmd.getString(0).equalsIgnoreCase("size")) {
                if (cmd.argsLength() == 4) {
                    MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getString(1));
                    if (matchTeam == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown team \"" + cmd.getString(1) + "\"");
                        return;
                    }
                    int min = 0;
                    int max = 0;
                    try {
                        min = cmd.getInteger(2);
                        max = cmd.getInteger(3);
                    } catch (CommandNumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                        return;
                    }
                    matchTeam.setMin(min);
                    matchTeam.setMax(max);
                    Bukkit.getPluginManager().callEvent(new TeamUpdateEvent(matchTeam));
                    sender.sendMessage(ChatColor.GREEN + "Set " + matchTeam.getColor() + matchTeam.getAlias() + ChatColor.GREEN + " size limits to " + min + "-" + max);
                } else {
                    sender.sendMessage(ChatColor.RED + "/team size (team) (min) (max)");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "/team alias|force|size");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "/team alias|force|size");
        }
    }

    @Command(aliases = {"loadmaps"}, desc = "Load maps.")
    @CommandPermissions({"tgm.loadmaps"})
    public static void loadmaps(CommandContext cmd, CommandSender sender) {
        TGM.get().getMatchManager().getMapLibrary().refreshMaps();
        TGM.get().getMatchManager().getMapRotation().refresh();
        sender.sendMessage(ChatColor.GREEN + "Refreshed map library and rotation.");
    }

    @Command(aliases = {"channel", "chatchannel", "cc"}, desc = "Change or select a chat channel.", usage = "(all|team|staff)", min = 1)
    public static void channel(CommandContext cmd, CommandSender sender) {
        Player player = (Player) sender;

        if(!(sender instanceof Player)) {
            sender.sendMessage("Error: Only players can use this command.");
            return;
        }

        if (cmd.getString(0).equalsIgnoreCase("all")) {
            ChatModule.getChannel().put(player.getUniqueId().toString(), ChatModule.Channel.ALL);
            player.sendMessage(ColorConverter.filterString("&7You've been added to chat channel &c&lALL&7."));
        } else if (cmd.getString(0).equalsIgnoreCase("team")) {
            ChatModule.getChannel().put(player.getUniqueId().toString(), ChatModule.Channel.TEAM);
            player.sendMessage(ColorConverter.filterString("&7You've been added to chat channel &c&lTEAM&7."));
        } else if (cmd.getString(0).equalsIgnoreCase("staff")) {
            if(player.hasPermission("tgm.staffchat")) {
                ChatModule.getChannel().put(player.getUniqueId().toString(), ChatModule.Channel.STAFF);
                player.sendMessage(ColorConverter.filterString("&7You've been added to chat channel &c&lSTAFF&7."));
            } else {
                player.sendMessage(ColorConverter.filterString("&cError: Insufficient permissions."));
            }
        } else {
            sender.sendMessage(ColorConverter.filterString("&cUnknown subcommand."));
        }
    }

    @Command(aliases = {"t"}, desc = "Send a message to your team.", usage = "(message)", min = 1)
    public static void t(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext((Player) sender);
            TGM.get().getModule(ChatModule.class).sendTeamChat(playerContext, cmd.getJoinedStrings(0));
        }
    }

    @Command(aliases = {"next"}, desc = "View the next map in the rotation")
    public static void next(CommandContext cmd, CommandSender sender) {
        MapInfo info = TGM.get().getMatchManager().getNextMap().getMapInfo();
        sender.sendMessage(ChatColor.GRAY + "Next Map: " + ChatColor.YELLOW + info.getName() + ChatColor.GRAY + " by " + ChatColor.YELLOW + info.getAuthors().stream().collect(Collectors.joining(", ")));
    }
    
    @Command(aliases = {"map"}, desc = "View the map info for the current map")
    public static void map(CommandContext cmd, CommandSender sender) {
        MapInfo info = TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo();
        sender.sendMessage(ChatColor.GRAY + "Currently playing " + ChatColor.YELLOW + info.getGametype() + ChatColor.GRAY + " on map " + ChatColor.YELLOW + info.getName() + ChatColor.GRAY + " by " + ChatColor.YELLOW + info.getAuthors().stream().collect(Collectors.joining(", ")));
    }

    @Command(aliases = {"time"}, desc = "Time options")
    public static void time(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() <= 0) {
            ChatColor timeColor = ChatColor.GREEN;
            MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
            if (matchStatus == MatchStatus.PRE) {
                timeColor = ChatColor.GOLD;
            } else if (matchStatus == MatchStatus.POST) {
                timeColor = ChatColor.RED;
            }
            sender.sendMessage(ChatColor.AQUA + "Time elapsed: " + timeColor + Strings.formatTime(TGM.get().getModule(TimeModule.class).getTimeElapsed()));
            return;
        }
        if (cmd.getString(0).equalsIgnoreCase("limit")) {
            if (!sender.hasPermission("tgm.time.limit")) {
                sender.sendMessage(ChatColor.RED + "Insufficient permissions.");
                return;
            }
            if (cmd.argsLength() == 1 || cmd.argsLength() > 2) {
                sender.sendMessage(ChatColor.RED + "/" + cmd.getCommand() + " limit <seconds>");
                return;
            }
            
            TimeModule timeModule = TGM.get().getModule(TimeModule.class);
            if (cmd.getString(1).equalsIgnoreCase("on") || cmd.getString(1).equalsIgnoreCase("true")) {
                timeModule.setTimeLimited(true);
                sender.sendMessage(ChatColor.AQUA + "Time limit: " + ChatColor.GREEN + "true");
                return;
            } else if (cmd.getString(1).equalsIgnoreCase("off") || cmd.getString(1).equalsIgnoreCase("false")) {
                timeModule.setTimeLimited(false);
                sender.sendMessage(ChatColor.AQUA + "Time limit: " + ChatColor.RED + "false");
                return;
            }

            try {
                timeModule.setTimeLimit(cmd.getInteger(1));
                timeModule.setTimeLimited(true);
                sender.sendMessage(ChatColor.AQUA + "Set time limit to: " + ChatColor.GREEN + timeModule.getTimeLimit() + " seconds");
            } catch (CommandNumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "/" + cmd.getCommand() + " limit <seconds>");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "/" + cmd.getCommand() + " limit <seconds>");
        }
    }

    @Command(aliases = {"config"}, desc = "Edit the configuration", usage = "(stats)", min = 1)
    @CommandPermissions({"tgm.config"})
    public static void config(CommandContext cmd, CommandSender sender) {
        if (cmd.getString(0).equalsIgnoreCase("stats")) {
            if (cmd.argsLength() != 2) {
                sender.sendMessage(ChatColor.WHITE + "Stat uploading is set to \"" + TGM.get().getConfig().getBoolean("api.stats.enabled") + "\"");
                return;
            }
            if (cmd.getString(1).equalsIgnoreCase("off")) {
                TGM.get().getConfig().set("api.stats.enabled", false);
                TGM.get().saveConfig();

                sender.sendMessage(ChatColor.GREEN + "Disabled stat uploading.");
            } else if (cmd.getString(1).equalsIgnoreCase("on")) {
                TGM.get().getConfig().set("api.stats.enabled", true);
                TGM.get().saveConfig();

                sender.sendMessage(ChatColor.GREEN + "Enabled stat uploading.");
            } else {
                sender.sendMessage(ChatColor.RED + "Unknown value \"" + cmd.getString(0) + "\". Please specify [on/off]");
            }
        }
    }

    @Command(aliases = {"stats", "stat"}, desc = "View your stats.")
    public static void stats(final CommandContext cmd, CommandSender sender) {
        Player player = (Player) sender;

        if (cmd.argsLength() == 0) {
            viewStats(player, player.getName());
        } else {
            viewStats(player, cmd.getString(0));
        }
    }

    public static void viewStats(Player player, String target) {
        Player targetPlayer = Bukkit.getServer().getPlayer(target);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Unable to find online player " + ChatColor.YELLOW + target);
            return;
        }

        PlayerContext targetUser = TGM.get().getPlayerManager().getPlayerContext(targetPlayer);
        player.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "-------------------------------");
        player.sendMessage(ChatColor.DARK_AQUA + "   Viewing stats for " +  ChatColor.AQUA + targetPlayer.getName());
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_AQUA + "   Level: " + targetUser.getLevelString().replace("[", "").replace("]", ""));
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_AQUA + "   Kills: " + ChatColor.GREEN + targetUser.getUserProfile().getKills());
        player.sendMessage(ChatColor.DARK_AQUA + "   Deaths: " + ChatColor.RED + targetUser.getUserProfile().getDeaths());
        player.sendMessage(ChatColor.DARK_AQUA + "   K/D: " + ChatColor.AQUA + targetUser.getUserProfile().getKDR());
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_AQUA + "   Wins: " + ChatColor.GREEN + targetUser.getUserProfile().getWins());
        player.sendMessage(ChatColor.DARK_AQUA + "   Losses: " + ChatColor.RED + targetUser.getUserProfile().getLosses());
        player.sendMessage(ChatColor.DARK_AQUA + "   W/L: " + ChatColor.AQUA + targetUser.getUserProfile().getWLR());
        player.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "-------------------------------");
    }


    public static void attemptJoinTeam(Player player, MatchTeam matchTeam, boolean autoJoin) {
        attemptJoinTeam(player, matchTeam, autoJoin, false);
    }

    public static void attemptJoinTeam(Player player, MatchTeam matchTeam, boolean autoJoin, boolean ignoreFull) {
        if (!ignoreFull && autoJoin && !player.hasPermission("tgm.pickteam") && !TGM.get().getModule(TeamManagerModule.class).getTeam(player).isSpectator()) {
            player.sendMessage(ChatColor.RED + "You are already in a team.");
            return;
        }
        if (matchTeam.getMembers().size() >= matchTeam.getMax() && !ignoreFull) {
            player.sendMessage(ChatColor.RED + "Team is full! Wait for a spot to open up.");
            return;
        }

        if (!autoJoin) {
            if (!player.hasPermission("tgm.pickteam")) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Only premium users can pick their team! Purchase a rank at http://warzone.store/");
                return;
            }
        }

        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(player);
        TGM.get().getModule(TeamManagerModule.class).joinTeam(playerContext, matchTeam);
    }

}
