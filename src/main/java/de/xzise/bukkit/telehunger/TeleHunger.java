package de.xzise.bukkit.telehunger;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;
import org.matheclipse.parser.client.SyntaxError;
import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.eval.DoubleEvaluator;
import org.matheclipse.parser.client.eval.DoubleVariable;

import de.xzise.MinecraftUtil;
import de.xzise.XLogger;

public class TeleHunger extends JavaPlugin {

    public static final float EXHAUSTION_MAX = 4.0F;
    public static final int FOOD_MAX = 20;

    private final Set<Algorithm> nodes = new HashSet<Algorithm>();
    private XLogger logger;
    private boolean useEvent;
    private final DoubleVariable distance = new DoubleVariable(0);

    public boolean useEvent() {
        return this.useEvent;
    }

    public void onDisable() {
        this.logger.disableMsg();
    }

    public void onEnable() {
        this.logger = new XLogger(this);
        // Read all formulas
        Configuration config = new Configuration(new File(this.getDataFolder(), "config.yml"));
        config.load();
        this.useEvent = config.getBoolean("event", true);
        List<ConfigurationNode> formulas = config.getNodeList("formulas", null);
        this.nodes.clear();
        final DoubleEvaluator engine = new DoubleEvaluator();
        engine.defineVariable("len", this.distance);
        if (formulas != null) {
            for (ConfigurationNode formula : formulas) {
                final String formulaText = formula.getString("formula");
                if (formulaText != null) {
                    try {
                        ASTNode formulaNode = engine.parse(formulaText);
                        this.nodes.add(new Algorithm(formula.getDouble("distance", -1), formula.getBoolean("always", false), engine, formulaNode, formulaText));
                    } catch (SyntaxError e) {
                        this.logger.warning(e.getMessage());
                    }
                }
            }
        }
        this.logger.info("Loaded " + this.nodes.size() + " formula(s)");

        this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_TELEPORT, new THPlayerListener(this), Event.Priority.Highest, this);

//        this.getCommand("exdr").setExecutor(new CommandExecutor() {
//
//            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//                if (args.length == 1 || args.length == 2) {
//                    final Player checked;
//                    if (args.length == 2) {
//                        checked = TeleHunger.this.getServer().getPlayer(args[1]);
//                        if (checked == null) {
//                            sender.sendMessage(ChatColor.RED + "Invalid name!");
//                            return true;
//                        }
//                    } else if (sender instanceof Player) {
//                        checked = (Player) sender;
//                    } else {
//                        sender.sendMessage(ChatColor.RED + "Only players could execute this with one argument!");
//                        return false;
//                    }
//                    Float exhaustion = MinecraftUtil.tryAndGetDouble(args[0]).floatValue();
//
//                    if (exhaustion != null) {
//                        drainHunger(checked, exhaustion);
//                        sender.sendMessage("Exhausted " + ChatColor.GREEN + exhaustion + " to player '" + ChatColor.GREEN + checked.getName() + ChatColor.WHITE + "'!");
//                    } else {
//                        sender.sendMessage(ChatColor.RED + "Invalid exhaustion!");
//                    }
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });
//        this.getCommand("exnatdr").setExecutor(new CommandExecutor() {
//
//            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//                if (args.length == 1 || args.length == 2) {
//                    final Player checked;
//                    if (args.length == 2) {
//                        checked = TeleHunger.this.getServer().getPlayer(args[1]);
//                        if (checked == null) {
//                            sender.sendMessage(ChatColor.RED + "Invalid name!");
//                            return true;
//                        }
//                    } else if (sender instanceof Player) {
//                        checked = (Player) sender;
//                    } else {
//                        sender.sendMessage(ChatColor.RED + "Only players could execute this with one argument!");
//                        return false;
//                    }
//                    Float exhaustion = MinecraftUtil.tryAndGetDouble(args[0]).floatValue();
//
//                    if (exhaustion != null) {
//                        drainHungerNative(checked, exhaustion);
//                        sender.sendMessage("Exhausted " + ChatColor.GREEN + exhaustion + " to player '" + ChatColor.GREEN + checked.getName() + ChatColor.WHITE + "'!");
//                    } else {
//                        sender.sendMessage(ChatColor.RED + "Invalid exhaustion!");
//                    }
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        });
//
        this.getCommand("exc").setExecutor(new CommandExecutor() {

            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                Player checked = null;
                if (args.length == 1) {
                    checked = TeleHunger.this.getServer().getPlayer(args[0]);
                    if (checked == null) {
                        sender.sendMessage(ChatColor.RED + "Invalid name!");
                        return true;
                    }
                } else if (args.length == 0) {
                    if (sender instanceof Player) {
                        checked = (Player) sender;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Only players could execute this without arguments!");
                        return true;
                    }
                } else {
                    return false;
                }

                sender.sendMessage("Player '" + ChatColor.GREEN + checked.getName() + ChatColor.WHITE + "' has total exhaustion: " + getTotalExhaustion(checked));
                return true;
            }
        });
        
        this.getCommand("exnatc").setExecutor(new CommandExecutor() {

            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                Player checked = null;
                if (args.length == 1) {
                    checked = TeleHunger.this.getServer().getPlayer(args[0]);
                    if (checked == null) {
                        sender.sendMessage(ChatColor.RED + "Invalid name!");
                        return true;
                    }
                } else if (args.length == 0) {
                    if (sender instanceof Player) {
                        checked = (Player) sender;
                    } else {
                        sender.sendMessage(ChatColor.RED + "Only players could execute this without arguments!");
                        return true;
                    }
                } else {
                    return false;
                }

                sender.sendMessage("Player '" + ChatColor.GREEN + checked.getName() + ChatColor.WHITE + "' has total exhaustion: " + getTotalExhaustionNative(checked));
                return true;
            }
        });

        this.logger.enableMsg();
    }

//    public boolean teleport(Player player, Location target) {
//        if (player.getWorld() == target.getWorld()) {
//            final double distance = player.getLocation().distance(target);
//            if (!this.handleTeleport(player, distance)) {
//                return false;
//            }
//        }
//        player.teleport(target);
//        return true;
//    }

    public boolean handleTeleport(Player player, double distance) {
        this.distance.setValue(distance);
        float hungerSum = 0;
        Algorithm lowest = null;
        int i = 0;
        for (Algorithm algorithm : this.nodes) {
            if (algorithm.matched(distance)) {
                // Don't calculate if this is a better match!
                if (algorithm.betterMatch(lowest)) {
                    lowest = algorithm;
                } else if (algorithm.isApplingAlways()) {
                    hungerSum += algorithm.calculateHunger();
                    System.out.println("hs " + hungerSum + " @" + (++i));
                }
            }
        }
        if (lowest != null) {
            hungerSum += lowest.calculateHunger();
            System.out.println("hs " + hungerSum + " @" + (++i) + " w/ " + lowest.getFormula());
        }
        this.logger.info("Handle teleport by " + player.getName() + " with a distance of " + distance + " m. Hungersum: " + hungerSum);
        if (hasEnoughFood(player, hungerSum)) {
            drainHunger(player, hungerSum);
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Not enought food eaten!");
            return false;
        }
    }

    public void drainHunger(Player player, float hunger) {
        try {
            player.exhaust(hunger);
        } catch (NoSuchMethodError e) {
            this.logger.info("Couldn't call Player.exhaust(float)");
            this.drainHungerNative(player, hunger);
        }
        
        // Debug
        this.logger.info("DEBUG exhausted " + hunger);
    }

    private void drainHungerNative(Player player, float hunger) {
        // TODO: Special way to handle exhaustion?
        final float exhaustion = hunger % EXHAUSTION_MAX;
        final int saturationOffset = (int) Math.floor((player.getExhaustion() + exhaustion) / EXHAUSTION_MAX);
        final float leftHunger = (float) Math.floor(hunger / EXHAUSTION_MAX) + saturationOffset;
        final float saturation = Math.min(leftHunger, player.getSaturation());
        final int foodLevel = (int) Math.round(leftHunger - saturation);
        this.logger.info(exhaustion + " " + saturationOffset + " " + leftHunger + " " + saturation + " " + foodLevel);
        this.logger.info("Player '" + player.getName() + "' has: Foodlevel: " + player.getFoodLevel() + " Saturation: " + player.getSaturation() + " Exhaustion: " + player.getExhaustion());
        
        player.setExhaustion((player.getExhaustion() + exhaustion) % EXHAUSTION_MAX);
        player.setSaturation(player.getSaturation() - saturation);
        player.setFoodLevel(player.getFoodLevel() - foodLevel);
    }

    public static float getTotalExhaustion(Player player, XLogger logger) {
        try {
            return player.getTotalExhaustion();
        } catch (NoSuchMethodError e) {
            logger.info("Couldn't call Player.getTotalExhaustion()");
            return getTotalExhaustionNative(player);
        }
    }

    public float getTotalExhaustion(Player player) {
        return getTotalExhaustion(player, this.logger);
    }
    
    public boolean hasEnoughFood(Player player, float hunger) {
        return getTotalExhaustion(player) > hunger;
    }

    private static float getTotalExhaustionNative(Player player) {
        return (player.getFoodLevel() + player.getSaturation()) * EXHAUSTION_MAX + player.getExhaustion();
    }
}
