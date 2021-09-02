import '../polyfill.forEach';
import '../UTIF';
import {batchUUID, myDropzone, invalidList, uploadList, uploadType} from './internals';

var movFileCounter = 0;

export function _DZArrayToString(arr) {
    if (!Array.isArray(arr)) {
        return false;
    }

    var s = '[';

    arr.forEach((item, index) => {
        s += JSON.stringify(item);

    if (index < arr.length - 1) {
        s +=',';
    }
});

    s += ']';

    return s;
}

export function showValidateButton(){
    if(uploadType === 'multi'){
        $('#validate-all').show();
        $('#validate-all').addClass('submit-ready');
    } else if (uploadType === 'fasttrack') {
        $('#validate-QC').show();
        $('#upload-batch').show();
        $('#validate-sequence').hide();
        $('#show-sequence').hide();
        $('#validate-QC').addClass('submit-ready');
    }
}

export function hideSubmitButton(){
    if(uploadType === 'multi'){
        $('#submit-all').hide();
        $('#submit-all').removeClass('submit-ready');
    } else if (uploadType === 'fasttrack') {
        // Todo : check which button to hide at what condition
        $('#validate-sequence').hide();
        $('#show-sequence').hide();
        $('#validate-QC').show();
    }
}

export function handleDropZoneIndicator(files) {
//    showValidateButton();
    if(files && files.length>0) {
        $('#drop-more-indicator').addClass("drop-more-display");
        $('#drop-more-indicator').appendTo(".dropzone");
    }
    else {
        $('#drop-more-indicator').removeClass("drop-more-display");
    }
}

export let generateUuid = function () { // Public Domain/MIT
    var d = new Date().getTime();
    if (typeof performance !== 'undefined' && typeof performance.now === 'function'){
        d += performance.now(); //use high-precision timer if available
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
};

export function movFileAdded() {
    if(uploadType === 'single') {
        movFileCounter ++;
        $('.mov-files').show();
    }

}

export function movFileRemoved(){
    if(uploadType === 'single' ) {
        if (movFileCounter > 0) {
            movFileCounter --;
            if(movFileCounter === 0) {
                $('.mov-files').hide();
            }
        }
    }
}

export function timeoutBanner(elm) {
    // Hide banner after 5 seconds
    setTimeout(function() {
        $(elm).remove();
    }, 5000);
}

export function cleanupInvalid() {
    if ((uploadList.length > 0 || Object.keys(invalidList).length > 0 && myDropzone.getRejectedFiles().length > 0) && $('#uploadError').length > 0) {
        $('#uploadError').remove();
    }
}

export function areUploadsDone() {
    return myDropzone.getQueuedFiles().length == 0;
}

export function getTotalUploadProgress (totalUploadProgress) {
    var numInQueue = $(".dropzone").find('.status-in-queue').length;
    var totalFile = uploadList.length;
    var totalProgress = (totalFile - numInQueue)/totalFile*100 + totalUploadProgress/totalFile;
    var finalProgress = Math.floor(totalProgress);

    if(finalProgress>100) {
        finalProgress = 100;
    }
    return finalProgress;

}

export function isFileInList(file) {
    for(let i = 0; i < uploadList.length; i++) {
        let f = uploadList[i];
        if(f.uuid === file.upload.uuid) {
            return true;
        }
    }
    return false;
}

function removeAllUploadStatus(file) {
    var fileRow = file.previewElement,
        fileDimensions = $(fileRow).find('.dz-status');

    var inQueue = $(fileDimensions).find('.status-in-queue'),
        error = $(fileDimensions).find('.status-error'),
        success = $(fileDimensions).find('.status-success'),
        uploading = $(fileDimensions).find('.status-uploading');

    if (inQueue.length >0) {
        inQueue.remove();
    }
    if (error.length >0) {
        error.remove();
    }
    if (success.length >0) {
        success.remove();
    }
    if(uploading.length >0) {
        uploading.remove();
    }
}

export function addQueuedStatus(file) {
    removeAllUploadStatus(file);

    var status = "In Queue";

    var fileRow = file.previewElement,
        fileDimensions = $(fileRow).find('.dz-status');

    if ($(fileDimensions).find('.status-in-queue').length === 0) {
        $(fileDimensions).prepend(`<div class="status-in-queue">` + status + `</div>`);
    }
}

export function addSuccessStatus(file) {
    removeAllUploadStatus(file);

    var status = "Success";

    var fileRow = file.previewElement,
        fileDimensions = $(fileRow).find('.dz-status');

    if ($(fileDimensions).find('.status-success').length === 0) {
        $(fileDimensions).prepend(`<div class="status-success">` + status + `</div>`);
    }
}

export function addUploadingStatus(file) {
    removeAllUploadStatus(file);

    var status = "Uploading...";

    var fileRow = file.previewElement,
        fileDimensions = $(fileRow).find('.dz-status');

    if ($(fileDimensions).find('.status-uploading').length === 0) {
        $(fileDimensions).prepend(`<div class="status-uploading">` + status + `</div>`);
    }


    var toggleUploadingStatus = setInterval( function() {
        var uploadingDiv = $(fileDimensions).find('.status-uploading');
        if( uploadingDiv.length ) {
            if(uploadingDiv.hasClass("status-uploading-blink")) {
                uploadingDiv.removeClass("status-uploading-blink");
            } else {
                uploadingDiv.addClass("status-uploading-blink");
            }
        } else {
            clearInterval( toggleUploadingStatus );
        }}, 1000 );

}

export function handleInvalid(file, error, _status, retry) {
    removeAllUploadStatus(file);

    var status = 'Cannot be used';
    if(_status) {
        status = _status;
    }

    var fileRow = file.previewElement,
        fileDimensions = $(fileRow).find('.dz-status');

    if ($(fileDimensions).find('.status-error').length === 0) {
        $(fileDimensions).prepend(`<div class="status-error">` + status + `<span class="tooltip-upload"><div class="status-tooltip">${error}</div></span></div>`);

        if ($(fileDimensions).find('.analyze').text().indexOf('Analyzing') > -1) {
            $(fileDimensions).find('.analyze').text('N/A');
        }
    }

    // Prevent duplicates in invalid file list
    if (invalidList[file.name] || retry) {
        //myDropzone.removeFile(file);
        return false;
    }

    invalidList[file.name] = error;
    //myDropzone.removeFile(file);

    // Display error message for single invalid file upload
    if ($('.upload-result.error-bar').length === 0) {
        $('#upload').prepend('<div class="upload-result error-bar" id="uploadError"><h2>Upload Failed</h2>File(s) did not meet requirements.</div>');
        timeoutBanner('#uploadError');
    }

    let filePlural = 'file';
    let length = Object.keys(invalidList).length;

    if (length) {
        if (length > 1) {
            filePlural = 'files';
        }
        document.querySelector('.dropzone-invalid').innerHTML = `<a class="toggle-error-popup" href="#errorPopup"><span>${length}</span> ${filePlural} cannot be used</a>`;
    }

    // Remove error banner
    cleanupInvalid();
}

export function updateUploadList(action, file) {
    //console.log(JSON.stringify(file));
    if (action === 'push') {
        uploadList.push(file);
    } else if (action === 'splice') {
        uploadList.splice(file, 1);
    }
    if(uploadType === 'single') {
        document.getElementById('singleuploadList').value = _DZArrayToString(uploadList);
    } else {
        document.getElementById('uploadList').value = _DZArrayToString(uploadList);
    }

}

export function validateFileDimensions(file, width, height) {

    let isValid = false;
    const fullUse = {
        width: 5000,
        height: 5000
    };
    const minUse = {
        width: 1000,
        height: 1000
    };

    width = width ? width : file.width;
    height = height ? height : file.height;

    if(width >= fullUse.width && height >= fullUse.height) {
        file.previewElement.classList.add('dimensions-full-use');
    }
    if((width < minUse.width || height < minUse.height) || (width > fullUse.width || height > fullUse.height)) {
        var prevElem = file.previewElement;
        prevElem.classList.add('dimensions-no-use');
        prevElem.querySelector('.dz-dimensions .tooltip_container div').removeAttribute("hidden");
        prevElem.querySelector('.dz-dimensions .tooltip_container').classList.add('alert');
        prevElem.querySelector('.dz-dimensions .tooltip_container div').classList.add('alert');
        prevElem.querySelector('.dz-dimensions .tooltip_container span').innerText = 'Image must be a minimum of 1000x1000 and a maximum of 5000x5000.';
    }
    if(!file.previewElement.classList.contains('dimensions-full-use') && !file.previewElement.classList.contains('dimensions-no-use')) {
        file.previewElement.classList.add('dimensions-limited-use');
    }

    let dimensions = ``;
    if ((width < minUse.width || height < minUse.height) || (width > fullUse.width || height > fullUse.height)) {
        // handleInvalid(file, 'Image must be a minimum of 1000x1000 and a maximum of 5000x5000.');
        file.rejectFile("Image must be a minimum of 1000x1000 and a maximum of 5000x5000.");
    } else {
        try {
            file.acceptDimensions();
        } catch (err) {
            //console.log("error acceptDimensions : " + err);
        }
        if(file.type==='image/svg+xml') {
            width = 0;
            height = 0;
        }

        updateUploadList('push', {
            "uuid": file.upload.uuid,
            "fileName": file.name,
            "width": width,
            "height": height,
            "colorSpace": file.previewElement.querySelector('span[data-dz-color]').innerText
        });

        isValid = true;
    }
    if(width == undefined || height == undefined) {
        dimensions += `N/A`;
    } else {
        dimensions += `${width} x ${height}`;
    }
    let dimSpan = file.previewElement.querySelector('span[data-dz-dimensions]');
    dimSpan.innerHTML = dimensions;

    return isValid;
}

export function getAspectRatio(fileWidth, fileHeight){
    if(fileWidth && fileHeight){
        let gcd = function(x,y){
            if(y > x){
                let temp = x;
                x = y;
                y = temp;
            }
            while(y!=0){
                let m = x%y;
                x = y;
                y = m;
            }
            return x;
        }

        let ratio = gcd(fileWidth, fileHeight);

        return fileWidth/ratio +":"+ fileHeight/ratio;
    }
}

export function resultForm() {

    window.scrollTo(0,0);

    let uploadForm = document.getElementById('upload');
    uploadForm.querySelectorAll('input,textarea').forEach((element) => {
        element.classList.add('readonly');
        element.readOnly = true;
    });

    $('#validate-all').hide();
    $('#submit-all').hide();
    $('#submit-fasttrack').hide();
    $('#submit-single').hide();
    $('#close-win').hide();
    $('#cancel-all').hide();
    if (document.getElementById('dzDialog')) {
        document.getElementById('dzDialog').style.display = 'none';
    }
    let removeLinks = document.querySelectorAll('.dz-remove-custom');
    removeLinks.forEach((element) => {
        element.style.display = 'none';
    });

    if ($('textarea#note').val() === '') {
        $('textarea#note').parent().hide();
    }

    //$('.dropzone-invalid').addClass('hidden');
    $('.datepicker-input').removeClass('datepicker-input');
    $('#new-batch').removeClass('hidden');
}

export function removeDuplicate(files, currentFile){
    if (files.length) {
        var _i, _len;
        for (
          _i = 0, _len = files.length;
          _i < _len;
          _i++
        ) {
          if (files[_i].name === currentFile.name) {
            myDropzone.removeFile(currentFile);
            handleDropZoneIndicator(files);
            return true;
          }
        }
    }
}

export function getTagsList(){
    var list;
    for(var i = 0; i < $('.tags span').length; i++){
        list = !list ? $('.tags span')[i].innerText : list+","+$('.tags span')[i].innerText;
    }
    return list;
}

export function isDupeFile(files,file,method) {
    let isDupe = false;
    // Don't recount for duplicate file names
    if (files.length) {
        Object.keys(files).forEach(function (key) {
            var fileName = files[key].name;

            if (file.name === fileName) {
                isDupe = true;
            }
        });
    }
    return isDupe;
};

export function setInvalidFile(file){

    file.previewElement.querySelector('.dz-type .tooltip_container div').removeAttribute("hidden");
    file.previewElement.querySelector('.dz-type .tooltip_container').classList.add('alert');
    file.previewElement.querySelector('.dz-type .tooltip_container div').classList.add('alert');
    file.previewElement.querySelector('.dz-type .tooltip_container span').innerText = 'System accepts the following formats; JPEG, TIFF, PNG, PDF, MP4, WMV, MPEG-4, MOV, SRT. Please review File Requirements.';
    file.rejectFile("File type is not accepted.");
}

export function setInvalidFileNameLength(file){
    file.previewElement.querySelector('.dz-details .tooltip_container div').removeAttribute("hidden");
    file.previewElement.querySelector('.dz-details .tooltip_container').classList.add('alert');
    file.previewElement.querySelector('.dz-details .tooltip_container div').classList.add('alert');
    file.previewElement.querySelector('.dz-details .tooltip_container span').innerText = 'File Name must be less than 50 characters.';
    file.rejectFile("File Name must be less than 50 characters.");
}

export function addErrorMessage(column, errorMessage){
    column.querySelector('.tooltip_container div').removeAttribute("hidden");
    column.querySelector('.tooltip_container').classList.add('alert');
    column.querySelector('.tooltip_container div').classList.add('alert');
    column.querySelector('.tooltip_container span').innerText = errorMessage;
}

export function createThumbnail(file, fileThumbnail){
    var reader = new FileReader();
      reader.readAsDataURL( file );
      reader.onload = function ( ev ) {
        fileThumbnail.src = ev.target.result;
        fileThumbnail.width = 45;
      };
  }

export function getImageColorSpace(file,dataUrl) {
    return new Promise(function(resolve, reject){
        $.ajax({
                type: 'POST',
                url: "/bin/bedbath/imageHelper",
                data: {
                    'base64' : dataUrl
                },
                success: function(data, status, response) {

                    var colorSpace = response.responseText;
                    var validColor = new Array("RGB","CMYK","GRAY");

                    if(!colorSpace || validColor.indexOf(colorSpace) >= 0 ) {
                        var filecColorSpan = file.previewElement.querySelector('span[data-dz-color]');
                        filecColorSpan.innerText = response.responseText;
                    } else {
                        file.previewElement.querySelector('.dz-color .tooltip_container div').removeAttribute("hidden");
                        file.previewElement.querySelector('.dz-color .tooltip_container').classList.add('alert');
                        file.previewElement.querySelector('.dz-color .tooltip_container div').classList.add('alert');
                        file.previewElement.querySelector('.dz-color .tooltip_container span').innerText = 'System accepts the following colorspace; RGB, SRGB, Adobe RGB, CMYK, null value, blank value, black and white, grayscale, uncalibrated.';
                    }
                    resolve('done');
                },
                error: function(request, status, error) {
                    console.log(error);
                }
            });
    });
};

export function checkIfFileExists(file) {

    $.ajax({
        type: 'POST',
        url: "/bin/nordstrom/servlets/checkAssetStatus",
        data: {
            'uuid' : batchUUID,
            'filename' : file.upload.filename
        },
        success: function(data, status, response) {
            console.log("res: " + response.responseText);
            // return response.responseText;
            if(response.responseText=="true") {
                myDropzone.emit("success", file);
                myDropzone.processQueue();
            } else {
                myDropzone.uploadFile(file);
            }
        },
        error: function(request, status, error) {
            // return false;
            myDropzone.processQueue();
        }
    });
};

export function setFileTypeThumbnail(file){

    let fileType = file.type;

    let fileTypeThumb = file.previewElement.querySelector(
        "img[data-dz-thumbnail]"
    );
    let fileTypeSpan = file.previewElement.querySelector(
        "span[data-dz-file-type]"
    );
    let fileName = file.name.split(".");

    if (
        fileType.toLowerCase() === "image/jpg" ||
        fileType.toLowerCase() === "image/jpeg"
      ) {
        if (fileName[fileName.length - 1].toLowerCase() === "jfif") {
        fileTypeSpan.innerText = "JFIF";
        createThumbnail(file, fileTypeThumb);
        setInvalidFile(file);
      } else {
	    fileTypeSpan.innerText = "JPG";
        createThumbnail(file, fileTypeThumb);
	  }
      } else if (fileType.toLowerCase() === "image/png") {
        fileTypeSpan.innerText = "PNG";
        createThumbnail(file, fileTypeThumb);
      } else if (fileType.toLowerCase() === "video/x-ms-wmv") {
        fileTypeSpan.innerText = "WMV";
        fileTypeThumb.src = "/etc/designs/bbby/icons/video-icon.png";
        fileTypeThumb.width = "45";
        movFileAdded();
      } else if (fileType.toLowerCase() === "image/tiff") {
        fileTypeSpan.innerText = "TIFF";
        fileTypeThumb.src = "/etc/designs/bbby/icons/tiff-icon.png";
        fileTypeThumb.width = "45";
      } else if (fileType.toLowerCase() === "image/bmp") {
        fileTypeSpan.innerText = "BMP";
        createThumbnail(file, fileTypeThumb);
        setInvalidFile(file);
      } else if (fileType.toLowerCase() === "image/svg+xml") {
        fileTypeSpan.innerText = "SVG";
        setInvalidFile(file);
      } else if (fileName[fileName.length - 1].toLowerCase() === "ai") {
        fileTypeSpan.innerText = "AI";
        setInvalidFile(file);
      } else if (fileName[fileName.length - 1].toLowerCase() === "eps") {
        fileTypeSpan.innerText = "EPS";
        setInvalidFile(file);
      } else if (fileName[fileName.length - 1].toLowerCase() === "psd") {
        fileTypeSpan.innerText = "PSD";
        fileTypeThumb.src = "/etc/designs/bbby/icons/psd-icon.png";
        fileTypeThumb.width = "45";
        setInvalidFile(file);
      } else if (fileName[fileName.length - 1].toLowerCase() === "pdf") {
        fileTypeThumb.src = "/etc/designs/bbby/icons/pdf-icon.png";
        fileTypeThumb.width = "45";
        fileTypeSpan.innerText = "PDF";
      } else if (fileName[fileName.length - 1].toLowerCase() === "mov") {
        fileTypeThumb.src = "/etc/designs/bbby/icons/video-icon.png";
        fileTypeThumb.width = "45";
        fileTypeSpan.innerText = "MOV";
        movFileAdded();
      } else if (fileName[fileName.length - 1].toLowerCase() === "srt") {
        fileTypeSpan.innerText = "SRT";
        fileTypeThumb.src = "/etc/designs/bbby/icons/srt-icon.png";
        fileTypeThumb.width = "45";
      } else if (
        fileName[fileName.length - 1].toLowerCase() === "mp4" ||
        fileType.toLowerCase() === "video/mp4"
      ) {
        fileTypeThumb.src = "/etc/designs/bbby/icons/video-icon.png";
        fileTypeThumb.width = "45";
        fileTypeSpan.innerText = "MP4";
        movFileAdded();
      } else if (fileType.toLowerCase() === "text/html") {
        fileTypeSpan.innerText = "HTML";
        fileTypeThumb.src = "/etc/designs/bbby/icons/html-icon.png";
        fileTypeThumb.width = "45";
        setInvalidFile(file);
      } else if (fileType.toLowerCase() === "image/gif") {
        fileTypeSpan.innerText = "GIF";
        createThumbnail(file, fileTypeThumb);
        setInvalidFile(file);
      } else if (fileTypeSpan.classList.length < 1) {
        fileTypeSpan.innerText = "UNKNOWN";
        setInvalidFile(file);
      }
    //DAM-489 : To check the file name should not more than 50 characters
    if(file.name.length > 50){
    	setInvalidFileNameLength(file);
    }
}

export function updateFileCounts(){

    updateActiveItemsList();
    updateInvalidList();
  //DAM-374 : Add validation on the Vendor portal to limit asset drop #
    checkAssetItemsListCount();
}

function updateActiveItemsList(){
    const addedFilesCount = myDropzone.getAcceptedFiles().length + myDropzone.getRejectedFiles().length;
    let fileStr =  addedFilesCount > 1 ? "files" : "file" ;

    let message = `${addedFilesCount} ${fileStr}`;

    $(".file-count").html(message);

}

//DAM-374 : Add validation on the Vendor portal to limit asset drop #
function checkAssetItemsListCount(){
    const addedFilesCount = myDropzone.getAcceptedFiles().length + myDropzone.getRejectedFiles().length;
    let validateCount = document.getElementById('vendorAssetDropLimit').value;
    if ( addedFilesCount > validateCount ) {
    	let message = "";
        if(uploadType === 'multi'){
        	message = `Maximum number of assets to be uploaded in a batch is ${validateCount}. If you have more than ${validateCount} assets, please upload in multiple batches.<br>`;
        }else{
        	message = `Single Upload feature is for one image only. For multiple image uploads, please utilize the spreadsheet and submit through "Multi Image Upload".<br>`;
        }
        $(".dropzone-invalid-count").html(message);
    }else {
        $(".dropzone-invalid-count").html("");
    }
}

function updateInvalidList(){
    let rejectedFiles = myDropzone.getRejectedFiles().filter((item)=>{return item.fileErrorMessage || item.metadataErrorMessage});
    let invalidMetadataFiles = myDropzone.getAcceptedFiles().filter((item)=>{return item.metadataErrorMessage});
    let fileStr;

    let invalidFilesList = [...rejectedFiles, ...invalidMetadataFiles];

    if (invalidFilesList.length > 0) {
      fileStr = invalidFilesList.length > 1 ? "files" : "file";
      document.querySelector(
        ".dropzone-invalid"
      ).innerHTML = `<a class="toggle-error-popup" href="#errorPopup"><span>${
        invalidFilesList.length
      }</span> ${fileStr} cannot be used</a>`;
    }else if(invalidFilesList.length === 0){
        $('.dropzone-invalid').html("");
    }
}


