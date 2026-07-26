package io.github.foecollab.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

// config for the backup feature
public class StatsBackupConfig {
    public static class StatsBackup {
        @ConfigEntry.Gui.Tooltip
        public boolean backupOnServerJoin = false;
    }
}
