package com.bretth.osmosis.core.plugin;

//automatically created logger for debug and error -output
import java.util.logging.Logger;



public class CorePlugin extends org.java.plugin.Plugin {

    /**
     * Automatically created logger for debug and error-output.
     */
    private static final Logger LOG = Logger.getLogger(CorePlugin.class
            .getName());


    /**
     * Just an overridden ToString to return this classe's name
     * and hashCode.
     * @return className and hashCode
     */
    @Override
    public String toString() {
        return "CorePlugin@" + hashCode();
    }

    /**
     * ignored.
     * ${@inheritDoc}.
     */
    @Override
    protected void doStart() throws Exception {
        // ignored
    }

    /**
     * ignored.
     * ${@inheritDoc}.
     */
    @Override
    protected void doStop() throws Exception {
        // ignored
    }
}


