'use strict'

/*
 * Gulp task that rebuilds the ui.content package, downloads it, then copies
 * the content into the ui.content module.
 *
 * Useful for syncing ui.content updates made on localhost (or any other server)
 * back into this project's git repository.
 */

const request = require('request')
const rp = require('request-promise')
const Promise = require("bluebird")
const read = require('read-file')
const xpath = require('xpath')
const dom = require('xmldom').DOMParser
const path = require('path')
const util = require('util')
const fs = require('fs-extra')
const gutil = require('gulp-util')
const chalk = require('chalk')
const AdmZip = require('adm-zip')

const argv = require('yargs')
    .default('contentPath', '/content/bbby-components')
    .default('damPath', '') // In order to reduce unecessary and expensive image commits, force user to provide a specific image path
    .default('tags', false)
    .default('tagPath', '/etc/tags/bbby-components')
    .default('u', 'admin') // user
    .default('p', 'admin') // password
    .default('s', 'http://localhost:4502') // server
    .default('x', getUiContentPackagePath()) // path to ui.content package
    .argv

module.exports = function pullUiContent(cb) {
    cleanFunc()
        .then(buildFunc)
        .then(downloadFunc)
        .then(unzipFunc)
        .then(copyFunc)
        .catch(function(e) {
            gutil.log(chalk.red('Error: ' + e))
        })
        .finally(function() {
            cb()
        })
}

const cleanFunc = function() {
    return new Promise(function(resolve, reject) {
        try {
            fs.emptyDirSync('target')
            gutil.log('Clean: ' + chalk.cyan('target'))

            resolve('done')
        } catch (e) {
            reject(e)
        }
    })
}

function buildFunc() {
    gutil.log('Building: ' + chalk.cyan(argv.x))
    return rp({
        method: 'POST',
        uri: argv.s + '/crx/packmgr/service/.json' + argv.x + '?cmd=build',
        headers: {
            'Authorization': 'Basic ' + new Buffer(argv.u + ':' + argv.p).toString('base64')
        }
    })
}

function downloadFunc() {
    return new Promise(function(resolve, reject) {
        gutil.log('Downloading: ' + chalk.cyan(argv.s + argv.x))
        try {
            request({
                    method: 'GET',
                    uri: argv.s + argv.x,
                    headers: {
                        'Authorization': 'Basic ' + new Buffer(argv.u + ':' + argv.p).toString('base64')
                    }
                })
                .pipe(fs.createWriteStream('target/ui.content.zip'))
                .on('close', function() {
                    resolve('target/ui.content.zip')
                })
        } catch (e) {
            reject(e)
        }
    })
}

function unzipFunc() {
    return new Promise(function(resolve, reject) {
        gutil.log('Unzipping: ' + chalk.cyan('target/ui.content.zip'))
        try {
            var zip = new AdmZip('./target/ui.content.zip')
            zip.extractAllTo("./target/ui.content", true)
            resolve('done')
        } catch (e) {
            reject(e)
        }
    })
}

function copyFunc() {
    return new Promise(function(resolve, reject) {
        const sourcePages = path.join('./target/ui.content/jcr_root', argv.contentPath)
        const targetPages = path.join('../ui.content/src/main/content/jcr_root', argv.contentPath)

        const sourceDam = path.join('./target/ui.content/jcr_root', argv.damPath)
        const targetDam = path.join('../ui.content/src/main/content/jcr_root', argv.damPath)

        const sourceTags = path.join('./target/ui.content/jcr_root', argv.tagPath)
        const targetTags = path.join('../ui.content/src/main/content/jcr_root', argv.tagPath)

        try {
            fs.copySync(sourcePages, targetPages, {
                clobber: true
            })
            gutil.log('Copied: ' + chalk.cyan(sourcePages) + ' -> ' + chalk.cyan(targetPages))

            if (argv.damPath) {
                fs.copySync(sourceDam, targetDam, {
                    clobber: true,
                    filter: (path) => { return !path.match(/\/dam\/.*\/cq5dam/) } // exclude dam renditions
                })
                gutil.log('Copied: ' + chalk.cyan(sourceDam) + ' -> ' + chalk.cyan(targetDam))
            }

            if (argv.tags) {
                fs.copySync(sourceTags, targetTags, {
                    clobber: true
                })
                gutil.log('Copied: ' + chalk.cyan(sourceTags) + ' -> ' + chalk.cyan(targetTags))
            }

            resolve('done')
        } catch (e) {
            reject(e)
        }
    })
}

function getUiContentPackagePath() {
    const pom = read.sync(path.join('..', 'ui.content/pom.xml'))
    const doc = new dom().parseFromString(pom.toString())
    const select = xpath.useNamespaces({
        'pom': 'http://maven.apache.org/POM/4.0.0'
    })
    const artifactId = select('/pom:project/pom:artifactId/text()', doc)[0] + ''
    const pomVersion = select('/pom:project/pom:version/text()', doc)[0]
    const parentVersion = select('/pom:project/pom:parent/pom:version/text()', doc)[0]
    const version = pomVersion || parentVersion
    const group = select("//pom:plugin[pom:artifactId='content-package-maven-plugin']/pom:configuration/pom:group/text()", doc)[0]

    return '/etc/packages/' + group + '/' + artifactId + '-' + version + '.zip'
}
