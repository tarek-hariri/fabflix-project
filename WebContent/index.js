let search_type = $("#searchtype");
let next_button = $("#next");
let prev_button = $("#previous");

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function buttonPressed(buttonId, price){
    console.log("item costing" + price + "added to cart");
    jQuery.ajax({
        url: "api/shoppingcart",
        data: {title : buttonId, price : price},
        method: "POST",
        success : (resultData) => handleAlert(resultData)
    })
    function handleAlert(resultData){
        let resultDataJson = JSON.parse(resultData);

        if(resultDataJson["failed"]===("false"))
            window.alert("Added to cart!");
        else
            window.alert("Failed to add to cart.");
    }
}

function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += "<th>" + '<a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">' + resultData[i]["movie_title"] + '</a>' + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>";
        for(let j = 0; j < (Object.keys(resultData[i]["movie_genres"]).length); j++){
            rowHTML +=
                '<a href="index.html?genre=' + resultData[i]["movie_genres"][j] + '">' + resultData[i]["movie_genres"][j] + '</a>';
            if(j !=(Object.keys(resultData[i]["movie_genres"]).length-1)){
                rowHTML += ', ';
            }
        }
        rowHTML+= "</th>";
        rowHTML += "<th>";
        for(let j = 0; j < (Object.keys(resultData[i]["movie_stars"]).length)/2; j++){
            rowHTML +=
                '<a href="single-star.html?id=' + resultData[i]["movie_stars"][2*j] + '">' + resultData[i]["movie_stars"][2*j+1] + '</a>';
            if(j !=(Object.keys(resultData[i]["movie_stars"]).length)/2-1){
                rowHTML += ', ';
            }
        }
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "<th> <button id = '"+ resultData[i]["movie_title"] + "' action = '#' onClick = 'buttonPressed(this.id, " + Math.floor(Math.random() * (100 - 1 + 1) + 1) + ")'>Add</button></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

function goNext(){
    const params = new URLSearchParams(window.location.search);
    if(params.has('page'))
        params.set('page',parseInt(params.get('page'))+1);
    else
        params.set('page',2);
    window.location.assign("index.html?"+params.toString());
}
function goPrev(){
    const params = new URLSearchParams(window.location.search);
    if(params.has('page')&&parseInt(params.get('page'))>1)
        params.set('page',parseInt(params.get('page'))-1);
    window.location.assign("index.html?"+params.toString());
}

function submitSearchForm(formSubmitEvent) {
    console.log("submit search form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    const searchType = new URLSearchParams(window.location.search).has('selectorder');
    if(searchType){
        search_type_array = search_type.serializeArray();
        let newURL = new URLSearchParams(window.location.search);
        for (let i=0; i<search_type_array.length; i++) {
            if (search_type_array[i].name === "selectorder") {
                newURL.set('selectorder', search_type_array[i].value);
            }
            else if (search_type_array[i].name === "numresults") {
                newURL.set('numresults', search_type_array[i].value);
            }
        }
        window.location.assign("index.html?"+newURL.toString());
    }
    else {
        // If sorting by default page length and apply sorting filter, go back to page 1 of the newly applied filter.
        let newURL = new URLSearchParams(window.location.search);
        newURL.set('page', 1);
        window.location.assign('index.html?'+ newURL.toString() + "&" + search_type.serialize());
    }
}

// Makes the HTTP GET request and registers on success callback function handleMovieResult

let url = new URL(window.location.href);

window.sessionStorage.setItem("movieListURL",window.location.href);

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by MoviesServlet in Movies.java
    data: {page : url.searchParams.get("page"), numResults: url.searchParams.get("numresults"), selectOrder: url.searchParams.get("selectorder"), titleSearch: url.searchParams.get("titleSearch"), title : url.searchParams.get("title"), year : url.searchParams.get("year"), director : url.searchParams.get("director"), star : url.searchParams.get("star"), genre : url.searchParams.get("genre"), fulltext : url.searchParams.get("fulltext")},
    success: (resultData) => handleMovieResult(resultData)
});



search_type.submit(submitSearchForm);
next_button.click(goNext);
prev_button.click(goPrev);
