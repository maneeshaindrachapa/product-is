/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AdaptiveAuthTemplates;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.AuthProtocolMetadata;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.OIDCMetaData;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.SAMLMetaData;
import org.wso2.identity.integration.test.rest.api.server.application.management.v1.model.WSTrustMetaData;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for happy paths of the Application Metadata REST API.
 */
public class ApplicationMetadataPositiveTest extends ApplicationManagementBaseTest {

    private static final String INBOUND_PROTOCOLS_PATH = "inbound-protocols";
    private static final String OIDC_PATH = "oidc";
    private static final String SAML_PATH = "saml";
    private static final String WS_TRUST_PATH = "ws-trust";
    private static final String ADAPTIVE_AUTH_PATH = "adaptive-auth-templates";

    private List<AuthProtocolMetadata> allInboundProtocolsResponse;
    private OIDCMetaData oidcMetaData;
    private SAMLMetaData samlMetaDataSuperTenant;
    private SAMLMetaData samlMetaDataTenant;
    private WSTrustMetaData wsTrustMetaDataSuperTenant;
    private WSTrustMetaData wsTrustMetaDataTenant;
    private AdaptiveAuthTemplates adaptiveAuthTemplates;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationMetadataPositiveTest(TestUserMode userMode) throws Exception {

        super.init(userMode);
        this.context = isServer;
        this.authenticatingUserName = context.getContextTenant().getTenantAdmin().getUserName();
        this.authenticatingCredential = context.getContextTenant().getTenantAdmin().getPassword();
        this.tenant = context.getContextTenant().getDomain();
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws IOException {

        super.testInit(API_VERSION, swaggerDefinition, tenant);

        // Init getAllEmailTemplateTypes method response
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        String expectedResponse = readResource("all-inbound-protocols-response.json");
        allInboundProtocolsResponse =
                Arrays.asList(jsonWriter.readValue(expectedResponse, AuthProtocolMetadata[].class));

        // Init OIDC Metadata
        expectedResponse = readResource("oidc-metadata.json");
        oidcMetaData = jsonWriter.readValue(expectedResponse, OIDCMetaData.class);

        // Init SAML Metadata
        expectedResponse = readResource("saml-metadata-super-tenant.json");
        samlMetaDataSuperTenant = jsonWriter.readValue(expectedResponse, SAMLMetaData.class);
        expectedResponse = readResource("saml-metadata-tenant.json");
        samlMetaDataTenant = jsonWriter.readValue(expectedResponse, SAMLMetaData.class);

        // Init WS Trust Metadata
        expectedResponse = readResource("ws-trust-metadata-super-tenant.json");
        wsTrustMetaDataSuperTenant = jsonWriter.readValue(expectedResponse, WSTrustMetaData.class);
        expectedResponse = readResource("ws-trust-metadata-tenant.json");
        wsTrustMetaDataTenant = jsonWriter.readValue(expectedResponse, WSTrustMetaData.class);

        // Init adaptive authentication templates
        expectedResponse = readResource("adaptive-metadata.json");
        adaptiveAuthTemplates = jsonWriter.readValue(expectedResponse, AdaptiveAuthTemplates.class);
    }

    @AfterClass(alwaysRun = true)
    public void testConclude() {

        super.conclude();
    }

    @BeforeMethod(alwaysRun = true)
    public void testInit() {

        RestAssured.basePath = basePath;
    }

    @AfterMethod(alwaysRun = true)
    public void testFinish() {

        RestAssured.basePath = StringUtils.EMPTY;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN}, {TestUserMode.TENANT_ADMIN}};
    }

    @Test
    public void testGetAllInboundProtocols() throws IOException {

        Response response = getResponseOfGet(METADATA_API_BASE_PATH + PATH_SEPARATOR + INBOUND_PROTOCOLS_PATH);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        List<AuthProtocolMetadata> responseFound =
                Arrays.asList(jsonWriter.readValue(response.asString(), AuthProtocolMetadata[].class));
        Assert.assertEquals(responseFound, allInboundProtocolsResponse,
                "Response of the get all inbound protocols doesn't match.");
    }

    @Test
    public void testGetOIDCMetadata() throws IOException {

        Response response = getResponseOfGet(METADATA_API_BASE_PATH +
                PATH_SEPARATOR + INBOUND_PROTOCOLS_PATH +
                PATH_SEPARATOR + OIDC_PATH);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        OIDCMetaData responseFound = jsonWriter.readValue(response.asString(), OIDCMetaData.class);
        Assert.assertEquals(sortScopeValidators(responseFound), oidcMetaData,
                "OIDC Metadata returned from the API doesn't match.");
    }

    private OIDCMetaData sortScopeValidators(OIDCMetaData oidcMetaData) {

        List<String> scopeValidators = oidcMetaData.getScopeValidators().getOptions();
        Collections.sort(scopeValidators);
        oidcMetaData.getScopeValidators().setOptions(scopeValidators);
        return oidcMetaData;
    }

    @Test
    public void testGetSAMLMetadata() throws IOException {

        Response response = getResponseOfGet(METADATA_API_BASE_PATH +
                PATH_SEPARATOR + INBOUND_PROTOCOLS_PATH +
                PATH_SEPARATOR + SAML_PATH);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        SAMLMetaData responseFound = jsonWriter.readValue(response.asString(), SAMLMetaData.class);

        if (this.tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            Assert.assertEquals(responseFound, samlMetaDataSuperTenant,
                    "SAML Metadata returned from the API doesn't match.");
        } else {
            Assert.assertEquals(responseFound, samlMetaDataTenant,
                    "SAML Metadata returned from the API doesn't match.");
        }
    }

    @Test
    public void testGetWSTrustMetadata() throws IOException {

        Response response = getResponseOfGet(METADATA_API_BASE_PATH +
                PATH_SEPARATOR + INBOUND_PROTOCOLS_PATH +
                PATH_SEPARATOR + WS_TRUST_PATH);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        WSTrustMetaData responseFound = jsonWriter.readValue(response.asString(), WSTrustMetaData.class);

        if (this.tenant.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            Assert.assertEquals(responseFound, wsTrustMetaDataSuperTenant,
                    "WS Trust Metadata returned from the API doesn't match.");
        } else {
            Assert.assertEquals(responseFound, wsTrustMetaDataTenant,
                    "WS Trust Metadata returned from the API doesn't match.");
        }
    }

    @Test
    public void testGetAdaptiveAuthTemplates() throws IOException {

        Response response = getResponseOfGet(METADATA_API_BASE_PATH + PATH_SEPARATOR + ADAPTIVE_AUTH_PATH);
        response.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
        ObjectMapper jsonWriter = new ObjectMapper(new JsonFactory());
        AdaptiveAuthTemplates responseFound = jsonWriter.readValue(response.asString(), AdaptiveAuthTemplates.class);
        Assert.assertEquals(responseFound, adaptiveAuthTemplates,
                "Adaptive auth templates returned from the API doesn't match.");
    }
}