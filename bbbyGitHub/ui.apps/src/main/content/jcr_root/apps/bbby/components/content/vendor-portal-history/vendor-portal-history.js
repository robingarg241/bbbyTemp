'use strict';
import * as wcmmode from 'js/utils/wcmmode';

// import './polyfill.after';
// import './polyfill.fetch';
// import Promise from './polyfill.promise';
import { Spinner } from 'js/utils/spin';
// import moment from 'moment';
// import './vanilla-dataTables';

if (document.getElementById('vpUploadHistory')) {
    const batchDetailsPath = document.getElementById('batchDetails').getAttribute('data-batchdetailsurl');

    let spinnerElement = document.getElementById('historySpinner');
    let spinner = new Spinner().spin(spinnerElement);
    let containerElement = document.getElementById('historyContainer');
    let errorElement = document.getElementById('historyError');

    fetch('/bin/bedbath/vendor-portal/history', { credentials: 'include' })
        .then((response) => {
            return response.json();
        }).then((response) => {
            spinner.stop();

            spinnerElement.style.display = 'none';

            if (response.batches) {
                containerElement.style.display = 'block';

                function normalizeCaps(string) {
                    // https://stackoverflow.com/questions/1026069/how-do-i-make-the-first-letter-of-a-string-uppercase-in-javascript
                    return string.charAt(0).toUpperCase() + string.slice(1);
                }
                var batchID,
                upDisplay,
                requestedBy,
                totalNoOfF,
                noOfF,
                noOfInvalidF,
                noOfFailedToUploadF;

                let results = [];
                response.batches.forEach((batch) => {
                    try {
                        batchID = batch.batchID ? batch.batchID :"";
                        upDisplay = batch.uploadDisplayDate ? batch.uploadDisplayDate  :"";
                        requestedBy = batch.requestedBy ? batch.requestedBy :"";
                        totalNoOfF = batch.totalNoOfFiles ? batch.totalNoOfFiles :"";
                        noOfF = batch.noOfFiles ? batch.noOfFiles :"";
                        noOfInvalidF = batch.noOfInvalidFiles ? batch.noOfInvalidFiles :"";
                        noOfFailedToUploadF = batch.noOfFailedToUploadFiles ? batch.noOfFailedToUploadFiles :"";

                        results.push([
                            batchID,
                            upDisplay,
                            requestedBy,
                            totalNoOfF,
                            noOfF,
                            noOfInvalidF,
                            noOfFailedToUploadF
                        ]);
                    } catch (err) {

                    }
                });
                $('.batches').html("<span>"+results.length+"</span> Batches")

                let uploadHistory = new DataTable('#uploadHistory', {
                    data: {
                        headings: [
                            'Vendor Portal ID',
                            'Upload Date',
                            'Requested By',
                            'Total # of Files',
                            '# of Accepted Files',
                            '# of Rejected Files',
                            '# of Failed Files'
                        ],
                        data: results
                    },
                    columns: [
                        {
                            select: 0,
                            render: function (data, cell, row) {
                                return '<span class="uh-batch">' + data + '</span>';
                            }
                        },
                        {
                            select: 1,
                            type: 'date',
                            format: 'MMMM D, YYYY',
                            render: function (data, cell, row) {
                                var date = moment(data, 'MMM DD,YYYY').format('MMMM D, YYYY');
                                return date;
                            }
                        },
                        {
                            select: 3,
                            type: 'number'
                        },
                        {
                            select: 4,
                            type: 'number'
                        },
                        {
                            select: 5,
                            type: 'number'
                        },
                        {
                            select: 6,
                            type: 'number'
                        }
                    ],
                    perPage: 20,
                    perPageSelect: false,
                    searchable: false,
                    labels: {
                        info: "Showing {start} - {end} of {rows} Batches"
                    },
                    layout: {
                        bottom: "{info}{pager}"
                    }
                });
                uploadHistory.on('datatable.init', function(event) {
                    this.columns().sort(2, 'asc');

                    this.activeRows.forEach((row) => {
                        row.addEventListener('click', function(event) {
                            window.location = `${batchDetailsPath}?batch=${this.querySelector('.uh-batch').innerHTML}`;
                        });
                    });
                });
                uploadHistory.on('datatable.sort', function(column, direction) {
                    this.activeRows.forEach((row) => {
                        row.addEventListener('click', function(event) {
                            window.location = `${batchDetailsPath}?batch=${this.querySelector('.uh-batch').innerHTML}`;
                        });
                    });
                })
            } else {
                errorElement.style.display = 'block';
                errorElement.innerHTML = 'Upload History is empty';
            }
        })
    ;
}
