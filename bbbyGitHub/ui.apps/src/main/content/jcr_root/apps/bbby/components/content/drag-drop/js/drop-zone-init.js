import {onSending, onSendingMultiple, onAddedFile, onRemovedFile, onThumbnail, onError, onTotalUploadProgress, onSuccess, onComplete, onSuccessMultiple, onErrorMultiple, generateUuid} from "./internals"

export let batchUUID;
export let myDropzone;
export let uploadList = [];
export let invalidList = {};
export let uploadType = "single";

export function init() {
    
    batchUUID = generateUuid();
    myDropzone = this;
    uploadType = $('#uploadTypeForm').val();

    this.autoDiscover = false;
    //document.getElementById("submit-all").disabled = true;
    // First change the button to actually tell Dropzone to process the queue.

    

    this.on("sending", onSending);

    // Listen to the sendingmultiple event. In this case, it's the sendingmultiple event instead
    // of the sending event because uploadMultiple is set to true.
    this.on("sendingmultiple", onSendingMultiple);

    this.on("removedfile", onRemovedFile);

    this.on("addedfile", onAddedFile);

    this.on("thumbnail", onThumbnail);

    this.on("error", onError);

    this.on("totaluploadprogress", onTotalUploadProgress);

    this.on("success", onSuccess);

    this.on("complete", onComplete);
    this.on("successmultiple", onSuccessMultiple);

    this.on("errormultiple", onErrorMultiple);

    this.on("completemultiple", onErrorMultiple);
}