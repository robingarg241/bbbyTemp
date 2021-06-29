package com.bbby.aem.core.ondeploy.workflow;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Convenience class for holding metadata for a given workflow.
 *
 * @author joelepps
 * 7/13/18
 */
public class WorkflowMetadata {

    public static final String BASE_CONF_PATH = "/conf/global/settings/workflow/models/";
    public static final String BASE_VAR_PATH = "/var/workflow/models/";

    @Nonnull
    public final String path;
    @Nonnull
    public final String dataPath;
    @Nonnull
    public final String varPath;
    @Nonnull
    public final String varDataPath;
    @Nonnull
    public final String syncUrl;

    public WorkflowMetadata(String workflowPath) {
        this.path = workflowPath;
        this.dataPath = workflowPath + "/jcr:content";
        this.varPath = BASE_VAR_PATH + workflowPath.replace(BASE_CONF_PATH, "");
        this.varDataPath = varPath + "/metaData";

        this.syncUrl = "http://localhost:4502" + dataPath + ".generate.json";
    }

    @Nonnull
    public Resource getResource(ResourceResolver resolver) {
        return getResource(resolver, path);
    }

    @Nonnull
    public Resource getDataResource(ResourceResolver resolver) {
        return getResource(resolver, dataPath);
    }

    @Nonnull
    public Resource getVarDataResource(ResourceResolver resolver) {
        return getResource(resolver, varDataPath);
    }

    @Nonnull
    private static Resource getResource(ResourceResolver resolver, String path) {
        Resource r = resolver.getResource(path);
        if (r == null) {
            throw new IllegalStateException("Missing required node: " + path);
        }
        return r;
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowMetadata that = (WorkflowMetadata) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
