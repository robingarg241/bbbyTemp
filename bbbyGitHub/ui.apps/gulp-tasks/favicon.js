const gulp = require('gulp')
const path = require('path')
const favicons = require("favicons").stream
const gutil = require("gulp-util")
const filter = require('gulp-filter')
const replace = require('gulp-replace')

module.exports = generateFavicons

function generateFavicons({srcIcon, faviconDest, htmlDest}) {
    return gulp.src(srcIcon)
        .pipe(favicons({
            // ::: Web App Configs :::
            // appName: 'BBBY',
            // appDescription: 'BBBY Website',
            // developerName: 'BBBY',
            // developerURL: '',
            // background: '#FFF',
            // url: '',
            // display: 'browser',
            // orientation: 'any',
            logging: false,
            pipeHTML: false,
            replace: true,
            path: faviconDest,
            html: "favicon.html",
            pipeHTML: true,
            icons: {
                favicons: true,
                appleIcon: true,

                windows: false,
                android: false,
                appleStartup: false,
                coast: false,
                firefox: false,
                yandex: false
            }
        }))
        .on("error", gutil.log)

        // Exclude web-app metatags from output. This project does not support mobile web app specific functionality.
        .pipe(replace(/.*mobile-web-app.*/g, ''))

        // Favicon assets output
        .pipe(gulp.dest(path.join('src/main/content/jcr_root', faviconDest)))

        // HTML output
        .pipe(filter('**/favicon.html'))
        .pipe(gulp.dest(path.join('src/main/content/jcr_root', htmlDest)));
}
