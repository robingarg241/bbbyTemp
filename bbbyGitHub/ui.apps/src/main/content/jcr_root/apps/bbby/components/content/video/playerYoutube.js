import log from 'loglevel'
import Player from './player'

const YOUTUBE_API_URL = "https://www.youtube.com/player_api";
const youtubeReady = $.Deferred();

export default class YoutubePlayer extends Player {

    constructor($elem) {
        super($elem)

        this.youtubeElemId = Player.guid()
        $elem.find('.cmp-video__player').attr('id', this.youtubeElemId)
    }

    initPlayer() {
        // create player once API script load is done AND youtube reports as ready.
        this.playerPromise = Promise.all([loadYoutubeApi(), youtubeReady]).then(() => {
            const videoId = this.getVideoId()
            const autoplay = this.getAutoplay()

            return new YT.Player(this.youtubeElemId, {
                height: '400',
                width: '100%',
                videoId: videoId,
                playerApiId: this.youtubeElemId,
                playerVars: {
                    autoplay: autoplay ? 1 : 0,
                    modestbranding: 1,
                    controls: 1,
                    showinfo: 0,
                    autohide: 1,
                    color: 'white'
                },
                events: {
                    'onStateChange': this._onPlayerStateChange.bind(this),
                    'onReady': this._onPlayerReady.bind(this)
                }
            })
        })

        log.debug('Created player for: ', this.$elem)
    }

    _startPlayback() {
        this.playerPromise.then(player => {
            player.playVideo()
        })
    }

    _pausePlayback() {
        this.playerPromise.then(player => {
            player.pauseVideo()
        })
    }

    _onPlayerStateChange(event) {
        if (event && event.target && event.target.getVideoData) {

            //
            // Below are placeholder for if we need to add analytics hooks.
            //

            if (event.data == 1) {
                // playing
                this.$elem.parents('.owl-carousel').trigger('stop.owl.autoplay') // If in a carousel, stop autoplay
                log.debug('Playing video: ', event.target.getVideoData());
            }
            if (event.data == 2) {
                // paused
                log.debug('Pause video: ', event.target.getVideoData());
            }
            if (event.data == 0) {
                // ended
                log.debug('End video: ', event.target.getVideoData());
            }
        }
    }

    _onPlayerReady(event) {
        if (event && event.target && event.target.getVideoData) {

            //
            // Below is placeholder for if we need to add analytics hooks.
            //

            log.debug('Video ready: ', event.target.getVideoData());
        }
    }
}

// YouTube API will call this on API being ready.
// https://developers.google.com/youtube/iframe_api_reference#Requirements
window.onYouTubeIframeAPIReady = function() {
    // resolve deferred object. player creation will only happen once this is resolved.
    youtubeReady.resolve();
}

/**
 * Load YouTube API
 * @return promise for ajax response
 */
function loadYoutubeApi() {
    const options = {
        dataType: "script",
        cache: true,
        url: YOUTUBE_API_URL
    };
    return $.ajax(options);
}
