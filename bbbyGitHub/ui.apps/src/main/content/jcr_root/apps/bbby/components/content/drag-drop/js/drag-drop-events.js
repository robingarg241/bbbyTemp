import 'tooltipster';
import "../polyfill.forEach";
import '../polyfill.object.assign';
import {validateDZForm, checkfile, invalidList, showValidateButton, hideSubmitButton, myDropzone, mdVerifiedList, validateFileSequence} from './internals';

function addDragFileHereIndicator() {
    $(".dropzone").addClass("dropzone-drag-here");

}

function removeDragFileHereIndicator() {
    $(".dropzone").removeClass("dropzone-drag-here");

}

function onDragEnter(event) {
    event.preventDefault();
    $("body").addClass("blur-background");
    $(".dropzone").addClass("dropzone-items");
    addDragFileHereIndicator();

};

function onDrop(event) {
    event.preventDefault();
    $("body").removeClass("blur-background");
    $(".dropzone").addClass("dropzone-items");
    removeDragFileHereIndicator();
};

function onMouseLeave(event) {
    event.preventDefault();
    $(this).removeClass("blur-background");
    removeDragFileHereIndicator();
}

export function addDragDropEventListerners(){

    $(".dropzone").on("drop", onDrop).on("dragenter", onDragEnter );

    $("body").on("dragenter", onDragEnter )
        .on("drop", onDrop)
        .on('mouseleave', onMouseLeave );

    window.addEventListener("dragover",function(e){
        e = e || event;
        e.preventDefault();
    },false);
    window.addEventListener("drop",function(e){
        e = e || event;
        e.preventDefault();
    },false);

    $('.browse-btn').on("click", function (e) {
        event.preventDefault();
        event.stopPropagation();
        showValidateButton();
        hideSubmitButton();
        validateDZForm();
        $("#excelFile").val('');
        $('#excelFile').click();
    });
     $('#excelFile').on("change", function (e) {
        const name = $('#excelFile').val().split(/\\|\//).pop();
        $(".file-info").val(name);
        checkfile(name);
        validateDZForm();

    });

    let tooltipOptions = {
        arrow: false,
        distance: 2,
        interactive: true,
        side: ['bottom', 'left'],
        viewportAware: false,
        functionPosition: function(instance, helper, position) {
            position.coord.left = helper.origin.getBoundingClientRect().left;
            if (helper.origin.hasAttribute('data-tooltip-right')) {
                position.coord.left += helper.origin.getBoundingClientRect().width - position.size.width;
            }

            return position;
        }
    };

    $('#upload-batch').on('click', '.toggle-error-popup', function(event) {
        event.preventDefault();
        event.stopPropagation();
        var modal = $(this).attr('href'),
            $modal = $(modal),
            list = $modal.find('.error-list'),
            length = Object.keys(invalidList).length;


        let rejectedFiles = myDropzone.getRejectedFiles();
        let invalidMetadataFiles = myDropzone.getAcceptedFiles().filter((item)=>{return item.metadataErrorMessage});

        let invalidFiles = [...rejectedFiles, ...invalidMetadataFiles];


        $modal.removeClass('hidden');
        if(invalidFiles.length > 1){
            $modal.find('.error-popup_title').text(invalidFiles.length+' Files Could Not Be used');
        } else {
            $modal.find('.error-popup_title').text(invalidFiles.length+' File Could Not Be used');
        }


        // Reset list
        $(list).children().remove();

        invalidFiles.forEach((item, index) => {
            let fileErrorMessage = item.fileErrorMessage ? item.fileErrorMessage : "" ;
            let metadataErrorMessage = item.metadataErrorMessage? item.metadataErrorMessage : "";
            let errorMessage =  fileErrorMessage + metadataErrorMessage;
            $(list).append(`<li class="error-list-item"><span style="width: 50% ;word-wrap:break-word;">${item.name}</span><span style="color: red;">${errorMessage}</span></li>`);
        });

        // Build list
        for (var file in invalidList) {
            var message = '';
            if (typeof invalidList[file] === 'string') {
                message = invalidList[file];
            }
            $(list).append(`<span class="list-items" >${file}</span><span class="list-items" >${message}</span >`);
        }
    });

    $('#drop-more-indicator').on('click', function(event) {
       event.preventDefault();

       $('.dz-hidden-input').click();
   });
    $('.error-popup').on('click', '.close-error-popup','.error-btn', function(event) {
        event.preventDefault();

        $('.error-popup').addClass('hidden');
    });

     $('.error-btn').on('click', function (event) {
        event.preventDefault();

        $('.error-popup').addClass('hidden');
     });

    $('#close-win').on('click', function (event) {
        event.preventDefault();
        event.stopPropagation();
    });

    $(".tags input").on({
        focusout : function() {
            // Split by comma or space
            var tags = this.value.split(/[ ,\n]+/);

            if (tags.length && this.value != ' ') {
                for (var tag = 0; tag < tags.length; tag++) {
                	//var str = tags[tag].replace(/[^a-z0-9\+\-\.\#]/ig, ''); // allowed characters
                    var str = tags[tag]; // allowed characters
                    // Make sure string is not just whitespace
                    if (/\S/.test(str)) {
                        $("<span/>", { text: str.toLowerCase().trim(), insertBefore: this });
                    }
                }
            }

            this.value = "";
        },
        keyup : function(ev) {
            // if: comma|enter (delimit more keyCodes with | pipe)
            if(/(188|13|32)/.test(ev.which)) $(this).focusout();
        }
    });

   $('.tags input').on('paste', function() {
       // Test for IE
       if (/*@cc_on!@*/false || !!document.documentMode) {
           var str = window.clipboardData.getData("Text");
           str = str.replace(/\s|\n/g, ","); // replace space or newline by commas
           this.value = str;
           return false;
       }
   });


    $('.tags').on('click', 'span', function() {
        if(confirm("Remove "+ $(this).text() +"?")) $(this).remove();
    });

    //****************** Start of Button Events ***************/
    $("#cancel-all").on("click", function (event) {
       // event.preventDefault();
        event.stopPropagation();

     //   document.getElementById('cancel-all').parentNode.style.display  = 'none';
     //   document.getElementById("submit-all").disabled = true;
        myDropzone.removeAllFiles(true);
    });

    $("#validate-sequence").on("click", function (event) {
        event.preventDefault();
        event.stopPropagation();
        validateFileSequence();
    });

    $("#submit-all").on("click", function (event) {
        event.preventDefault();
        event.stopPropagation();

        if (validateDZForm(event, true)) {
            document.getElementById('cancel-all').parentNode.style.display = 'block';
            $('#drop-more-indicator').removeClass("drop-more-display");
            $('input#uploadList').val(JSON.stringify(mdVerifiedList));
            myDropzone.processQueue();
        }
    });

    $("#submit-single").on("click", function (event) {
        event.preventDefault();
        event.stopPropagation();

        if (validateDZForm(event, true)) {
            document.getElementById('cancel-all').parentNode.style.display = 'block';
            $('#drop-more-indicator').removeClass("drop-more-display");

            myDropzone.processQueue();
        }
    });
    //******************End of Button Events ***************/

    document.getElementById('dzDialog').addEventListener('click', function(event) {
        event.preventDefault();
        myDropzone.hiddenFileInput.click();
    });
}
