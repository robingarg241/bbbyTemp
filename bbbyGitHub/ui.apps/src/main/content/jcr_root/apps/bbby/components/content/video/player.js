export default class Player {

    constructor($elem) {
        this.$elem = $elem
        this.$overlay = this.$elem.find('.cmp-video__overlay')

        // Attach click handles
        this.$overlay.on('click', () => this.play())

        // Saved to dom so that other component can get a reference to this player via video.getVideoPlayer(id)
        this.id = Player.guid();
        this.$elem.attr('id', this.id)
    }

    // ==========================================================
    // START NON-IMPLEMENTED METHODS
    //
    // Functions that video provider classes must implement
    // ==========================================================

    /**
     * Initializes the video player.
     *
     * Implementations should use the `this.$elem` object to update the dom.
     */
    initPlayer() {
        throw "Must be implemented by video provider class"
    }

    /**
     * Starts playback of the video.
     */
    _startPlayback() {
        throw "Must be implemented by video provider class"
    }

    /**
     * Stops playback of the video.
     */
    _pausePlayback() {
        throw "Must be implemented by video provider class"
    }

    // ====================================================================
    // START DEFAULT IMPLEMENTED METHODS
    //
    // Functions that video provider classes should not need to override
    // ====================================================================

    play() {
        this.$elem.parents('.owl-carousel').trigger('stop.owl.autoplay') // If in a carousel, stop autoplay
        this.hideOverlay()
        this._startPlayback()
    }

    pause() {
        this._pausePlayback()
    }

    hideOverlay() {
        this.$overlay.addClass('is-hidden')
    }

    getVideoId() {
        return this.$elem.attr('data-video-id')
    }

    getId() {
        return this.id
    }

    getAutoplay() {
        return this.$elem.attr('data-autoplay')
    }

    static guid() {
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
    }
}

function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
        .toString(16)
        .substring(1);
}
