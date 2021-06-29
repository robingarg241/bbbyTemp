import Dropzone from 'dropzone';
import {init, validations, addDragDropEventListerners, updateFileCounts} from './js/internals';

if (document.getElementById('vpUpload')) { 

    Dropzone.options.uploadBatch = {
        url: "/bin/bedbath/vendor-portal/upload",
        autoProcessQueue: false,
        autoQueue: true,
        createImageThumbnails: false,
        uploadMultiple: false,
        parallelUploads: 1,
        timeout: 1800000,
        maxFiles: 100,
        maxFilesize: 1999,
        maxThumbnailFilesize: 100,
        thumbnailWidth: 50,
        thumbnailHeight: 50,
        addRemoveLinks: false,
        // acceptedFiles: ".jpg, .jpeg, .png,.psd, .tif,.tiff, .pdf,.mov,.mp4, .m4a, .m4p, .m4b, .m4r , .m4v, .wmv,.srt",
        // dictInvalidFileType: "File type is not accepted",
        dictRemoveFileConfirmation: "Are you sure you want to Delete?",
        dictDefaultMessage: "<div class='icon-plus'></div><h4 style='text-transform: initial;font-size: larger;' >Drag and drop files here</h4> <div class='orselect'>Browse for Files</div>",
        dictRemoveFile: "Delete",
        dictUploadCanceled: '',

        // chunking: true,
        // chunkSize: chunkingSize,
        previewTemplate: document.getElementById('dzTemplate').innerHTML,

        // The setting up of the dropzone
        init: init,
        accept: function(file, done) {
            file.rejectFile = function(fileErrorMessage){
                
                if(file.fileErrorMessage)
                    file.fileErrorMessage += fileErrorMessage;
                else
                    file.fileErrorMessage = fileErrorMessage;

                done(fileErrorMessage);
                updateFileCounts();
            }

            file.acceptFile = ()=>{                
                done();
                updateFileCounts();
            };            
        },
        _processThumbnailQueue: function() {
            console.log("_processThumbnailQueue");
            var _this10 = this;

            if (this._processingThumbnail || this._thumbnailQueue.length === 0) {
                return;
            }

            this._processingThumbnail = true;
            var file = this._thumbnailQueue.shift();
            return this.createThumbnail(file, this.options.thumbnailWidth, this.options.thumbnailHeight, this.options.thumbnailMethod, true, function (dataUrl) {
                _this10.emit("thumbnail", file, dataUrl);
                _this10._processingThumbnail = false;
                return _this10._processThumbnailQueue();
            });
        }
    };

    $(function() {
        addDragDropEventListerners();
        validations();
    });
}
