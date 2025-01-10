package org.monxef.mpunishments.utils;

import org.bukkit.ChatColor;
import org.monxef.mpunishments.data.PunishmentData;
import org.monxef.mpunishments.enums.PunishmentType;

public class MessagesUtils {

    public static String format(String s){
        return ChatColor.translateAlternateColorCodes('&',s);
    }
    public static String getFormattedMessage(String message, PunishmentData data) {
        message = message.replace("{player}", data.getPlayerName());
        message = message.replace("{staff}", data.getPunisherName());
        message = message.replace("{reason}", data.getReason());
        if (data.getExpiry() == null) {
            message = message.replace("{expiry}", "Permanent");
        }else {
            message = message.replace("{expiry}",data.getExpiry().toGMTString());
        }

        message = message.replace("{date}", data.getDate().toString());
        message = message.replace("{action}", getAction(data.getType()));
        message = message.replace("{punishment}", data.getType()+"");

        return message;
    }

    private static String getAction(PunishmentType type){
        switch (type){
            case BAN: return "banned";
            case MUTE: return "muted";
            case IPBAN: return "IP banned";
            case TEMPBAN: return "temporary banned";
            case TEMPMUTE: return "temporary muted";
        }
        return "Punished";
    }
}
