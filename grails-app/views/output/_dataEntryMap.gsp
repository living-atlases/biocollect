<g:set var="orientation" value="${orientation ?: 'horizontal'}"/>

<g:if test="${orientation == 'horizontal'}">
    <div class="span6">
        <m:map id="${source}Map" width="100%"/>
    </div>
</g:if>

<div class="${orientation == 'horizontal' ? 'span6' : 'row-fluid'}" data-bind="visible: activityLevelData.pActivity.sites.length > 1">
    <div class="well">
        <div class="span12">
            <div class="span3">
                <label for="siteLocation">${readonly ? 'Location:' : 'Select a location'}</label>
            </div>

            <div class="span9">
                <g:if test="${readonly}">
                    <span class="output-text" data-bind="text: data.${source}Name() "></span>
                </g:if>
                <g:else>
                    <select id="siteLocation"
                            data-bind='options: activityLevelData.pActivity.sites, optionsText: "name", optionsValue: "siteId", value: data.${source}, optionsCaption: "Choose a site...", disable: ${readonly}'></select>
                </g:else>
            </div>
        </div>
    </div>
</div>

<g:if test="${orientation == 'vertical'}">
    <div class="row-fluid margin-bottom-1">
        <m:map id="${source}Map" width="100%"/>
    </div>
</g:if>

<div class="${orientation == 'horizontal' ? 'span6' : 'row-fluid'}">
    <div class="well">
        <div class="row-fluid">
            <div class="span3">
                <label for="${source}Latitude">Latitude<g:if test="${validation?.contains('required')}"><i class="req-field"></i></g:if></label>
            </div>

            <div class="span9">
                <g:if test="${readonly}">
                    <span data-bind="text: data.${source}Latitude"></span>
                </g:if>
                <g:else>
                    <input id="${source}Latitude" type="text" data-bind="value: data.${source}Latitude"
                        ${validation}>
                </g:else>
            </div>
        </div>

        <div class="row-fluid">
            <div class="span3">
                <label for="${source}Longitude">Longitude<g:if test="${validation?.contains('required')}"><i class="req-field"></i></g:if></label>
            </div>

            <div class="span9">
                <g:if test="${readonly}">
                    <span data-bind="text: data.${source}Longitude"></span>
                </g:if>
                <g:else>
                    <input id="${source}Longitude" type="text" data-bind="value: data.${source}Longitude"
                        ${validation}>
                </g:else>
            </div>
        </div>

        <g:if test="${includeAccuracy}">
            <div class="row-fluid">
                <div class="span3">
                    <label for="${source}Accuracy">Accuracy (metres)</label>
                </div>

                <div class="span9">
                    <g:if test="${readonly}">
                        <span data-bind="text: data.${source}Accuracy"></span>
                    </g:if>
                    <g:else>
                        <select data-bind="options: ['',0, 10, 50, 100, 500, 1000, 5000, 10000]
                           optionsCaption: 'Choose one...',
                           value: data.${source}Accuracy,
                           valueAllowUnset: true">
                        </select>
                    </g:else>
                </div>
            </div>
        </g:if>
        <g:if test="${includeSource}">
            <div class="row-fluid">
                <div class="span3">
                    <label for="${source}Source">Source of coordinates</label>
                </div>

                <div class="span9">
                    <g:if test="${readonly}">
                        <span data-bind="text: data.${source}Source"></span>
                    </g:if>
                    <g:else>
                        <select data-bind="options: ['', 'Google maps', 'Google earth', 'GPS device', 'camera/phone', 'physical maps', 'other']
                           optionsCaption: 'Choose one...',
                           value: data.${source}Source,
                           valueAllowUnset: true"></select>
                    </g:else>
                </div>
            </div>
        </g:if>
        <g:if test="${includeLocality}">
            <div class="row-fluid">
                <div class="span3">
                    <label for="${source}Locality">Matched locality</label>
                </div>

                <div class="span9">
                    <div class="span9">
                        <g:if test="${readonly}">
                            <textarea id="${source}Locality" type="text" data-bind="value: data.${source}Locality" readonly></textarea>
                        </g:if>
                        <g:else>
                            <form class="form-inline">
                                <textarea id="${source}Locality" type="text" data-bind="value: data.${source}Locality"></textarea>
                                <button id="reverseGeocodeLocality" class="btn btn-default">Search for locality match</button>
                            </form>
                        </g:else>
                    </div>
                </div>
            </div>
        </g:if>
        <g:if test="${includeNotes}">
            <div class="row-fluid">
                <div class="span3">
                    <label for="${source}Notes">Location notes</label>
                </div>

                <div class="span9">
                    <div class="span9">
                        <g:if test="${readonly}">
                            <textarea id="${source}Notes" type="text" data-bind="text: data.${source}Notes" readonly></textarea>
                        </g:if>
                        <g:else>
                            <textarea id="${source}Notes" type="text" data-bind="value: data.${source}Notes"></textarea>
                        </g:else>
                    </div>
                </div>
            </div>
        </g:if>

        <g:if test="${includeLocality}">
            <div class="row-fluid">
                <g:if test="${!readonly}">
                    <div class="span3">
                        <label for="bookmarkedLocations">Saved locations</label>
                    </div>
                    <div class="span9">
                        <form class="form-inline">
                            <select name="bookmarkedLocations" id="bookmarkedLocations" class="form-control ">
                                <option value="">-- saved locations --</option>
                            </select>
                            <button id="saveBookmarkLocation" class="btn btn-default">Save this location</button>
                        </form>
                    </div>
                </g:if>
            </div>
        </g:if>
    </div>
</div>
<r:script>

    $(function () {
        loadBookmarks();

        // Save current location
        $('#reverseGeocodeLocality').click(function (e) {
            e.preventDefault();
            reverseGeocode()
        });

        // Save current location
        $('#saveBookmarkLocation').click(function (e) {
            e.preventDefault();
            var bookmark = {
                locality: $('#${source}Locality').val(),
                decimalLatitude: Number($('#${source}Latitude').val()),
                decimalLongitude: Number($('#${source}Longitude').val())
            };

            $.ajax({
                url: "${createLink(controller:"ajax", action:"saveBookmarkLocation")}",
                dataType: 'json',
                type: 'POST',
                data: JSON.stringify(bookmark),
                contentType: 'application/json; charset=utf-8'
            }).done(function (data) {
                if (data.error) {
                    bootbox.alert("Location could not be saved - " + data.error, 'Error');
                } else {
                    // reload bookmarks
                    bootbox.alert("Location was saved");
                    loadBookmarks();
                }
            }).fail(function (jqXHR, textStatus, errorThrown) {
                bootbox.alert("Error: " + textStatus + " - " + errorThrown);
            });
        });

        // Trigger loading of bookmark on select change
        $('#bookmarkedLocations').change(function (e) {
            e.preventDefault();
            var location;
            var id = $(this).find("option:selected").val();

            if (id && id != 'error') {
                $.each(bookmarks, function (i, el) {
                    if (id == el.locationId) {
                        location = el;
                    }
                });

                if (location) {
                    updateLocation(location.decimalLatitude, location.decimalLongitude, location.locality)
                } else {
                    bootbox.alert("Error: bookmark could not be loaded.");
                }
            } else if (id == 'error') {
                loadBookmarks();
            }
        });

        function loadBookmarks() {
            $.ajax({
                url: "${createLink(controller:"ajax", action:"getBookmarkLocations")}",
                dataType: 'json',
            }).done(function (data) {
                if (data.error) {
                    bootbox.alert("Bookmark could not be loaded - " + data.error, 'Error');
                } else {
                    // reload bookmarks
                    bookmarks = data; // cache json
                    // inject values into select widget
                    $('#bookmarkedLocations option[value != ""]').remove(); // clear list if already loaded
                    $.each(data, function(i, el) {
                        $('#bookmarkedLocations').append('<option value="' + el.locationId + '">' + el.locality + '</option>');
                    });
                }
            }).fail(function( jqXHR, textStatus, errorThrown ) {
                //alert("Error: " + textStatus + " - " + errorThrown);
                bootbox.alert("Error: bookmarks could not be loaded at this time. " + textStatus + " - " + errorThrown);
                $('#bookmarkedLocations').append('<option value="error">Error: bookmarks could not be loaded at this time. Select to retry.</option>');
            });
        }

        function updateLocation(lat, lng, locality, keepView) {
            $('#${source}Locality').val(locality)
            $('#${source}Latitude').val(lat)
            $('#${source}Longitude').val(lng)
            $('#${source}Locality').change()
            $('#${source}Latitude').change()
            $('#${source}Longitude').change()
        }

        /**
         * Get address for a given lat/lng using openstreetmap
         */
        function reverseGeocode() {
            var lat = $('#${source}Latitude').val();
            var lng = $('#${source}Longitude').val();
            $.ajax({
                url: 'http://nominatim.openstreetmap.org/reverse?format=json&zoom=18&addressdetails=1' + '&lat=' + lat + '&lon=' + lng,
                dataType: 'json',
            }).done(function (data) {
                console.log(data)
                if (!data.error) {
                    $('#${source}Locality').val(data.display_name)
                    $('#${source}Locality').change()
                }
            });
        }
    })
</r:script>
