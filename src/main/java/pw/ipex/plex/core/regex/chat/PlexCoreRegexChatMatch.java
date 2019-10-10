package pw.ipex.plex.core.regex.chat;

import net.minecraft.util.IChatComponent;
import pw.ipex.plex.core.regex.PlexCoreRegex;
import pw.ipex.plex.core.regex.PlexCoreRegexEntry;

import java.util.HashMap;
import java.util.Map;

public class PlexCoreRegexChatMatch {
    private Map<String, PlexCoreRegexChatMatchItem> entries = new HashMap<>();

    public PlexCoreRegexChatMatch(IChatComponent component) {
        for (PlexCoreRegexEntry entry : PlexCoreRegex.getEntriesMatchingText(component.getFormattedText())) {
            entries.put(entry.entryName, new PlexCoreRegexChatMatchItem(component.getFormattedText(), entry));
        }
    }

    public boolean matches(String entryName) {
        return this.entries.containsKey(entryName);
    }

    public PlexCoreRegexChatMatchItem get(String entryName) {
        return this.entries.get(entryName);
    }
}
