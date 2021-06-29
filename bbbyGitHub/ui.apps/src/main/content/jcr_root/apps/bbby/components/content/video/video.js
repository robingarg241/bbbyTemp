import $ from 'jquery'
import log from 'loglevel'
import VimeoVideo from './playerVimeo'
import YoutubeVideo from './playerYoutube'

const PLAYERS = {}

/**
 * Get the Player associated with the given DOM ID.
 */
export function getVideoPlayer(id) {
    return PLAYERS[id]
}

/**
 * Initializes one or more .cmp-video video component elements.
 *
 * @return array of players that were created
 */
export function initVideoPlayers($root) {
    const players = []

    $root.each(function() {
        let $videoElems = $(this)
        if (!$videoElems.hasClass('cmp-video')) {
            $videoElems = $videoElems.find('.cmp-video')
        }

        $videoElems.each(function() {
            const $this = $(this)
            const videoPlayer = createPlayer($this)
            if (videoPlayer) {
                videoPlayer.initPlayer()
                PLAYERS[videoPlayer.getId()] = videoPlayer
                players.push(videoPlayer)
            }
        })
    })

    return players
}

function createPlayer($videoElem) {
    const provider = $videoElem.attr('data-provider')

    // Verify .cmp-video element was passed
    if (!$videoElem.hasClass('cmp-video') || !provider) {
        log.warn('Bad element provided ' + $videoElem)
        return
    }

    // If ID is present video has already been initialized
    if ($videoElem.attr('id')) {
        log.debug('Element already initialized ' + $videoElem)
        return
    }

    let videoPlayer = null;
    if (provider == 'youtube') {
        videoPlayer = new YoutubeVideo($videoElem)
    } else if (provider == 'vimeo') {
        videoPlayer = new VimeoVideo($videoElem)
    }

    return videoPlayer
}

// Initialize all .cmp-video elements not handled by other components
initVideoPlayers(
    $('.cmp-video').filter(function() {
        // Exlude video components that are in components which need to do initialization themselves
        return !$(this).parents('.owl-carousel').length
    })
)
