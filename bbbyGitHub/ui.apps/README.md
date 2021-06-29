# BBBY Project - UI Apps

1. [Overview](#1-overview)
2. [Environment Setup](#2-envrionment-setup)
3. [Gulp.js Tasks](#2-gulpjs-tasks)
4. [Adding a New Component](#3-adding-a-new-component)
5. [What Does the Build Do?](#31-what-does-the-build-do)
6. [Further Reading](#5-further-reading)

## 1. Overview

This directory contains the non-Java AEM code. This consists of primarily
frontend component code (HTML/HTL, JavaScript, and CSS).

This is a hybrid gulp.js / Maven managed module.

The frontend code can be built using `gulp build`.

Or the entire module can be built (including frontend) using
`mvn clean install`. Refer to root `pom.xml` for common `mvn` commands.

This hybrid setup allows us to take advantage of best practices in
JavaScript and CSS (Sass) development while still resulting in an AEM package
(zip) containing AEM templates and client libraries.

Features this gulp managed module allows for:

* Automatically push CSS, JS and HTML changes to your localhost:4502 instance
* ES6+ JavaScript support for older browsers ([babel](https://babeljs.io/))
* Sass for CSS ([sass](http://sass-lang.com/))
* Automatically add vendor specific CSS prefixes ([autoprefixer](https://github.com/postcss/autoprefixer))
* Automatically strip out comments from JS and CSS ([gulp-strip-css-comments](https://www.npmjs.com/package/gulp-strip-css-comments))
* Automatically split CSS files suitably for IE < 10 ([gulp-bless](https://www.npmjs.com/package/gulp-bless))*
* Automatically style check JavaScript (lint) ([gulp-eslint](https://github.com/adametry/gulp-eslint))*
* Automatically create SVG sprite files ([svg-sprite](https://www.npmjs.com/package/svg-sprite))*

Must be configured if needed*

## 2. Environment Setup

It is highly recommended that you manage your node/npm installations with
the [Node Version Manager](https://github.com/creationix/nvm).

Refer to *ui.apps/pom.xml* for the version of Node this project requires
(*<nodeVersion>*).

Install gulp:

    npm install -g gulp

Install all npm dependencies:

    cd ./ui.apps
    npm install

If you will be creating a new component you will need our bundled
[Yeoman](http://yeoman.io) generator.

    npm install -g yo
    cd ./generator-bbby-component
    npm link

## 3. Common Gulp.js Commands

For a full list of gulp tasks that can be run check the `gulpfile.js` file.

Gulp build project (very quick, but only frontend):

    gulp build

Gulp watch and push HTL/CSS/JS changes to http://localhost:4502 AEM on file change:

    gulp aemsync

Pull ui.content from localhost:

    gulp pullUiContent

### 4. Adding a New Component

This project uses [Yeoman](http://yeoman.io/), to automate the process of
creating a new component's starter Java, HTL (markup), Sass, and JavaScript.

Please read the *generator-bbby-component/README.md* for instructions.

### 5. What Does the Maven/Gulp Build Do?

At a high level, building this module performs the following:

1. Compile all Sass files into a single `pagelibs.bundle.css`
2. Compile all JavaScript files into a single `pagelibs.bundle.js`
3. Build the AEM package (zip) containing all ui.apps code.

Refer to the *gulpfile.js* file and *gulp-tasks* for more information.

## 6. Further Reading

* [Adobe Documentation: AEM Client-Side Libraries](https://docs.adobe.com/docs/en/aem/6-1/develop/the-basics/clientlibs.html)
* [Yeoman](http://yeoman.io/)
