$(document).ready(function() {
    var currentNodePath = $('#currentNodePath').val();
    var ajaxSel = "/content/aemavengers/us/en";
    var itemsPerPage = 3; // Number of items to display per page
    var currentPage = 1;
    var totalItems = 0;
    var totalPages = 1;
    
    $.ajax({
        type: 'GET',
        url: ajaxSel + '.articlesList.json',
        data: {
            listComponentNode: currentNodePath
        },
        success: function(msg) {
            var jsonArr = JSON.parse(msg);
            totalItems = jsonArr[0].articlesSize;
            updatePagination();
            loadPage(currentPage);
        }
    });

    function updatePagination() {
        totalPages = Math.ceil(totalItems / itemsPerPage);
        $('.page').prop('disabled', true);
        $('#pages').text(currentPage);
        
        if (currentPage === 1) {
            $('#prev').prop('disabled', true);
        } else {
            $('#prev').prop('disabled', false);
        }
        if (currentPage === totalPages) {
            $('#next').prop('disabled', true);
        } else {
            $('#next').prop('disabled', false);
        }
    }

    function loadPage(page) {
        var searchInput = $('#searchInput').val();
         if (!searchInput) {
        $.ajax({
            type: 'GET',
            url: ajaxSel + '.articlesList.json',
            data: {
                listComponentNode: currentNodePath,
                currentHit: currentPage,
            },
            success: function(jsonData) {
                var jsonArr = JSON.parse(jsonData);
                var cmpList = document.querySelector('.cmp-list');
                while (cmpList.firstChild) {
                    cmpList.removeChild(cmpList.firstChild);
                }
                $.each(jsonArr, function (i, item) {
                    var itemContainer = document.createElement('div');
                    itemContainer.className = 'cmp-list__item';

                    if(i != 0){
                    var itemHTML = `
                        <h2 class="cmp-teaser__title">
                            <a class="cmp-teaser__title-link"
                               data-sly-attribute="${jsonArr[i].pagePath}">${jsonArr[i].pageTitle}</a>
                        </h2><br/>
                        <span style="font-size: 14px; color: green;">Author: ${jsonArr[i].author}</span> <br/>
                        <span style="font-size: 14px; color: green;">Date: ${jsonArr[i].date}</span><br/>
                        <a href="${jsonArr[i].pagePath}.html?wcmmode=disbaled"><span style="font-size: 12px; color: blue; text-align: right;">Read more</span></a>
                    `;

                    // Set the HTML content for the item container
                    itemContainer.innerHTML = itemHTML;

                    // Append the item container to the cmpList
                    cmpList.appendChild(itemContainer);
                    }
                });

            },
            error: function(xhr, status, error) {
                console.error(error);
            }
        });
        }
    }

    $('#prev').click(function() {
        if (currentPage > 1) {
            currentPage--;
            loadPage(currentPage);
            updatePagination();
        }
    });

    $('#next').click(function() {
        if (currentPage < totalPages) {
            currentPage++;
            loadPage(currentPage);
            updatePagination();
        }
    });

});
$(document).on('click','.filter_btn',function(e){
    var fitemsPerPage = 3; // Number of items to display per page
    var fcurrentPage = 1;
    var ftotalItems = 0;
    var ftotalPages = 1;
    var selectedValues = [];
            var checkboxes = document.querySelectorAll('input[type="checkbox"]:checked');

            checkboxes.forEach(function (checkbox) {
                selectedValues.push(checkbox.getAttribute('name'));
            });

            var facets = selectedValues.join(',');

    console.log("selectedValuesString",facets);
    selector = "articlesList";
    var ajaxSel = "/content/aemavengers/us/en";
    var searchInput = $('#searchInput').val();
    var currentNodePath = $('#currentNodePath').val();
    $.ajax({
        type: 'GET',
        url: ajaxSel + '.articlesList.json',
        data: {
            facets: facets,
            listComponentNode: currentNodePath,
            searchKeyword: searchInput,
            currentHit: "1"
        },
        success: function(msg) {
            var jsonArr = JSON.parse(msg);
            ftotalItems = jsonArr[0].articlesSize;
            updatePagination();
            loadPage(fcurrentPage);
        }
    });
    function updatePagination() {
        ftotalPages = Math.ceil(ftotalItems / fitemsPerPage);
        $('#pages').text(fcurrentPage);
        
        if (fcurrentPage === 1) {
            $('#prev').prop('disabled', true);
        } else {
            $('#prev').prop('disabled', false);
        }

        if (fcurrentPage === ftotalPages) {
            $('#next').prop('disabled', true);
        } else {
            $('#next').prop('disabled', false);
        }
    }
    
    function loadPage(page) {
        var searchInput = $('#searchInput').val();
         if (!searchInput) {
        $.ajax({
            type: 'GET',
            url: ajaxSel + '.articlesList.json',
            data: {
                facets: facets,
                listComponentNode: currentNodePath,
                searchKeyword: searchInput,
                currentHit: fcurrentPage,
            },
            success: function(jsonData) {
                var jsonArr = JSON.parse(jsonData);
                var cmpList = document.querySelector('.cmp-list');
                while (cmpList.firstChild) {
                    cmpList.removeChild(cmpList.firstChild);
                }
                $.each(jsonArr, function (i, item) {
                    // Create a container for each item
                    var itemContainer = document.createElement('div');
                    itemContainer.className = 'cmp-list__item';

                    // Create the HTML content based on the item's data
                    if(i != 0){
                    var itemHTML = `
                        <h2 class="cmp-teaser__title">
                            <a class="cmp-teaser__title-link"
                               data-sly-attribute="${jsonArr[i].pagePath}">${jsonArr[i].pageTitle}</a>
                        </h2><br/>
                        <span style="font-size: 14px; color: green;">Author: ${jsonArr[i].author}</span> <br/>
                        <span style="font-size: 14px; color: green;">Date: ${jsonArr[i].date}</span><br/>
                        <a href="${jsonArr[i].pagePath}.html?wcmmode=disbaled"><span style="font-size: 12px; color: blue; text-align: right;">Read more</span>
                    `;

                    itemContainer.innerHTML = itemHTML;
                    cmpList.appendChild(itemContainer);
                    }
                });

            },
            error: function(xhr, status, error) {
                console.error(error);
            }
        });
        }
    }

    $('#prev').click(function() {
        if (fcurrentPage > 1) {
            fcurrentPage--;
            loadPage(fcurrentPage);
            updatePagination();
        }
    });

    $('#next').click(function() {
        if (fcurrentPage < ftotalPages) {
            fcurrentPage++;
            loadPage(fcurrentPage);
            updatePagination();
        }
    });

});