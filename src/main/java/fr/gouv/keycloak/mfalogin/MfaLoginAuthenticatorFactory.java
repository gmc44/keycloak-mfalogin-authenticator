package fr.gouv.keycloak.mfalogin;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;


public class MfaLoginAuthenticatorFactory
    implements AuthenticatorFactory
{

    // public static final String ID = "keycloak-mfalogin";
    public static final String ID = "keycloak-otp-login";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED,
            AuthenticationExecutionModel.Requirement.CONDITIONAL
    };

    private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

    static {
        ProviderConfigProperty property;
        //KEYCLOAK USER MOBILE
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_KEYCLOAK_USER_MOBILE_ATTRIBUTE);
        property.setLabel("Keycloak User Mobile Attribute");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter the keycloak user mobile attribute");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_KEYCLOAK_USER_MOBILE_MODIFYTIMESTAMP_ATTRIBUTE);
        property.setLabel("Keycloak User Mobile ModifyTimeStamp Attribute");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter the keycloak user mobile modifytimestamp attribute");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_KEYCLOAK_USER_BIRTHDATE_ATTRIBUTE);
        property.setLabel("Keycloak User Birthdate Attribute");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter the keycloak user birthdate attribute");
        configProperties.add(property);
        //API
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_API_ROOT_URL);
        property.setLabel("Api Root URL");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter your Api Root URL");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_API_TOKENID);
        property.setLabel("[Optional] Api TokenId");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter your Api Token Name");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_API_TOKEN);
        property.setLabel("[Optional] Api Token");
        property.setType(ProviderConfigProperty.PASSWORD);
        property.setHelpText("Enter your Api Token Key");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_API_MFA_SEND_OTP);
        property.setLabel("Api Send Otp Path");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter your Api Path to send the Otp (POST)");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_API_MFA_SEND_USER_NOTIF);
        property.setLabel("Api Send User Notif Path");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter your Api Path to send the User Notification (POST)");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_API_EMAIL_SEND);
        property.setLabel("Api Send Email Path");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter your Api Path to send an email (POST)");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_API_LDAP_ADDATTR);
        property.setLabel("Api Ldap Add Attribute Path");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter your Api Path to add an ldap attribute on user (POST)");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_API_LDAP_MODATTR);
        property.setLabel("Api Ldap Modify Attribute Path");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter your Api Path to modify an ldap attribute on user (POST)");
        configProperties.add(property);
        //ADMIN EMAIL
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_ADMIN_EMAIL);
        property.setLabel("[Optional] Admin Email");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Default is keycloak admin user's email");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_ALERT_SUBJECT);
        property.setLabel("Admin Email Alert Subject");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("When there is a user first connection with OTP, we store mobile in Ldap and send an Alert to Admin");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_ALERT_MESSAGE);
        property.setLabel("Admin Email Alert Message");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("When there is a user first connection with OTP, we store mobile in Ldap and send an Alert to Admin");
        configProperties.add(property);
        //LDAP
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_LDAP_MASTER_DN);
        property.setLabel("Ldap Master User DN");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter your Ldap Master User Full DN, having grant on Ldap Mobile Attribute");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_LDAP_MASTER_PWD);
        property.setLabel("Ldap Master Password");
        property.setType(ProviderConfigProperty.PASSWORD);
        property.setHelpText("Enter your Ldap Master User password");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_LDAP_USER_MOBILE_ATTRIBUTE);
        property.setLabel("Ldap User Mobile Attribute");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter the Ldap Mobile Attribute");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_LDAP_USER_MOBILE_MODIFYTIMESTAMP_ATTRIBUTE);
        property.setLabel("Ldap User Mobile ModifyTimeStamp Attribute");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter the Ldap Mobile ModifyTimeStamp Attribute");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_LDAP_USER_OTPVIAMAIL_ATTRIBUTE);
        property.setLabel("[Optional] Ldap User Otp Via Mail Attribute");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter the Ldap Otpviamail flag Attribute");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_LDAP_USER_OTPVIAMAIL_VALUE);
        property.setLabel("[Optional] Ldap User Otp Via Mail Value");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter the Ldap Otpviamail flag Value");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_LDAP_USER_RSAOTPOWNER_ATTRIBUTE);
        property.setLabel("[Optional] Ldap User RSA Otp Owner Attribute");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter the Ldap RSA Otp Owener Flag Attribute");
        configProperties.add(property);
        property = new ProviderConfigProperty();
        property.setName(MfaloginConstants.CONF_LDAP_USER_RSAOTPOWNER_VALUE);
        property.setLabel("[Optional] Ldap User RSA Otp Owner Value");
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setHelpText("Enter the Ldap RSA Otp Owener Flag Value");
        configProperties.add(property);
    }

    @Override
    public Authenticator create(KeycloakSession session)
    {
        return new MfaLoginAuthenticator(new SecureCode());
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public String getReferenceCategory()
    {
        return "OtpLogin";
    }

    @Override
    public boolean isConfigurable()
    {
        // return false;
        return true;
    }

    @Override
    public boolean isUserSetupAllowed()
    {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices()
    {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType()
    {
        return "MFA Login";
    }

    @Override
    public String getHelpText()
    {
        return "MFA Login";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public void init(Config.Scope config)
    {
        // not needed for current version
    }

    @Override
    public void postInit(KeycloakSessionFactory factory)
    {
        // not needed for current version
    }

    @Override
    public void close()
    {
        // not used for current version
    }

}