$(document).on('click','.cmp-search__item',function(){
    console.log("item clicked");
    $('.cmp-search__item').removeClass('selected');
        
        // Add the 'selected' class to the clicked item
        $(this).addClass('selected');
        
        // Get the text content of the clicked item and put it into the search input field
        var itemTitle = $(this).find('.cmp-search__item-title').text();
        $('#searchInput').val(itemTitle);
});
$(document).on('click','.submit_btn',function(e){
    console.log("submit button","submit");
    var submit = $('submit_btn').val;
   	var currentNodePath = $('#currentNodePath').val();
    var searchInput = $('#searchInput').val();
    //var searchInput = $(this).find('.cmp-search__form').val;
    //var searchInput = $('#searchInput').attr('name');
    var ajaxSel="/content/aemavengers/us/en";
    console.log("searchKeyword",searchInput);
         $.ajax({
            type: 'GET',
             url: ajaxSel+'.articleSearch.json',
              data: {
                    //data: jsonString,
                    searchKeyword: searchInput,
                    listComponentNode: currentNodePath,
                    currentHit: "1"
                    },
            success: function(msg){
                console.log("msg",msg);
            }
        });
});