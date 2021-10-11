package eu.openanalytics.phaedra.scriptengine.service;

import org.springframework.stereotype.Service;

@Service
public class ShutdownService {

    private static volatile boolean shuttingDown = false;

    public ShutdownService() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shuttingDown = true;
        }));
    }

    public static boolean isShuttingDown() {
        return shuttingDown;
    }

}
