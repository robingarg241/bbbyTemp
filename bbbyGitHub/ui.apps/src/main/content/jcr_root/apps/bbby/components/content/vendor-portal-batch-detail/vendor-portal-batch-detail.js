/**
 *  This is the main file for vendor-portal-batch-detail
 */

'use strict';

import * as wcmmode from 'js/utils/wcmmode';
// import 'js/polyfills/polyfill.after';
// import 'js/pollyfills/polyfill.fetch';
// import Promise from 'js/pollyfills/polyfill.promise';
// import 'js/pollyfills/polyfill.object.keys';
import { Spinner } from 'js/utils/spin';
// import './vanilla-dataTables';
//import './moment';

if (document.getElementById('vpUploadDetails')) {
    let spinnerElement = document.getElementById('batchSpinner');
    let spinner = new Spinner().spin(spinnerElement);
    let containerElement = document.getElementById('batchContainer');
    let errorElement = document.getElementById('batchError');

    let params = new URLSearchParams(window.location.search);

    // https://stackoverflow.com/questions/15900485/correct-way-to-convert-size-in-bytes-to-kb-mb-gb-in-javascript
    function formatBytes(bytes,decimals) {
        if(bytes == 0) return '0 Bytes';
        var k = 1024,
        dm = decimals || 2,
        sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
        i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
    }

    if (params.get('batch') !== '') {
        fetch(`/bin/bedbath/vendor-portal/history/batch-details?batch=${params.get('batch')}`, { credentials: 'include' })
            .then((response) => {
                return response.json();
            }).then((response) => {
                spinner.stop();
                spinnerElement.style.display = 'none';

                if (Object.keys(response).length) {
                    containerElement.style.display = 'block';

                    document.getElementById('batchID').innerHTML = response.batchID ? response.batchID : 'Unavailable';
                    document.getElementById('adobeUUID').innerHTML = response.adobeUUID ? response.adobeUUID : 'Unavailable';
                    document.getElementById('requestedBy').innerHTML = response.requestedBy ? response.requestedBy : 'Unavailable';
                    document.getElementById('totalNoOfFiles').innerHTML = response.totalNoOfFiles ? response.totalNoOfFiles : 'Unavailable';
                    document.getElementById('noOfFiles').innerHTML = response.noOfFiles ? response.noOfFiles : 'Unavailable';
                    document.getElementById('rejectedFiles').innerHTML = response.rejectedFiles ? response.rejectedFiles : 'Unavailable';
                    document.getElementById('failedToUploadFiles').innerHTML = response.failedToUploadFiles ? '<font color="red">' + response.failedToUploadFiles + '  (Please resubmit failed files)</font>' : 'Unavailable';
                    document.getElementById('noOfRejectedFiles').innerHTML = response.noOfRejectedFiles ? response.noOfRejectedFiles : 'Unavailable';
                    document.getElementById('noOfFailedFiles').innerHTML = response.noOfFailedFiles ? response.noOfFailedFiles : 'Unavailable';
                    
                    if (response.files && response.files.length) {
                        let results = [];
                        response.files.forEach((file) => {
                            if (!file || Object.keys(file).length === 0) return;

                            let fileType;
                            if (file.fileExtension.toLowerCase() === 'jpg' || file.fileExtension.toLowerCase() === 'jpeg') {
                                fileType = `<span class="type-jpg"></span>`;
                            } else if (file.fileExtension.toLowerCase() === 'png') {
                                fileType = `<span class="type-png"></span>`;
                            } else if (file.fileExtension.toLowerCase() === 'tif' || file.fileExtension.toLowerCase() === 'tiff') {
                                fileType = `<span class="type-tif"></span>`;
                            } else if (file.fileExtension.toLowerCase() === 'psd') {
                                fileType = `<span class="type-psd"></span>`;
                            } else if (file.fileExtension.toLowerCase() === 'ai') {
                                fileType = `<span class="type-ai"></span>`;
                            } else if (file.fileExtension.toLowerCase() === 'eps') {
                                fileType = `<span class="type-eps"></span>`;
                            } else if (file.fileExtension.toLowerCase() === 'pdf') {
                                fileType = `<span class="type-pdf"></span>`;
                            } else if (file.fileExtension.toLowerCase() === 'mov') {
                                fileType = `<span class="type-movie"></span>`;
                            } else if (file.fileExtension.toLowerCase() === 'mp4') {
                                fileType = `<span class="type-movie"></span>`;
                            } else if (file.fileExtension.toLowerCase() === 'svg') {
                                fileType = `<span class="type-svg"></span>`;
                            } else if (file.fileExtension.toLowerCase() === 'bmp') {
                                fileType = `<span class="type-bmp"></span>`;
                            } else {
                                // unknown file type
                                fileType = `<span class="type-other"></span>`;
                            }

                            results.push([
                                // type
                                fileType,

                                // filename
                                file.fileName,

                                // file Extension
                                file.fileExtension,

                                // dimensions
                                (file.width === '0' && file.height === '0') ? 'N/A' : `${file.width} x ${file.height}`,

                                // colorSpace
                                file.colorSpace? file.colorSpace:"N/A"
                            ]);
                        });

                        let uploadBatchDetails = new DataTable('#uploadBatchDetails', {
                            data: {
                                headings: [
                                    'Thumbnail',
                                    'File Name',
                                    'File Type',
                                    'Dimensions',
                                    'Color Space'
                                ],
                                data: results
                            },
                            columns: [
                                {
                                    select: 0,
                                    render: function(data, cell, row) {
                                        cell.classList.add('type');
                                        return data;
                                    }
                                },
                                {
                                    select: 1,
                                    render: function(data, cell, row) {
                                        cell.classList.add('name');
                                        return data;
                                    }
                                },
                                {
                                    select: 2,
                                    render: function(data, cell, row) {
                                        cell.classList.add('file-ext');
                                        return data;
                                    }
                                },
                                {
                                    select: 3,
                                    render: function(data, cell, row) {
                                        const fullUse = {
                                            width: 2400,
                                            height: 3680
                                        };
                                        const minUse = {
                                            width: 1000,
                                            height: 1000
                                        };
                                        let width, height;

                                        cell.classList.add('dimensions');

                                        if (data !== 'N/A') {
                                            const dimensionsRegEx = /(\S*) x (\S*)/g;
                                            const matches = dimensionsRegEx.exec(data);
                                            width = matches[1];
                                            height = matches[2];
                                        }

                                        return data;
                                    }
                                },
                                {
                                  select: 4,
                                  render: function(data, cell, row) {
                                    cell.classList.add('color-space');
                                    return data;
                                  }
                                }
                            ],
                            perPage: 10,
                            perPageSelect: false,
                            searchable: false,
                        });
                    } else {
                        errorElement.style.display = 'block';
                        errorElement.innerHTML = 'ERROR: No files listed in json';
                    }
                } else {
                    errorElement.style.display = 'block';
                    errorElement.innerHTML = 'ERROR: Batch json is empty';
                }
            })
        ;
    } else {
        // no batch in query string
        spinner.stop();
        spinnerElement.style.display = 'none';

        errorElement.style.display = 'block';
        errorElement.innerHTML = 'ERROR: No batch to show';
    }
}
