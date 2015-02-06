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

function getEntireList(listID, callback)
{
    getList(listID, function(result) {

        var list = result;
        console.log(list);
        callback(list);
        return list;
    });
}

function toggleCheckedStatus(index){


}

function addItemToListAJAX(listID, userName, itemName, price, quantity, callback) {
    $.ajax(
        {
            type : "POST",
            url  : "/cs480/addItem/" + listID + "/" + userName,
            data : {
                "itemName" : itemName,
                "price" : price,
                "quantity": quantity
            },
            success : function(result) {
                callback(result);
                return result;
            },
            error: function (jqXHR, exception) {
                alert("Failed to add item");
            }
        });
}

function testAdd(){
    addItemToListAJAX("54cebe0d17ef75cddfb06a35","isaac","ISAAC APPLES",45,2, function(){
        console.log("inside testAdd() callback");
    })
}

