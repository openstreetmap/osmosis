package com.bretth.osmosis.core.plugin;


public class CorePlugin extends org.java.plugin.Plugin {

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


