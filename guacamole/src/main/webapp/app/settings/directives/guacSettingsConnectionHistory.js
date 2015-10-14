/*
 * Copyright (C) 2015 Glyptodon LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * A directive for viewing connection history records.
 */
angular.module('settings').directive('guacSettingsConnectionHistory', [function guacSettingsConnectionHistory() {
        
    return {
        // Element only
        restrict: 'E',
        replace: true,

        scope: {
        },

        templateUrl: 'app/settings/templates/settingsConnectionHistory.html',
        controller: ['$scope', '$injector', function settingsConnectionHistoryController($scope, $injector) {
                
            // Get required types
            var SortOrder = $injector.get('SortOrder');

            // Get required services
            var $routeParams   = $injector.get('$routeParams');
            var $translate     = $injector.get('$translate');
            var historyService = $injector.get('historyService');

            /**
             * The identifier of the currently-selected data source.
             *
             * @type String
             */
            $scope.dataSource = $routeParams.dataSource;

            /**
             * The identifier of the currently-selected data source.
             *
             * @type String
             */
            $scope.historyRecords = null;

            /**
             * The search terms to use when filtering the history records.
             *
             * @type String
             */
            $scope.searchString = '';

            /**
             * The date format for use for start/end dates.
             *
             * @type String
             */
            $scope.dateFormat = null;

            /**
             * SortOrder instance which stores the sort order of the history
             * records.
             *
             * @type SortOrder
             */
            $scope.order = new SortOrder([
                'username',
                'startDate',
                'endDate',
                'connectionName'
            ]);

            // Get session date format
            $translate('SETTINGS_CONNECTION_HISTORY.FORMAT_DATE')
            .then(function dateFormatReceived(retrievedDateFormat) {

                // Store received date format
                $scope.dateFormat = retrievedDateFormat;

            });
            
            /**
             * Returns true if the connection history records have been loaded,
             * indicating that information needed to render the page is fully 
             * loaded.
             * 
             * @returns {Boolean} 
             *     true if the history records have been loaded, false
             *     otherwise.
             * 
             */
            $scope.isLoaded = function isLoaded() {
                return $scope.historyRecords !== null
                    && $scope.dateFormat     !== null;
            };
            
            /**
             * Query the API for the connection record history, filtered by 
             * searchString, and ordered by order.
             */
            $scope.search = function search() {

                // Clear current results
                $scope.historyRecords = null;

                // Fetch history records
                historyService.getConnectionHistory(
                    $scope.dataSource, 
                    $scope.searchString.split(/\s+/),
                    $scope.order.predicate
                )
                .success(function historyRetrieved(historyRecords) {

                    // Store retrieved permissions
                    $scope.historyRecords = historyRecords;

                });

            };
            
            // Initialize search results
            $scope.search();
            
        }]
    };
    
}]);
