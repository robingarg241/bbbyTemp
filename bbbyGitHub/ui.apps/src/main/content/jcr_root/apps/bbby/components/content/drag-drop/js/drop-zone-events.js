// import "../polyfill.forEach";
import "../UTIF";
import ExifReader from "exifreader/dist/exif-reader";
import "../UPNG";
import {
  listCount,
  succesItems,
  addUploadingStatus,
  batchUUID,
  validateDZForm,
  validateFileDimensions,
  setInvalidFile,
  movFileAdded,
  movFileRemoved,
  handleInvalid,
  handleDropZoneIndicator,
  addQueuedStatus,
  getImageColorSpace,
  isFileInList,
  checkIfFileExists,
  getTotalUploadProgress,
  addSuccessStatus,
  areUploadsDone,
  resultForm,
  invalidList,
  myDropzone,
  updateUploadList,
  getTagsList,
  isDupeFile,
  uploadList,
  uploadType,
  addErrorMessage,
  createThumbnail,
  removeDuplicate,
  updateFileCounts,
  setFileTypeThumbnail,
  getAspectRatio,
  showValidateButton,
  hideSubmitButton
} from "./internals";

let addedFilesCount = 0;
const CONST_ANALYZING = "Analyzing...";
let fileSequence = 0;

export function onSending(file, xhr, formData) {
	  addUploadingStatus(file);
	  console.log("sending " + file.name);

	  let totalFilesCount = myDropzone.getAcceptedFiles().length;

	  let validFilesCount = myDropzone.getAcceptedFiles().filter(item => {
	    return !item.metadataErrorMessage;
	  }).length;

	  if (uploadType === "single") {
	    var batchId = $("input#singleBatchId").val(),
	      requestedBy = $("input#singleRequestedBy").val(),
	      email = $("input#singleEmail").val(),
	      assetUpdate = $('input[name="asset-update"]:checked').val(),
	      reasonUpdate = $("#reasonUpdate").val(),
	      upc = getTagsList(),
	      assetType = $("#assetType").val(),
	      shotType = $("#shotType").val(),
	      sequence = $("#sequence").val(),
	      additionalUpc = $("#additionalUpc").val(),
	      srtProvided = $('input[name="srt-provided"]:checked').val(),
	      bbbysrt = $('input[name="bbby-srt"]:checked').val();

	    formData.append("batchId", batchId);
	    formData.append("email", email);
	    formData.append("requestedBy", requestedBy);
	    formData.append("uploadList", JSON.stringify([file.uploadAssetItem]));
	    formData.append("fileCount", validFilesCount);
	    formData.append("assetUpdate", assetUpdate);
	    formData.append("reasonUpdate", reasonUpdate);
	    formData.append("upc", upc);
	    formData.append("assetType", assetType);
	    formData.append("shotType", shotType);
	    formData.append("sequence", sequence);

	    if (additionalUpc) {
	      formData.append("additionalUpc", additionalUpc);
	    }

	    if (srtProvided) {
	      formData.append("srtProvided", srtProvided);
	    }
	    if (bbbysrt) {
	      formData.append("bbbysrt", bbbysrt);
	    }

	    formData.append("batchUuid", batchUUID);
	    formData.append("fileOrder", fileSequence++);
	    formData.append("batchUpload", false);
	    formData.append("isUploadCompleted", true);

	    window.scrollTo(0, 0);

	    // Show progress bar on submit
	    if ($("#uploadProgress").hasClass("hidden")) {
	      $("#uploadProgress").removeClass("hidden");
	    }
	  } else {
	    var batchId = $("input#batchId").val(),
	      excelFile = $("input#excelFile")[0].files[0],
	      requestedBy = $("input#requestedBy").val(),
	      email = $("input#email").val(),
	      uploadItemsList = [],
	      uploadRejectedItemsList = [];

	    // This is a dirty fix to not upload the metadata error files. Browser
		// still sending request but server won't upload because of empty upload
		// info.
	    // TODO: Need a good fix.
	    if (!file.metadataErrorMessage){
	    	uploadItemsList.push(file.uploadAssetItem);
	    }else{
	    	uploadRejectedItemsList.push(file.uploadAssetItem);
	    }

	    formData.append("batchId", batchId);
	    formData.append("email", email);
	    formData.append("requestedBy", requestedBy);
	    formData.append("excelFile", excelFile, excelFile.name);
	    formData.append("uploadList", JSON.stringify(uploadItemsList));
	    formData.append("fileCount", validFilesCount);
	    formData.append("batchUuid", batchUUID);
	    formData.append("fileOrder", fileSequence++);
	    formData.append("uploadRejectedList", JSON.stringify(uploadRejectedItemsList));

	    console.log("fileSequence : "+fileSequence);
	    // DAM-309 : Send submission confirmation email to vendors from vendor
		// portal
	    if(fileSequence == totalFilesCount){
			formData.append("isUploadCompleted", true);
	    }
	    formData.append("totalFilesCount", totalFilesCount + myDropzone.getRejectedFiles().length);
	    console.log("totalFilesCount : "+totalFilesCount);

		let invalidMetadataFiles = myDropzone.getAcceptedFiles().filter(item => {
		   return item.metadataErrorMessage;
		});

		var inValidFilesArray = [];
		let invalidFiles = [...myDropzone.getRejectedFiles(), ...invalidMetadataFiles];
		invalidFiles.forEach((item, index) => {
		  inValidFilesArray.push(item.name);
		});

		formData.append("invalidFiles", inValidFilesArray);

		var acceptedFilesArray = [];
		let acceptedFiles = myDropzone.getAcceptedFiles().filter(item => {
		    return !item.metadataErrorMessage;
		});
		acceptedFiles.forEach((item, index) => {
			acceptedFilesArray.push(item.name);
		});

		formData.append("acceptedFiles", acceptedFilesArray);

	    formData.append("batchUpload", true);

	    $("#successBatchId").text(batchId);
	    $("#successEmail").text(email);
	    $("#successRequestedBy").text(requestedBy);
	    $("#successMetadataFile").text(successMetadataFile);

	    window.scrollTo(0, 0);

	    // Show progress bar on submit
	    if ($("#uploadProgress").hasClass("hidden")) {
	      $("#uploadProgress").removeClass("hidden");
	    }
	  }
}

export function onSendingMultiple(data, xhr, formData) {
  // Gets triggered when the form is actually being sent.
  // Hide the success button or the complete form.
  if (uploadType === "single") {
    var batchId = $("input#singleBatchId").val(),
      requestedBy = $("input#singleRequestedBy").val(),
      email = $("input#singleEmail").val(),
      uploadList = $("input#singleuploadList").val(),
      assetUpdate = $('input[name="asset-update"]:checked').val(),
      reasonUpdate = $("#reasonUpdate").val(),
      upc = getTagsList(),
      assetType = $("#assetType").val(),
      shotType = $("#shotType").val(),
      sequence = $("#sequence").val(),
      additionalUpc = $("#additionalUpc").val(),
      srtProvided = $('input[name="srt-provided"]:checked').val(),
      bbbysrt = $('input[name="bbby-srt"]:checked').val();

    formData.append("batchId", batchId);
    formData.append("email", email);
    formData.append("requestedBy", requestedBy);
    formData.append("uploadList", uploadList);
    formData.append("assetUpdate", assetUpdate);
    formData.append("reasonUpdate", reasonUpdate);
    formData.append("upc", upc);
    formData.append("assetType", assetType);
    formData.append("shotType", shotType);
    formData.append("sequence", sequence);
    formData.append("additionalUpc", additionalUpc);
    formData.append("srtProvided", srtProvided);
    formData.append("bbbysrt", bbbysrt);
  } else {
    var batchId = $("input#batchId").val(),
      email = $("input#email").val(),
      requestedBy = $("input#requestedBy").val(),
      excelFile = $("input#excelFile").val(),
      uploadList = $("input#uploadList").val();

    formData.append("batchId", batchId);
    formData.append("email", email);
    formData.append("requestedBy", requestedBy);
    formData.append("excelFile", excelFile, excelFile.name);
    formData.append("uploadList", uploadList);

    $("#successBatchId").text(batchId);
    $("#successEmail").text(email);
    $("#successRequestedBy").text(requestedBy);
    $("#successMetadataFile").text(successMetadataFile);
  }
}

export function onAddedFile(file) {
  let fileWidth, fileHeight, fileColorSpace, fileDPI;
  fileWidth = file.width;
  fileHeight = file.height;
  let fileType = file.type;

  if (addedFilesCount === 0) {
    if (
      myDropzone.getAcceptedFiles().length === 0 &&
      Object.keys(invalidList).length > 0
    ) {
      // Remove error bar if present
      $("#uploadError").remove();
    }
  }

  // Update file count
  addedFilesCount++;

  let fileColorSpaceSpan = file.previewElement.querySelector(
    "span[data-dz-color]"
  );
  let fileDPISpan = file.previewElement.querySelector("span[data-dz-dpi]");
  let fileDimensionsSpan = file.previewElement.querySelector(
    "span[data-dz-dimensions]"
  );

  let fReader = new FileReader();
  fReader.onload = function(event) {
    // files in reject status with error message
    let rejectedFilesWithError = myDropzone.getRejectedFiles().filter(item => {
      return item.fileErrorMessage;
    });

    let addedFiles = [
      ...myDropzone.getAcceptedFiles(),
      ...rejectedFilesWithError
    ];

    if (removeDuplicate(addedFiles, file)) return;

    setFileTypeThumbnail(file);

    // Video files Aspect Ratio and Dimensions
    if (file.type.startsWith("video")) {
      const video = document.createElement("video");
      video.addEventListener("loadedmetadata", function(e) {
        fileWidth = video.videoWidth;
        fileHeight = video.videoHeight;

        if (fileWidth && fileHeight)
          fileDimensionsSpan.innerText = `${fileWidth} x ${fileHeight}`;

        fileDPI = getAspectRatio(fileWidth, fileHeight);
        if (fileDPI) {
          fileDPISpan.innerText = fileDPI;
          if (fileDPI !== "16:9") {
            addErrorMessage(
              file.previewElement.querySelector(".dz-dpi"),
              "System only accepts 16:9 Widescreen."
            );
            file.rejectFile("System only accepts 16:9 Widescreen.");
          }
        }
      });
      video.src = URL.createObjectURL(file);
    }
    try {
      if (
        fileType.toLowerCase() === "image/jpeg" ||
        fileType.toLowerCase() === "image/jpg"
      ) {
        let exifData = ExifReader.load(event.target.result, { expanded: true });

        if (exifData) {
          const {
            file: {
              "Color Components": { value: colorSpace },
              "Image Width": { value: imageWidth },
              "Image Height": { value: imageHeight }
            }
          } = exifData;

          fileWidth = fileWidth ? fileWidth : imageWidth;
          fileHeight = fileHeight ? fileHeight : imageHeight;

          if (exifData.exif && exifData.exif.XResolution) {
            fileDPI = exifData.exif.XResolution.value;
            fileDPISpan.innerText = fileDPI;
          }

          switch (colorSpace) {
            case 3:
              fileColorSpace = "RGB";
              break;
            case 4:
              fileColorSpace = "CMYK";
              break;
            default:
              fileColorSpace = "Unknown";
          }
          fileColorSpaceSpan.innerText = fileColorSpace;
        }
      } else if (fileType === "image/tiff") {
        let ifdEntries = UTIF.decode(event.target.result);
        UTIF.decodeImage(event.target.result, ifdEntries[0]);

        fileWidth = fileWidth ? fileWidth : ifdEntries[0].width;
        fileHeight = fileHeight ? fileHeight : ifdEntries[0].height;

        // XResolution code is 282
        if (ifdEntries[0] && ifdEntries[0]["t282"]) {
          fileDPI = ifdEntries[0]["t282"][0];
          fileDPISpan.innerText = fileDPI;
        }

        // Color Space code is 262
        if (ifdEntries[0] && ifdEntries[0]["t262"]) {
          const colorSpace = ifdEntries[0]["t262"][0];
          if (colorSpace === 2) {
            fileColorSpace = "RGB";
          }
          fileColorSpaceSpan.innerText = fileColorSpace;
        }
      } else if (fileType === "image/png") {
        const PNGMetadata = UPNG.decode(event.target.result);
        const { ctype, height, width } = PNGMetadata;

        fileWidth = fileWidth ? fileWidth : width;
        fileHeight = fileHeight ? fileHeight : height;

        // Xresolution should be pHY's multiply by 0.0254
		// https://www.w3.org/TR/PNG-Chunks.html
        if (PNGMetadata.tabs.pHYs && PNGMetadata.tabs.pHYs.length) {
          fileDPI = Math.round(PNGMetadata.tabs.pHYs[0] * 0.0254);
          fileDPISpan.innerText = fileDPI;
        }

        if (ctype === 2) {
          fileColorSpace = "RGB";
          fileColorSpaceSpan.innerText = fileColorSpace;
        }
      }
    } catch (err) {
      console.log("Error retrieving metadat from image.");
    }

    validateFileDimensions(file, fileWidth, fileHeight);

    // Check file DPI
    if (fileDPISpan.innerText && fileDPISpan.innerText < 150) {
      addErrorMessage(
        file.previewElement.querySelector(".dz-dpi"),
        "DPI should be greater than 150."
      );
      file.rejectFile("DPI should be greater than 150.");
    }

    if (!file.fileErrorMessage) file.acceptFile();

    file.uploadAssetItem = {
      uuid: file.upload.uuid,
      fileName: file.name,
      width: fileWidth,
      height: fileHeight,
      colorSpace: fileColorSpace
    };

    if (fileDPI == undefined) fileDPISpan.innerText = "N/A";
    if (fileColorSpace == undefined) fileColorSpaceSpan.innerText = "N/A";
  };

  fReader.readAsArrayBuffer(file);

  handleDropZoneIndicator(this.files);

  if (this.files.length && $("#excelFile").val()) {
    showValidateButton();
    hideSubmitButton();
  }
}

export function onRemovedFile(file) {
  updateFileCounts();
  handleDropZoneIndicator(this.files);

  if (!this.files.length) {
    $("#validate-all").show();
    $("#validate-all").removeClass("submit-ready");
    hideSubmitButton();
  }
}

export function onThumbnail(file, dataUrl) {
  // if (file.type !== "image/tiff") {
  // // getImageColorSpace(file, dataUrl).then(()=>validateImage(file));
  // validateDZForm();
  // } else {
  // return false;
  // validateDZForm();
  // getImageColorSpace(file, file.dataURL);
  // }
}

export function onError(file, errorMessage, xhr) {
  // if(errorMessage.includes('Server') || errorMessage.includes('server')) {
  if (xhr) {
    handleInvalid(file, errorMessage, "Retrying..", true);
    /* one file failed due to whatever reason, apply retry logic here */
    setTimeout(function() {
      if (isFileInList(file)) {
        checkIfFileExists(file);
      } else {
        myDropzone.processQueue();
      }
    }, 5000);
  } else {
    if (errorMessage) {
      // handleInvalid(file, errorMessage);
      // file.rejectFile(errorMessage);
    }
  }
}

export function onTotalUploadProgress(totalUploadProgress) {
  var width = getTotalUploadProgress(totalUploadProgress),
    files = myDropzone.getAcceptedFiles(),
    filesCancelled = 0,
    isCancelled = false;

  // Update progress bar
  $("#uploadProgress")
    .find(".progress-bar_inner")
    .width(width + "%");

  files.forEach(file => {
    if (file.status === "canceled") {
      filesCancelled++;
    }
  });

  if (filesCancelled > 0) {
    isCancelled = true;
  }

  // Show saving message
  if (
    totalUploadProgress >= 100 &&
    myDropzone.getAcceptedFiles().length > 0 &&
    areUploadsDone()
  ) {
    $("#uploadProgress").addClass("hidden");
    $("#uploadSuccess")
      .html("<h2>Upload complete. Saving...</h2>")
      .removeClass("hidden");

    if (isCancelled) {
      $("#uploadSuccess").addClass("hidden");
    }
  }
}

export function onSuccess(file, response) {
  var fileRow = file.previewElement,
    fileDimensions = $(fileRow).find(".dz-status");

  fileRow.classList.remove("dz-error");
  fileRow.classList.add("dz-success");
  var status_error = $(fileDimensions).find(".status-error");

  if (status_error.length > 0) {
    status_error.remove();
  }

  addSuccessStatus(file);
debugger;
  if (!areUploadsDone()) {
    myDropzone.processQueue();
  } else {
    if ((uploadType === 'fasttrack') || (uploadType !== 'fasttrack' && validateDZForm()) {
      // Show success message
      let totalFilesCount =
        myDropzone.getAcceptedFiles().length +
        myDropzone.getRejectedFiles().length;
      let validFilesCount = myDropzone.getAcceptedFiles().filter(item => {
        return !item.metadataErrorMessage;
      }).length;
      $("#uploadSuccess")
        .html(
          "<h2>Submission Confirmed</h2>" +
            validFilesCount +
            " of " +
            totalFilesCount +
            " assets uploaded <br>Your submission is now being processed and will be reviewed by the team"
        )
        .removeClass("hidden");

      // Make the form readonly
      resultForm();
    }
  }
}

export function onComplete(file) {
  // console.log("in complete");
  var fileRow = file.previewElement,
    fileDimensions = $(fileRow).find(".dz-status");

  var inQueue = $(fileDimensions).find(".status-in-queue"),
    error = $(fileDimensions).find(".status-error"),
    success = $(fileDimensions).find(".status-success"),
    uploading = $(fileDimensions).find(".status-uploading");

  if (uploading.length > 0) {
    // console.log("uploading");
    myDropzone.processQueue();
  } else if (inQueue.length > 0) {
    // console.log("inQueue");
  } else if (error.length > 0) {
    // console.log("error");
  } else if (success.length > 0) {
    // console.log("success");
  }
}

export function onSuccessMultiple(files, response) {
  if (validateDZForm()) {
    // Show success message
    $("#uploadSuccess")
      .html(
        "<h2>Submission Confirmed</h2>" +
          succesItems +
          " of " +
          listCount +
          " assets uploaded <br>Your submission is being processed and will be reviewed by Bed Bath & Beyond."
      )
      .removeClass("hidden");

    // Make the form readonly
    resultForm();
  }
}

export function onErrorMultiple(files, response, xhr) {
  if (!$("#uploadSuccess").hasClass("hidden")) {
    $("#uploadSuccess").addClass("hidden");
  }

  if (xhr && xhr.status === 500) {
    $(".upload-result.error-bar").html(
      "<h2>Submission Failed</h2>Contact your administrator.</div>"
    );
    resultForm();
  } else if (validateDZForm()) {
    document.getElementById("upload").innerHTML =
      '<div class="upload-result error-bar"><h2>Submission Failed</h2>Refresh this page and try your submission again.</div>' +
      document.getElementById("upload").innerHTML;
    resultForm();
  }
}

export function onCompleteMultiple(files) {
  var errors = 0;

  files.forEach(f => {
    if (f.status === "error") {
      errors++;
    }
  });

  if (files.length > 0 && errors === 0) {
    myDropzone.disable();
  }
}
