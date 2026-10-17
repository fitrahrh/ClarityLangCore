package com.harukiyu.claritylangcore.managers;

import com.harukiyu.claritylangcore.ClarityLangCore;

/**
 * Legacy reference holder, forwards to {@link ClarityLangCore}.
 */
@Deprecated
public class PluginManager {
    private static final PluginManager INSTANCE = new PluginManager();
    
    private PluginManager() {}
    
    @Deprecated
    public static PluginManager getInstance() {
        return INSTANCE;
    }

    @Deprecated
    public void initialize() {
        // Handled directly in ClarityLangCore#onEnable()
    }
}
