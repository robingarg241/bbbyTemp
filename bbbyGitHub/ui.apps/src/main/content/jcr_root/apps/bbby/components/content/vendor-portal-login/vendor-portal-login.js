'use strict';

/**
 *  This is the main file for Vendor Portal Login
 */


import $ from 'jquery'
import log from 'loglevel'

if (document.getElementById('vpLogin')) {
    $(function () {
        var verification_reminder = "Your account is not yet active. Please check your e-mail at the address you provided during registration for a message with the subject line \"Nordstrom Asset Portal New User Registration\" and click the link to activate your account.";

        var status_verified = "0";
        var status_not_verified = "1";
        var status_user_not_exists = "2";
        checkCookie();

        $("#inputPassword").keyup(function (event) {
            if (event.keyCode == 13) {
                $("#loginButton").click();
            }
        });

        $("#loginButton").click(function (e) {
            e.preventDefault();
            //if ($('#rememberMe').is(":checked")) {
            var user = $("#userId").val();
            if (user != "" && user != null) {
                setCookie("usr_c", user, 30);
            }
            //}

            var valid = validateForm();
            if (valid) {
                $.ajax({
                    type: "POST",
                    url: $('#url').val(),
                    data: {
                        j_username: $("#userId").val(),
                        j_password: $("#inputPassword").val(),
                        j_validate: "true"
                    },
                    success: function (data, textStatus, jqXHR) {
                        window.location.href = getRedirectPath();
                    },
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                        /***** cunstruction zone *********/
                        $.ajax({
                            type: "POST",
                            url: '/bin/nordstrom/servlets/userValidationCheckerServlet',
                            data: {
                                j_username: $("#userId").val(),
                                j_password: $("#inputPassword").val(),
                            },
                            success: function (data) {
                                if(data===status_not_verified) {
                                    $("#errordiv").removeClass('hidden').text(verification_reminder);
                                } else {
                                    $("#errordiv").removeClass('hidden').text("Invalid User Name or Password");
                                }
                            },
                            error: function (XMLHttpRequest ){

                            }
                        });


                        /***** cunstruction zone *********/





                    }
                });
            } else {
                $("#errordiv").removeClass('hidden').text("Invalid User Name or Password");
            }
        });

        // add focus class for input label animation
        var focusInputs = $('input[type="text"], input[type="email"], input[type="password"]');

        $(focusInputs).on('focus', function (event) {
            $(this).parent().addClass('focus');
        });

        $(focusInputs).on('blur', function (event) {
            var val = $(this).val();

            if (!val || val === '') {
                $(this).parent().removeClass('focus');
            }
        });
    });

    //Function to redirect to homepage after login
    var getRedirectPath = function () {
        var redirectPath = $('#redirectUrl').attr("data-redirecturl");
        return redirectPath;
    }

    // Function for setting cookies at login page
    var setCookie = function (cname, cvalue, exdays) {
        var d = new Date();
        d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
        var expires = "expires=" + d.toGMTString();
        document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
    }

    // Function for getting cookies at login page
    var getCookie = function (cname) {
        var name = cname + "=";
        var decodedCookie = decodeURIComponent(document.cookie);
        var ca = decodedCookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') {
                c = c.substring(1);
            }
            if (c.indexOf(name) == 0) {
                return c.substring(name.length, c.length);
            }
        }
        return "";
    };

    // Function for checking cookies at login page
    var checkCookie = function () {
        var user = getCookie("usr_c");
        if (user != "") {
            $("#userId").val(user);
        }
    };

    // Function for validating Login form
    var validateForm = function () {
        var valid = false;
        if (($("#userId").val().length > 0) || ($("#inputPassword").val().length > 0)) {
            valid = true;
        }
        return valid;
    };
}

/*
class VendorPortalLogin {
    constructor($inst) {
        log.trace('Initializing VendorPortalLogin')
    }
}

$('.cmp-vendor-portal-login').each((index, elem) => {
    new VendorPortalLogin($(elem))
})

*/
