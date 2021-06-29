package com.bbby.aem.core.ondeploy.workflow;

import com.adobe.acs.commons.ondeploy.scripts.OnDeployScriptBase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base on-deploy script class that provides the logic for syncing workflow models.
 *
 * @see WorkflowSyncScriptProvider
 * @author joelepps
 * 7/13/18
 */
public abstract class WorkflowSyncScript extends OnDeployScriptBase {

    private static final Logger log = LoggerFactory.getLogger(WorkflowSyncScript.class);
    private static final Gson gson = new GsonBuilder().create();

    private final String username;
    private final String password;
    private final WorkflowMetadata workflow;

    public WorkflowSyncScript(String username, String password, WorkflowMetadata workflow) {
        this.username = username;
        this.password = password;
        this.workflow = workflow;
    }

    @Override
    protected void execute() throws Exception {
        validate(workflow);
        sync(workflow);
    }

    protected void validate(WorkflowMetadata workflow) {
        // These calls will throw exceptions if paths to not exist
        workflow.getResource(getResourceResolver());
        workflow.getDataResource(getResourceResolver());
    }

    protected void sync(WorkflowMetadata workflow) throws IOException {
        HttpPost post = new HttpPost(workflow.syncUrl);

        post.setHeader("Accept", "application/json");

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        post.addHeader("Authorization", BasicScheme.authenticate(credentials, "UTF-8"));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                // Validation
                if (401 == response.getStatusLine().getStatusCode()) {
                    throw new IOException("Incorrect username/password. Manually update /system/console/configMgr.");
                }
                if (200 != response.getStatusLine().getStatusCode()) {
                    throw new IOException("Unexpected response code: " + response.getStatusLine().getStatusCode());
                }

                String responseMsg = EntityUtils.toString(response.getEntity());
                SyncResponse syncResponse = gson.fromJson(responseMsg, SyncResponse.class);

                // Error handling
                if (syncResponse.errorList != null && !syncResponse.errorList.isEmpty()) {
                    throw new IOException("Errors: " +
                        syncResponse.errorList.stream()
                            .map(SyncResponse.Error::toString)
                            .collect(Collectors.joining(" ")));
                }

                log.info("{}: {} {}", workflow, syncResponse.msg, syncResponse.modelPath);
            }
        } catch (IOException e) {
            throw new IOException("Failed to sync: " + workflow, e);
        }
    }

    public static class SyncResponse {
        public final String msg;
        public final String modelPath;
        public final List<Error> errorList;

        public SyncResponse(String msg, String modelPath, List<Error> errorList) {
            this.msg = msg;
            this.modelPath = modelPath;
            this.errorList = errorList;
        }

        public static class Error {
            public final String errorPath;
            public final String errorMsg;

            public Error(String errorPath, String errorMsg) {
                this.errorPath = errorPath;
                this.errorMsg = errorMsg;
            }

            @Override
            public String toString() {
                return errorMsg + " " +  errorPath;
            }
        }
    }
}
