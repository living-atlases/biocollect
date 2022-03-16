<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="bs4"/>
    <title>Organisations | <g:message code="g.biocollect"/></title>
    <meta name="breadcrumbParent1" content="${createLink(controller: 'project', action: 'homePage')},Home"/>
    <meta name="breadcrumb" content="Organisations"/>

    <script type="text/javascript" src="${grailsApplication.config.google.maps.url}" async defer></script>
    <asset:script type="text/javascript">
        var fcConfig = {
            <g:applyCodec encodeAs="none">
            serverUrl: "${grailsApplication.config.grails.serverURL}",
            createOrganisationUrl: "${createLink(controller: 'organisation', action: 'create')}",
            viewOrganisationUrl: "${createLink(controller: 'organisation', action: 'index')}",
            organisationSearchUrl: "${createLink(controller: 'organisation', action: 'search')}",
            noLogoImageUrl: "${asset.assetPath(src: 'biocollect-logo-dark.png')}"
            </g:applyCodec>
            };
    </asset:script>
    <asset:javascript src="common-bs4.js"/>
    <asset:javascript src="organisation.js"/>
</head>

<body>
<g:render template="organisationListing"></g:render>

</body>

</html>