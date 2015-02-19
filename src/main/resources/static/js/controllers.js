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
    loadUserData();

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
	function removeItemAndUpdate(listID, item) {
	    removeItem(listID, item, function(result) {
	    
	    	$scope.$apply(function(result){

		        pullListAndUpdate(result.listID);
		        
		    })
		    
	    });

	}
    $scope.removeItemAndUpdate = removeItemAndUpdate;
    
    
    //add item to list
    //Modified to streamline adding from page
    //May want to change to be a callback
    function addItemToList(listID, userName) {
	   //Calls addItem in custom.js
    	addItem(listID,userName, function(result) {

    		$scope.$apply(function(result) {
                
    			pullListAndUpdate(result.listID);
    			
    		})
    	});
    }
	$scope.addItemToList = addItemToList;

	//Edit item
	function editItemAndUpdate(listID, itemName, userName, checked, price){
	   //Calls editItem in custom.js
		editItem(listID, itemName, userName, checked, price, function(result) {
			$scope.$apply(function(result) {
			
				pullListAndUpdate(result.listID);
			})
			
		});
	}
    $scope.editItemAndUpdate = editItemAndUpdate;

    //Invite user
    function inviteUserToList(listID) {
		//Calls in
		inviteUser(listID, function(result){
		    $scope.$apply(function(result) {
		    
		        //?
		    })
		    
		});
    }
    $scope.inviteUserToList = inviteUserToList;

    function getUserLists(userID) {
        $.ajax(
            {
                type : "GET",
                url  : "/cs480/getUserLists/" + userID,
                data : {

                },
                success : function(result) {
                    //callback(result);
                    console.log("Results of getUserLists()")
                    console.log(result)

                    $scope.$apply(function() {
                        $scope.lists = result;
                        //Sets the first loaded page to the first user
                        //accessible list
                        pullListAndUpdate(result[0].oid);
                     
                    })


                    return result;
                },
                error: function (jqXHR, exception) {
                    alert("Failed to add item");
                }
            });
    }

    function loadUserData(){
        var _account = localStorage.getItem('_Account');
        //parse to Object Literal the JSON object
        if(_account) _account = JSON.parse(_account);
        //Checks whether the stored data exists
        if(_account) {
            console.log(_account)
            //TODO: call getUserLists(_account.UserOID), then populate angular $scope.lists with it
            console.log("hello workd from loadUserData")
            getUserLists(_account.UserOID)
            //Makes the User OID available to access by other funcitons
            $scope.UserOID = _account.UserOID;
            //(_account.User, _account.Pass);
            //If you want to delete the object
            //localStorage.removeItem('_Account');
           
        }
        else{
            alert("You have not logged in.")
        }
    }

	//Removes the list
	function removeListAndUpdate(listID){
		removeList(listID, function(result){
		    
		    $scope.$apply(function(result){
		    	//As we have deleted a list remotely, we have to
		    	//Change the lists locally
				//Reload lists from remote
				getUserLists(UserOID);	
	    	})
    	 });
     }
     $scope.removeListAndUpdate = removeListAndUpdate;

    function updateCheckedState(itemName, newState){
        console.log("Updating Checked State of: "+itemName +" -> "+newState)



    }
    $scope.updateCheckedState = updateCheckedState;
}



