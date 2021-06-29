'use strict';

var ngApp = angular.module('vendor-asset-approval', []);

ngApp.config(function ($anchorScrollProvider) {
    $anchorScrollProvider.disableAutoScrolling();
});

ngApp.controller('vendor-asset-approval-ctrl', ['$scope', '$document', '$location', '$http', '$anchorScroll', '$timeout', '$window'
    , function ($scope, $document, $location, $http, $anchorScroll, $timeout, $window) {
        $scope.test = "test varrr";

        // constants
        var loadingState = 0;
        var selectingState = 1;
        var completeState = 2;


        var buy = "buy";
        var notbuy = "notbuy";
        var decisionUndefined = "undefined";

        var selectedStyle = "selected";
        var unselectedStyle = "asset-thumbnail-case ";
        var thumbnailCaseBuyStyle = "accepted";


        var normalThumbnailImageStyle = "asset-thumbnail ";
        var rejectedThumbNailImageStyle = "reject";


        var displayStyleMid = "asset-case ";
        var displayStyleLeft = "left";
        var displayStyleRight = "right";

        var noLikedStyle = "like-icon ";
        var likedStyle = "like-icon-show";

        var noDislikedStyle = "dislike-icon ";
        var dislikedStyle = "dislike-icon-show";

        // declare variables
        var singleSelectIndex = 0;
        var multiSelectArray = [];

        var path = "";
        var state = 0;

        $scope.imgList = [];
        $scope.i = 0;
        $scope.projectName = "";
        $scope.brandName = "";
        $scope.campaignName = "";
        $scope.contactNumber = "";
        $scope.description = "";
        $scope.endDate = "";
        $scope.selectAllVal = false;

        $scope.init = function () {
            path = $location.absUrl().split('assetPath=')[1].split('#!#')[0];
            //$location.search()['assetPath']
            loadImgs(path);
        };

        $scope.isLoading = function () {
            return state === loadingState;
        };

        $scope.isSelecting = function () {
            return state === selectingState;
        };

        $scope.isComplete = function () {
            return state === completeState;
        };

        function loadImgs(path) {
            var imgs = [];


            $http({
                method: 'POST',
                url: '/bin/nordstrom/servlets/fetchImagesMetadata',
                params: {
                    body: {"path": path}
                }
            }).then(function successCallback(response) {
                if (response.data.complete === "true") {
                    state = completeState;
                } else {


                    var assets = response.data.images;
                    $scope.brandName = response.data.brandName;
                    $scope.campaignName = response.data.campaignName;
                    $scope.contactNumber = response.data.contactNumber;
                    $scope.description = response.data.description;
                    $scope.endDate = response.data.endDate;
                    $scope.projectName = response.data.projectName;

                    for (var i = 0; i < assets.length; i++) {
                        var asset = {
                            name: assets[i].name,
                            path: assets[i].path,
                            showThumbNail: false,
                            thumbnailCheckbox: false,
                            onScreen: false,
                            decision: assets[i].buynotbuy,
                            imagePath: assets[i].imagePath
                        };
                        imgs.push(asset);
                    }

                    $scope.imgList = imgs;


                    var initialPosition = +$location.absUrl().split('#!#asset-')[1];

                    if (!isNaN(initialPosition) && angular.isNumber(initialPosition)) {
                        $scope.i = initialPosition;
                    } else {
                        $scope.i = 0;
                    }

                    $timeout(function () {
                        $scope.selectImage($scope.i);
                        $window.scrollTo(0, 0);
                        loadThumbNails(document.querySelector('#asset-queue'));
                    });

                    state = selectingState;
                }

            }, function errorCallback(response) {
                var errorMsg = "An error has occured while fetching assets with error: " + JSON.stringify(response);
                console.log(errorMsg);
                alert(errorMsg);
            });
        }

        $scope.getImageClass = function (show) {
            if (show === true) {
                return "ng-hide";
            } else {
                return "display";
            }
        };

        function getAssetPathList(idxes) {
            var assetPathList = [];
            for (var i = 0; i < idxes.length; i++) {
                var idx = idxes[i];

                assetPathList.push($scope.imgList[idx].path);
            }
            return assetPathList;
        }

        $scope.pass = function () {
            var selectedAssetIndexArray = getSelectedIndexes();
            var selectedAssetPathArray = getAssetPathList(selectedAssetIndexArray);
            uploadDecision("notbuy", selectedAssetPathArray);
            updateDecision("notbuy", selectedAssetIndexArray);
            if (!isMultiSelect(selectedAssetIndexArray)) {
                nextImage();
            }

        };

        var isMultiSelect = function (selectedAssetIndexArray) {
            if (selectedAssetIndexArray.length === 1 &&
                $scope.imgList[selectedAssetIndexArray[0]].thumbnailCheckbox == false) {
                return false;
            } else {
                return true;
            }
        };


        $scope.like = function () {
            var selectedAssetIndexArray = getSelectedIndexes();
            var selectedAssetPathArray = getAssetPathList(selectedAssetIndexArray);
            uploadDecision("buy", selectedAssetPathArray);
            updateDecision("buy", selectedAssetIndexArray);

            if (!isMultiSelect(selectedAssetIndexArray)) {
                nextImage();
            }
        };

        var updateDecision = function( decision, selectedAssetIndexArray) {
            for (var i = 0; i < selectedAssetIndexArray.length; i++) {
                $scope.imgList[selectedAssetIndexArray[i]].decision = decision;
            }
        };


        $scope.reset = function () {
            var selectedAssetIndexArray = getSelectedIndexes();
            var selectedAssetPathArray = getAssetPathList(selectedAssetIndexArray);
            uploadDecision("undefined", selectedAssetPathArray);
            updateDecision("undefined", selectedAssetIndexArray);

            if (!isMultiSelect(selectedAssetIndexArray)) {
                nextImage();
            }
        };

        var getFullListInIndexes = function () {
            var indexes = [];
            for (var i = 0; i < $scope.imgList.length; i++) {
                indexes.push(i);
            }
            return indexes;
        };


        var getSelectedIndexes = function () {
            var indexes = [];
            for (var i = 0; i < $scope.imgList.length; i++) {
                if ($scope.imgList[i].thumbnailCheckbox === true) {
                    indexes.push(i);
                }
            }
            if (indexes.length < 1) {
                indexes.push($scope.i);
            }

            return indexes;
        };

        $scope.clearAllRatings = function () {
            var selectedAssetIndexArray = getFullListInIndexes();
            var selectedAssetPathArray = getAssetPathList(selectedAssetIndexArray);
            erase(selectedAssetIndexArray);
            uploadDecision(decisionUndefined, selectedAssetPathArray);

        };

        function uploadDecision(decision, assets) {

            $http({
                method: 'GET',
                url: '/bin/nordstrom/reviewUpdate',
                params: {
                    body: {"assetPaths": assets, "assetProperty": decision}
                }
            }).then(function successCallback(response) {
                console.log(response.data);
            }, function errorCallback(response) {
                var errorMsg = "An error has occured while update decisions on asset with error: " + JSON.stringify(response);
                console.log(errorMsg);
                alert(errorMsg);
            });
        }

        $scope.prev = function () {
            prevImage();
        };

        $scope.next = function () {
            nextImage();
        };

        function getThumbnailId(idx) {
            return "asset-" + idx;
        };

        $scope.scrollToEnd = function() {
            scrollTo($scope.imgList.length-1, true);
        };

        $scope.scrollToStart = function() {
            scrollTo(0, true);
        };

        function scrollTo(idx, withoutSettingAnchor) {
            if(withoutSettingAnchor!=true) {
                $location.hash(getThumbnailId(idx));
            }

            if ($scope.imgList[idx].onScreen === true) {
                return;
            }


                var firstShowingElement = -1;
                var numberOfElementShwoing = 0;
                for(var i = 0; i < $scope.imgList.length; i ++) {
                    if(firstShowingElement<0 && $scope.imgList[i].onScreen == true) {
                        firstShowingElement=i;
                        numberOfElementShwoing++;
                    } else if(firstShowingElement>=0 && $scope.imgList[i].onScreen == true) {
                        numberOfElementShwoing++;
                    } else if (firstShowingElement>=0 && $scope.imgList[i].onScreen == false) {
                        break;
                    }
                }

            if (firstShowingElement >= 0) {

                var assetQueue = document.querySelector('#' + getThumbnailId(0));
                var width = assetQueue.getBoundingClientRect().width + 4;

                if (firstShowingElement < idx) {
                    var scrollLength = width * (idx - numberOfElementShwoing + 1);
                    angular.element("#asset-queue")[0].scrollLeft = scrollLength;
                } else {
                    var scrollLength = width * idx;
                    angular.element("#asset-queue")[0].scrollLeft = scrollLength;
                }
                //}
            } else {
                $anchorScroll();
            }

            //goTo(idx);

        };

        // function scrollTo(idx) {
        //     $location.hash(getThumbnailId(idx));
        //     $anchorScroll();
        // };

        function scrollForwardByOne(idx) {
            $location.hash(getThumbnailId(idx));

            var assetQueue = document.querySelector('#' + getThumbnailId(0));
            var width = assetQueue.getBoundingClientRect().width + 4;
            if ($scope.imgList[idx].onScreen === false)
                angular.element("#asset-queue")[0].scrollLeft = scrollLength;
        };

        function scrollbackwardByOne(idx) {
            $location.hash(getThumbnailId(idx));
            // $anchorScroll();
            //     if($scope.imgList[idx].onScreen===false)
            //         angular.element("#asset-queue")[0].scrollLeft = scrollLength;
        };

        function prevImage() {

            if ($scope.i === 0) {
                return;
            } else {
                $scope.i--;
                scrollTo($scope.i);
            }
        };

        function nextImage() {
            if ($scope.i + 1 >= $scope.imgList.length) {
                return;
            } else {
                $scope.i++;
                scrollTo($scope.i);

            }
        };


        $scope.selectImage = function (idx) {
            $scope.i = idx;
            scrollTo(idx);
        };

        $scope.toggleSelectAll = function () {
            if ($scope.selectAllVal === true) {
                $scope.selectAll();
            } else {
                $scope.unselectAll();
            }
        };

        $scope.unselectAll = function () {
            for (var i = 0; i < $scope.imgList.length; i++) {
                $scope.imgList[i].thumbnailCheckbox = false;
            }
        };


        $scope.selectAll = function () {
            for (var i = 0; i < $scope.imgList.length; i++) {
                $scope.imgList[i].thumbnailCheckbox = true;
            }
        };

        $scope.loadThumbNail = function (idx) {
            var thumbnail = $document[0].getElementById(getThumbnailId(idx));
            var bounding = thumbnail.getBoundingClientRect();
            console.log(idx + ":::" + JSON.stringify(bounding));
            return true;
        };

        angular.element(document.querySelector('#asset-queue')).bind('scroll', function () {
            //console.log('scrolling');
            $scope.$apply(loadThumbNails(this));
        });

        angular.element($window).bind('resize', function () {
            //console.log('resizing');
            $scope.$apply(loadThumbNails(document.querySelector('#asset-queue')));
        });

        function loadThumbNails(assetQueue) {
            var queueDimension = assetQueue.getBoundingClientRect();
            var left = queueDimension.x;
            var right = left + queueDimension.width;
            var top = queueDimension.y;
            var bot = top + queueDimension.height;

            // console.log(left);
            // console.log(right);
            // console.log(top);
            // console.log(bot);

            if ($scope.imgList !== undefined && $scope.imgList.length > 0) {

                var inShowZone = false;

                for (var i = 0; i < $scope.imgList.length; i++) {
                    var thumbnail = $document[0].getElementById(getThumbnailId(i));
                    var thumbNailDimension = thumbnail.getBoundingClientRect();
                    var thumbNailLeft = thumbNailDimension.x;
                    var thumbNailRight = thumbNailLeft + thumbNailDimension.width;
                    var thumbNailTop = thumbNailDimension.y;
                    var thumbNailBot = thumbNailTop + thumbNailDimension.height;

                    /* lose bound */
                    if (thumbNailRight >= left
                        && thumbNailLeft <= right
                        && thumbNailBot >= top
                        && thumbNailTop <= bot) {
                        inShowZone = true;
                        $scope.imgList[i].showThumbNail = true;
                    } else {
                        $scope.imgList[i].showThumbNail = false;
                    }

                    /* tight bound */
                    if (thumbNailLeft >= left
                        && thumbNailRight <= right
                        && thumbNailTop >= top
                        && thumbNailBot <= bot) {
                        inShowZone = true;
                        $scope.imgList[i].onScreen = true;
                    } else {
                        $scope.imgList[i].onScreen = false;
                    }
                    // console.log(i + " ::: " + $scope.imgList[i].showThumbNail);
                }
            }
        };

        $scope.complete = function () {
            var confirmMsg = "Once you click 'OK', this selection page will be locked."
                +"\nAny further changes please contact imaging team."
                +"\nAny unselected images will not be selected on buy if there are any other changes please go back now."
                +"\nAre you sure you want to proceed?";
            var completeConfirmed = confirm(confirmMsg);

            if (completeConfirmed === true) {
                $http({
                    method: 'POST',
                    url: '/bin/nordstrom/servlets/completeReview',
                    params: {
                        body: {"path": path}
                    }
                }).then(function successCallback(response) {
                    if (response.status == 200) {
                        state = completeState;
                    } else {
                        var errorMsg = "An error has occured while updating project state with error: " + JSON.stringify(response);
                        console.log(errorMsg);
                        alert(errorMsg);
                    }
                }, function errorCallback(response) {
                    var errorMsg = "An error has occured while updating project state with error: " + JSON.stringify(response);
                    console.log(errorMsg);
                    alert(errorMsg);
                });
            }
        };

        var forwardScrollCarousel = function (firstShowingElement, numberOfElementShwoing) {
            //$scope.selectImage(scrollCarousel(true));
            var numAssets = $scope.imgList.length - 1;
            var rightHandBound = firstShowingElement + numberOfElementShwoing + numberOfElementShwoing - 1;
            if (rightHandBound > numAssets) {
                rightHandBound = numAssets;
            }

            scrollTo(rightHandBound);

            var leftHandBound = rightHandBound - numberOfElementShwoing + 1;
            if (leftHandBound < 0) {
                leftHandBound = 0;
            }

            scrollTo(leftHandBound);
        };

        var backwardScrollCarousel = function (firstShowingElement, numberOfElementShwoing) {
            var leftHandBound = firstShowingElement - numberOfElementShwoing;
            if (leftHandBound < 0) {
                leftHandBound = 0;
            }
            scrollTo(leftHandBound);
        };

        $scope.scrollCarousel = function (forward) {
            var assetQueue = document.querySelector('#asset-queue');
            var queueDimension = assetQueue.getBoundingClientRect();
            var width = queueDimension.width;

            if (forward == true) {
                angular.element("#asset-queue")[0].scrollLeft += width;
            } else {
                angular.element("#asset-queue")[0].scrollLeft -= width;
            }
        };

        /*** > style related functions ***/

        $scope.getThumbnailCaseStyle = function (idx) {
            var thumbnailStyle = unselectedStyle;
            if(idx === $scope.i) {
                thumbnailStyle += selectedStyle;
            } else if ($scope.imgList[idx].decision===buy) {
                thumbnailStyle += thumbnailCaseBuyStyle;
            }

            return thumbnailStyle;
        };

        $scope.getThumbnailImageStyle = function (idx) {
            var thumbnailImageStyle = normalThumbnailImageStyle;

            if ($scope.imgList[idx].decision===notbuy) {
                thumbnailImageStyle += rejectedThumbNailImageStyle;
            }

            return thumbnailImageStyle;
        };

        $scope.getDisplayImageStyle = function (idx) {
            var displayImageStyle = displayStyleMid;

            if(idx > $scope.i) {
                displayImageStyle += displayStyleRight;
            } else if(idx < $scope.i) {
                displayImageStyle += displayStyleLeft;
            }


            return displayImageStyle;
        };

        $scope.getLikeStyle = function(idx) {
            var likeStyle = noLikedStyle;

            if($scope.imgList[idx].decision===buy) {
                likeStyle += likedStyle;
            }

            return likeStyle;
        };

        $scope.getDislikeStyle = function(idx) {
            var dislikeStyle = noDislikedStyle;

            if($scope.imgList[idx].decision===notbuy) {
                dislikeStyle += dislikedStyle;
            }

            return dislikeStyle;
        };

        $scope.doRenderImage = function (idx) {
            if (Math.abs(idx-$scope.i)< 2) {
                return true;
            }
            return false;
        };

        $scope.doRenderThumbnails = function (idx) {
            if (Math.abs(idx-$scope.i)< 2) {
                return true;
            }
            return false;
        };
    }]);

