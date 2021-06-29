package com.bbby.aem.core.services.impl;

import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.bbby.aem.core.services.GsonProvider;
import com.bbby.aem.core.util.CommonConstants;
import com.bbby.aem.core.util.ServiceUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Default implementation.
 */
@Component(service = GsonProvider.class, name = "BBBY Gson Provider")
public class GsonProviderImpl implements GsonProvider {

    private static final Gson DEBUG_GSON = newDebugGsonBuilder().create();
    private static final Gson DEBUG_GSON_UNSAFE = newDebugGsonBuilder().disableHtmlEscaping().create();

    private static final Gson GSON = newGsonBuilder().create();
    private static final Gson GSON_UNSAFE = newGsonBuilder().disableHtmlEscaping().create();

    @Reference
    private SlingSettingsService slingSettingsService;

    @Override
    public Gson getGson(boolean unsafe) {
        boolean preProd = ServiceUtils.isPreProd(slingSettingsService);
        if (preProd) {
            if (unsafe) return DEBUG_GSON_UNSAFE;
            return DEBUG_GSON;
        } else {
            if (unsafe) return GSON_UNSAFE;
            return GSON;
        }
    }

    @Override
    public GsonBuilder getGsonBuilder() {
        boolean preProd = ServiceUtils.isPreProd(slingSettingsService);
        if (preProd) {
            return newDebugGsonBuilder();
        } else {
            return newGsonBuilder();
        }
    }

    private static GsonBuilder newDebugGsonBuilder() {
        return new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat(CommonConstants.DATE_FORMATTER_FULL);
    }

    private static GsonBuilder newGsonBuilder() {
        return new GsonBuilder()
            .setDateFormat(CommonConstants.DATE_FORMATTER_FULL);
    }

}

