/**
 * Created by isaacsiegel on 2/17/15.
 */



function attemptSignup() {
    var userName = $('#emailField').val();
    var password = $('#passwordField').val();

    $.ajax(
        {
            type : "POST",
            url  : "/cs480/createUser/" + userName,
            data : {
                "password": password
            },
            success : function(result) {
                //console.log(result)
                if(result == true)  {
                    alert("You're awesome. Please Login.")
                    window.location.href = "login.html";

                }
                else {
                       alert("Username taken, try another.")
                }

            },
            error: function (jqXHR, exception) {
                alert("Error during signup.");
            }
        });
}
