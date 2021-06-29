package com.bbby.aem.core.adapters;

import com.bbby.aem.core.services.AltPageResolver;
import com.bbby.aem.core.services.impl.AltPageResolverImpl;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Adapter factory for the {@link AltPageResolver}.
 *
 * @author joelepps 7/14/17
 */
@Component(
    immediate = true,
    service = AdapterFactory.class,
    property = {
        AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.ResourceResolver",
        AdapterFactory.ADAPTABLE_CLASSES + "=org.apache.sling.api.resource.Resource",
        AdapterFactory.ADAPTER_CLASSES + "=com.bbby.aem.core.services.AltPageResolver"
    })
public class AltPageResolverAdapterFactory implements AdapterFactory {

    private static final Logger log = LoggerFactory.getLogger(AltPageResolverAdapterFactory.class);

    @Override
    public <AdapterType> AdapterType getAdapter(@Nonnull Object adaptable, @Nonnull Class<AdapterType> type) {
        try {
            ResourceResolver resolver = null;

            if (adaptable instanceof Resource) {
                resolver = ((Resource) adaptable).getResourceResolver();
            } else if (adaptable instanceof ResourceResolver) {
                resolver = (ResourceResolver) adaptable;
            }

            if (resolver == null) {
                log.warn("{} is not supported", adaptable.getClass());
                return null;
            }

            return (AdapterType) new AltPageResolverImpl(resolver);
        } catch (Exception e) {
            log.error("Failed to during construction of " + getClass().getName() + " for " + adaptable, e);
        }

        return null;
    }
}
