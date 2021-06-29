# bbby-com

This is the bbby-com AEM project.

## Modules

Below is a high-level summary of project modules. For more information please
refer to the README.md files in each module.

* __core__: OSGi bundle contains all Java based functionality for the site.
* __ui.apps__: _/apps_ and _/etc_ code, markup, and overlays.
* __ui.content__: Static (codebase controlled) content that should be present on all AEM instances.
* __generator-bbby-component__: [Yeoman](http://yeoman.io/learning/index.html) generator for creating starter files for new components.

## First Time Install Instructions

These are things that should be done the very first time this project is deployed to a new environment.

### 1. Author Servers: ImageMagick

ImageMake must be installed on author servers in order for the DAM Update Asset workflow to run.

You can verify this by ensuring the command `convert -h` returns help text.

If missing, you can typically install ImageMagick via the operating system's package manager (brew, yum, apt-get, etc.).

### 2. Workflow Sync

Workflows managed by the project, like the DAM Update Asset workflow, must be synced after changes are made and deployed.

This can be done automatically on-deploy through the "Workflow Sync Script Provider" (`com.bbby.aem.core.ondeploy.scripts.WorkflowSyncScriptProvider`). See javadoc for instructions.

At a minimum you must make sure the "Workflow Sync Script Provider" configuration in `/system/console/configMgr` has valid credentials for the system (default config is admin/admin).

### 3. Initial Content

This project includes the following

#### Vendor Portal pages

Located under /content/bedbath/vendor-portal are in ui.content package and will install as part of the maven build

#### Folder structure

These are asset folders under /content/dam/bbby. To install them in your local AEM, run these commands 

    cd bbby/scripts/tags/scripts
    chmod +x ./taxonomy.sh
    ./taxonomy.sh

#### Tag Taxonomy

To create tags under /content/cq:tags/bbby, run the following script

    cd bbby/scripts/tags/scripts
    ./runAll.sh

## I need to create a new component, what do I do?

You should look through the README.md files, here is the TL;DR version.

Create the starter files (see readme in __generator-bbby-component__):

    yo bbby-component

Update the HTL/Sightly markup in:

    ui.apps/src/main/content/jcr_root/apps/bbby/components/content/[component-name]/[component-name].html

Update the components JavaScript and CSS (Sass):

    ui.apps/[...]/[component-name]/[component-name].scss
    ui.apps/[...]/[component-name]/[component-name].js

Deploy to your localhost author environment to test:

    mvn clean install -P autoInstallPackage

If you are working on Sass/JS/HTL automatically push file changes to your localhost on save:

    cd ui.apps
    gulp aemsync

## How to build

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

If you have a running AEM instance you can build and package the whole project and deploy into AEM with:

    mvn clean install -P autoInstallPackage

You can also specify only certain modules are built and deployed:

    mvn clean install -P autoInstallPackage -pl core,ui.apps

Only build and deploy the java code (core) updates:

    mvn clean install -P autoInstallBundle -pl core

## Artifact Repository

When performing a Maven release, this project will by default use the Hero Digital Nexus artifact repository ([here](https://build.herodigital.com:8081/nexus/)).

In order to access the project specific artifacts hosted here, please ask Hero Digital for instructions.

Long term, it is recommended that you migrate to your own hosted repository.
