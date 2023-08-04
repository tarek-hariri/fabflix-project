let login_form = $("#employee_login_form");

/**
 * Handle the data returned by EmployeeLoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    try {
        let resultDataJson = JSON.parse(resultDataString);

        console.log("handle login response");
        console.log(resultDataJson);
        console.log(resultDataJson["status"]);

        // If login succeeds, it will redirect the user to index.html
        if (resultDataJson["status"] === "success") {
            window.location.assign("_dashboard.html");
        } else {
            // If login fails, the web page will display
            // error messages on <div> with id "login_error_message"
            console.log("show error message");
            console.log(resultDataJson["message"]);
            $("#login_error_message").text(resultDataJson["message"]);
        }
    }
    catch{
        $("#login_error_message").text("Humanity not verified!");
    }

}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit employee login form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/employee", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);