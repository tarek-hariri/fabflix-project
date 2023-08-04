let payment_button = $("#to_payment");

function toPayment(){
    $.ajax({
        url: "api/shoppingcart",
        method: "GET",
        success: (resultData) => sumTotal(resultData)
    });
    function sumTotal(resultData){
        let sumVal = 0;
        let resultDataJson = JSON.parse(resultData);

        for(let i = 0; i < resultDataJson["previousItems"].length/3; i++){
            sumVal += resultDataJson["previousItems"][3*i+1] * resultDataJson["previousItems"][3*i+2];
        }

        console.log(sumVal);
        window.location.assign("payment.html?total=" + sumVal);
    }
}

function decrementQuantity(valueId){
    let text = "#" + valueId.toString();
    let value = jQuery(text);
    if(parseInt(value.text())>0) {
        value.text(parseInt(value.text()) - 1);
        jQuery.ajax({
            url : "api/shoppingcartupdate",
            data : {elementIndex : valueId, quantity : parseInt(value.text()), action: "update"},
            method : "POST",
            dataType: "json",
            success : updateTotal
        });
        function updateTotal(resultData){
            let total = jQuery("#total" + valueId.toString());
            console.log(resultData["total"]);
            total.text(resultData["total"]);
        }
    }
}
function incrementQuantity(valueId){
    let text = "#" + valueId.toString();
    let value = jQuery(text);
    value.text(parseInt(value.text())+1);
    jQuery.ajax({
        url : "api/shoppingcartupdate",
        data : {elementIndex : valueId, quantity : parseInt(value.text()), action : "update"},
        method : "POST",
        dataType: "json",
        success : updateTotal
    });
    function updateTotal(resultData){
        let total = jQuery("#total" + valueId.toString());
        console.log(resultData["total"]);
        total.text(resultData["total"]);
    }
}

function deleteEntry(valueId){
    jQuery.ajax({
        url : "api/shoppingcartupdate",
        data : {elementIndex : valueId, action : "delete"},
        method : "POST"
    });
    location.reload();
}

function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    console.log(resultDataJson);

    let itemTableBodyElement = jQuery("#item_table_body");
    let itemTableHTML = ""
    // show cart information
    for(let i = 0; i < resultDataJson["previousItems"].length/3; i++){
        itemTableHTML += "<tr>";
        itemTableHTML += "<th>" + resultDataJson["previousItems"][3*i] + "</th>";
        itemTableHTML += "<th> <button onClick = decrementQuantity(" + i + ")>-</button><text id = '" + i + "' action = '#'>"+resultDataJson["previousItems"][3*i+2]+"</text><button onClick = incrementQuantity(" + i + ")>+</button></th>";
        itemTableHTML += "<th> <button onClick = deleteEntry(" + i + ")>Delete</button></th>";
        itemTableHTML += "<th>" + resultDataJson["previousItems"][3*i+1] + "</th>>";
        itemTableHTML += "<th>" + "<text id ='total" + i + "' action = '#'>" + resultDataJson["previousItems"][3*i+1] * resultDataJson["previousItems"][3*i+2] + "</text></th>";
        itemTableHTML += "</tr>";
    }

    itemTableBodyElement.append(itemTableHTML);
}

$.ajax({
    url: "api/shoppingcart",
    method: "GET",
    success: (resultData) => handleSessionData(resultData)
});

payment_button.click(toPayment);