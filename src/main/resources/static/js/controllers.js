/**
 * Created with JetBrains WebStorm.
 * User: isaacsiegel
 * Date: 1/31/15
 * Time: 2:36 PM
 */

var REPLACE_WITH_SESSION_SAVED_OID =  "54d6631c0fdcf8e36aa174d8";
var REFRESH_INTERVAL = 5000;

//Warning: Changing AUTOUPDATE flag will cause the list to autoupdate,
// which will use lots of GET AJAX calls
var AUTOUPDATE = false;

angular.module('listView', []);

function itemsController($scope) {

    //TODO pull list of lists, possibly oids, listNames, need to write AJAX call/api method
    $scope.lists = ['54d6631c0fdcf8e36aa174d8','54d6625a0fdcf0e2679092ad', '54cebe0d17ef75cddfb06a35']
    
    
    pullListAndUpdate(REPLACE_WITH_SESSION_SAVED_OID);

    if(AUTOUPDATE){
        setInterval(function(){
            pullListAndUpdate();


        },REFRESH_INTERVAL);
    }

    function pullListAndUpdate(listOID){
        getEntireList(listOID, function(result) {

            $scope.$apply(function(){

                $scope.itemList = result;
                $scope.listID = listOID;

            })

        });

    }
    
    function setCurrentList(listOID){
        console.log("setCurrentList()"+"Switching lists to: " + listOID)
        pullListAndUpdate(listOID)

    }

    $scope.setCurrentList = setCurrentList;


//    $scope.add = function(item) {
//        $scope.items.push(item);
//    };

    //Added remove item due to nodejs scope issues
	function removeItem(listID, item) {
	    $.ajax(
	            {
	                type : "POST",
	                url  : "/cs480/removeItem/" + listID +"/" + item,
	                data : {
	                },
	                success : function(result) {
	                	pullListAndUpdate(listID);
	                },
	                error: function (jqXHR, exception) {
	                    alert("Failed to remove item. Please check inputs.");
	                }
	            });
	}
    $scope.removeItem = removeItem;
    
    
    //add item to list
    //Modified to streamline adding from page
    function addItem(listID, userName, itemName, quantity) {
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
	                return result;
	            },
	            error: function (jqXHR, exception) {
	                alert("Failed to add item");
	            }
	        });
	}
	$scope.addItem = addItem;
}


