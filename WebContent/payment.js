let payment_form = $("#payment_form");

function submitPaymentForm(formSubmitEvent){
    formSubmitEvent.preventDefault();
    console.log("submitting payment form");
    jQuery.ajax("api/payment", {
            method: "POST",
            data: payment_form.serialize(),
            success: handlePaymentResult
        });
}

function handlePaymentResult(resultDataString){
    let resultDataJson = JSON.parse(resultDataString);

    if (resultDataJson["status"] === "success") {
        console.log("Payment success!")
        window.location.assign("confirmation.html?id="+resultDataJson["id"]);
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#payment_error_message").text(resultDataJson["message"]);
    }
}

const urlParams = new URLSearchParams(window.location.search);
$("#total").text("Total: " + urlParams.get("total"));

payment_form.submit(submitPaymentForm);