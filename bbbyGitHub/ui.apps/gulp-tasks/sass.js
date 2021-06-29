const gulp = require('gulp')
const path = require('path')
const source = require('vinyl-source-stream')
const rename = require('gulp-rename')
const sass = require('gulp-sass')
const postcss = require('gulp-postcss')
const autoprefixer = require('autoprefixer')
const eol = require('gulp-line-ending-corrector')
const stripCssComments = require('gulp-strip-css-comments')

module.exports = sassTask

/**
 * @param input (string): the index/input scss file.
 * @param output (string): the path for the result scss directory.
 */
function sassTask({input, output}) {

    const outputDir = path.dirname(output)
    const outputFile = path.basename(output, '.css')

    return gulp.src(input)
        .pipe(sass({
            includePaths: ['src/main/gulp/common', 'node_modules']
        }).on('error', sass.logError))
        .pipe(rename({
            basename: outputFile,
            extname: '.css'
        }))
        .pipe(postcss([autoprefixer({
            browsers: ['Chrome >= 30', 'ff >= 30', 'ie >= 10', 'Safari >= 6']
        })]))
        .pipe(stripCssComments())
        .pipe(eol({
            eolc: 'LF',
            encoding: 'utf8'
        }))
        .pipe(gulp.dest(outputDir)) // aem css
}
