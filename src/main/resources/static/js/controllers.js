/**
 * Created with JetBrains WebStorm.
 * User: isaacsiegel
 * Date: 1/31/15
 * Time: 2:36 PM
 */

var REPLACE_WITH_SESSION_SAVED_OID =  "54cebe0d17ef75cddfb06a35";
angular.module('listView', []);

function itemsController($scope) {

    getItemsFromList(REPLACE_WITH_SESSION_SAVED_OID, function(result) {

        $scope.$apply(function(){

            $scope.itemList = result;

        })

    });

//    $scope.add = function(item) {
//        $scope.items.push(item);
//    };

}

