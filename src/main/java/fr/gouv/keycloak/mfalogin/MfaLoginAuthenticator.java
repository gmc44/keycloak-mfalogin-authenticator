package fr.gouv.keycloak.mfalogin;

import static fr.gouv.keycloak.mfalogin.MfaloginConstants.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.entity.StringEntity;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;

public class MfaLoginAuthenticator extends AbstractUsernameFormAuthenticator
{
    private SecureCode  secureCode;
    private Api         api = new Api();

    public MfaLoginAuthenticator(SecureCode secureCode)
    {
        this.secureCode = secureCode;
    }

    /**
     * Compare dates
     * @param strdateymd form date 1983-10-03
     * @param strdatedmy ldap date 03/10/1983
     * @return
     */
    private Boolean compareDateymd2dmy(String strdateymd, String strdatedmy)
    {
        //exception : if no valid date from ldap, return true
        if (strdatedmy == null || strdatedmy.trim().isEmpty()) return true;

        //check
        String[] ar = strdatedmy.trim().split("/");
        return (ar.length == 3 && (ar[2]+"-"+ar[1]+"-"+ar[0]).equals(strdateymd)) || (strdateymd.trim().equals(strdatedmy.trim())); //date in format 22/12/2022 from HTML5 form
    }

    @Override
    public void action(AuthenticationFlowContext context)
    {
        // Get Form Input
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        String mobileInput          = formData.getFirst("mobileInput");
        String codeInput            = formData.getFirst("codeInput");
        String secondFactorInput    = formData.getFirst("secondFactor");
        String newMobileInput       = formData.getFirst("newMobile");
        
        // Get Notes
        String generatedCodeNote   = context.getAuthenticationSession().getAuthNote(NOTE_GENERATED_CODE);
        String timeStampNote       = context.getAuthenticationSession().getAuthNote(NOTE_TIMESTAMP);
        String mobileNote          = context.getAuthenticationSession().getAuthNote(NOTE_USER_MOBILE);

        // Get Extension Parameters
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        String KeycloakUserMobileAttribute = config.getConfig().get(CONF_KEYCLOAK_USER_MOBILE_ATTRIBUTE);
        String KeycloakUserMobileModifytimestampAttribute = config.getConfig().get(CONF_KEYCLOAK_USER_MOBILE_MODIFYTIMESTAMP_ATTRIBUTE);
        String KeycloakUserBirthdateAttribute = config.getConfig().get(CONF_KEYCLOAK_USER_BIRTHDATE_ATTRIBUTE);
        //API
        String ApiRootUrl = config.getConfig().get(CONF_API_ROOT_URL);
        String ApiTokenid = config.getConfig().get(CONF_API_TOKENID);
        String ApiToken = config.getConfig().get(CONF_API_TOKEN);
        String ApiMfaSendUserNotif = config.getConfig().get(CONF_API_MFA_SEND_USER_NOTIF);
        String ApiEmailSend = config.getConfig().get(CONF_API_EMAIL_SEND);
        String ApiLdapAddattr = config.getConfig().get(CONF_API_LDAP_ADDATTR);
        String ApiLdapModattr = config.getConfig().get(CONF_API_LDAP_MODATTR);
        //ADMIN EMAIL
        String AdminEmail = config.getConfig().get(CONF_ADMIN_EMAIL);
        String AlertSubject = config.getConfig().get(CONF_ALERT_SUBJECT);
        String AlertMessage = config.getConfig().get(CONF_ALERT_MESSAGE);
        //LDAP
        String LdapMasterDn = config.getConfig().get(CONF_LDAP_MASTER_DN);
        String LdapMasterPwd = config.getConfig().get(CONF_LDAP_MASTER_PWD);
        String LdapUserMobileAttribute = config.getConfig().get(CONF_LDAP_USER_MOBILE_ATTRIBUTE);
        String LdapUserMobileModifytimestampAttribute = config.getConfig().get(CONF_LDAP_USER_MOBILE_MODIFYTIMESTAMP_ATTRIBUTE);
        
        // Get User
        UserModel user  = context.getUser();
        String uid      = user.getUsername();

        // Misc
        StringEntity data;
        String res;

        // (3.3) click on my mobile number has changed
        if (newMobileInput != null) {
            ServicesLogger.LOGGER.info(uid+": has a new mobile");
            context.challenge(context.form().createForm(FTL_ENTER_MOBILE));
        }
        // (3.1) back from enter-code
        else if (codeInput != null && generatedCodeNote != null) {

            if (secureCode.isValid(codeInput, generatedCodeNote, timeStampNote, 5, 2)) {
                
                String mobileLdap = user.getFirstAttribute(KeycloakUserMobileAttribute);
                ServicesLogger.LOGGER.info(uid+": kcUserMobile = "+mobileLdap);
                
                //Store in Ldap ?
                if ((mobileLdap == null) || ((mobileNote != null) && (mobileLdap != mobileNote))) {

                    // mobile
                    String addOrReplaceMobile = mobileLdap != null ? ApiLdapModattr : ApiLdapAddattr; //add or modify ?
                    data = new StringEntity("{\"bind_dn\":\"" + LdapMasterDn + "\",\"bind_pwd\":\"" + LdapMasterPwd + "\",\"uid\":\"" + uid + "\",\"attr\":\""+LdapUserMobileAttribute+"\",\"value\":\"" + mobileNote + "\"}","utf-8");
                    res = api.post(ApiRootUrl, ApiTokenid, ApiToken, addOrReplaceMobile + "/", data);
                    ServicesLogger.LOGGER.info(uid+": Connexion OK, storing LDAP "+LdapUserMobileAttribute+" = "+res);
                    
                    // mobilemodifytimestamp
                    String addOrReplaceMobileModifyTimeStamp = user.getFirstAttribute(KeycloakUserMobileModifytimestampAttribute) != null ? ApiLdapModattr : ApiLdapAddattr; //add or modify ?
                    DateFormat format = new SimpleDateFormat("yyyyMMddHHmmsszzz", Locale.FRANCE);
                    format.setTimeZone(TimeZone.getTimeZone("CET"));
                    Date date = new Date();
                    String mobiledermaj = format.format(date);
                    data = new StringEntity("{\"bind_dn\":\"" + LdapMasterDn + "\",\"bind_pwd\":\"" + LdapMasterPwd + "\",\"uid\":\"" + uid + "\",\"attr\":\""+LdapUserMobileModifytimestampAttribute+"\",\"value\":\"" + mobiledermaj + "\"}","utf-8");
                    res = api.post(ApiRootUrl, ApiTokenid, ApiToken, addOrReplaceMobileModifyTimeStamp + "/", data);
                    ServicesLogger.LOGGER.info(uid+": Connexion OK, storing LDAP "+LdapUserMobileModifytimestampAttribute+" = "+res);

                    // send Alert email to admin
                    String ALERTE_EXP     = AdminEmail;
                    String ALERTE_DST     = AdminEmail;
                    String ALERTE_SUJET   = AlertSubject + " " + uid + " : " + mobileNote;
                    String ALERTE_MSG     = AlertMessage + " " + uid + " : " + mobileNote;
                    
                    data = new StringEntity("{\"from\":\"" + ALERTE_EXP + "\",\"to\":[\"" + ALERTE_DST + "\"],\"subject\":\"" + ALERTE_SUJET + "\",\"body\":\"" + ALERTE_MSG + "\"}","utf-8");
                    String msgres = api.post(ApiRootUrl, ApiTokenid, ApiToken, ApiEmailSend, data);
                    ServicesLogger.LOGGER.info(uid+": Send Alert = "+msgres);

                    // send Notification to user
                    data = new StringEntity("{\"uid\":\"" + uid + "\"}","utf-8");
                    String msgresnotif = api.post(ApiRootUrl, ApiTokenid, ApiToken, ApiMfaSendUserNotif,data);
                    ServicesLogger.LOGGER.info(uid+": Send Notif = "+msgresnotif);
                }
                context.success();
            } else {
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                                         context.form().createForm(smartForm(context)));
            }
        
        }
        
        // (2) Back from enter-mobile
        else if (mobileInput != null) {
            String birthdateInput = formData.getFirst("birthdateInput");
            ServicesLogger.LOGGER.info(uid+": from mobile = "+mobileInput+" birthdate = "+birthdateInput);

            if (user == null || !user.isEnabled()) {
                ServicesLogger.LOGGER.info(uid+": failed to get user");
                context.failure(AuthenticationFlowError.INVALID_USER);
            } else {
                String birthdateLdap = user.getFirstAttribute(KeycloakUserBirthdateAttribute);
                //check dates
                if (compareDateymd2dmy(birthdateInput,birthdateLdap)) {
                    ServicesLogger.LOGGER.info(uid+": ok user and mobile = "+mobileInput+" birthdate = "+birthdateInput+" birthdateLdap = "+birthdateLdap);
                    context.getAuthenticationSession().setAuthNote(NOTE_USER_MOBILE, mobileInput);
                    String sendMsg = generateAndSendCode(context);
                    context.form().setAttribute("sendMsg", sendMsg);
                    ServicesLogger.LOGGER.info(uid+": send code "+sendMsg);
                    context.challenge(context.form().createForm(FTL_ENTER_CODE));
                } else {
                    context.challenge(context.form().createForm(smartForm(context)));
                }
            }

        }
        
        // (1) back from otp-router (other methode)
        else if (secondFactorInput != null) {
            // add sms note 
            context.getAuthenticationSession().setAuthNote(NOTE_SECOND_FACTOR, "sms");
            context.challenge(context.form().createForm(smartForm(context)));
        } else {
            context.challenge(context.form().createForm(smartForm(context))); // sinon on revoie sur le SmartForm
        }
    }

    /**
     * calls secureCode class method to generate code and sends it to the flow attached users mobile
     */
    private String generateAndSendCode(AuthenticationFlowContext context)
    {
        // Get Extension Parameters
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        // API
        String ApiRootUrl = config.getConfig().get(CONF_API_ROOT_URL);
        String ApiTokenid = config.getConfig().get(CONF_API_TOKENID);
        String ApiToken = config.getConfig().get(CONF_API_TOKEN);
        String ApiMfaSendOtp = config.getConfig().get(CONF_API_MFA_SEND_OTP);
        
        // Code Generation
        String code = secureCode.generateCode(6);

        // Save code and timestamp
        context.getAuthenticationSession().setAuthNote(NOTE_GENERATED_CODE, code);
        context.getAuthenticationSession().setAuthNote(NOTE_TIMESTAMP,Long.toString(System.currentTimeMillis()));
        
        // Get mobileNote and uid from context
        String mobileNote = context.getAuthenticationSession().getAuthNote(NOTE_USER_MOBILE);
        String uid = context.getUser().getUsername();

        StringEntity data = new StringEntity("{\"uid\":\"" + uid + "\",\"code\":\"" + code + "\",\"num\":\"" + mobileNote + "\"}","utf-8");

        return api.post(ApiRootUrl, ApiTokenid, ApiToken, ApiMfaSendOtp, data);
    }

    
    /**
     * checks if there already is a user attached to the authentication flow to avoid asking for
     * identity more than once
     */
    private String smartForm(AuthenticationFlowContext context)
    {
        String redirect;

        // Get User
        UserModel user = context.getUser();
        String uid = user.getUsername();

        // Get Notes
        String secondFactorNote = context.getAuthenticationSession().getAuthNote(NOTE_SECOND_FACTOR);

        // Get Extension Config
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        String KeycloakUserMobileAttribute = config.getConfig().get(CONF_KEYCLOAK_USER_MOBILE_ATTRIBUTE);
        String LdapUserOtpviamailAttribute = config.getConfig().get(CONF_LDAP_USER_OTPVIAMAIL_ATTRIBUTE);
        String LdapUserOtpviamailValue = config.getConfig().get(CONF_LDAP_USER_OTPVIAMAIL_VALUE);
        String LdapUserRsaotpownerAttribute = config.getConfig().get(CONF_LDAP_USER_RSAOTPOWNER_ATTRIBUTE);
        String LdapUserRsaotpownerValue = config.getConfig().get(CONF_LDAP_USER_RSAOTPOWNER_VALUE);
        
        // Get RSAOtp Owner Attribute
        List<Object> rsaOtpOwner = user.getAttributeStream(LdapUserRsaotpownerAttribute).collect(Collectors.toList());
        
        // Get Otp Via Mail Attribute
        List<Object> otpViaMail = user.getAttributeStream(LdapUserOtpviamailAttribute).collect(Collectors.toList());

        if (rsaOtpOwner.contains(LdapUserRsaotpownerValue)) { ServicesLogger.LOGGER.info(uid+": OTP User"); }

        // IF (OTP Owner)               AND (no SMS note)   => Form otp-router
        if (rsaOtpOwner.contains(LdapUserRsaotpownerValue) && secondFactorNote == null) {
            redirect = FTL_OTP_ROUTER;
        } else {
            // Get kcUserMobile
            String kcUserMobile = user.getFirstAttribute(KeycloakUserMobileAttribute);
            ServicesLogger.LOGGER.info(uid+": mobile = "+kcUserMobile);
            //SI (kcUserMobile)      OR (Otpviamail flag isset)
            if (kcUserMobile != null || otpViaMail.contains(LdapUserOtpviamailValue)) {
                String sendMsg = generateAndSendCode(context);
                context.form().setAttribute("sendMsg", sendMsg);
                ServicesLogger.LOGGER.info(uid+": retour send code "+sendMsg);
                redirect = FTL_ENTER_CODE;
            } else {
                // no kcUserMobile and no otpViaMail
                redirect = FTL_ENTER_MOBILE;
            }
        }
        
        
        return redirect;
    }

    
    @Override
    public void authenticate(AuthenticationFlowContext context)
    {
        context.challenge(context.form().createForm(smartForm(context)));
    }

    @Override
    public boolean requiresUser()
    {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user)
    {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user)
    {
        // not needed for current version
    }

    @Override
    public void close()
    {
        // not used for current version
    }

}