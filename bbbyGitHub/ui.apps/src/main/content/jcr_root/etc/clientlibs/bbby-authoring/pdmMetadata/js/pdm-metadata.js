
$(document).ready(function(){
    if('.owl-carousel'){
        $('.owl-carousel').owlCarousel({
            items:1,
            margin:10,
            nav:false,
            mouseDrag:false
        });

        if($('.owl-carousel').data('item-count')){
            $('.pdm-metadata-pagination').pagination({
                items: $('.owl-carousel').data('item-count'),
                itemsOnPage: 1,
                ellipsePageSet: false,
                cssStyle: 'light-theme',
                onPageClick: pdmPageClick
            });
        }
    }

});

function pdmPageClick(pageNumber,event){
    $('.owl-carousel').trigger('to.owl.carousel', pageNumber-1);
}
