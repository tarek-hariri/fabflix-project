let star_form = $("#star_form");
let movie_form = $("#movie_form");
let metadata = $("#metadata");


function handleStarInsertResult(resultDataString){
    let resultDataJson = JSON.parse(resultDataString);
    if(resultDataJson["status"] === "failed"){
        $("#star_insert_error_message").text(resultDataJson["message"]);
    }
    else{
        $("#star_insert_error_message").text(resultDataJson["message"]);
    }
}
function submitStarForm(formSubmitEvent) {
    console.log("submit star form");
    formSubmitEvent.preventDefault();
    $.ajax({
        method: "POST",
        data: star_form.serialize(),
        url: "api/employeeactions",
        success: handleStarInsertResult})
}
function submitMovieForm(formSubmitEvent){
    console.log("submit movie form");
    formSubmitEvent.preventDefault();
    $.ajax({
        method: "POST",
        data: movie_form.serialize(),
        url: "api/employeeactions",
        success: handleMovieInsertResult})
}

function handleMovieInsertResult(resultDataString){
    let resultDataJson = JSON.parse(resultDataString);
    if(resultDataJson["status"] === "failed"){
        $("#movie_insert_error_message").text(resultDataJson["message"]);
    }
    else{
        $("#movie_insert_error_message").text(resultDataJson["message"]);
    }
}

function populateMetadata(resultData){
    let resultDataJson = JSON.parse(resultData);
    for(let i = 0; i < Object.keys(resultDataJson).length; i++){
        metadata.append("<h2>"+Object.keys(resultDataJson)[i]+"</h2><p>");
        for(let j = 0; j < Object.values(resultDataJson)[i].length; j++){
            metadata.append("    " + Object.values(resultDataJson)[i][j]["column_name"] + ": " + Object.values(resultDataJson)[i][j]["data_type"] + "<br>");
        }
    }
}

$.ajax({
    method: "GET",
    url: "api/employeeactions",
    success: (resultData) => populateMetadata(resultData)
})


star_form.submit(submitStarForm);
movie_form.submit(submitMovieForm);