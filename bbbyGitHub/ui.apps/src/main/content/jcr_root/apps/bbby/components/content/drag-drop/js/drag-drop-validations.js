// import '../polyfill.forEach';
import XLSX from 'xlsx';
import {timeoutBanner, cleanupInvalid, invalidList, uploadType, myDropzone, updateFileCounts} from './internals';
export let listCount = 0;
export let succesItems = 0;
export let mdVerifiedList = [];

let validxls = [];
let sequenceGroups = {};

function getAnalyzeCount() {
    var i = 0;
    var analyzeList = $(".analyze");
    for(var j = 0; j < analyzeList.length; j++) {
        var txt = analyzeList[j].innerHTML;
        if(txt==="Analyzing..."){
            i++;
        }
    }
    return i;
};

export function validateDZForm(event, submitClicked = false) {

    if(uploadType === 'single') {
        return validateSingleForm(event,submitClicked);
    } else if (uploadType === 'multi'){
        return validateMultiForm(event,submitClicked);
    } else if (uploadType === 'fasttrack') {
        return validateFastTrackForm(event,submitClicked);
    }
}

function validateSingleForm(event,submitClicked){

    listCount = document.querySelectorAll('.dropzone .dz-image-preview').length;
    succesItems =  listCount - document.querySelectorAll('.dropzone .dimensions-no-use,.dz-error').length;

    let invalidFiles = document.querySelectorAll('.dropzone .dz-preview').length === 0 ||
                     document.querySelectorAll('.dropzone .dz-preview').length === document.querySelectorAll('.dropzone .dimensions-no-use,.dz-error').length;

    var analyzing = getAnalyzeCount() - 3;

    let inputFields = document.querySelectorAll('input.required.single,select.required.single,#singleEmail,input[name="asset-update"]:checked');
    let inputFieldsError = false;

  //DAM-374 : Add validation on the Vendor portal to limit asset drop #
	let validateCount = document.getElementById('vendorAssetDropLimit').value;
	if ( listCount > validateCount ) {
        inputFieldsError = true;
    }

    for (let i = 0; i < inputFields.length; i++) {
        let input = inputFields[i];

        if ( input.type ==='email' ) {
            inputFieldsError = !inputFieldsError ? input.value !== '' && !validateEmail(input.value) : inputFieldsError;

            if (submitClicked || input.classList.contains('error')) {
                input.classList.toggle('error', input.value !== '' && !validateEmail(input.value));
            }
        } else if ( input.type ==='radio' ) {

            var dropdown = $('#reasonUpdate');
            inputFieldsError = !inputFieldsError ? input.value === 'yes' && !dropdown.val() : inputFieldsError;

            if (submitClicked || dropdown[0].classList.contains('error')) {
                dropdown[0].classList.toggle('error',input.value === 'yes' && !dropdown.val() );
            }
        } else if ( input.type ==='select' || input.type ==='select-one' ) {

            if(input.value === '') {

                inputFieldsError = true;

                if (submitClicked || dropdown[0].classList.contains('error')) {
                    input.classList.toggle('error',true );
                }
            } else if(input.value === 'Product Images' ) {

                if (submitClicked || dropdown[0].classList.contains('error')) {
                    input.classList.toggle('error',false );
                }

                var shotTypeDrop = $('#shotType');
                inputFieldsError = !inputFieldsError ? input.value === 'Product Images' && !shotTypeDrop.val() : inputFieldsError;

                if (submitClicked || shotTypeDrop[0].classList.contains('error')) {
                    shotTypeDrop[0].classList.toggle('error',input.value === 'Product Images' && !shotTypeDrop.val() );
                }
            }
            else if(input.value !== '' && input.value !== "Product Images"){
                if (submitClicked || dropdown[0].classList.contains('error')) {
                    input.classList.toggle('error',false );
                }

                var shotTypeDrop = $('#shotType');
                if (submitClicked || shotTypeDrop[0].classList.contains('error')) {
                    shotTypeDrop[0].classList.toggle('error',input.value === 'Product Images' && !shotTypeDrop.val());
                }
            }

        } else if ( input.id ==='upc' ) {

                inputFieldsError = !inputFieldsError ? $('.tags span').length === 0 || !validateUPC($('.tags span')) : inputFieldsError;

                if (submitClicked || $('.tags').hasClass('error')) {
                    $('.tags').toggleClass('error',$('.tags span').length === 0 || !validateUPC($('.tags span')));
                }
        } else if (submitClicked || input.classList.contains('error')) {
            inputFieldsError = !inputFieldsError ? input.value === '' : inputFieldsError;
            input.classList.toggle('error', input.value === '');
        }
    }

    let button = $('#submit-single');
    //button.classList.toggle('submit-ready', !invalidFiles && !inputFieldsError);

    return !invalidFiles && !inputFieldsError && !analyzing;
}

function validateMultiForm(event,submitClicked){
    listCount = document.querySelectorAll('.dropzone .dz-image-preview').length;
    let invalidFiles =document.querySelectorAll('.dropzone .dz-preview').length === 0;

    var analyzing = getAnalyzeCount() - 3;

    let inputFields = document.querySelectorAll('input.required.multi,input[name="email"]');
    let inputFieldsError = false;

    //DAM-374 : Add validation on the Vendor portal to limit asset drop #
	let validateCount = document.getElementById('vendorAssetDropLimit').value;
	if ( listCount > validateCount ) {
        inputFieldsError = true;
    }

    for (let i = 0; i < inputFields.length; i++) {
        let input = inputFields[i];

        if ( input.type ==='email' ) {
            if ( input.value !== '' && !validateEmail(input.value) ) {

                inputFieldsError = true;

                if (submitClicked || input.classList.contains('error')) {
                    input.classList.toggle('error', (input.value === ''|| !isXlsFile(input.value) ) );
                }
            }
        } else if ( input.value === '' || !isXlsFile(input.value) ) {
            inputFieldsError = true;

            if (submitClicked || input.classList.contains('error')) {
                input.classList.toggle('error', (input.value === ''|| !isXlsFile(input.value) ) );
            }
        }
    }

    let button = document.getElementById('validate-all');
    button.classList.toggle('submit-ready', !invalidFiles && !inputFieldsError);

    return !invalidFiles && !inputFieldsError && !analyzing;
}

function validateFastTrackForm(event,submitClicked){
    listCount = document.querySelectorAll('.dropzone .dz-image-preview').length;
    let invalidFiles = document.querySelectorAll('.dropzone .dz-preview').length === 0;

    var analyzing = getAnalyzeCount() - 3;

    let inputFields = document.querySelectorAll('input.required.multi,input[name="email"]');
    let inputFieldsError = false;

    //DAM-374 : Add validation on the Vendor portal to limit asset drop #
	let validateCount = document.getElementById('vendorAssetDropLimit').value;
	if ( listCount > validateCount ) {
        inputFieldsError = true;
    }

    for (let i = 0; i < inputFields.length; i++) {
        let input = inputFields[i];

        if ( input.type ==='email' ) {
            if ( input.value !== '' && !validateEmail(input.value) ) {

                inputFieldsError = true;

                if (submitClicked || input.classList.contains('error')) {
                    input.classList.toggle('error', (input.value === ''|| !isXlsFile(input.value) ) );
                }
            }
        } else if ( input.value === '' || !isXlsFile(input.value) ) {
            inputFieldsError = true;

            if (submitClicked || input.classList.contains('error')) {
                input.classList.toggle('error', (input.value === ''|| !isXlsFile(input.value) ) );
            }
        }
    }

    let button = document.getElementById('validate-QC');
    button.classList.toggle('submit-ready', !invalidFiles && !inputFieldsError);

    return !invalidFiles && !inputFieldsError && !analyzing;
}

function isXlsFile(name){

    var fileExt = name;
    var validExts = new Array(".xlsx", ".xls");
    fileExt = fileExt.substring(fileExt.lastIndexOf('.'));
    return (validExts.indexOf(fileExt) >= 0) ;
}

function validateEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}

function validateUPC(upc) {
	var isValidUPC = true;
	var isValidUPCLength = true;
	for(var i=0;i<upc.length;i++)
	{
		var upcStr = upc[i].innerHTML;
		if(isNaN(upcStr)){
			isValidUPC = false;
		}else if(upcStr.indexOf(".") > -1){
			isValidUPC = false;
		}else if(upcStr.indexOf("-") > -1){
			isValidUPC = false;
		}else if(upcStr.indexOf("+") > -1){
			isValidUPC = false;
		}else if(upcStr.length < 10 || upcStr.length > 15){
			isValidUPCLength = false;
		}
	}
	if(!isValidUPC){
		var parentDiv = $(".tags").parent();
		$(".tags").addClass("error");
		parentDiv.find("span.upcSpan").remove();
		parentDiv.append("<span class='upcSpan' style='color: red;'>Please enter a valid UPC(accepts only numeric values)</span>");
	}else if(!isValidUPCLength){
		var parentDiv = $(".tags").parent();
		$(".tags").addClass("error");
		parentDiv.find("span.upcSpan").remove();
		parentDiv.append("<span class='upcSpan' style='color: red;'>Please enter a valid UPC(InValid UPC length)</span>");
		isValidUPC = false;
	}else{
		var parentDiv = $(".tags").parent();
		parentDiv.find("span.upcSpan").remove();
		$(".tags").removeClass("error");
	}
	return isValidUPC;
}

export function checkfile(name) {

    var fileExt = name;
    var validExts = new Array(".xlsx");
    fileExt = fileExt.substring(fileExt.lastIndexOf('.'));
    if (validExts.indexOf(fileExt) < 0) {
        $(".file-info").addClass("error");
        $(".col-bbb-button").find("span").remove();
        $(".col-bbb-button").append("<span style='color: red;'>System only accepts .xlsx. Please re-upload the metadata sheet in the correct format.</span>");
    } else {
    	//DAM-508 : Check on the Vendor Portal to ensure that the correct version of the multi asset upload sheet is being used
    	checkExlFileVersion();
    }
}

function checkExlFileVersion() {
	//Reference the FileUpload element.
    var fileUpload = $("#excelFile")[0];
    //Validate whether File is valid Excel file.
    var regex = /^([a-zA-Z0-9\s_\\.\-:])+(.xls|.xlsx)$/;
    var excelVersion = document.getElementById('excelVersion').value;
    var cellAddress = 'C29';//cell address where version is mentioned in ExcelSheet.
    if (regex.test(fileUpload.value.toLowerCase())) {
        if (typeof (FileReader) != "undefined") {
            var reader = new FileReader();
            //For Browsers other than IE.
            if (reader.readAsBinaryString) {
                reader.onload = function (e) {

                    var workbook = XLSX.read(e.target.result, {type: 'binary'});
                    var sheet_name = workbook.SheetNames[0];
                    let worksheet = workbook.Sheets[sheet_name];
                    var cell = 0;
                    if(worksheet[cellAddress]){
                    	cell = worksheet[cellAddress].v;
                    }
                    if (excelVersion == cell) {
                    	 $(".file-info").removeClass("error");
                	     $(".col-bbb-button").find("span").remove();
                    } else {
                    	$(".file-info").addClass("error");
                        $(".col-bbb-button").find("span").remove();
                        $(".col-bbb-button").append("<span style='color: red;'>Template uploaded is old version, please download the latest template and upload.</span>");
                    }
                };
                reader.readAsBinaryString(fileUpload.files[0]);
            } else {
                //For IE Browser.
                reader.onload = function (e) {
                    // Pre-process file before reading
                    // https://github.com/SheetJS/js-xlsx/wiki/Reading-XLSX-from-FileReader.readAsArrayBuffer()
                    var data = "";
                    var bytes = new Uint8Array(e.target.result);
                    for (var i = 0; i < bytes.byteLength; i++) {
                        data += String.fromCharCode(bytes[i]);
                    }
                    var workbook = XLSX.read(data, { type: 'binary' });
                    var sheet_name = workbook.SheetNames[0];
                    let worksheet = workbook.Sheets[sheet_name];
                    var cell = 0;
                    if(worksheet[cellAddress]){
                    	cell = worksheet[cellAddress].v;
                    }
                    if (excelVersion == cell) {
                    	$(".file-info").removeClass("error");
               	     	$(".col-bbb-button").find("span").remove();
                    } else {
                   		$(".file-info").addClass("error");
                   		$(".col-bbb-button").find("span").remove();
                   		$(".col-bbb-button").append("<span style='color: red;'>Template uploaded is old version, please download the latest template and upload.</span>");
                    }
                };
                reader.readAsArrayBuffer(fileUpload.files[0]);
            }
        } else {
            alert("This browser does not support HTML5.");
        }
    } else {
        alert("Please upload a valid Excel file.");
    }
}

function validateAssets(xls) {

    console.log("validate assets");
    console.log($('input#uploadList').val());

    // var uploadList = JSON.parse($('input#uploadList').val());

    let uploadList = [...myDropzone.getAcceptedFiles(), ...myDropzone.getRejectedFiles()];

    mdVerifiedList = [];
    succesItems = 0;
    var duplicatedList = [];
    uploadList.forEach((uploadItem) => {

        var fileMD = xls.filter(function(item){
          return (item.Filename.trim() === uploadItem.name.trim());
        });
        if(fileMD[0]) {
            if (isValidateFields( fileMD[0], uploadItem)  ) {

                var parentDiv = $('span').filter(function(){ return $(this).text() == uploadItem.name; }).closest('.dz-preview');
                var span = parentDiv.find('span[data-dz-metadata]');


                parentDiv.find('.dz-metadata .tooltip_container')[0].classList.remove('error');
                parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.remove('error');

                parentDiv.find('.dz-metadata .tooltip_container div')[0].removeAttribute("hidden");
                parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.add('check');

                parentDiv[0].classList.remove('dimensions-no-use');

                if(!uploadItem.fileErrorMessage){
                    parentDiv[0].classList.remove("dz-error");
                }

                // Delete metadataErrorMessage on correct validation
                delete uploadItem.metadataErrorMessage;
                updateFileCounts();
                mdVerifiedList.push(uploadItem);
                succesItems ++;

            } else {

                var parentDiv = $('span').filter(function(){ return $(this).text() == uploadItem.name; }).closest('.dz-preview');
                var span = parentDiv.find('span[data-dz-metadata]');

                parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.remove('check');

                parentDiv.find('.dz-metadata .tooltip_container div')[0].removeAttribute("hidden");
                parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.add('error');
                parentDiv.find('.dz-metadata .tooltip_container')[0].classList.add('error');

                parentDiv[0].classList.add('dimensions-no-use');
                console.log("metadata error");
                console.log(uploadItem);
             // update uploadList


            }
        } else {

            //var parentDiv = $("span:contains('"+uploadItem.fileName+"')").closest('.dz-preview');
            var parentDiv = $('span').filter(function(){ return $(this).text() == uploadItem.name; }).closest('.dz-preview');
            var span = parentDiv.find('span[data-dz-metadata]');

            parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.remove('check');

            parentDiv.find('.dz-metadata .tooltip_container div')[0].removeAttribute("hidden");
            parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.add('error');
            parentDiv.find('.dz-metadata .tooltip_container')[0].classList.add('error');

            parentDiv[0].classList.add('dimensions-no-use');

            console.log("metadata error");
            console.log(uploadItem);

            uploadItem.metadataErrorMessage = "Unmatched filename.";
            updateFileCounts();
        }
   });

    //$('input#uploadList').val(JSON.stringify(mdVerifiedList));
    console.log($('input#uploadList').val());
   if (succesItems >= 1) {
        $('#validate-all').hide();
        $('#submit-all').show();
        $('#submit-all').addClass('submit-ready');
   }
}

function validateFasttrackAssets(xls) {

    let uploadList = [...myDropzone.getAcceptedFiles(), ...myDropzone.getRejectedFiles()];

    mdVerifiedList = [];
    succesItems = 0;
    var duplicatedList = [];
    var count = 0;
    validxls = [];
    uploadList.forEach((uploadItem) => {

        var fileMD = xls.filter(function(item){
          return (item.Filename.trim() === uploadItem.name.trim());
        });
        if(fileMD[0]) {
            if (isValidateFields( fileMD[0], uploadItem)  ) {
                validxls[count++] = fileMD[0];
                var parentDiv = $('span').filter(function(){ return $(this).text() == uploadItem.name; }).closest('.dz-preview');
                var span = parentDiv.find('span[data-dz-metadata]');

                if(parentDiv.find('.dz-metadata .tooltip_container div').length) {
                parentDiv.find('.dz-metadata .tooltip_container')[0].classList.remove('error');
                parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.remove('error');

                parentDiv.find('.dz-metadata .tooltip_container div')[0].removeAttribute("hidden");
                parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.add('check');
                }
                parentDiv[0].classList.remove('dimensions-no-use');

                if(!uploadItem.fileErrorMessage){
                    parentDiv[0].classList.remove("dz-error");
                }
                if(fileMD[0].SharedAsset) {
                    uploadItem.uploadAssetItem.sharedAsset = fileMD[0].SharedAsset.trim();
                }
                // Delete metadataErrorMessage on correct validation
                delete uploadItem.metadataErrorMessage;
                updateFileCounts();
                mdVerifiedList.push(uploadItem);
                succesItems ++;

            } else {

                var parentDiv = $('span').filter(function(){ return $(this).text() == uploadItem.name; }).closest('.dz-preview');
                var span = parentDiv.find('span[data-dz-metadata]');
                if(parentDiv.find('.dz-metadata .tooltip_container div').length) {
                parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.remove('check');

                parentDiv.find('.dz-metadata .tooltip_container div')[0].removeAttribute("hidden");
                parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.add('error');
                parentDiv.find('.dz-metadata .tooltip_container')[0].classList.add('error');
                }
                parentDiv[0].classList.add('dimensions-no-use');
                console.log("metadata error");
                console.log(uploadItem);
            }
        } else {
            //var parentDiv = $("span:contains('"+uploadItem.fileName+"')").closest('.dz-preview');
            var parentDiv = $('span').filter(function(){ return $(this).text() == uploadItem.name; }).closest('.dz-preview');
            var span = parentDiv.find('span[data-dz-metadata]');

            if(parentDiv.find('.dz-metadata .tooltip_container div').length) {
            parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.remove('check');

            parentDiv.find('.dz-metadata .tooltip_container div')[0].removeAttribute("hidden");
            parentDiv.find('.dz-metadata .tooltip_container div')[0].classList.add('error');
            parentDiv.find('.dz-metadata .tooltip_container')[0].classList.add('error');
            }

            parentDiv[0].classList.add('dimensions-no-use');

            console.log("metadata error");
            console.log(uploadItem);

            uploadItem.metadataErrorMessage = "Unmatched filename.";
            updateFileCounts();
        }
   });
   if (succesItems >= 1 && uploadList.length == succesItems) {
        showFileSequence();
   }
}

function isValidateFields(file, uploadFileItem) {

    var fileName = file['Filename'];
    var assetUpdate = file['Asset Update'];
    var sequence = file['Sequence'];

    var containsReasonUpdate = false;
    var isReasonUpdateRequired = false;
    var isFileNameValid = true;

    if(sequence){
	    if(isNaN(sequence)){
	    	file['Sequence'] = '';
	    }
    }

    if(assetUpdate){
        if(assetUpdate.toLowerCase() === 'yes'){
            isReasonUpdateRequired = true;
            if(file['Reason for Asset Update']){
                containsReasonUpdate = true;
            }
        }
        else if (assetUpdate.toLowerCase() === 'no'){
            isReasonUpdateRequired = false;
        }

    }

    var upc = file['UPC'];
    var assetType = file['Asset Type'];
    var shotType = file['Shot Type'];

    var containsShotType = false;
    var isShotTypeRequired = false;
    var isUPCNumeric = true;
    var isValidUPCLength = true;

    var isValidShotType = true;
    var isValidAssetType = true;

    if(assetType){
        if(assetType.toLowerCase() === 'product images'){
            isShotTypeRequired = true;
            if(shotType){
                containsShotType = true;
            }
        }else if(assetType.toLowerCase() !== 'product images'){
            isShotTypeRequired = false;
        }

        isValidAssetType = validateAssetType(assetType.toLowerCase());
        console.log("validate isValidAssetType : " + isValidAssetType);
    }

    if(shotType){
    	isValidShotType = validateShotType(shotType.toLowerCase());
    	console.log("validate isValidShotType : " + isValidShotType);
    }

    // Clear metadataErrorMessage for excel re-submission.
    delete uploadFileItem.metadataErrorMessage;

    if(!assetUpdate){
        updateMetadataErrorMessage(uploadFileItem, "Asset Update Metadata is missing. ");
        updateFileCounts();
    }
    if(!assetType){
        updateMetadataErrorMessage(uploadFileItem, "Asset Type Metadata is missing. ");
        updateFileCounts();
    }else if(!isValidAssetType){
    	updateMetadataErrorMessage(uploadFileItem, "Asset Type Metadata is inValid. ");
        updateFileCounts();
    }

    if(shotType){
    	if(!isValidShotType){
	    	updateMetadataErrorMessage(uploadFileItem, "Shot Type Metadata is inValid. ");
	        updateFileCounts();
    	}
    }

    if(isReasonUpdateRequired) {
        if(!containsReasonUpdate){
            updateMetadataErrorMessage(uploadFileItem, "Reason for Update Metadata is missing. ");
            updateFileCounts();
        }
    }
    if(!upc) {
    	console.log("UPC Metadata is missing. ");
        updateMetadataErrorMessage(uploadFileItem, "UPC Metadata is missing. ");
        updateFileCounts();
    }else {
    	console.log("UPC Metadata is present. "+ upc);
    	//DAM-518 : If it is NOT numeric, we reject the file and validation should fail on portal itself.
    	var res;

    	//DAM-1097 : When UPC column in metadata spreadsheet have scientific expressions values "**E+**" or incorrect format values, Validate button in Multi asset upload not showing any error or nothing happens.
    	if(!isNaN(upc)){
    		upc = upc.toString();
    	}

    	if(upc.indexOf(';') > -1){
    		res = upc.split(";");
		}else{
			res = upc.split(",");
		}
    	var i;
    	for (i = 0; i < res.length; i++) {
    		if(isNaN(res[i])){
    			isUPCNumeric = false;
        	}else if(res[i].indexOf(".") > -1){
        		isUPCNumeric = false;
    		}else if(res[i].indexOf("-") > -1){
        		isUPCNumeric = false;
    		}else if(res[i].indexOf("+") > -1){
        		isUPCNumeric = false;
    		}else if(res[i].trim().length < 10 || res[i].trim().length > 15){
    			isValidUPCLength = false;
    		}
    	}
    	if(!isUPCNumeric){
    		updateMetadataErrorMessage(uploadFileItem, "Please enter a valid UPC(accepts only numeric values). ");
            updateFileCounts();
    	}else if(!isValidUPCLength){
    		updateMetadataErrorMessage(uploadFileItem, "Please enter a valid UPC(InValid UPC length). ");
            updateFileCounts();
    	}
    }

    if(isShotTypeRequired)  {
        if(!containsShotType){
            updateMetadataErrorMessage(uploadFileItem, "Shot Type Metadata is missing. ");
            updateFileCounts();
        }
    }

    return fileName && assetUpdate && (isReasonUpdateRequired ? containsReasonUpdate : true) && upc && assetType && (isShotTypeRequired ? containsShotType : true) && isUPCNumeric && isValidUPCLength && isValidShotType && isValidAssetType;
}

function validateAssetType(assetType) {
	console.log("validate assetType : " + assetType);
	var assetTypes = ["product images", "assembly instructions", "energy guides", "parts lists", "prop 65", "user guides and manuals", "videos", "lighting facts", "warranty information", "rds logo", "forestry cert fsc", "forestry cert sfi", "forestry cert fair trade", "cotton cert bci", "cotton cert okeo tex", "cotton cert abrapa", "cotton cert basf", "cotton cert e3", "cotton cert cleaner cotton", "cotton cert cotton made in africa", "cotton cert fair trade", "cotton cert field to market", "cotton cert gots", "cotton cert iscc", "cotton cert mybmp", "cotton cert ocs", "cotton cert reel cotton", "cotton cert us cotton trust protocol"];
	return assetTypes.includes(assetType);
}

function validateShotType(shotType) {
	console.log("validate shotType : " + shotType);
	var shotTypes = ["silhouette", "collection", "construction", "dimensions", "electronic display", "environment", "hardware or installation", "in use", "logo", "nutritional information",
	                 "orientation - back view", "orientation - interior view", "orientation - left degree angle", "orientation - left side", "orientation - overhead", "orientation - right degree angle",
	                 "orientation - right side", "orientation - under side", "power source", "product in packaging", "scale", "surface detail"];
	return shotTypes.includes(shotType);
}

function updateMetadataErrorMessage(file, errorMessage){
    if(file.metadataErrorMessage)
        file.metadataErrorMessage += errorMessage;
    else
        file.metadataErrorMessage = errorMessage;
}

function handleInvalid(file, error) {

    var status = 'Cannot be used';
    if(_status) {
        status = _status;
    }
    // Prevent duplicates in invalid file list
    if (invalidList[file]) {
        //myDropzone.removeFile(file);
        return false;
    }

    invalidList[file] = error;
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

function validateAll(event){
    event.preventDefault();
        event.stopPropagation();
        //Reference the FileUpload element.
        var fileUpload = $("#excelFile")[0];
        //Validate whether File is valid Excel file.
        var regex = /^([a-zA-Z0-9\s_\\.\-:])+(.xls|.xlsx)$/;
        var excelVersion = document.getElementById('excelVersion').value;
        var cellAddress = 'C29';//cell address where version is mentioned in ExcelSheet.
        if (regex.test(fileUpload.value.toLowerCase())) {
            if (typeof (FileReader) != "undefined") {
                var reader = new FileReader();
                //For Browsers other than IE.
                if (reader.readAsBinaryString) {
                    reader.onload = function (e) {

                        var workbook = XLSX.read(e.target.result, {type: 'binary'});

                        var sheet_name = workbook.SheetNames[1];
                        var sheet_name1 = workbook.SheetNames[0];
                        //Convert the cell value to Json
                        var roa = XLSX.utils.sheet_to_json(workbook.Sheets[sheet_name]);
                        if (roa.length > 0) {
                        	let worksheet = workbook.Sheets[sheet_name1];
                            var cell = 0;
                            if(worksheet[cellAddress]){
                            	cell = worksheet[cellAddress].v;
                            }
                            if (excelVersion == cell) {
                            	$(".file-info").removeClass("error");
                       	     	$(".col-bbb-button").find("span").remove();
                       	     	validateAssets(roa);
                            } else {
                           		$(".file-info").addClass("error");
                           		$(".col-bbb-button").find("span").remove();
                           		$(".col-bbb-button").append("<span style='color: red;'>Template uploaded is old version, please download the latest template and upload.</span>");
                           		alert("Template uploaded is old version, please download the latest template and upload.");
                            }
                        } else {
                            alert("Please ensure the Metadata sheet is populated and re upload.");
                        }
                    };
                    reader.readAsBinaryString(fileUpload.files[0]);
                } else {
                    //For IE Browser.
                    reader.onload = function (e) {
                        // Pre-process file before reading
                        // https://github.com/SheetJS/js-xlsx/wiki/Reading-XLSX-from-FileReader.readAsArrayBuffer()
                        var data = "";
                        var bytes = new Uint8Array(e.target.result);
                        for (var i = 0; i < bytes.byteLength; i++) {
                            data += String.fromCharCode(bytes[i]);
                        }
                        var workbook = XLSX.read(data, { type: 'binary' });
                        var sheet_name = workbook.SheetNames[1];
                        var sheet_name1 = workbook.SheetNames[0];
                        var roa = XLSX.utils.sheet_to_json(workbook.Sheets[sheet_name]);
                        if (roa.length > 0) {
                        	let worksheet = workbook.Sheets[sheet_name1];
                            var cell = 0;
                            if(worksheet[cellAddress]){
                            	cell = worksheet[cellAddress].v;
                            }
                            if (excelVersion == cell) {
                            	$(".file-info").removeClass("error");
                       	     	$(".col-bbb-button").find("span").remove();
                       	     	validateAssets(roa);
                            } else {
                           		$(".file-info").addClass("error");
                           		$(".col-bbb-button").find("span").remove();
                           		$(".col-bbb-button").append("<span style='color: red;'>Template uploaded is old version, please download the latest template and upload.</span>");
                           		alert("Template uploaded is old version, please download the latest template and upload.");
                            }
                        } else {
                            alert("Please ensure the Metadata sheet is populated and re upload.");
                        }
                    };
                    reader.readAsArrayBuffer(fileUpload.files[0]);
                }
            } else {
                alert("This browser does not support HTML5.");
            }
        } else {
            alert("Please upload a valid Excel file.");
        }
}

function validateQC(event){
    event.preventDefault();
    event.stopPropagation();
    //Reference the FileUpload element.
    var fileUpload = $("#excelFile")[0];
    //Validate whether File is valid Excel file.
    var regex = /^([a-zA-Z0-9\s_\\.\-:])+(.xls|.xlsx)$/;
    var excelVersion = document.getElementById('excelVersion').value;
    var cellAddress = 'C29';//cell address where version is mentioned in ExcelSheet.
    if (regex.test(fileUpload.value.toLowerCase())) {
        if (typeof (FileReader) != "undefined") {
            var reader = new FileReader();
            //For Browsers other than IE.
            if (reader.readAsBinaryString) {
                reader.onload = function (e) {

                    var workbook = XLSX.read(e.target.result, {type: 'binary'});

                    var sheet_name = workbook.SheetNames[1];
                    var sheet_name1 = workbook.SheetNames[0];
                    //Convert the cell value to Json
                    var roa = XLSX.utils.sheet_to_json(workbook.Sheets[sheet_name]);
                    if (roa.length > 0) {
                        let worksheet = workbook.Sheets[sheet_name1];
                        var cell = 0;
                        if(worksheet[cellAddress]){
                            cell = worksheet[cellAddress].v;
                        }
                        if (excelVersion == cell) {
                            $(".file-info").removeClass("error");
                            $(".col-bbb-button").find("span").remove();
                            validateFasttrackAssets(roa);
                        } else {
                            $(".file-info").addClass("error");
                            $(".col-bbb-button").find("span").remove();
                            $(".col-bbb-button").append("<span style='color: red;'>Template uploaded is old version, please download the latest template and upload.</span>");
                            alert("Template uploaded is old version, please download the latest template and upload.");
                        }
                    } else {
                        alert("Please ensure the Metadata sheet is populated and re upload.");
                    }
                };
                reader.readAsBinaryString(fileUpload.files[0]);
            } else {
                //For IE Browser.
                reader.onload = function (e) {
                    // Pre-process file before reading
                    // https://github.com/SheetJS/js-xlsx/wiki/Reading-XLSX-from-FileReader.readAsArrayBuffer()
                    var data = "";
                    var bytes = new Uint8Array(e.target.result);
                    for (var i = 0; i < bytes.byteLength; i++) {
                        data += String.fromCharCode(bytes[i]);
                    }
                    var workbook = XLSX.read(data, { type: 'binary' });
                    var sheet_name = workbook.SheetNames[1];
                    var sheet_name1 = workbook.SheetNames[0];
                    var roa = XLSX.utils.sheet_to_json(workbook.Sheets[sheet_name]);
                    if (roa.length > 0) {
                        let worksheet = workbook.Sheets[sheet_name1];
                        var cell = 0;
                        if(worksheet[cellAddress]){
                            cell = worksheet[cellAddress].v;
                        }
                        if (excelVersion == cell) {
                            $(".file-info").removeClass("error");
                            $(".col-bbb-button").find("span").remove();
                            validateFasttrackAssets(roa);
                        } else {
                            $(".file-info").addClass("error");
                            $(".col-bbb-button").find("span").remove();
                            $(".col-bbb-button").append("<span style='color: red;'>Template uploaded is old version, please download the latest template and upload.</span>");
                            alert("Template uploaded is old version, please download the latest template and upload.");
                        }
                    } else {
                        alert("Please ensure the Metadata sheet is populated and re upload.");
                    }
                };
                reader.readAsArrayBuffer(fileUpload.files[0]);
            }
        } else {
            alert("This browser does not support HTML5.");
        }
    } else {
        alert("Please upload a valid Excel file.");
    }
}

function showFileSequence () {
    if (document.createElement("template").content) {
        $("#validate-QC").hide();
        $("#validate-sequence").addClass("submit-ready").show();
        $("#upload-batch").hide();
        $(".dz-sequence-preview").remove();
        $(".sequence-count").html("");
        $(".sequence-invalid").html("");
        // for or while loop for multiple sequences
        generateSequence();
        $("#show-sequence").show();
    } else {
      console.log("Your browser does not supports template!");
    }
}

function generateSequence() {
    sequenceGroups = validxls.reduce((r, a) => {
     r[a.UPC] = [...r[a.UPC] || [], a];
     return r;
    }, {});
    var groupSize = 0;
    for (var key in sequenceGroups) {
        if (sequenceGroups.hasOwnProperty(key)) {
            sequenceGroups[key] = [...sequenceGroups[key]].sort((a, b) => a.Sequence - b.Sequence);
            populateFileSequence(sequenceGroups[key]);
            groupSize++;
        }
    };
    if(groupSize > 0) {
        $(".sequence-count").html(groupSize + " Sequence");
    }
}

function populateFileSequence(group) {

    var previewImagesetTempContent = document.querySelector('#dzImagesetTemplate').content;
    var clone = document.importNode(previewImagesetTempContent, true);
    var dzSequence = clone.querySelector('.dz-sequence-preview');
    var imgThumbnail = clone.querySelector('img');
    var fileName = clone.querySelector('.dz-filename span');

    var seqNo = clone.querySelector('.dz-sequence-number span');
    var isPrimaryKeyPresent = false;
    group.forEach(function(values, index) {
        if(index === 0) {
            dzSequence.className = dzSequence.className + " " + values.UPC.replace(",", "-");
            var upc = clone.querySelector('.dz-upc span');
            upc.textContent = values.UPC;
        }
        if(values.Sequence == 1) {
            fileName.innerHTML = fileName.innerHTML + "<span class='dz-primary-img icon-p'>" + values.Filename + "</span>"
            isPrimaryKeyPresent = true;
        } else {
            fileName.innerHTML = fileName.innerHTML + "<span class='dz-alt-filename'>" + values.Filename + "</span>";
        }
        seqNo.innerHTML = seqNo.innerHTML + "<span class='dz-alt-seq-no'>" + values.Sequence + "</span>";
    });
    if(!isPrimaryKeyPresent) {
        dzSequence.className = dzSequence.className + " noPrimaryKey";
    }
    document.querySelector(".checkSequenceZone").appendChild(clone);
}

export function validateFileSequence() {
    var validSeq = true;
    var noOfInvalidSequences = 0;
    for (var key in sequenceGroups) {
        var errorMessage = "";
        if (sequenceGroups.hasOwnProperty(key)) {
            var seq = sequenceGroups[key];
            var sumOfSequence = 0;
            var size =  seq.length;
            var n = seq[size-1].Sequence; // where n is the largest number in the sequence

            var sumOf1toN = n*(n+1)/2 ;;
            var isPrimaryKeyPresent = false;
            var isDuplicatePresent = false;
            seq.forEach(function(values, index) {
                sumOfSequence = sumOfSequence + values.Sequence;
                if(values.Sequence == 1) {
                    isPrimaryKeyPresent = true;
                }
                if(index + 1 != size && (values.Sequence == seq[index+1].Sequence)) {
                    isDuplicatePresent = true;
                }
            });
            var misssingSeq = sumOf1toN - sumOfSequence;
            if(!isPrimaryKeyPresent) {
                errorMessage = errorMessage + "Primary Image is Missing.<br/>";
                validSeq = false;
            } else if(isDuplicatePresent) {
                errorMessage = errorMessage + "Duplicate Sequence Number.<br/>";
                validSeq = false;
            } else if( misssingSeq != 0) {
                errorMessage = errorMessage + "Sequence is Missing.<br/> ";
                validSeq = false;
            }
            // put all error message
            var keyClass = key.replace(",", "-");
            if(errorMessage != "") {
                $(".dz-sequence-preview." + keyClass).find(".dz-status .tooltip_container").addClass("alert");
                $(".dz-sequence-preview." + keyClass).find(".dz-status .icon").addClass("alert");
                $(".dz-sequence-preview." + keyClass).addClass("dz-error").find(".dz-status .tooltiptext").html(errorMessage);
                noOfInvalidSequences++;
            } else {
                $(".dz-sequence-preview." + keyClass).find(".dz-status .icon").addClass("check");
            }
        }
    }
    if(noOfInvalidSequences > 0) {
        $(".sequence-invalid").html(noOfInvalidSequences + " sequences are invalid. Please recheck excel file and upload again.")
    }
    return validSeq;
}

export function validations(){

    $("input[type=email]").on('blur input', function() {
        var parentDiv = $(this).parent();
        if($(this).val() ==='' ) {
            $(this).removeClass("error");
            parentDiv.find("span").remove();
        } else {
            var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            if(re.test($(this).val())) {
                $(this).removeClass("error");
                parentDiv.find("span").remove();
            } else {
                $(this).addClass("error");
                parentDiv.find("span").remove();
                parentDiv.append("<span style='color: red;'>Please enter a valid email address</span>");
            }
        }
         validateDZForm();
    });

    $("#validate-all").on("click", validateAll);
    $("#validate-QC").on("click", validateQC);


    // add validateDZForm to change/input events
    let requireds = document.querySelectorAll('input.required, textarea.required');
    requireds.forEach((element) => {
        if (element.type === 'checkbox') {
        element.addEventListener('change', validateDZForm);
        } else {
            element.addEventListener('input', validateDZForm);
        }
    });
}

