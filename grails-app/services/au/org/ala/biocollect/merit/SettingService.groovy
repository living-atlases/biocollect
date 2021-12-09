package au.org.ala.biocollect.merit


import asset.pipeline.jsass.SassAssetFile
import asset.pipeline.jsass.SassProcessor
import asset.pipeline.processors.CssMinifyPostProcessor
import au.org.ala.biocollect.merit.hub.HubSettings
import grails.converters.JSON
import groovy.text.GStringTemplateEngine
import org.apache.commons.io.FileUtils
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.context.request.RequestAttributes

import static grails.async.Promises.task

//import grails.plugin.cache.Cacheable

class SettingService {

    private static def ThreadLocal localHubConfig = new ThreadLocal()
    private static final String HUB_LIST_CACHE_KEY = 'hubList'
    private static final String HUB_CACHE_KEY_SUFFIX = '_hub'
    public static final String HUB_CONFIG_ATTRIBUTE_NAME = 'hubConfig'
    public static final String LAST_ACCESSED_HUB = 'recentHub'


    public static void setHubConfig(HubSettings hubSettings) {
        localHubConfig.set(hubSettings)
        GrailsWebRequest.lookup()?.setAttribute(HUB_CONFIG_ATTRIBUTE_NAME, hubSettings, RequestAttributes.SCOPE_REQUEST)
    }

    public static void clearHubConfig() {
        localHubConfig.remove()
    }

    public static HubSettings getHubConfig() {
        return localHubConfig.get()
    }

    def webService, cacheService, cookieService
    def grailsApplication

//    @PostConstruct
    def initService () {
//        copy scss file to temp directory

        File source = new File(getClass().getResource("/data/${grailsApplication.config.bootstrap4.copyFromDir}").path)
        File target = new File("${grailsApplication.config.temp.dir}/${grailsApplication.config.bootstrap4.copyFromDir}")
        FileUtils.deleteDirectory(target)
        FileUtils.copyDirectory(source, target)
        generateStyleSheetForHubs()
    }


    /**
     * Checks if there is a configuration defined for the specified hub.
     */
    boolean isValidHub(String hubUrlPath) {
        List hubs = listHubs()
        hubs.find{it.urlPath == hubUrlPath}
    }

    def loadHubConfig(hub) {

        if (!hub) {
            hub = grailsApplication.config.app.default.hub?:'default'
            String previousHub = cookieService.getCookie(LAST_ACCESSED_HUB)
            if (!previousHub) {
                cookieService.setCookie(LAST_ACCESSED_HUB, hub, -1 /* -1 means the cookie expires when the browser is closed */)
            }
        }
        else {
            // Hub value in multiple places like url path and in parameter causes Array to be passed instead of String.
            // This causes setCookie method to throw exception since it expects String.
            if (hub && ((hub instanceof List) || hub.getClass().isArray())) {
                hub = hub[0]
            }

            // Store the most recently accessed hub in a cookie so that 404 errors can be presented with the
            // correct skin.
            cookieService.setCookie(LAST_ACCESSED_HUB, hub, -1 /* -1 means the cookie expires when the browser is closed */)
        }

        GrailsWebRequest.lookup().params.hub = hub
        def settings = getHubSettings(hub)
        if (!settings) {
            log.warn("no settings returned for hub ${hub}!")
            settings = new HubSettings(
                    title:'Default',
                    skin:'ala2',
                    urlPath:grailsApplication.config.app.default.hub?:'default',
                    availableFacets: ['isExternal','status', 'organisationFacet','associatedProgramFacet','associatedSubProgramFacet','mainThemeFacet','stateFacet','nrmFacet','lgaFacet','mvgFacet','ibraFacet','imcra4_pbFacet','otherFacet', 'gerSubRegionFacet','electFacet'],
                    adminFacets: ['electFacet'],
                    availableMapFacets: ['status', 'organisationFacet','associatedProgramFacet','associatedSubProgramFacet','stateFacet','nrmFacet','lgaFacet','mvgFacet','ibraFacet','imcra4_pbFacet','electFacet']
            )
        }

        SettingService.setHubConfig(settings)
    }

    def getSettingText(SettingPageType type) {
        def key = localHubConfig.get().urlPath + type.key

        get(key)

    }

    def getSettingText(String type) {
        def key = localHubConfig.get().urlPath + ".${type}"

        get(key)

    }

    def getSettingText(String urlPath, String path) {
        urlPath = urlPath?.trim()
        path = path?.trim()
        urlPath = urlPath ?: localHubConfig.get().urlPath
        if (path) {
            def key = urlPath + path
            get(key)
        }
    }

    def setSettingText(SettingPageType type, String content) {
        def key = localHubConfig.get().urlPath + type.key

        set(key, content)
    }

    def setSettingText(String type, String content) {
        def key = localHubConfig.get().urlPath + ".${type}"

        set(key, content)
    }

    private def get(key) {
        String url = grailsApplication.config.ecodata.service.url + "/setting/ajaxGetSettingTextForKey?key=${key}"
        def res = cacheService.get(key,{ webService.getJson(url) })
        return res?.settingText?:""
    }

    private def getJson(key) {
        cacheService.get(key, {
            def settings = get(key)
            return settings ? JSON.parse(settings) : [:]
        })
    }

    private def set(key, settings) {
        cacheService.clear(key)
        String url = grailsApplication.config.ecodata.service.url + "/setting/ajaxSetSettingText/${key}"
        webService.doPost(url, [settingText: settings, key: key])
    }

    /**
     * Allows for basic GString style substitution into a Settings page.  If the saved template text includes
     * ${}, these will be substituted for values in the supplied model
     * @param type identifies the settings page to return.
     * @param substitutionModel values to substitute into the page.
     * @return the settings page after substitutions have been made.
     */
    def getSettingText(SettingPageType type, substitutionModel) {
        String templateText = getSettingText(type)
        GStringTemplateEngine templateEngine = new GStringTemplateEngine();
        return templateEngine.createTemplate(templateText).make(substitutionModel).toString()
    }

    private def projectSettingsKey(projectId) {
        return projectId+'.settings'
    }

    def getProjectSettings(projectId) {
        getJson(projectSettingsKey(projectId))
    }

    def updateProjectSettings(projectId, settings) {
        def key = projectSettingsKey(projectId)
        set(key, (settings as JSON).toString())
    }

    private String hubCacheKey(String prefix) {
        return prefix+HUB_CACHE_KEY_SUFFIX
    }

    HubSettings getHubSettings(String urlPath) {

        cacheService.get(hubCacheKey(urlPath), {
            String url = grailsApplication.config.ecodata.service.url + '/hub/findByUrlPath/' + urlPath
            Map json = webService.getJson(url, null, true)
            json.hubId ? new HubSettings(new HashMap(json)) : null
        })
    }

    void updateHubSettings(HubSettings settings) {
        cacheService.clear(HUB_LIST_CACHE_KEY)
        cacheService.clear(hubCacheKey(settings.urlPath))

        String url = grailsApplication.config.ecodata.service.url+'/hub/'+(settings.hubId?:'')
        webService.doPost(url, settings)
    }

    List listHubs() {
        cacheService.get(HUB_LIST_CACHE_KEY, {
            String url = grailsApplication.config.ecodata.service.url+'/hub/'
            Map resp = webService.getJson(url, null, true, false)
            resp.list ?: []
        })
    }

    @Cacheable("styleSheetCache")
    public Map getConfigurableHubTemplate1(String urlPath, Map styles) {
        Map config = [
        "menubackgroundcolor": styles?.menuBackgroundColor,
        "menutextcolor": styles?.menuTextColor,
        "bannerbackgroundcolor": styles?.bannerBackgroundColor,
        "insetbackgroundcolor": styles?.insetBackgroundColor,
        "insettextcolor": styles?.insetTextColor,
        "bodybackgroundcolor": styles?.bodyBackgroundColor?:'#fff',
        "bodytextcolor": styles?.bodyTextColor?:'#637073',
        "footerbackgroundcolor": styles?.footerBackgroundColor,
        "footertextcolor": styles?.footerTextColor,
        "socialtextcolor": styles?.socialTextColor,
        "titletextcolor": styles?.titleTextColor,
        "headerbannerspacebackgroundcolor": styles?.headerBannerBackgroundColor,
        "navbackgroundcolor":  styles?.navBackgroundColor?:'#e5e6e7',
        "navtextcolor":  styles?.navTextColor?:'#5f5d60',
        "primarybackgroundcolor": styles?.primaryButtonBackgroundColor?:'#009080',
        "primarytextcolor": styles?.primaryButtonTextColor?:'#fff',
        "gettingStartedButtonBackgroundColor": styles?.gettingStartedButtonBackgroundColor?:'',
        "gettingStartedButtonTextColor": styles?.gettingStartedButtonTextColor?:'',
        "whatIsThisButtonBackgroundColor": styles?.whatIsThisButtonBackgroundColor?:'',
        "whatIsThisButtonTextColor": styles?.whatIsThisButtonTextColor?:'',
        "addARecordButtonBackgroundColor": styles?.addARecordButtonBackgroundColor ?: '',
        "addARecordButtonTextColor": styles?.addARecordButtonTextColor ?: '',
        "viewRecordsButtonBackgroundColor": styles?.viewRecordsButtonBackgroundColor ?: '',
        "viewRecordsButtonTextColor": styles?.viewRecordsButtonTextColor ?: '',
        "primaryButtonOutlineTextHoverColor": styles?.primaryButtonOutlineTextHoverColor?:'#fff',
        "primaryButtonOutlineTextColor": styles?.primaryButtonOutlineTextColor?:'#000',
        "makePrimaryButtonAnOutlineButton": styles?.makePrimaryButtonAnOutlineButton?: false,
        "defaultbackgroundcolor": styles?.defaultButtonBackgroundColor?:'#f5f5f5',
        "defaulttextcolor": styles?.defaultButtonTextColor?:'#000',
        "makeDefaultButtonAnOutlineButton": styles?.makeDefaultButtonAnOutlineButton?: false,
        "defaultButtonOutlineTextColor": styles?.defaultButtonOutlineTextColor?:'#343a40',
        "defaultButtonOutlineTextHoverColor": styles?.defaultButtonOutlineTextHoverColor?:'#000',
        "tagBackgroundColor": styles?.tagBackgroundColor ?: 'orange',
        "tagTextColor": styles?.tagTextColor?: 'white',
        "hrefcolor": styles?.hrefColor?:'#009080',
        "facetbackgroundcolor": styles?.facetBackgroundColor?: '#f5f5f5',
        "tilebackgroundcolor": styles?.tileBackgroundColor?: '#f5f5f5',
        "wellbackgroundcolor": styles?.wellBackgroundColor?: '#f5f5f5',
        "defaultbtncoloractive": styles?.defaultButtonColorActive?: '#fff',
        "defaultbtnbackgroundcoloractive": styles?.defaultButtonBackgroundColorActive?: '#000',
        "breadcrumbbackgroundcolour": styles?.breadCrumbBackGroundColour ?: '#E7E7E7',
        "primarycolor": '#009080',
        "primarycolorhover": '#007777'
        ];

        return config
    }

    /**
     * Is the current hub a works hub
     * @return
     */
    public boolean isWorksHub() {
        hubConfig?.defaultFacetQuery?.contains('isWorks:true')
    }

    /**
     * Is the current hub a eco science hub
     * @return
     */
    public boolean isEcoScienceHub() {
        hubConfig?.defaultFacetQuery?.contains('isEcoScience:true')
    }

    /**
     * Is the current hub a citizen science hub
     * @return
     */
    public boolean isCitizenScienceHub() {
        hubConfig?.defaultFacetQuery?.contains('isCitizenScience:true')
    }

    /**
     *
     * @param controller
     * @param action
     * @return
     */
    public List getCustomBreadCrumbsSetForControllerAction(String controller, String action){
        List customBreadCrumbs = hubConfig?.customBreadCrumbs
        Map item = customBreadCrumbs?.find { page ->
            if(page.controllerName == controller && page.actionName == action){
                page.breadCrumbs
            }
        }

        item?.breadCrumbs
    }

    void generateStyleSheetForHubs() {
        List hubs = listHubs()
        task {
            hubs?.each {  hubMap ->
                HubSettings hub = new HubSettings(new HashMap(hubMap))
                generateStyleSheetForHub(hub)
            }
        }
    }

    Map generateStyleSheetForHub(HubSettings hub) {
        String scssFileName = "${grailsApplication.config.bootstrap4.themeFileName}.${grailsApplication.config.bootstrap4.themeExtension}"
        String scssFileURI = "${grailsApplication.config.temp.dir}${grailsApplication.config.bootstrap4.themeDirectory}${File.separator}${scssFileName}"
        String themeDir = "${grailsApplication.config.temp.dir}${grailsApplication.config.bootstrap4.themeDirectory}"
        SassAssetFile input = new SassAssetFile(inputStreamSource: { new ByteArrayInputStream(new File(scssFileURI).bytes) }, path: scssFileURI )
        String output

        if (hub && hub.templateConfiguration?.styles ) {
            Map styles = hub.templateConfiguration?.styles
            String urlPath = hub.urlPath
            Long lastUpdated = au.org.ala.biocollect.DateUtils.parse(hub.lastUpdated).toDate().getTime()
            String scssFileFullPath =  "${themeDir}${File.separator}${scssFileName}.${urlPath}.${lastUpdated}.scss"

            String cssFileURI = "${grailsApplication.config.bootstrap4.themeDirectory}${File.separator}${grailsApplication.config.bootstrap4.themeFileName}.${urlPath}.${lastUpdated}"
            String cssFileName = "${grailsApplication.config.bootstrap4.themeFileName}.${urlPath}.${lastUpdated}.css"
            String cssFileFullPath = "${themeDir}${File.separator}${cssFileName}"

            if(!new File(cssFileFullPath).exists()){
                String contentScss = """
                ${styles?.primaryColor ? "\$primary: ${styles?.primaryColor};" : '' }
                ${styles?.primaryDarkColor ? "\$primary-dark: ${styles?.primaryDarkColor};" : ''}
                ${styles?.secondaryColor ? "\$secondary: ${styles?.secondaryColor};" : ''}
                ${styles?.successColor ? "\$success: ${styles?.successColor};" : ''}
                ${styles?.infoColor ? "\$info: ${styles?.infoColor} ;" : ''}
                ${styles?.warningColor ? "\$warning: ${styles?.warningColor} ;" : ''}
                ${styles?.dangerColor ? "\$danger: ${styles?.dangerColor} ;" : ''}
                ${styles?.lightColor ? "\$light: ${styles?.lightColor} ;" : ''}
                ${styles?.darkColor ? "\$dark: ${styles?.darkColor};" : ''}
                ${styles?.bodyBackgroundColor ? "\$body-bg: ${styles?.bodyBackgroundColor};" : ''}
                ${styles?.bodyTextColor ? "\$body-color: ${styles?.bodyTextColor};" : ''}
                ${styles?.titleTextColor ? "\$headings-color: ${styles?.titleTextColor};" : ''}
                ${styles?.breadCrumbBackGroundColour ? "\$breadcrumb-bg: ${styles?.breadCrumbBackGroundColour};" : ''}
                ${styles?.hrefColor ? "\$link-color: ${styles?.hrefColor};" : ''}
                ${styles?.navbackgroundcolor ? "\$nav-background-color: ${styles?.navbackgroundcolor};" : ''}
                ${styles?.navtextcolor ? "\$nav-text-color: ${styles?.navtextcolor};" : ''}
                ${styles?.facetBackgroundColor ? "\$facet-background-color: ${styles?.facetBackgroundColor};" : ''}
                ${styles?.tileBackgroundColor ? "\$tile-background-color: ${styles?.tileBackgroundColor};" : ''}
                ${styles?.tagBackgroundColor ? "\$tag-background-color: ${styles?.tagBackgroundColor};" : ''}
                ${styles?.tagTextColor ? "\$tag-text-color: ${styles?.tagTextColor};" : ''}
                ${styles?.footerBackgroundColor ? "\$footer-background-color: ${styles?.footerBackgroundColor};" : ''}
                ${styles?.footerTextColor ? "\$footer-text-color: ${styles?.footerTextColor};" : ''}
                ${styles?.socialTextColor ? "\$social-text-color: ${styles?.socialTextColor};" : ''}
                ${styles?.insetTextColor ? "\$inset-text-color: ${styles?.insetTextColor};" : ''}
                ${styles?.menuBackgroundColor ? "\$menu-background-color: ${styles?.menuBackgroundColor};" : ''}
                ${styles?.menuTextColor ? "\$menu-text-color: ${styles?.menuTextColor};" : ''}
                ${styles?.whatIsThisButtonBackgroundColor ? "\$what-is-btn: true;\$what-is-this-button-background-color: ${styles?.whatIsThisButtonBackgroundColor};" : "\$what-is-btn: false;"}
                ${styles?.gettingStartedButtonBackgroundColor ? "\$getting-started-btn: true;\$getting-started-button-background-color: ${styles?.gettingStartedButtonBackgroundColor};" : "\$getting-started-btn: false;"}
                ${styles?.addARecordButtonBackgroundColor ? "\$add-a-record-btn: true;\$add-a-record-button-background-color: ${styles?.addARecordButtonBackgroundColor};" : "\$add-a-record-btn: false;"}
                ${styles?.viewRecordsButtonBackgroundColor ? "\$view-records-btn: true;\$view-records-button-background-color: ${styles?.viewRecordsButtonBackgroundColor};" : "\$view-records-btn: false;"}
                ${styles?.makePrimaryButtonAnOutlineButton ? "\$make-primary-btn: true;\$primary-button-outline-text-color: ${styles?.primaryButtonOutlineTextColor};" : "\$make-primary-btn: false;"}
                ${styles?.makeDefaultButtonAnOutlineButton ? "\$make-default-btn: true;\$default-button-outline-text-color: ${styles?.defaultButtonOutlineTextColor};" : "\$make-default-btn: false;"}
                ${input.getInputStream().text}
                """

                output = processScssContent(contentScss, input, cssFileFullPath)
            } else {
                FileReader cssFileReader = new FileReader (cssFileFullPath)
                try {
                    output = cssFileReader.text
                } catch(Exception e) {
                    log.error("Error reading css file ${cssFileFullPath} - ${e.message}", e)
                } finally {
                    cssFileReader.close()
                }
            }
        } else {
            String cssFileName = "${grailsApplication.config.bootstrap4.themeFileName}.${urlPath}.css"
            String cssFileFullPath = "${themeDir}${File.separator}${cssFileName}"

            if(!new File(cssFileFullPath).exists()) {
                String contentScss = input.getInputStream().text
                output = processScssContent(contentScss, input, cssFileFullPath)
            } else {
                FileReader cssFileReader = new FileReader (cssFileFullPath)
                try {
                    output = cssFileReader.text
                } catch (Exception e) {
                    log.error("An error reading css file", e)
                } finally {
                    cssFileReader.close()
                }
            }
        }


        if (output != null) {
            return  [css: output, status: 200]
        } else {
            return  [css: "An error occurred during compilation of SCSS file", status: 500]
        }
    }

    String processScssContent(String contentScss, SassAssetFile input, String cssFileFullPath) {
        String output
        SassProcessor processor = new SassProcessor()
        output = processor.process(contentScss, input)
        def minifyCssProcessor = new CssMinifyPostProcessor()
        try {
            output = minifyCssProcessor.process(output)
            FileWriter cssFile = new FileWriter(cssFileFullPath)
            cssFile.write(output)
            cssFile.close()
        } catch (Exception e) {
            log.error("Error minifying CSS - ${e.message}", e)
        }

        output
    }
}
