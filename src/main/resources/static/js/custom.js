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

function removeItem(listID, item, callback) {
    $.ajax(
            {
                type : "POST",
                url  : "/cs480/removeItem/" + listID +"/" + item,
                data : {
                },
                success : function(result) {
                	callback(listID);
                	return listID;
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
            url  : "/cs480/getList/" + listID,
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

function removeList(listID, callback)
{
	    $.ajax(
        {
            type : "POST",
            url  : "/cs480/deleteList/" + listID,
            data : {
            },
            success : function(result) {
            	callback(result);
                return result;
            },
            error: function (jqXHR, exception) {
                alert("Failed to remove list. Please check the inputs.");
            }
        });
}

function toggleCheckedStatus(index){


}

function addItemToListAJAX(listID, userName, itemName, price, quantity, isChecked, callback) {
    $.ajax(
        {
            type : "POST",
            url  : "/cs480/addItem/" + listID + "/" + userName,
            data : {
                "itemName" : itemName,
                "price" : price,
                "quantity": quantity,
                "isChecked": isChecked
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

//Modified Add item with callback
function addItem(listID, userName, callback) {
    var itemName = $('#itemName').val();
    var quantity = $('#itemQuantity').val();
    $.ajax(
        {
            type : "POST",
            url  : "/cs480/addItem/" + listID + "/" + userName,
            data : {
                "itemName" : itemName,
                "price" : -1,
                "quantity": quantity,
                "isChecked": false
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
    addItemToListAJAX("54cebe0d17ef75cddfb06a35","isaac","ISAAC APPLES",45,2,false, function(){
        console.log("inside testAdd() callback");
    })
}

function removeItem(listID, item, callback) {
    $.ajax(
            {
                type : "POST",
                url  : "/cs480/removeItem/" + listID +"/" + item,
                data : {
                },
                success : 	function(listID) {
                	callback(listID);
                	return listID;
                },
                error: function (jqXHR, exception) {
                    alert("Failed to remove item. Please check inputs.");
                }
            });
}

	function editItem(listID, itemName, userName, checked, price, callback)
	{
	        var newItemName = $('#' + itemName + 'newItemName').val();
	        var itemQuantity = $('#' + itemName + 'quantity').val();
	        //var price = $('#' + itemName + 'itemPrice').val();
			$.ajax(
	        {
	            type : "POST",
	            url  : "/cs480/editItem/" + listID + "/" + itemName,
	            data : {
	                "name" : newItemName,
	                "user" : userName,
	                "quantity": itemQuantity,
	                "price" : price,
	                "isChecked": false
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



