let add_button = $("#add_to_cart");
let movie_title = "";

function addToCart(){
    let price =  movie_title.charCodeAt(0).toString();
    console.log("item costing" + price + "added to cart");
    jQuery.ajax({
        url: "api/shoppingcart",
        data: {title : movie_title, price : price},
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

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");

    movie_title = resultData[0]["movie_title"];

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>Movie Title: " + resultData[0]["movie_title"] + "</p>" +
        "<p>Year: " + resultData[0]["movie_year"] + "</p>" +
        "<p>Director: " + resultData[0]["movie_director"] + "</p>" +
        "<p>Rating: " + resultData[0]["movie_rating"] + "</p>");

    console.log("handleResult: populating star info from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let starInfoElement = jQuery("#star_info");

    let starHTML = "Stars: ";

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData[0]["movie_stars"].length/2; i++) {
        starHTML += '<a href="single-star.html?id=' + resultData[0]["movie_stars"][2*i] + '">' + resultData[0]["movie_stars"][2*i+1] + '</a>';
        if(i != resultData[0]["movie_stars"].length/2-1){
            starHTML += ', ';
        }
    }
    starInfoElement.append(starHTML);

    let genreInfoElement = jQuery("#genre_info");

    let genreHTML = "Genres: ";

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData[0]["movie_genres"].length; i++) {
        genreHTML += '<a href="index.html?genre=' + resultData[0]["movie_genres"][i] + '">' + resultData[0]["movie_genres"][i] + '</a>';
        if(i != resultData[0]["movie_genres"].length-1){
            genreHTML += ', ';
        }
    }
    genreInfoElement.append(genreHTML);

    let mainInfoElement = jQuery("#main_info");
    let movieListURL = window.sessionStorage.getItem("movieListURL");
    let mainHTML = '<a href='+ movieListURL + '>Back to Movie List</a>';
    mainInfoElement.append(mainHTML);
}

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by SingleMovieServlet in SingleMovieServlet.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleMovieServlet
});

add_button.click(addToCart);