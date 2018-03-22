package ch.batthomas.labycord;

import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Material;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LabyCord extends LabyModAddon {

    private boolean enabled;
    private boolean showElapsedTime;

    private String state;

    @Override
    public void onEnable() {
        getApi().getEventManager().registerOnJoin(serverData -> {
            if (enabled) {
                Logger.getLogger(LabyCord.class.getName()).log(Level.SEVERE, "LabyCord - Player joined - " + (serverData != null ? serverData.getIp() : "null"));
                state = serverData != null ? "Online: " + serverData.getIp() : "Idle: Main Menu";
                updateRichPresence();
            }

        });

        getApi().getEventManager().registerOnQuit(serverData -> {
            if (enabled) {
                Logger.getLogger(LabyCord.class.getName()).log(Level.SEVERE, "LabyCord - Player left");
                state = "Idle: Main Menu";
                updateRichPresence();
            }
        });
    }

    @Override
    public void onDisable() {
        DiscordRPC.discordShutdown();
    }

    @Override
    public void loadConfig() {
        enabled = !getConfig().has("enabled") || getConfig().get("enabled").getAsBoolean();
        showElapsedTime = !getConfig().has("showElapsedTime") || getConfig().get("showElapsedTime").getAsBoolean();
        if (enabled) {
            state = "Idle: Main Menu";
            DiscordRPC.discordInitialize("398546835909640192", null, true);
        }
    }

    @Override
    protected void fillSettings(List<SettingsElement> subSettings) {
        subSettings.add(new BooleanElement("Use Discord Rich Presence", new ControlElement.IconData(Material.LEVER), accepted -> {
            enabled = accepted;
            getConfig().addProperty("enabled", accepted);
            saveConfig();
            if (enabled) {
                state = "Idle: Main Menu";
                DiscordRPC.discordInitialize("398546835909640192", null, true);
            } else {
                DiscordRPC.discordShutdown();
            }
            updateRichPresence();
        }, enabled));

        subSettings.add(new BooleanElement("Show elapsed time", new ControlElement.IconData(Material.WATCH), accepted -> {
            showElapsedTime = accepted;
            getConfig().addProperty("showElapsedTime", accepted);
            saveConfig();
            updateRichPresence();
        }, showElapsedTime));
    }

    private void updateRichPresence() {
        DiscordRichPresence presence = new DiscordRichPresence();
        presence.state = state;
        presence.largeImageKey = "block_large";
        presence.largeImageText = "Minecraft";
        presence.smallImageKey = "labymod_small";
        presence.smallImageText = "LabyMod";
        if (showElapsedTime) {
            presence.startTimestamp = new Date().getTime() / 1000L;
        }
        DiscordRPC.discordUpdatePresence(presence);
    }
}
