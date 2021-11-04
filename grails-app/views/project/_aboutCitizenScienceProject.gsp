<div class="container-fluid" id="cs-about">
    <button class="accordion-header" type="button" data-toggle="collapse" data-target="#cs-about-section1" aria-expanded="false" aria-controls="cs-about-section1">
        <h4>${hubConfig.getTextForAboutTheProject(grailsApplication.config.content.defaultOverriddenLabels)}</h4>
    </button>
    <div class="accordion-body collapse show" id="cs-about-section1">
        <div data-bind="if:isBushfire()" class="my-2">
            <div class="alert alert-success">
                <span class="fa fa-fire"></span> <g:message code="project.bushfireInfo"/>
            </div>
        </div>
        <h4>${hubConfig.getTextForAim(grailsApplication.config.content.defaultOverriddenLabels)}</h4>
        <p data-bind="text:aim"></p>
        <h4>${hubConfig.getTextForDescription(grailsApplication.config.content.defaultOverriddenLabels)}</h4>
        <p data-bind="html:description.markdownToHtml()"></p>

        <g:if test="${hubConfig?.content?.hideProjectAboutOriginallyRegistered != true}">
            <!-- ko if: origin -->
                <h4><g:message code="project.display.origin" /></h4>
                <p class="text-small-heading"> <!-- ko text:origin --> <!-- /ko --></p>
            <!-- /ko -->
        </g:if>


        <g:if test="${hubConfig?.content?.hideProjectAboutContributing != true}">
            <p data-bind="visible:!isExternal()" class="my-2">
                <img src="${asset.assetPath(src: "ala-logo-small.png")}" class="logo-icon" alt="Atlas of Living Australia logo"><g:message code="project.contributingToALA"/>
            </p>
        </g:if>
    </div>
    <button class="accordion-header" type="button" data-toggle="collapse" data-target="#cs-about-section2" aria-expanded="false" aria-controls="cs-about-section2">
        <h4 data-bind="visible:projectType() == 'survey'"><g:message code="project.display.involved" /></h4>
        <h4 data-bind="visible:projectType() != 'survey'">
            ${hubConfig.getTextForProjectInformation(grailsApplication.config.content.defaultOverriddenLabels)}
        </h4>
    </button>
    <div class="accordion-body collapse show" id="cs-about-section2">
        <p class="mb-3" data-bind="visible:getInvolved, html:getInvolved.markdownToHtml()">
        </p>
        <div class="row">
            <div class="col-12 col-md-6">
                <!-- ko if:externalId -->
                    <h4 class="text-small-heading"><g:message code="project.display.externalId" /></h4>
                    <p data-bind="text:externalId"></p>
                <!-- /ko -->
                <!-- ko if:grantId -->
                    <h4 class="text-small-heading"><g:message code="project.display.grantId" /></h4>
                    <p data-bind="text:grantId"></p>
                <!-- /ko -->
                <!-- ko if:funding -->
                    <h4 class="text-small-heading"><g:message code="project.display.fundingValue" /></h4>
                    <p data-bind="text:funding.formattedCurrency"></p>
                    <g:if test="project.fundings">
                        <p><a href="#" data-toggle="modal" data-target="#fundingDetails"><i class="fas fa-th-list"></i></a></p>
                    </g:if>
                <!-- /ko -->

                <g:if test="project.fundings">
                    <div class="modal fade"  id="fundingDetails" tabindex="-1" role="dialog" aria-labelledby="Funding" aria-hidden="true">
                        <div class="modal-dialog" role="document">
                            <div class="modal-content">
                                <div class="modal-body">
                                    <table class="table"><thead><td>Funding Source</td><td>Funding Type</td><td>Funding Amount</td></thead>
                                        <tbody>
                                        <!-- ko foreach: fundings -->
                                        <tr >
                                            <td ><span data-bind="text: fundingSource"></span></td>
                                            <td ><span data-bind="text: fundingType"></span></td>
                                            <td ><span data-bind="text: fundingSourceAmount.formattedCurrency"></span></td>
                                        </tr>
                                        <!-- /ko -->
                                        </tbody>
                                    </table>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="btn btn-dark" data-dismiss="modal"><i class="far fa-times-circle"></i> Close</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </g:if>

                <!-- ko if:associatedProgram -->
                    <h4 class="text-small-heading">${hubConfig.getTextForProgramName(grailsApplication.config.content.defaultOverriddenLabels)}</h4>
                    <p data-bind="text:associatedProgram"></p>
                <!-- /ko -->
                <!-- ko if:associatedSubProgram -->
                    <div class="text-small-heading">${hubConfig.getTextForSubprogramName(grailsApplication.config.content.defaultOverriddenLabels)}</div>
                    <span data-bind="text:associatedSubProgram"></span>
                <!-- /ko -->
                <!-- ko if:orgGrantee -->
                    <h4 class="text-small-heading"><g:message code="project.display.orgGrantee" /></h4>
                    <p data-bind="text:orgGrantee"></p>
                <!-- /ko -->
                <!-- ko if:orgSponsor -->
                    <h4 class="text-small-heading"><g:message code="project.display.orgSponsor" /></h4>
                    <p data-bind="text:orgSponsor"></p>
                <!-- /ko -->
                <!-- ko if:gear -->
                    <h4 class="text-small-heading"><g:message code="project.display.gear" /></h4>
                    <p data-bind="text:gear"></p>
                <!-- /ko -->
                <!-- ko if:task -->
                    <h4 class="text-small-heading"><g:message code="project.display.task" /></h4>
                    <p data-bind="text:task"></p>
                <!-- /ko -->
            </div>
            <div class="col-12 col-md-6">
                <!--  ko if: bushfireCategories().length -->
                    <h4 class="text-small-heading"><g:message code="project.display.bushfireCategories"/></h4>
                    <p data-bind="text:bushfireCategories().join(', ')"></p>
                <!-- /ko -->
                <!-- ko if: industries().length -->
                    <h4 class="text-small-heading"><g:message code="project.display.industries"/></h4>
                    <p data-bind="text:industries().join(', ')"></p>
                <!-- /ko -->
                <g:if test="${hubConfig?.content?.hideProjectAboutParticipateInProject != true}">
                    <!-- ko if: countries().length -->
                        <h4 class="text-small-heading">
                            <g:if test="${hubConfig.defaultFacetQuery.contains('isEcoScience:true')}">
                                <g:message code="project.display.countries.ecoscience" />
                            </g:if>
                            <g:else>
                                <g:message code="project.display.countries.citizenscience" />
                            </g:else>
                        </h4>
                        <p data-bind="text:countries().join(', ')"></p>
                    <!-- /ko -->
                </g:if>
                <g:if test="${hubConfig?.content?.hideProjectAboutUNRegions != true}">
                    <!-- ko if: uNRegions().length -->
                        <h4 class="text-small-heading"><g:message code="project.display.unregions" /></h4>
                        <p data-bind="text:uNRegions().join(', ')"></p>
                    <!-- /ko -->
                </g:if>
            %{-- TODO: swap fields. check issue - biocollect#667 --}%
                <!-- ko if:managerEmail -->
                    <h4 class="text-small-heading"><g:message code="project.display.contact.name" /></h4>
                    <p data-bind="text:managerEmail"></p>
                <!-- /ko -->
                <!-- ko if:manager -->
                    <h4 class="text-small-heading"><g:message code="project.display.contact.email" /></h4>
                    <p><a data-bind="attr:{href:'mailto:' + manager()}"><span data-bind="text:manager"></span></a></p>
                <!-- /ko -->
                %{-- TODO END--}%
            </div>
        </div>
        <hr id="hrGetStartedMobileAppTag" data-bind="visible: transients.checkVisibility('#contentGetStartedMobileAppTag', '#hrGetStartedMobileAppTag')" />
        <div id="contentGetStartedMobileAppTag">
            <div class="row">
                <div class="col-12"  data-bind="visible:isSciStarter">
                    <p>
                        <img class="logo-small d-inline"
                             src="${asset.assetPath(src: 'robot.png')}"
                             title="Project is sourced from SciStarter">
                        Project sourced from SciStarter
                    </p>
                </div>
            </div>
            <g:render template="/shared/listDocumentLinks"
                      model="${[transients:transients,imageUrl:asset.assetPath(src:'filetypes')]}"/>
            <div style="line-height:2.2em">
                <g:render template="tags" />
            </div>
            <g:if test="${!mobile}">
                <div class="row mt-3">
                    <div id="surveyLink" class="col-12 d-flex justify-content-center" data-bind="visible:transients.daysRemaining() != 0 && (!isExternal() || urlWeb()) && projectType() == 'survey' ">
                        <a class="btn btn-primary-dark btn-lg" data-bind="showTabOrRedirect: { url: isExternal() ? urlWeb() : '', tabId: '#activities-tab'}"><i class="fas fa-play"></i> <g:message code="project.display.join" /></a>
                    </div>
                </div>
            </g:if>

        </div>

    </div>
    <g:if test="${projectSite?.extent?.geometry}">
        <button class="accordion-header" type="button" data-toggle="collapse" data-target="#cs-about-section4" aria-expanded="false" aria-controls="cs-about-section4">
            <h4>${hubConfig.getTextForProjectArea(grailsApplication.config.content.defaultOverriddenLabels)}</h4>
        </button>
        <div class="accordion-body collapse show" id="cs-about-section4">

            <div class="row">
                <div class="col-12">
                        <m:map id="projectSiteMap" width="100%" height="512px"/>
                </div>
            </div>
        </div>
    </g:if>
    <!-- ko if:associatedOrgs().length > 0 -->
    <button class="accordion-header" type="button" data-toggle="collapse" data-target="#cs-about-section3" aria-expanded="false" aria-controls="cs-about-section3">
        <h4><g:message code="project.display.associatedOrgs"/></h4>
    </button>
    <div class="accordion-body collapse show" id="cs-about-section3">
        <div data-bind="template:{name:'associated-orgs'}"></div>
    </div>
    <!-- /ko -->

    <div class="image-attribution-panel accordion-body show" data-bind="visible: logoAttributionText() || mainImageAttributionText()">
        <strong>Image credits</strong>: <span data-bind="visible: logoAttributionText()">Logo: <span data-bind="text: logoAttributionText()"></span>;&nbsp;</span>
        <span data-bind="visible: mainImageAttributionText()">Feature image: <span data-bind="text: mainImageAttributionText()"></span></span>
    </div>
</div>
<asset:script type="text/javascript">

    <g:if test="${projectSite?.extent?.geometry}">
    if ((typeof map === 'undefined' || Object.keys(map).length == 0)) {
        var projectArea = <fc:modelAsJavascript model="${projectSite.extent.geometry}"/>;
        if (projectArea) {
            var overlayLayersMapControlConfig = Biocollect.MapUtilities.getOverlayConfig();
            var baseLayersAndOverlays = Biocollect.MapUtilities.getBaseLayerAndOverlayFromMapConfiguration(fcConfig.mapLayersConfig);
            var mapOptions = {
                addLayersControlHeading: true,
                drawControl: false,
                showReset: false,
                draggableMarkers: false,
                useMyLocation: false,
                allowSearchLocationByAddress: false,
                allowSearchRegionByAddress: false,
                trackWindowHeight: true,
                autoZIndex: false,
                preserveZIndex: true,
                baseLayer: baseLayersAndOverlays.baseLayer,
                otherLayers: baseLayersAndOverlays.otherLayers,
                overlays: baseLayersAndOverlays.overlays,
                overlayLayersSelectedByDefault: baseLayersAndOverlays.overlayLayersSelectedByDefault,
                wmsFeatureUrl: overlayLayersMapControlConfig.wmsFeatureUrl,
                wmsLayerUrl: overlayLayersMapControlConfig.wmsLayerUrl
            }

            map = new ALA.Map("projectSiteMap", mapOptions);
            Biocollect.MapUtilities.intersectOverlaysAndShowOnPopup(map);

            if (projectArea.pid && projectArea.pid != 'null' && projectArea.pid != "undefined") {
                map.addWmsLayer(projectArea.pid, {opacity: 0.5, zIndex: 1000});
            } else {
                var geometry = projectArea;
                var geoJson = ALA.MapUtils.wrapGeometryInGeoJSONFeatureCol(geometry);
                geoJson = ALA.MapUtils.getStandardGeoJSONForCircleGeometry(geoJson);
                map.setGeoJSON(geoJson);
            }
        }
    }
    </g:if>

    // make sure the list of associated organisations are below the shorter of the two columns.
    function placeAssociatedOrgs() {
        if ($("#column1").height() > $("#column2").height()) {
            $("#column2").append($("#associatedOrgs"));
        } else {
            $("#column1").append($("#associatedOrgs"));
        }
    }

    setTimeout(placeAssociatedOrgs, 2000);

    %{-- Map on about tab needs redrawing since leaflet viewer shows base layer on top left corner. #1253--}%
    var firstMapRedrawOnAboutTab = true;
    $('#csProjectContent,#worksProjectContent').on("knockout-visible", function() {
        firstMapRedrawOnAboutTab && map && map.redraw && map.redraw();
        firstMapRedrawOnAboutTab = false;
    });

</asset:script>



<script type="text/html" id="associated-orgs">
<div class="row" id="associatedOrgs" data-bind="visible: associatedOrgs().length > 0">
    <!-- ko foreach: associatedOrgs -->
    <div class="col-6 col-md-4 col-xl-3 associated-org thumbnail">
        <div data-bind="visible: url">
            <a href="#" data-bind="attr: {href: url}" target="_blank" class="do-not-mark-external">

                <g:set var="noImageUrl" value="${asset.assetPath(src: "no-image-2.png")}"/>

                %{--Use 'if' instead of 'visible' to prevent creating child elements that potentially will source non https content--}%
                <div data-bind="if: logo && logo.startsWith('https') "><img src="" data-bind="attr: {src: logo, title: name}"
                                                                            alt="Organisation logo" class="small-logo"></div>

                <div data-bind="visible: !logo || !logo.startsWith('https')" class="associated-org-no-logo"><span data-bind="text: name"></span></div>
            </a>
        </div>

        <div data-bind="visible: !url">
            <div data-bind="visible: logo"><img src="" data-bind="attr: {src: logo, title: name}"
                                                alt="Organisation logo" class="small-logo"></div>

            <div data-bind="visible: !logo" class="associated-org-no-logo"><span data-bind="text: name"></span></div>
        </div>
    </div>
    <!-- /ko -->
</div>
</script>