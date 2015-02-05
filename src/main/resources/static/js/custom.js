//JSON Dummy Object
var dummyItem;
var dummyList;


function getListTest() {
	var listID = $('#listIDGet').val();
    console.log(listID)

	$.ajax(
			{
				type : "GET",
				url  : "/cs480/list/" + listID,
				data : {
				},
				success : function(result) {
                    //console.log(result)
					$('#results').text(JSON.stringify(result));
				},
				error: function (jqXHR, exception) {
					alert("Failed to get list. Please check the inputs.");
				}
			});
}

function removeItem() {
	var listID = $('#listIDRemove').val();
	var item = $('#itemRemove').val();
	
  	$.ajax(
			{
				type : "POST",
				url  : "/cs480/list/" + listID +"/" + item,
				data : {
				},
				success : function(result) {
				//need to find a way to refresh list
				},
				error: function (jqXHR, exception) {
					alert("Failed to remove item. Please check inputs.");
				}
			});
}


//Returns list object of the given listID
function getList(listID, callback) {
    $.ajax(
        {
            type : "GET",
            url  : "/cs480/list/" + listID,
            data : {
            },
            success : function(result) {
            	callback(result);
                return result;
            },
            error: function (jqXHR, exception) {
                alert("Failed to get list. Please check the inputs.");
            }
        });
}

function getItemsFromList(listID, callback)
{
    getList(listID, function(result) {

        var items = result.items;
        console.log(items);
        callback(items);
        return items;
    });
}