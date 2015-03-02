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

var app = angular.module('listView', [])

.controller('itemsController', ['$scope',function($scope) {
//function itemsController($scope) {
    loadUserData();

    //TODO pull list of lists, possibly oids, listNames, need to write AJAX call/api method
    $scope.lists = ['54d6631c0fdcf8e36aa174d8','54d6625a0fdcf0e2679092ad', '54cebe0d17ef75cddfb06a35']
    $scope.editingItem = "";
    
    pullListAndUpdate(REPLACE_WITH_SESSION_SAVED_OID);

    initializeVoiceCommand();





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
            setupCheckboxes();

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
    function addItemToList() {
	   //Calls addItem in custom.js
    	addItem($scope.listID,$scope.UserOID, function(result) {

    		$scope.$apply(function(result) {
                
    			pullListAndUpdate(result.listID);
    			
    		})
    	});
    }
	$scope.addItemToList = addItemToList;

    //add item to list
    //Modified to streamline adding from page
    //May want to change to be a callback
    function addItemToListVC(listID, userName, itemName) {
        //Calls addItem in custom.js
        addItemVC(listID,userName, itemName, function(result) {

            $scope.$apply(function(result) {

                pullListAndUpdate(result.listID);

            })
        });
    }
    $scope.addItemToListVC = addItemToListVC;





    //Edit item
	function editItemAndUpdate(listID, itemName, oldItemName, userName, checked, price, quantity){
	   //Calls editItem in custom.js
		editItem(listID, itemName, oldItemName, userName, checked, price, quantity, function(result) {
			$scope.$apply(function(result) {
			
				pullListAndUpdate(result.listID);

            })

			
		});
        $('#editItemModal').modal('hide')

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

    function getUserLists(userID, callback) {
        //Calls get User list
        getUserListsAJAX(userID, function(result){
                var list = result;
                console.log("new var from callback is")
                console.log(list)
              $scope.$apply(
                $scope.lists = list
              )
                              callback(list);

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
            getUserLists(_account.UserOID, function(result){
                console.log(result)
                pullListAndUpdate(result[0].oid);
            });
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
	function removeListAndUpdate(){
		removeList($scope.listID, function(result){
		    
		    $scope.$apply(function(result){
		    	//As we have deleted a list remotely, we have to
		    	//Change the lists locally
				//Reload lists from remote
				getUserLists($scope.UserOID, function(result){
                    pullListAndUpdate(result[0].oid);
	    	    })
    	   });
        });
    }
     $scope.removeListAndUpdate = removeListAndUpdate;
    
    //Changes checked value of given item from checkbox input
    function updateCheckedState(item, newState){
        console.log("Updating Checked State of: "+item.name +" -> "+newState)
        item.isChecked = newState;
        updateItemCheckedState($scope.listID, $scope.UserOID, item);

    }
    $scope.updateCheckedState = updateCheckedState;
    
    //Clears the inputs for the username
    function resetUserName() {
    	$scope.userIDField = "";
    }
    $scope.resetUserName = resetUserName;
    
    //Clears the inputs for add item
    function resetItemAdd() {
    	$scope.itemNameInput = "";
    	$scope.itemQuantityInput = "";
    }
    $scope.resetItemAdd = resetItemAdd;

    function showEditModal(listID, itemName, userName, checked, price, quantity){
        console.log(listID)
        console.log(itemName)
        console.log(userName)
        console.log(checked)
        console.log(price)



        //Prepare Modal Fields

        //$scope.$apply(function() {
            $scope.editField_oldItemName  = itemName;

            $scope.editField_itemName = itemName;
            $scope.editField_price = price;
            $scope.editField_quantity  = quantity;
            $scope.editField_isChecked = checked;

            //$scope.editingItem.quantity = 4;

        //})

        //Show Modal
        $('#editItemModal').modal('show');

    }
    $scope.showEditModal = showEditModal;

	function getHistoryAndUpdate()
	{
	   
		getHistory($scope.listID, function(result){
		    //If we want to redraw the result
		    $scope.$apply(function(result){
		        $itemHistory = result.itemHistory;
		        pullListAndUpdate(result.listID);

	    	})
    	 });
	}
	$scope.getHistoryAndUpdate = getHistoryAndUpdate;
    
    function createNewListAndUpdate()
    {   //Upon creation of new list we go to it
    	createList($scope.UserOID, function(result){
            $scope.$apply(function(result){
                getUserLists($scope.UserOID, function(result){
                    pullListAndUpdate(result[result.length -1].oid);
                });
            });
    	});
    }
    $scope.createNewListAndUpdate = createNewListAndUpdate;
    
	function removeAllCheckedItemsAndUpdate()
	{
	    removeAllCheckedItems($scope.listID, $scope.itemList, function(result){
	        //We want to redraw the list with updated items
	        $scope.$apply(function(result){
	            pullListAndUpdate($scope.listID);
	         })
	    });
	}
	$scope.removeAllCheckedItemsAndUpdate = removeAllCheckedItemsAndUpdate;

    function changeItemQuantityByOne(listID, UserOID, item, upOrdown)
    {
       if(upOrdown == 1)
       {
          editItem(listID, item.name, item.name, UserOID, item.isChecked, item.price, item.quantity + 1, function(result) {
			$scope.$apply(function(result) {
			
				pullListAndUpdate(result.listID);

            })
          });		
       }
       else
       {
          editItem(listID, item.name, item.name, UserOID, item.isChecked, item.price, item.quantity - 1, function(result) {
			$scope.$apply(function(result) {
			
				pullListAndUpdate(result.listID);

            })	
           });
       }
       
    } 
    
    $scope.changeItemQuantityByOne = changeItemQuantityByOne; 

    function initializeVoiceCommand(){
        if (annyang) {
            var commands = {
                'add item *item': function(item) {
                    console.log("Voice Command Detected:" + "Adding item: "+ item)
                    addItemToListVC($scope.listID,$scope.UserOID, item);
                    resetItemAdd();
                }
            };

            // Add our commands to annyang
            annyang.addCommands(commands);

            // Start listening. You can call this here, or attach this call to an event, button, etc.
            annyang.start();
        }
    }
    
    function costSplit()
    {
           $.ajax(
            {
                type : "GET",
                url  : "/cs480/splitCostOfList/" + $scope.listID,
                data : {

                },
                success : function(result) {
                    //callback(result);
                    console.log("Results of Cost Split")
                    console.log(result)
                    angular.element('#costSplit').text("Your part is $" +Number(result).toFixed(2));
                    return result;
                },
                error: function (jqXHR, exception) {
                    alert("Failed to cost split");
                }
            });
    }
    $scope.costSplit = costSplit;
    
    }]);




