package org.wso2.identity.integration.test.saml;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.identity.integration.common.utils.ISIntegrationTest;
import org.wso2.identity.integration.test.util.Utils;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;

public class SAMLQueryProfileBase extends ISIntegrationTest {

    private ServerConfigurationManager serverConfigurationManager;

    @BeforeTest(alwaysRun = true)
    public void initTest() throws Exception {
        super.init();
        changeISConfiguration();
    }

    @AfterTest(alwaysRun = true)
    public void tearDownTest() throws Exception {
        super.init();
        resetISConfiguration();
    }

    public void changeISConfiguration() throws AutomationUtilException, IOException, XPathExpressionException {

        log.info("Changing deployment.toml file to enable assertion query profile");

        String carbonHome = Utils.getResidentCarbonHome();
        File defaultConfigFile = getDeploymentTomlFile(carbonHome);
        File configuredIdentityXML = new File(getISResourceLocation() + File.separator + "saml"
                + File.separator + "saml-assertion-query-enabled-deployment.toml");
        serverConfigurationManager = new ServerConfigurationManager(isServer);
        serverConfigurationManager.applyConfigurationWithoutRestart(configuredIdentityXML, defaultConfigFile, true);
        serverConfigurationManager.restartGracefully();
    }

    public void resetISConfiguration() throws AutomationUtilException, IOException {

        log.info("Reverting back to original deployment.toml");
        serverConfigurationManager.restoreToLastConfiguration(false);
    }

}
