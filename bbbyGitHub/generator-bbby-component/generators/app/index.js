'use strict';

/**
 * Yeoman generator that creates all starter files for new AEM components.
 *
 * This includes:
 * - Java Model
 * - HTL/Sightly File
 * - JavaScript file
 * - Sass file
 * - Wiring up JavaScript and Sass
 */

const yeoman = require('yeoman-generator');
const chalk = require('chalk');
const yosay = require('yosay');
const path = require('path');
const wiring = require('html-wiring');
const pathExists = require('path-exists');
const find = require('find');

const jsModuleIndexFilePath = 'ui.apps/src/main/content/jcr_root/apps/bbby/components/content/component-index.js';
const sassIndexFilePath = 'ui.apps/src/main/content/jcr_root/apps/bbby/components/content/component-index.scss';
const componentDir = 'ui.apps/src/main/content/jcr_root/apps/bbby/components/content';

module.exports = yeoman.Base.extend({

  initializing: function() {
    if (!pathExists.sync(jsModuleIndexFilePath)) {
      this.env.error('ERROR: Are you running this command from the right location? Could not find ' + jsModuleIndexFilePath);
    }
    if (!pathExists.sync(sassIndexFilePath)) {
      this.env.error('ERROR: Are you running this command from the right location? Could not find ' + sassIndexFilePath);
    }
  },

  prompting: function() {
    var done = this.async();
    var generator = this;

    this.log(yosay(
      'Welcome to the ' + chalk.red('bbby-component') + ' generator! \nI create stub files for new AEM components.'
    ));

    var prompts = [{
      type: 'input',
      name: 'componentNameDashed',
      message: 'What is the name of the component (with-dashes-all-lowercase)?',
      validate: function(componentNameDashed) {
        if (!/^[a-z\-]+$/.exec(componentNameDashed)) {
          return 'Invalid name [' + componentNameDashed + '], all lowercase and dashes.';
        }
        return true;
      }
    }, {
      type: 'list',
      name: 'mode',
      message: 'What do you want to create?',
      choices: [
        {name: 'Everything', value: ['htl','java','sass','js','xml']},
        {name: 'Backend Only', value: ['htl','java','xml']}
      ]
    }];

    this.prompt(prompts).then(function(props) {
      this.props = props;

      this.props.modeSet = new Set(this.props.mode);

      this.log(chalk.cyan('Will use the the following configurations:'));

      this.props.componentNameCamel = this.props.componentNameDashed
        .replace(/-([\w])/g, (m, p1) => `${p1.toUpperCase()}`);
      this.log('componentNameCamel:\t' + chalk.blue(this.props.componentNameCamel));

      this.props.prettyName = this.props.componentNameDashed
        .replace(/(?:^|-)(\w)/g, (m, p1, offset) => `${offset>0 ? ' ' : ''}${p1.toUpperCase()}`);
      this.log('prettyName:\t\t' + chalk.blue(this.props.prettyName));

      this.props.folderName = this.props.componentNameDashed;
      this.log('folderName:\t\t' + chalk.blue(this.props.folderName));

      this.props.className = 'cmp-' + this.props.componentNameDashed;
      this.log('className:\t\t' + chalk.blue(this.props.className));

      if (this.props.modeSet.has('sass')) {
        this.props.sassFileName = this.props.componentNameDashed;
        this.log('sassFileName:\t\t' + chalk.blue(this.props.sassFileName));
      }

      if (this.props.modeSet.has('js')) {
        this.props.jsModuleName = this.props.componentNameDashed;
        this.props.jsObjectName = capitalizeFirstLetter(this.props.componentNameCamel);
        this.props.jsFileName = this.props.componentNameDashed;
        this.log('jsModuleName:\t\t' + chalk.blue(this.props.jsModuleName));
        this.log('jsObjectName:\t\t' + chalk.blue(this.props.jsObjectName));
        this.log('jsFileName:\t\t' + chalk.blue(this.props.jsFileName));
      }

      if (this.props.modeSet.has('htl')) {
        this.props.htlName = this.props.componentNameDashed;
        this.props.htlTemplateName = 'render' + capitalizeFirstLetter(this.props.componentNameCamel);
        this.log('htlName:\t\t' + chalk.blue(this.props.htlName));
        this.log('htlTemplateName:\t' + chalk.blue(this.props.htlTemplateName));
      }

      if (this.props.modeSet.has('java')) {
        this.props.javaName = capitalizeFirstLetter(this.props.componentNameCamel);
        this.props.javaPackage = findComponentModelPackage(this);
        this.props.javaPath = findComponentModelPackagePath(this);
        this.log('javaName:\t\t' + chalk.blue(this.props.javaName));
        this.log('javaPackage:\t\t' + chalk.blue(this.props.javaPackage));
        this.log('javaPath:\t\t' + chalk.blue(this.props.javaPath));
      }

      // Final confirmation prompt
      this.prompt([{
        type    : 'confirm',
        name    : 'continue',
        default : false,
        message : 'Does this look correct?'
      }]).then(function(props) {
        if (!props.continue) {
          this.log(chalk.yellow('Exiting: Please re-run with correct inputs.'));
          process.exit(1);
        }
        done();
      }.bind(this));

    }.bind(this));
  },

  writing: function() {

    /*
     * SASS FILES
     */
    if (this.props.modeSet.has('sass')) {
      this.fs.copyTpl(
        this.templatePath('componentSass.scss'),
        this.destinationPath(path.join(componentDir, this.props.folderName, this.props.sassFileName + '.scss')),
        this.props
      );

      // UPDATE SASS INDEX FILE
      var sassToAdd = '@import "' + this.props.folderName + '/' + this.props.sassFileName + '";\n';
      var sassContent = wiring.readFileAsString(sassIndexFilePath);
      if (!sassContent.includes(sassToAdd)) {
        this.log(chalk.yellow('Updating ') + sassIndexFilePath);

        sassContent = sassContent + sassToAdd;
        wiring.writeFileFromString(sassContent, sassIndexFilePath);
      } else {
        this.log(chalk.cyan('Skipping ') + sassIndexFilePath + ' update');
      }
    }

    /*
     * JS FILES
     */
    if (this.props.modeSet.has('js')) {
      this.fs.copyTpl(
        this.templatePath('componentJavaScript.js'),
        this.destinationPath(path.join(componentDir, this.props.folderName, this.props.jsFileName + '.js')),
        this.props
      );

      this.fs.copyTpl(
        this.templatePath('package.json'),
        this.destinationPath(path.join(componentDir, this.props.folderName, 'package.json')),
        this.props
      )

      // UPDATE JS INDEX FILE
      var jsToAdd = 'import \'./' + this.props.jsFileName + '\';\n';
      var jsContent = wiring.readFileAsString(jsModuleIndexFilePath);
      if (!jsContent.includes(jsToAdd)) {
        this.log(chalk.yellow('Updating ') + jsModuleIndexFilePath);

        jsContent = jsContent + jsToAdd;
        wiring.writeFileFromString(jsContent, jsModuleIndexFilePath);
      } else {
        this.log(chalk.cyan('Skipping ') + jsModuleIndexFilePath + ' update');
      }
    }

    /*
     * HTL FILE
     */
    if (this.props.modeSet.has('htl')) {
      this.fs.copyTpl(
        this.templatePath('htl.html'),
        this.destinationPath(path.join(componentDir, this.props.folderName, this.props.htlName + '.html')),
        this.props
      );
    }

    /*
     * JAVA FILE
     */
    if (this.props.modeSet.has('java')) {
      this.fs.copyTpl(
        this.templatePath('Model.java'),
        this.destinationPath(path.join(this.props.javaPath, this.props.javaName + '.java')),
        this.props
      );
    }

    /*
     * XML FILES
     */
    if (this.props.modeSet.has('xml')) {
      this.fs.copyTpl(
        this.templatePath('_cq_dialog.xml'),
        this.destinationPath(path.join(componentDir, this.props.folderName, '_cq_dialog.xml')),
        this.props
      );
      this.fs.copyTpl(
        this.templatePath('_cq_editConfig.xml'),
        this.destinationPath(path.join(componentDir, this.props.folderName, '_cq_editConfig.xml')),
        this.props
      );
      this.fs.copyTpl(
        this.templatePath('.content.xml'),
        this.destinationPath(path.join(componentDir, this.props.folderName, '.content.xml')),
        this.props
      );
    }
  }
});

/*
 * START HELPER FUNCTIONS
 */

function capitalizeFirstLetter(string) {
  return string.charAt(0).toUpperCase() + string.slice(1);
}

function findComponentModelPackage(generator) {
  var file = _findComponentModelPath(generator);
  var split = file.split(path.join("src", "main", "java"));
  var dpath = split[1];
  dpath = path.dirname(dpath).replace(/^[\/\\]/, "")
  dpath = dpath.replace(/[\/\\]/g, '.');

  return dpath;
}

function findComponentModelPackagePath(generator) {
  var file = _findComponentModelPath(generator);
  var dpath = path.dirname(file);

  return dpath;
}

function _findComponentModelPath(generator) {
  var srcfolder = path.join("core","src")
  var files = find.fileSync(/.*[\/\\]ComponentSlingModel\.java$/, generator.destinationPath(srcfolder));
  if (files.length != 1) {
    generator.env.error('ERROR: Could not locate ComponentSlingModel.java');
  }
  return files[0];
}
