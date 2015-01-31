//JSON Dummy Object
var dummyItem;
var dummyList;


function getList() {
	var listID = $('#listIDGet').val();

	$.ajax(
			{
				type : "GET",
				url  : "/cs480/list/" + listID,
				data : {
				},
				success : function(result) {
					$('#results').text(result);
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