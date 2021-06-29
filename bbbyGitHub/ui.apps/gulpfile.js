'use strict';

const gulp = require('gulp');

const rollup = require('./gulp-tasks/rollup');
const sass = require('./gulp-tasks/sass');
const copy = require('./gulp-tasks/copy');
const aemsync = require('./gulp-tasks/aemsync.js');
const pullUiContent = require('./gulp-tasks/pull-uicontent.js');
const favicon = require('./gulp-tasks/favicon.js');

/*
 * Base set of tasks.
 *
 * You will likely never invoke these directly.
 */
gulp.task('rollupDev', () => rollup({
    input: 'src/main/gulp/pagelibs/index.js',
    output: 'src/main/content/jcr_root/etc/clientlibs/bbby-pagelibs/js/pagelibs.bundle.js',
    dev: true
}));
gulp.task('rollupProd', () => rollup({
    input: 'src/main/gulp/pagelibs/index.js',
    output: 'src/main/content/jcr_root/etc/clientlibs/bbby-pagelibs/js/pagelibs.bundle.js',
    dev: false
}));
gulp.task('sass', () => sass({
    input: 'src/main/gulp/pagelibs/index.scss',
    output: 'src/main/content/jcr_root/etc/clientlibs/bbby-pagelibs/css/pagelibs.bundle.css'
}));
gulp.task('copyFonts', () => copy({
    src: 'node_modules/font-awesome/fonts/**/**',
    dest: 'src/main/content/jcr_root/etc/clientlibs/bbby-pagelibs/fonts/fontawesome'
}));
gulp.task('watchJs', () => {
    return gulp.watch([
        'src/main/content/jcr_root/apps/bbby/components/**/*.js',
        'src/main/gulp/**/*.js'
    ], ['rollupDev']);
});
gulp.task('watchSass', () => {
    return gulp.watch([
        "src/main/content/jcr_root/apps/bbby/components/**/*.scss",
        "src/main/gulp/**/*.scss"
    ], ['sass']);
});
gulp.task('aemsyncAuth', () => aemsync({
    paths: [
        'src/main/content/jcr_root/**/*.{html,xml,js,less,css,jsp,txt}'
        , '../ui.content/src/main/content/jcr_root/**/*.{html,xml,js,less,css,jsp,txt}'
        , '../assets-ui.apps/src/main/content/jcr_root/**/*.{html,xml,js,less,css,jsp,txt}'
    ],
    port: 4502
}));
gulp.task('aemsyncPub', () => aemsync({
    paths: [
        'src/main/content/jcr_root/**/*.{html,xml,js,less,css,jsp,txt}'
        , '../ui.content/src/main/content/jcr_root/**/*.{html,xml,js,less,css,jsp,txt}'
        , '../assets-ui.apps/src/main/content/jcr_root/**/*.{html,xml,js,less,css,jsp,txt}'
    ],
    port: 4503
}));
gulp.task('favicon', () => favicon({
    srcIcon: './favicon.png',
    faviconDest: '/etc/designs/bbby/favicon',
    htmlDest: '/apps/bbby/components/structure/page'
}));

/**
 * Watch task that rebuilds source JS and Sass on file save.
 *
 * Does not actually push changes to localhost AEM, for that use 'aemsync'.
 */
gulp.task('watch', ['watchJs', 'watchSass']);

/**
 * Watch that also deploys all changed frontend files to your localhost
 * 4502 AEM server.
 *
 * This includes HTML (HTL), JavaScript, and Sass.
 */
gulp.task('aemsync', ['watch', 'aemsyncAuth']);
gulp.task('sync', ['aemsync']); // shortcut
gulp.task('watchPush', ['aemsync']); // legacy command

/**
 * Watch that also deploys all changed frontend files to your localhost
 * 4502 and 4503 (author and publish) AEM server.
 *
 * This includes HTML (HTL), JavaScript, and Sass.
 */
gulp.task('aemsync2', ['watch', 'aemsyncAuth', 'aemsyncPub']);
gulp.task('sync2', ['aemsync2']); // shortcut

/**
 * Pulls down the sample content from an AEM server (by default localhost:4502)
 *
 * Should be used when syncing ui.content from localhost (or any other server)
 * into this repo.
 *
 * Accepts the following optional parameters:
 * --contentPath Page content path to sync
 * --damPath dam path to pull (none if not specified)
 * --tags Include tags
 * --tagPath path to sync
 * -u username
 * -p password
 * -s server (ex. http://dev-server:4502)
 * -x package path (ex. /etc/packages/bbby/bbby.ui.content-6.4.0-SNAPSHOT.zip)
 */
gulp.task('pullUiContent', pullUiContent);
gulp.task('pull', ['pullUiContent']); // shortcut

/**
 * Main build task.
 *
 * - Builds Sass and JavaScript into AEM client library.
 * - Copies font files to AEM client library.
 * - Builds favicon files
 */
gulp.task('build',   ['sass', 'rollupProd', 'copyFonts', 'favicon']);
gulp.task('default', ['build']);
