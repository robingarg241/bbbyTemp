const rollup = require('rollup')
const babel = require('rollup-plugin-babel')
const resolve = require('rollup-plugin-node-resolve')
const commonjs = require('rollup-plugin-commonjs')
const util = require('gulp-util')
const replace = require('rollup-plugin-replace')
const includePaths = require('rollup-plugin-includepaths')
const builtins = require('rollup-plugin-node-builtins')
const globals = require('rollup-plugin-node-globals')

module.exports = rollupTask

/**
 * @param input (string): the index/input JS file.
 * @param output (string): the path for the result JS file.
 * @param dev (boolean): true is development mode, false if production mode.
 */
function rollupTask({input, output, dev}) {
    const mode = dev ? 'development' : 'production';

    return rollup.rollup({
        input: input,
        external: ['jquery', 'moment'],
        plugins: [
            // Allows components to reference common modules without
            // error prone "../../.. (etc)" relative paths
            includePaths({
                paths: ['src/main/gulp/common'],
                external: [] // https://github.com/calvinmetcalf/rollup-plugin-node-builtins/issues/41
            }),
            // Resolves node_module dependencies
            resolve({
                mainFields: ['module', 'main'],
                browser: true,
                preferBuiltins: false
            }),
            // Babel
            babel({
                // As a general rule we trust that dependencies do not
                // expose untranspiled ES6 to us in the same way they don't
                // expose CoffeeScript, ClojureScript or TypeScript.
                exclude: 'node_modules/**'
            }),
            // Converts CommonJS modules to ES2015 modules
            commonjs({
                sourceMap: false
            }),
            // Handle Node builtins: fs, stream, crypto, etc.
            globals(),
            builtins(),
            // Replace any any uses of the NodeJS NODE_ENV with the static
            // 'production' or 'development' string.
            replace({
                'process.env.NODE_ENV': JSON.stringify(mode)
            }),
        ]
    })
    .then(function(bundle) {
        bundle.write({
            format: 'iife',
            globals: {
                // alias jquery imports to global 'jQuery' object
                jquery: 'jQuery',
                moment: 'moment'
            },
            file: output
        })
    })
    .catch(util.log)
}
