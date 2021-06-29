import log from 'loglevel'
import Player from './player'

export default class VimeoPlayer extends Player {

    constructor($elem) {
        super($elem)

        this.vimeoElemId = Player.guid()
        $elem.find('.cmp-video__player').attr('id', this.vimeoElemId)
    }

    initPlayer() {
        const options = {
            id: this.getVideoId(),
            loop: false,
            autoplay: this.getAutoplay()
        };

        this.player = new Vimeo.Player(this.vimeoElemId, options);
        this._attachEvents();

        log.debug('Created player for: ', this.$elem)
    }

    _startPlayback() {
        return this.player.play()
    }

    _pausePlayback() {
        return this.player.pause()
    }

    _attachEvents() {
        //
        // Below are placeholder for if we need to add analytics hooks.
        //

        this.player.on('play', function(data) {
            this.$elem.parents('.owl-carousel').trigger('stop.owl.autoplay') // If in a carousel, stop autoplay
            log.debug('Playing video: ', data);
        });

        this.player.on('pause', function(data) {
            log.debug('Pause video: ', data);
        });

        this.player.on('ended', function(data) {
            log.debug('Ended video: ', data);
        });

        this.player.on('loaded', function(data) {
            log.debug('Loaded video: ', data);
        });
    }

}
