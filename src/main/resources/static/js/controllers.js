/**
 * Created with JetBrains WebStorm.
 * User: isaacsiegel
 * Date: 1/31/15
 * Time: 2:36 PM
 */

var REPLACE_WITH_SESSION_SAVED_OID =  "54cebe0d17ef75cddfb06a35";
var REFRESH_INTERVAL = 5000;

//Warning: Changing AUTOUPDATE flag will cause the list to autoupdate,
// which will use lots of GET AJAX calls
var AUTOUPDATE = false;

angular.module('listView', []);

function itemsController($scope) {
    pullListAndUpdate();

    if(AUTOUPDATE){
        setInterval(function(){
            pullListAndUpdate();


        },REFRESH_INTERVAL);
    }

    function pullListAndUpdate(){
        getEntireList(REPLACE_WITH_SESSION_SAVED_OID, function(result) {

            $scope.$apply(function(){

                $scope.itemList = result;

            })

        });

    }



//    $scope.add = function(item) {
//        $scope.items.push(item);
//    };

}


