use(function () {

    var path = request.getRequestParameter('assetPath').getString();
    var children = null;

    try {
        children = resolver.getResource(path).listChildren();
    } catch (err) {
        log.error(err);
    }
    //
    // for( var res in Iterator(children)) {
    //     images.push(res);
    // }

    return {
        images: children
    };
});
