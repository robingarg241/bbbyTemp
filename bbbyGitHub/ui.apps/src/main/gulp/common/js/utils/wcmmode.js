import $ from 'jquery'

export function isEdit() {
    return $('.site-wrapper').hasClass('edit');
}
export function isPreview() {
    return $('.site-wrapper').hasClass('preview');
}
export function isDesign() {
    return $('.site-wrapper').hasClass('design');
}
export function isPublish() {
    return $('.site-wrapper').hasClass('publish');
}
