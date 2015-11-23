import au.org.ala.biocollect.merit.SettingService
import org.codehaus.groovy.grails.commons.spring.GrailsWebApplicationContext

class UrlMappings {


        static isHubValid(applicationContext, hub) {
                def settingsService = applicationContext.getBean(SettingService)
                return settingsService.isValidHub(hub)
        }

        static mappings = { GrailsWebApplicationContext applicationContext ->
                "/$hub/$controller/$action?/$id?"{
                        constraints {
                                hub validator: {val, obj -> isHubValid(applicationContext, val)}
                        }
                }

                "/$controller/$action?/$id?"{

                }

                "/$hub/$controller/$id?"(parseRequest:true) {
                        constraints {
                                hub validator: {val, obj -> isHubValid(applicationContext, val)}
                        }
                        action = [GET: "get", POST: "upload", PUT: "upload", DELETE: "delete"]
                }

                "/sightingAjax/saveBookmarkLocation" controller: "sightingAjax", action: [POST:"saveBookmarkLocation"]
                "/uploads/$file**"(controller:"sightingImage", action:"index")
                "/project/getAuditMessagesForProject/$id"(controller: "project", action: 'getAuditMessagesForProject')

                "/activity/$entityId/comment"(controller: "comment"){
                        action = [GET: 'list', POST: 'create']
                        entityType = 'au.org.ala.ecodata.Activity'
                }
                "/activity/$entityId/comment/$id"(controller: 'comment'){
                        entityType = 'au.org.ala.ecodata.Activity'
                        action = [GET: 'get', POST: 'update', PUT: 'update', DELETE: 'delete']
                }

                "/bioActivity/$entityId/comment"(controller: "comment"){
                        action = [GET: 'list', POST: 'create']
                        entityType = 'au.org.ala.ecodata.Activity'
                }
                "/bioActivity/$entityId/comment/$id"(controller: 'comment'){
                        entityType = 'au.org.ala.ecodata.Activity'
                        action = [GET: 'get', POST: 'update', PUT: 'update', DELETE: 'delete']
                }

                "/$controller/$id?"(parseRequest:true) {

                        action = [GET: "get", POST: "upload", PUT: "upload", DELETE: "delete"]
                }

                "/$hub/"(controller: 'home', action: 'index') {
                        constraints {
                                hub validator: {val, obj -> isHubValid(applicationContext, val)}
                        }
                }
                "/$hub"(controller: 'home', action: 'index') {

                        constraints {
                                hub validator: {val, obj -> isHubValid(applicationContext, val)}
                        }
                }
                "/"(controller: 'home', action: 'index') {

                }
                "/$hub/nocas/geoService"(controller: 'home', action: 'geoService') {
                        constraints {
                                hub validator: {val, obj -> isHubValid(applicationContext, val)}
                        }
                }
                "/nocas/geoService"(controller: 'home', action: 'geoService') {

                }
                "/$hub/myProfile"(controller: 'home', action: 'myProfile') {
                        constraints {
                                hub validator: {val, obj -> isHubValid(applicationContext, val)}
                        }
                }
                "/myProfile"(controller: 'home', action: 'myProfile') {

                }

                "/$hub/admin/user/$id"(controller: "user", action: "show") {
                        constraints {
                                hub validator: {val, obj -> isHubValid(applicationContext, val)}
                        }
                }
                "/admin/user/$id"(controller: "user", action: "show") {

                }
                "500"(controller:'error', action:'response500')
                "404"(controller:'error', action:'response404')
                "/$hub?/$controller/ws/$action/$id" {
                        constraints {
                                hub validator: {val, obj -> isHubValid(applicationContext, val)}
                        }
                }
        }
}
