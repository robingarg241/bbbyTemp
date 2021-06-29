const gulp = require('gulp')
const rp = require('request-promise')
const fs = require('fs-extra')
const path = require('path')
const gutil = require('gulp-util')
const chalk = require('chalk')
const notifier = require('node-notifier')
const aemsync = require('aemsync')

module.exports = sync

function sync({paths, port}) {

    const watcher = gulp.watch(paths, {dot: true})

    watcher.on('change', function(event) {
        if (event.type == 'changed' || event.type == 'added') {
            const ext = path.extname(event.path)
            if (ext == '.xml') {
                pushAemSync(event, port)
            } else {
                pushCurl(event, port)
            }
        }
    })
}

function pushAemSync(event, port) {
    let pathToPush = event.path

    // .content.xml changes must push the entire directory
    // refer to https://github.com/gavoja/aemsync for more info
    if (path.basename(pathToPush) == '.content.xml') {
        pathToPush = path.dirname(pathToPush)
    }

    const targets = ["http://admin:admin@localhost:" + port]
    const onPushEnd = (err, host) => {
        if (err) {
            gutil.log(chalk.red('Error: ' + err))
        } else {
            notifier.notify({
                title: 'Synced: ' + host,
                message: path.basename(event.path)
            })
        }
    }

    aemsync.push({pathToPush, targets, onPushEnd})
}

function pushCurl(event, port) {
    const idx = event.path.indexOfEnd('jcr_root')
    const aemPath = event.path.substr(idx)
    const filename = path.basename(aemPath)
    const dirname = path.dirname(aemPath)
    const pathParts = event.path.split('jcr_root')
    const formData = {}
    formData[filename] = fs.createReadStream(event.path)

    rp({
        method: 'POST',
        url: "http://localhost:" + port + "/" + dirname,
        formData: formData,
        headers: {
            'Authorization': 'Basic ' + new Buffer('admin:admin').toString('base64')
        }
    }).then(function(resp) {
        if (resp) {
            gutil.log('Pushed: ' + chalk.cyan(port + ', ' + event.path))
            notifier.notify({
                title: 'Pushed: ' + port,
                message: filename
            })
        } else {
            throw 'Unexpected result pushing ' + event.path
        }
    }).catch(function(e) {
        gutil.log(chalk.red('Error: ' + e))
    })
}


String.prototype.indexOfEnd = function(string) {
    var io = this.indexOf(string)
    return io == -1 ? -1 : io + string.length
}
