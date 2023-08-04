let search_form = $("#search_form");
function handleSearchResult(resultData){
    console.log("handleSearchResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += "<th>" + '<a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">' + resultData[i]["movie_title"] + '</a>' + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
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
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}
function submitSearchForm(formSubmitEvent) {
    console.log("submit search form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    window.location.assign("index.html?"+search_form.serialize());
}

function populateTitles(){
    let titleElement = jQuery("#titles");

    let rowHTML = "";

    for(let i = 0; i < 10; i++) {

        rowHTML += '<a href="index.html?titleSearch=true&title=' + i + '">' + i + '</a>\t' ;
    }
    for(let i = 65; i < 91; i++) {

        rowHTML += '<a href="index.html?titleSearch=true&title=' + String.fromCharCode(i) + '">' + String.fromCharCode(i) + '</a>\t' ;
    }
    rowHTML += '<a href="index.html?titleSearch=true&title=*">*</a>\t' ;
    titleElement.append(rowHTML);
}

function handleGenreResult(resultData){
    let genreElement = jQuery("#genres");

    let rowHTML = "";

    for(let i = 0; i < resultData["genres"].length; i++) {

        rowHTML += '<a href="index.html?genre=' + resultData["genres"][i] + '">' + resultData["genres"][i] + '</a>\t' ;
    }
    genreElement.append(rowHTML);

}


jQuery.ajax({
   dataType: "json",
   method: "GET",
   url: "api/genres",
   success: (resultData) => handleGenreResult(resultData)
});

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    if(sessionStorage.getItem(query)!==null){
        console.log("using cached results");
        handleLookupAjaxSuccess(sessionStorage.getItem(query), query, doneCallback);
        return;
    }

    console.log("sending AJAX request to backend Java Servlet");

    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "movie-suggestion?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}

/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    // parse the string into JSON
    var jsonData = JSON.parse(data);
    console.log(jsonData)

    sessionStorage.setItem(query,data);

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    console.log("Handle selected suggestion");
    window.location.assign("single-movie.html?id=" + suggestion.data);
}

$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    minChars: 3
});

function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);

    window.location.assign("index.html?title="+query+"&fulltext=true");
}

$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})

populateTitles();
search_form.submit(submitSearchForm);