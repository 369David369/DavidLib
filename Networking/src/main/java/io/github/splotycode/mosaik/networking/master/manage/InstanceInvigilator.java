package io.github.splotycode.mosaik.networking.master.manage;

import io.github.splotycode.mosaik.networking.config.ConfigProvider;
import io.github.splotycode.mosaik.networking.host.Host;
import io.github.splotycode.mosaik.networking.master.host.MasterHost;
import io.github.splotycode.mosaik.networking.statistics.CloudStatistics;
import io.github.splotycode.mosaik.networking.statistics.Instance;
import io.github.splotycode.mosaik.networking.statistics.StatisticalHost;
import io.github.splotycode.mosaik.util.logger.Logger;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;

@AllArgsConstructor
public class InstanceInvigilator {

    private static final Logger LOGGER = Logger.getInstance(InstanceInvigilator.class);

    public static InstanceInvigilator fromConfig(MasterInstanceService service, String prefix) {
        ConfigProvider provider = service.kit.getConfigProvider();
        return new InstanceInvigilator(service,
                provider.getConfigValue(prefix + ".startupInstances", int.class),
                provider.getConfigValue(prefix + ".optimalConnections", int.class),
                provider.getConfigValue(prefix + ".maxInstances", int.class),
                provider.getConfigValue(prefix + ".stopThreshold", int.class),
                provider.getConfigValue(prefix + ".maxStop", double.class),
                provider.getConfigValue(prefix + ".minimumRam", long.class));
    }

    private MasterInstanceService service;

    private int startupInstances, optimalConnections, maxInstances;

    private int stopThreshold;
    /* In percentage */
    private double maxStop;

    private long minimumRam;

    protected void startNewInstance() {
        Optional<Host> oHost = service.getKit().getHosts().stream().filter(host -> {
            if (host instanceof MasterHost) {
                return ((StatisticalHost) host).getStatistics().getFreeRam() >= minimumRam;
            }
            return false;
        }).max(Comparator.comparingDouble(o -> ((StatisticalHost) o).getStatistics().getCpu()));
        if (oHost.isPresent()) {
            MasterHost host = (MasterHost) oHost.get();
            LOGGER.info("New instance for " + service.displayName() + " on " + host.toString());
            host.startNewInstance(service);
        } else {
            LOGGER.info("Not enough resources for " + service.displayName());
        }
    }

    public void updateComponents() {
        CloudStatistics statistics = service.getMaster().getStatistics();
        int instances = statistics.getTotalInstances(service);
        ArrayList<Instance> under = statistics.getInstancesUnder(service, optimalConnections);

        if (under.size() >= stopThreshold) {
            int close = (int) Math.min(under.size(), instances * maxStop);
            int closed = 0;
            Iterator<Instance> iterator = under.iterator();
            while (iterator.hasNext() && closed <= close) {
                Instance instance = iterator.next();
                if (instance.getHostStatistics().getFreeRam() <= minimumRam && !instance.isShuttingDown()) {
                    closed++;
                    iterator.remove();
                    instance.stop();
                }
            }
            int i = 0;
            while (closed <= close && i < 10_000) {
                ArrayList<StatisticalHost> hosts = new ArrayList<>();
                for (Instance instance : under) {
                    if (!hosts.contains(instance.getHost()) && !instance.isShuttingDown()) {
                        hosts.add(instance.getHost());
                    }
                }
                hosts.sort(Comparator.comparingDouble(o -> o.getStatistics().getCpu()));
                for (StatisticalHost host : hosts) {
                    Instance instance = host.getStatistics().getService(service).getLowestInstance();
                    if (!instance.isShuttingDown() && instance.getConnections() < optimalConnections) {
                        closed++;
                        instance.stop();
                    }
                }
                i++;
            }
            if (i == 10_000) {
                LOGGER.warn("Possible stop algorithm error");
            }
            return;
        }

        /* Make sure we have the minimum amount of Instances */
        int register = startupInstances - instances;
        for (int i = 0; i < register; i++) {
            startNewInstance();
        }

        /* If all instances less connections then the optimalConnections and maxInstances() is not reach start new instance */
        if (statistics.getMinimumConnections(service) > optimalConnections && instances < maxInstances) {
            startNewInstance();
        }
    }

}
