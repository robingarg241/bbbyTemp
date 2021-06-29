# BBBY Project - Core

The primary bundle for Java code in this AEM project.

* Component Models
* Services
* Event Listeners
* Servlets
* Workflow Processes

## Component Development

### Creating a New Component

Refer to the __generator-bbby-component__ README for instructions on creating
starter files for new components.

### Model Classes

Component models are written using the following frameworks:

* [Sling Models](https://sling.apache.org/documentation/bundles/models.html)
* [cq-component-maven-plugin](https://github.com/OlsonDigital/cq-component-maven-plugin)

Sling Models allows for `@annotation` based marshalling of model classes from 
the JCR.

cq-component-maven-plugin allows for the creation of AEM classic or touchui
dialog fields based on `@annotations` on Java fields or getter methods.

### HTL (Sightly), CSS, JavaScript

The majority of components will have their markup written in HTL. Each component
will have their HTL, CSS, and JS file stored in the same directory. Refer to the
README in __ui.apps__ for more information.


## Vendor Portal

### How to track file upload

#### On AEM Publisher

Batch and single upload files are stored on publisher instance under /content/vendor/admin/{user-name}. For example

	/content/vendor/admin/vendor10_xyz_com  (user node)
    	/9b45e9a5-3fe8-44f1-ab3a-4ff3283d65bb (batch node)
    		49845238681_1jpg  (asset wrapper node)
    		49845238681_2jpg  (asset wrapper node)


To search for a batch node, run the SQL2 query. This can be done from CRX/DE -> Tools > Query

	SELECT * FROM [cq:Page] AS N WHERE ISDESCENDANTNODE(N,"/content/vendor") AND N.[jcr:content/sling:resourceType]="bbby/components/structure/pageUploadBatch" AND N.[jcr:content/projectTitle] LIKE 'dt0823b1%'

To search using date / time

	SELECT * FROM [cq:Page] AS N WHERE ISDESCENDANTNODE(N,"/content/vendor") AND N.[jcr:content/sling:resourceType]="bbby/components/structure/pageUploadBatch" AND N.[jcr:content/projectTitle] LIKE '%2019-08-13-20-39%'


If successful, the resulting node can be expected in CRX/DE to confirm that files were saved.

#### On AEM Author

Immediately following uplload, the batch node and its assets are reverse replicated to AEM author, under the same path.

	/content/vendor/admin/vendor10_xyz_com
    	/9b45e9a5-3fe8-44f1-ab3a-4ff3283d65bb
    		49845238681_1jpg
    		49845238681_2jpg


If batch node is present on one or both publisher instance but is not on author, this indicates that reverse replication failed.

Batch node on author should have the same number of instance nodes under it as on publisher instance (this is the number of files successfully uploaded). If the fie count is different, this, again, may indicate reverse replication failure.

#### Asset Move

Following reverse replication, batch assets are moved to the vendor asset holding folder. This is done by queueing jobs using Sling queue processing.

#### In AEM log files

File upload is logged on AEM Publisher

	/var/log/aem/vendor-portal-publish.log

and on AEM Author

	/var/log/aem/vendor-portal.log


Sample output on publisher in DEBUG logging level

	2019-08-30 20:20:01.752 DEBUG [com.bbby.aem.core.servlets.FileUploadServlet] File upload Username test@testing.com
	2019-08-30 20:20:01.757 DEBUG [com.bbby.aem.core.servlets.FileUploadServlet] Creating Page: /content/vendor/admin/test_testing_com/cd250673-99fa-47aa-8b9e-12acdef24d56 with template JcrNodeResource, type=cq:Template, superType=null, path=/apps/bbby/templates/page-upload-batch
	2019-08-30 20:20:01.767 DEBUG [com.bbby.aem.core.servlets.FileUploadServlet] JCR Data content is null, so we have to create manually
	2019-08-30 20:20:01.789 INFO [com.bbby.aem.core.servlets.FileUploadServlet] Vendor Upload Batch /content/vendor/admin/test_testing_com/cd250673-99fa-47aa-8b9e-12acdef24d56 created. File Count expected is 1 
	2019-08-30 20:20:01.789 DEBUG [com.bbby.aem.core.services.impl.AssetServiceImpl] Reverse replicating /content/vendor/admin/test_testing_com/cd250673-99fa-47aa-8b9e-12acdef24d56
	2019-08-30 20:20:01.822 INFO [com.bbby.aem.core.services.impl.AssetServiceImpl] Saving uploaded file ... MS18RS09_TERNO_ALT_A_CF.jpg
	2019-08-30 20:20:01.884 INFO [com.bbby.aem.core.services.impl.AssetServiceImpl] Uploaded asset /content/vendor/admin/test_testing_com/cd250673-99fa-47aa-8b9e-12acdef24d56/ms18rs09_terno_altacfjpg/jcr:content/MS18RS09_TERNO_ALT_A_CF.jpg saved. Vendor filename is MS18RS09_TERNO_ALT_A_CF.jpg 
	2019-08-30 20:20:01.885 DEBUG [com.bbby.aem.core.services.impl.AssetServiceImpl] Reverse replicating /content/vendor/admin/test_testing_com/cd250673-99fa-47aa-8b9e-12acdef24d56/ms18rs09_terno_altacfjpg/jcr:content

Sample output on author in INFO logging level

	2019-08-30 19:44:37.883 INFO [com.bbby.aem.core.workflow.MoveAssets] Queeing the job for moving /content/vendor/admin/test_testing_com/08cdd839-fd27-4c97-ba27-b84b7fd57996/ms18rs09_terno_altacfjpg/jcr:content/MS18RS09_TERNO_ALT_A_CF.jpg
	2019-08-30 19:44:37.952 INFO [com.bbby.aem.core.workflow.MoveAssets] Queeing the job for moving /content/vendor/admin/test_testing_com/08cdd839-fd27-4c97-ba27-b84b7fd57996/ms18rs09_terno_altajpg/jcr:content/MS18RS09_TERNO_ALT_A.jpg
	2019-08-30 19:44:38.259 INFO [com.bbby.aem.core.workflow.MoveAssets] Queeing the job for moving /content/vendor/admin/test_testing_com/08cdd839-fd27-4c97-ba27-b84b7fd57996/ms18rs09_terno_altbcfjpg/jcr:content/MS18RS09_TERNO_ALT_B_CF.jpg
	30.08.2019 19:53:07.865 *INFO* [JobHandler: /var/workflow/instances/server0/2019-08-27_3/bbby-move-assets-to-dmz_999:/content/vendor/admin/test_testing_com/9b7f41d0-d3f8-4004-97d3-589ba2176bbd/ms18rs09_terno_altacfjpg/jcr:content/MS18RS09_TERNO_ALT_A_CF.jpg] com.bbby.aem.core.workflow.MoveAssets Queeing the job for moving /content/vendor/admin/test_testing_com/9b7f41d0-d3f8-4004-97d3-589ba2176bbd/ms18rs09_terno_altacfjpg/jcr:content/MS18RS09_TERNO_ALT_A_CF.jpg
	30.08.2019 19:53:07.933 *INFO* [JobHandler: /var/workflow/instances/server0/2019-08-27_3/bbby-move-assets-to-dmz_1000:/content/vendor/admin/test_testing_com/9b7f41d0-d3f8-4004-97d3-589ba2176bbd/ms18rs09_terno_altajpg/jcr:content/MS18RS09_TERNO_ALT_A.jpg] com.bbby.aem.core.workflow.MoveAssets Queeing the job for moving /content/vendor/admin/test_testing_com/9b7f41d0-d3f8-4004-97d3-589ba2176bbd/ms18rs09_terno_altajpg/jcr:content/MS18RS09_TERNO_ALT_A.jpg