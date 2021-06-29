'use strict';

// Toggle header navigation link status based on active page

// Upload Batch
if (document.getElementById('navUpload')) {
    if (document.getElementById('vpUpload')) {
        document.getElementById('navUpload').classList.add('active');
    }
}

// View History
if (document.getElementById('navHistory')) {
    if (document.getElementById('vpUploadHistory') || document.getElementById('vpUploadDetails')) {
        document.getElementById('navHistory').classList.add('active');
    }
}

// View FAQ
if (document.getElementById('navFaq')) {
    if (!document.getElementById('vpUploadHistory') && !document.getElementById('vpUploadDetails')
        && !document.getElementById('vpUpload')) {
        document.getElementById('navFaq').classList.add('active');
    }
}
