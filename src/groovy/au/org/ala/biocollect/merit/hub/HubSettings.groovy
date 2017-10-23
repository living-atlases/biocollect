package au.org.ala.biocollect.merit.hub

import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * The configuration for a hub.
 */
class HubSettings extends JSONObject {
    static List SPECIAL_FACET_DATA_TYPES = ['Date', 'GeoMap']
    static Map ALL_DATA_TYPES = [
                                          presenceAbsence: "PresenceOrAbsence",
                                          default: "Default",
                                          histogram: "Histogram",
                                          date: 'Date',
                                          geoMap: 'GeoMap',
                                          activeCompleted: 'ActiveOrCompleted'
                                  ]

    public HubSettings() {
        super()
    }

    public HubSettings(Map settings) {
        super()
        putAll(settings)
    }

    public boolean overridesHomePage() {
        return optString('homePagePath', null) as boolean
    }

    /**
     * Check if homePagePath is simple url (/controller/action) or complicated
     * @return
     */
    public boolean isHomePagePathSimple(){
        String path = optString('homePagePath', '')
        List portions = path.split('/')
        if(portions.size() == 3){
            return true
        } else if(portions.size() > 3) {
            return false
        }
    }

    /**
     * Returns a map [controller: , action: ] based on parsing the homePathPage.  If the homePathPath property
     * isn't set or doesn't match the expected pattern, the default home index page will be returned..
     */
    public Map getHomePageControllerAndAction() {
        if (overridesHomePage()) {
            def regexp = "\\/(.*)\\/(.*)"
            def matcher = (optString('homePagePath', '') =~ regexp)
            if (matcher.matches()) {
                def controller = matcher[0][1]
                def action = matcher[0][2]
                return [controller:controller, action:action]
            }
        }
        return [controller:'home', action:'index']
    }

    /**
     * List facets and their configuration settings
     * @return
     */
    public List getFacetsForProjectFinderPage(){
        getFacetConfigForPage('projectFinder')
    }

    /**
     * Check if the hub has selected configurable template as its skin.
     * @return
     */
    public boolean isFacetListConfigured(String page){
        Boolean flag = false;
        if(this.pages?.get(page)){
            flag = true
        }

        flag
    }

    /**
     * Get facet configuration for a data page
     * @param view
     * @return
     */
    List getFacetConfigForPage(String view){
        this.pages?.get(view)?.facets
    }

    /**
     * Get facets without special meaning like 'date', 'geoMap' etc.
     * @param facets
     * @return
     */
    public static List getFacetConfigMinusSpecialFacets(List facets){
        facets?.grep({
            !(it.facetTermType in SPECIAL_FACET_DATA_TYPES)
        })
    }

    public static List getFacetConfigWithSpecialFacets(List facets){
        facets?.grep({
            (it.facetTermType in SPECIAL_FACET_DATA_TYPES)
        })
    }

    public static List getFacetConfigWithPresenceAbsenceSetting(List facetConfig){
        facetConfig.grep{ it.facetTermType in [ALL_DATA_TYPES.presenceAbsence] }
    }

    public static List getFacetConfigWithActiveCompletedSetting(List facetConfig){
        facetConfig.grep{ it.facetTermType in [ALL_DATA_TYPES.activeCompleted] }
    }


    public static List getFacetConfigWithHistogramSetting(List facetConfig){
        facetConfig.grep{ it.facetTermType in [ALL_DATA_TYPES.histogram] }
    }

    public static List getFacetConfigForElasticSearch(List facetConfig){
        facetConfig.grep{ !( it.facetTermType in [ALL_DATA_TYPES.presenceAbsence, ALL_DATA_TYPES.histogram, ALL_DATA_TYPES.date, ALL_DATA_TYPES.activeCompleted]) }
    }

    public static Boolean isFacetConfigSpecial(Map config){
        config.facetTermType in SPECIAL_FACET_DATA_TYPES
    }
}
