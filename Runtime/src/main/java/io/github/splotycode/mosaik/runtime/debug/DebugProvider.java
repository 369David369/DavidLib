package io.github.splotycode.mosaik.runtime.debug;

import io.github.splotycode.mosaik.runtime.LinkBase;
import io.github.splotycode.mosaik.runtime.Links;
import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DebugProvider {

    public static DebugProvider getInstance() {
        return LinkBase.getInstance().getLink(Links.DEBUG_PROVIDER);
    }

    @Getter private Set<String> enabledModes = new HashSet<>();

    public DebugProvider() {
        Map<String, String> modes = LinkBase.getBootContext().getArgParameters("debug");
        for (Map.Entry<String, String> mode : modes.entrySet()) {
            if (mode.getValue().equals("_no_value_") || mode.getValue().equals("true")) {
                enabledModes.add(mode.getKey().toLowerCase());
            }
        }
    }

    public boolean hasDebug(DebugModeType debug) {
        return hasDebug(debug.modeName());
    }

    public boolean hasDebug(String debug) {
        debug = debug.toLowerCase();
        return enabledModes.contains(debug) || enabledModes.contains("all");
    }

}
