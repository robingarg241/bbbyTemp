/**
 *  This is the main file for pagelibs
 */

// Babel polyfill
// https://babeljs.io/docs/en/babel-polyfill/
import "@babel/polyfill";

// Logging setup
import 'js/logsetup.js';

// Custom Base Scripts
import 'js/custom-polyfills.js';
import 'js/polyfills/polyfill.forEach.js';
import 'js/polyfills/polyfill.after.js';
import 'js/polyfills/polyfill.fetch.js';
import 'js/polyfills/polyfill.object.keys.js';
import 'js/polyfills/polyfill.promise.js';
import 'js/polyfills/polyfill.url.js';

// owl carousel
import 'owl.carousel/dist/owl.carousel';

//anugalr js
import 'angular/angular.min.js';

// data table
import 'js/vanilla-dataTables.js';

// Component Scripts
import '../../content/jcr_root/apps/bbby/components/content/component-index';
