/**
 * Created with JetBrains WebStorm.
 * User: isaacsiegel
 * Date: 2/15/15
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */


function attemptLogin() {
    var userName = $('#emailField').val();
    var password = $('#passwordField').val();

    $.ajax(
        {
            type : "POST",
            url  : "/cs480/authenticate/" + userName,
            data : {
                password: password
            },
            success : function(result) {
            //console.log(result)
                if(result == "Login Failed")
                    alert("Invalid Login")
                else {

                }

            },
            error: function (jqXHR, exception) {
                alert("Error during login.");
            }
        });
}