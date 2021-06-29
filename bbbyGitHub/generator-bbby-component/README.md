# generator-bbby-component

This is a Yeoman generator that creates starter source files for new AEM
components.

Files created by this generator are tightly coupled with the development
practices and patterns of the individual project. The template files may
be changed based on the project needs.

Because the template files may change based on the needs of the project,
this generator is installed and run in an atypical fashion
(as the rest of this document outlines).

## Installation / Running

It is highly recommended that you manage your node/npm installations with
the [Node Version Manager](https://github.com/creationix/nvm).

Refer to *ui.apps/pom.xml* for the version of Node this project requires.

Once you have node and npm installed, perform the one time task of installing
[Yeoman](http://yeoman.io) and project specific generator.

    npm install -g yo
    cd [project-root]/generator-bbby-component
    npm link

Then you can run the generator to create a new starter component:

    cd [project-root]
    yo bbby-component

The process will look something like this:

```
~/Documents/workspace/[project-root] yo bbby-component

    _-----_     ╭──────────────────────────╮
   |       |    │      Welcome to the      │
   |--(o)--|    │    bbby-component    │
  `---------´   │        generator!        │
   ( _´U`_ )    │  I create stub files for │
   /___A___\   /│    new AEM components.   │
    |  ~  |     ╰──────────────────────────╯
  __'.___.'__
´   `  |° ´ Y `

? What is the name of the component (with-dashes-all-lowercase)? hello-world
? What do you want to create? Everything
Will use the the following configurations:
componentNameCamel:	helloWorld
prettyName:		Hello World
folderName:		hello-world
className:		component-hello-world
sassFileName:		hello-world
jsModuleName:		hello-world
jsObjectName:		HelloWorld
jsFileName:		hello-world
htlName:		hello-world
htlTemplateName:	renderHelloWorld
javaName:		HelloWorld
javaPackage:		com.bbby.aem.core.models.component
javaPath:		core/src/main/java/com/bbby/aem/core/models/component
? Does this look correct? Yes
Updating ui.apps/src/main/content/jcr_root/apps/bbby/components/content/component-index.scss
Updating ui.apps/src/main/content/jcr_root/apps/bbby/components/content/component-index.js
  create ui.apps/src/main/content/jcr_root/apps/bbby/components/content/hello-world/hello-world.scss
  create ui.apps/src/main/content/jcr_root/apps/bbby/components/content/hello-world/hello-world.js
  create ui.apps/src/main/content/jcr_root/apps/bbby/components/content/hello-world/package.json
  create ui.apps/src/main/content/jcr_root/apps/bbby/components/content/hello-world/hello-world.html
  create core/src/main/java/com/bbby/aem/core/models/component/HelloWorld.java
```
