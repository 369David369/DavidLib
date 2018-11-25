package me.david.davidlib.link;

import me.david.davidlib.datafactory.DataKey;
import me.david.davidlib.link.transformer.IStartUpManager;
import me.david.davidlib.link.transformer.ITransformerManager;
import me.david.davidlib.startup.BootContext;
import me.david.davidlib.startup.envirement.StartUpEnvironmentChanger;

public final class Links {

    public static final DataKey<IApplicationManager> APPLICATION_MANAGER = new DataKey<>("startup.applicationManager");
    public static final DataKey<BootContext> BOOT_DATA = new DataKey<>("startup.bootContext");
    public static final DataKey<ITransformerManager> TRANSFORMER_MANAGER = new DataKey<>("transformer.manager");
    public static final DataKey<StartUpEnvironmentChanger> STARTUP_ENVIRONMENT_CHANGER = new DataKey<>("startup.environment_changer");
    public static final DataKey<IStartUpManager> STARTUP_MANAGER = new DataKey<>("startup.manager");

}
