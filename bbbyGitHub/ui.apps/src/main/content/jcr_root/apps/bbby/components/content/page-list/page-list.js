'use strict';

/**
 *  This is the main file for page-list
 */

import $ from 'jquery'
import log from 'loglevel'

class PageList {
    constructor($inst) {
        log.trace('Initializing PageList')
    }
}

$('.cmp-page-list').each((index, elem) => {
    new PageList($(elem))
})


//abcde
