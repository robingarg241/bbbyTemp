'use strict';

import log from 'loglevel'
import queryString from 'query-string'

var parms = queryString.parse(location.search);

if (parms.trace) {
    log.setLevel('trace');

} else if (parms.debug) {
    log.setLevel('debug');

} else if (parms.info) {
    log.setLevel('info');

} else if (parms.warn) {
    log.setLevel('warn');

} else if (parms.error) {
    log.setLevel('error');

} else if (parms.silent) {
    log.setLevel('silent');
}
