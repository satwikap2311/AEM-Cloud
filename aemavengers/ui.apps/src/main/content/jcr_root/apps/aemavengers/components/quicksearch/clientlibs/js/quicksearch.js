$(document).on('click','.cmp-search__item',function(){
        $(this).addClass('selected');
        var itemTitle = $(this).find('.cmp-search__item-title').text();
        $('#searchInput').val(itemTitle);
    	$('.cmp-search__results').hide();
    	$('.cmp_search__info').hide();
});
$(document).on('click','.submit_btn',function(e){
    var totalItems = 0;
    var itemsPerPage = 3;
    var currentPage = 1;
    var totalItems = 0;
    var totalPages = 1;
    
   	var currentNodePath = $('#currentNodePath').val();
    var searchInput = $('#searchInput').val();
    var ajaxSel="/content/aemavengers/us/en";
    $('submit_btn').attr('id', searchInput);
         $.ajax({
            type: 'GET',
             url: ajaxSel+'.articleSearch.json',
              data: {
                    searchKeyword: searchInput,
                    listComponentNode: currentNodePath,
                    currentHit: "1"
                    },
            success: function(msg){
                var jsonArr = JSON.parse(msg);
                totalItems = jsonArr[0].articlesSize;
                updatePagination();
                loadPage(currentPage);
            }
        });
    function updatePagination() {
        totalPages = Math.ceil(totalItems / itemsPerPage);
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
        $.ajax({
            type: 'GET',
            url: ajaxSel + '.articleSearch.json',
            data: {
                searchKeyword: searchInput,
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
