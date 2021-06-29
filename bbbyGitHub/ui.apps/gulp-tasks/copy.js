const gulp = require('gulp')

module.exports = copy

function copy({src, dest}) {
    return gulp.src([src]).pipe(gulp.dest(dest))
}
