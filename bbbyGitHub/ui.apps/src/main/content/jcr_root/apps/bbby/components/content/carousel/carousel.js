'use strict';

/**
 *  This is the main file for carousel
 */

import $ from 'jquery'
import log from 'loglevel'
import { initVideoPlayers, getVideoPlayer } from '../video'
import * as wcmmode from 'js/utils/wcmmode'

if (wcmmode.isPublish()) {
    $('.cmp-carousel')
        .each((index, value) => init($(value)))
}

function init($carousel) {
    let owlConfig = $carousel.attr('data-owl')
    try {
        log.trace(`Initializing carousel with: ${owlConfig}`)
        $carousel.owlCarousel(JSON.parse(owlConfig))

        initVideoPlayers($carousel)

        // Stop video playback for all non-active slides
        $carousel.on('changed.owl.carousel', function(event) {
            const $nonActiveSlides = $(event.target)
                .find('.owl-item')
                .filter((index) => event.item.index != index)
                .each(function() {
                    const $slide = $(this)
                    const $video = $slide.find('.cmp-video')
                    const playerId = $video.attr('id')
                    const videoPlayer = getVideoPlayer(playerId)
                    if (videoPlayer) {
                        videoPlayer.pause()
                    }
                })
        })

    } catch (e) {
        log.error(`${e} on ${owlConfig}`)
    }
}

window.carousel = window.carousel || {}
window.carousel.init = init
