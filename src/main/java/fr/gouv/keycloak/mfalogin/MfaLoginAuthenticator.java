package fr.gouv.keycloak.mfalogin;

import static fr.gouv.keycloak.mfalogin.MfaloginConstants.*;

import java.text.DateFormat;
import java.text.ParseException;
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
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;

public class MfaLoginAuthenticator extends AbstractUsernameFormAuthenticator
{
    private SecureCode  secureCode;

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
        if (strdatedmy == null || strdatedmy.trim().isEmpty()) return false;

        //check
        String[] ar = strdatedmy.trim().split("/");
        return (ar.length == 3 && (ar[2]+"-"+ar[1]+"-"+ar[0]).equals(strdateymd)) || (strdateymd.trim().equals(strdatedmy.trim())); //date in format 22/12/2022 from HTML5 form
    }

    //genModifyTimeStamp ldap string
    /**
     * genModifyTimeStamp
     * @return
     */
    private String genModifyTimeStamp()
    {
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmsszzz", Locale.FRANCE);
        format.setTimeZone(TimeZone.getTimeZone("CET"));
        Date date = new Date();
        return format.format(date);
    }

    @Override
    public void action(AuthenticationFlowContext context)
    {
        // Get Form Input
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        String mobileInput          = formData.getFirst("mobileInput");
        String emailInput           = formData.getFirst("emailInput");
        String codeInput            = formData.getFirst("codeInput");
        String uidInput             = formData.getFirst("uidInput");
        String secondFactorInput    = formData.getFirst("secondFactor");
        String newMobileInput       = formData.getFirst("newMobile");
        
        // Get Notes
        String generatedCodeNote   = context.getAuthenticationSession().getAuthNote(NOTE_GENERATED_CODE);
        String timeStampNote       = context.getAuthenticationSession().getAuthNote(NOTE_TIMESTAMP);
        String mobileNote          = context.getAuthenticationSession().getAuthNote(NOTE_USER_MOBILE);
        String emailNote           = context.getAuthenticationSession().getAuthNote(NOTE_USER_EMAIL);

        // Get Extension Parameters
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        String KeycloakUserMobileAttribute = config.getConfig().get(CONF_KEYCLOAK_USER_MOBILE_ATTRIBUTE);
        String KeycloakUserMobileModifytimestampAttribute = config.getConfig().get(CONF_KEYCLOAK_USER_MOBILE_MODIFYTIMESTAMP_ATTRIBUTE);
        String KeycloakUserEmailAttribute = config.getConfig().get(CONF_KEYCLOAK_USER_EMAIL_ATTRIBUTE);
        String KeycloakUserEmailModifytimestampAttribute = config.getConfig().get(CONF_KEYCLOAK_USER_EMAIL_MODIFYTIMESTAMP_ATTRIBUTE);
        String KeycloakUserEnrollfactor1Attribute = config.getConfig().get(CONF_KEYCLOAK_USER_ENROLLFACTOR1_ATTRIBUTE);
        String KeycloakUserEnrollfactor2Attribute = config.getConfig().get(CONF_KEYCLOAK_USER_ENROLLFACTOR2_ATTRIBUTE);
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
        String LdapUserEmailAttribute = config.getConfig().get(CONF_LDAP_USER_EMAIL_ATTRIBUTE);
        String LdapUserEmailModifytimestampAttribute = config.getConfig().get(CONF_LDAP_USER_EMAIL_MODIFYTIMESTAMP_ATTRIBUTE);
        String LdapUserPreferedMfaAttribute = config.getConfig().get(CONF_LDAP_USER_PREFERED_MFA_ATTRIBUTE);
        String LdapFunctionalAccountBranch = config.getConfig().get(CONF_LDAP_FUNCTIONAL_ACCOUNT_BRANCH);
    
        // Get User
        UserModel user  = context.getUser();
        String uid      = user.getUsername();
        
        // Get User Ldap Branch
        String userDN = user.getFirstAttribute(LDAPConstants.LDAP_ENTRY_DN);

        // Misc
        StringEntity data;
        String res;

        // Api
        Api api = new Api(ApiRootUrl, ApiTokenid, ApiToken);
        Api.Ldap ldapApi = api.getLdapConnexion(LdapMasterDn, LdapMasterPwd, uid);
        


        // (3.3) click on my mobile number has changed
        if (newMobileInput != null) {
            ServicesLogger.LOGGER.info(uid+": has a new mobile");
            if (userDN.contains(LdapFunctionalAccountBranch)) {
                ServicesLogger.LOGGER.info(uid+" est un compte fonctionnel");
                context.challenge(context.form().createForm(FTL_ENTER_UID));
            } else {
                context.challenge(context.form().createForm(FTL_SELF_ENROLL));
            }
        }
        // (3.1) back from enter-code
        else if (codeInput != null && generatedCodeNote != null) {

            if (secureCode.isValid(codeInput, generatedCodeNote, timeStampNote, 5, 2)) {
                
                ServicesLogger.LOGGER.info(uid+": emailNote = >"+emailNote+"<   mobileNote >"+mobileNote+"<");
                String preferedMfa = user.getFirstAttribute(LdapUserPreferedMfaAttribute);

                if (mobileNote != null) {
                    //cas mobile
                    String mobileLdap = user.getFirstAttribute(KeycloakUserMobileAttribute);
                    ServicesLogger.LOGGER.info(uid+": kcUserMobile = "+mobileLdap);

                    //Store/Update Ldap Attribute
                    if ((mobileLdap == null) || ((mobileNote != null) && (mobileLdap != mobileNote))) {
                        // save mobile
                        ldapApi.post(mobileLdap != null ? ApiLdapModattr : ApiLdapAddattr, LdapUserMobileAttribute, mobileNote);
                        // save mobilemodifytimestamp
                        ldapApi.post(user.getFirstAttribute(LdapUserMobileModifytimestampAttribute) != null ? ApiLdapModattr : ApiLdapAddattr, LdapUserMobileModifytimestampAttribute, genModifyTimeStamp());
                        // update preferedMfa
                        if (preferedMfa != "sms") { ldapApi.post(ApiLdapModattr, LdapUserPreferedMfaAttribute, "sms"); }

                        // send Alert email to admin
                        String ALERT_FROM     = AdminEmail;
                        String ALERT_TO     = AdminEmail;
                        String ALERT_SUBJECT   = AlertSubject + " " + uid + " : " + mobileNote;
                        String ALERT_MSG     = AlertMessage + " " + uid + " : " + mobileNote;
                        
                        data = new StringEntity("{\"from\":\"" + ALERT_FROM + "\",\"to\":[\"" + ALERT_TO + "\"],\"subject\":\"" + ALERT_SUBJECT + "\",\"body\":\"" + ALERT_MSG + "\"}","utf-8");
                        String msgres = api.post(ApiEmailSend, data);
                        ServicesLogger.LOGGER.info(uid+": Send Alert = "+msgres);

                        // send Notification to user
                        data = new StringEntity("{\"uid\":\"" + uid + "\"}","utf-8");
                        String msgresnotif = api.post(ApiMfaSendUserNotif,data);
                        ServicesLogger.LOGGER.info(uid+": Send Notif = "+msgresnotif);
                    }
                    context.success();
                } else if (emailNote != null) {
                    //cas email
                    String emailLdap = user.getFirstAttribute(KeycloakUserEmailAttribute);
                    ServicesLogger.LOGGER.info(uid+": kcUserEmail = "+emailLdap);

                    //Store/Update Ldap Attribute
                    if ((emailLdap == null) || ((emailNote != null) && (emailLdap != emailNote))) {
                        // save email
                        ldapApi.post(emailLdap != null ? ApiLdapModattr : ApiLdapAddattr, LdapUserEmailAttribute, emailNote);
                        // save mobilemodifytimestamp
                        ldapApi.post(user.getFirstAttribute(LdapUserEmailModifytimestampAttribute) != null ? ApiLdapModattr : ApiLdapAddattr, LdapUserEmailModifytimestampAttribute, genModifyTimeStamp());
                        // update preferedMfa
                        if (preferedMfa != "mail") { ldapApi.post(ApiLdapModattr, LdapUserPreferedMfaAttribute, "mail"); }

                        // send Alert email to admin
                        String ALERT_FROM     = AdminEmail;
                        String ALERT_TO     = AdminEmail;
                        String ALERT_SUBJECT   = AlertSubject + " " + uid + " : " + emailNote;
                        String ALERT_MSG     = AlertMessage + " " + uid + " : " + emailNote;
                        
                        data = new StringEntity("{\"from\":\"" + ALERT_FROM + "\",\"to\":[\"" + ALERT_TO + "\"],\"subject\":\"" + ALERT_SUBJECT + "\",\"body\":\"" + ALERT_MSG + "\"}","utf-8");
                        String msgres = api.post(ApiEmailSend, data);
                        ServicesLogger.LOGGER.info(uid+": Send Alert = "+msgres);

                        // send Notification to user
                        data = new StringEntity("{\"uid\":\"" + uid + "\"}","utf-8");
                        String msgresnotif = api.post(ApiMfaSendUserNotif,data);
                        ServicesLogger.LOGGER.info(uid+": Send Notif = "+msgresnotif);
                    }
                    context.success();      
                } else {
                    //Access allowed, nothing to save in ldap
                    context.success();
                }
                
            } else {
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                                         context.form().createForm(smartForm(context)));
            }
        
        }
        

        // (4) Back from enter-uid
        else if (uidInput != null) {
            String sendMsg = generateAndSendCode(context,uidInput);
            context.form().setAttribute("sendMsg", sendMsg);
            ServicesLogger.LOGGER.info("boite fonctionnelle "+uidInput+" => "+uid+": send code "+sendMsg);
            context.challenge(context.form().createForm(FTL_ENTER_CODE));
        }
        // (2.1) Back from self-enroll
        else if (mobileInput != null || emailInput != null) {
            //check factor1
            String enrollfactor1Input = formData.getFirst("enrollfactor1Input");
            ServicesLogger.LOGGER.info(uid+": enrollfactor1 = "+enrollfactor1Input);

            if (user == null || !user.isEnabled()) {
                ServicesLogger.LOGGER.info(uid+": failed to get user");
                context.failure(AuthenticationFlowError.INVALID_USER);
            } else {
                String enrollfactor1Ldap = user.getFirstAttribute(KeycloakUserEnrollfactor1Attribute);
                //compareDateymd2dmy : if factor1 is a date (birthdate) : check dates Else just compare strings
                if (compareDateymd2dmy(enrollfactor1Input,enrollfactor1Ldap)) {
                    //Factor1 validated
                    ServicesLogger.LOGGER.info(uid+": enrollfactor1 VALIDATED");
                    //if mobileForm (factor1 only)
                    if (mobileInput != null) {
                        ServicesLogger.LOGGER.info(uid+": ok user and mobile = "+mobileInput+" enrollfactor1 = "+enrollfactor1Input+" enrollfactor1Ldap = "+enrollfactor1Ldap);
                        context.getAuthenticationSession().setAuthNote(NOTE_USER_MOBILE, mobileInput);
                        String sendMsg = generateAndSendCode(context,uid);
                        context.form().setAttribute("sendMsg", sendMsg);
                        ServicesLogger.LOGGER.info(uid+": send code "+sendMsg);
                        context.challenge(context.form().createForm(FTL_ENTER_CODE));
                    //else if emailForm (factor1 + factor2)
                    } else if (emailInput != null) {
                        //check factor2
                        String enrollfactor2Input = formData.getFirst("enrollfactor2Input");
                        ServicesLogger.LOGGER.info(uid+": enrollfactor2 = "+enrollfactor2Input);
                        String enrollfactor2Ldap = user.getFirstAttribute(KeycloakUserEnrollfactor2Attribute);
                        if (enrollfactor2Input.equals(enrollfactor2Ldap)) {
                            //Factor2 validated
                            ServicesLogger.LOGGER.info(uid+": enrollfactor2 VALIDATED");
                            ServicesLogger.LOGGER.info(uid+": ok user and email = "+emailInput+" enrollfactor1 = "+enrollfactor1Input+" enrollfactor1Ldap = "+enrollfactor1Ldap+"     enrollfactor2 = "+enrollfactor2Input+" enrollfactor2Ldap = "+enrollfactor2Ldap);
                            context.getAuthenticationSession().setAuthNote(NOTE_USER_EMAIL, emailInput);
                            String sendMsg = generateAndSendCode(context,uid);
                            context.form().setAttribute("sendMsg", sendMsg);
                            ServicesLogger.LOGGER.info(uid+": send code "+sendMsg);
                            context.challenge(context.form().createForm(FTL_ENTER_CODE));
                        } else {
                            ServicesLogger.LOGGER.info(uid+": enrollfactor2 FAILED "+enrollfactor2Input+" <> "+enrollfactor2Ldap);
                            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);        
                        }

                    }                  
                } else {
                    ServicesLogger.LOGGER.info(uid+": enrollfactor1 FAILED "+enrollfactor1Input+" <> "+enrollfactor1Ldap);
                    context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
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
    private String generateAndSendCode(AuthenticationFlowContext context, String uid)
    {
        // Get Extension Parameters
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        // API
        String ApiRootUrl = config.getConfig().get(CONF_API_ROOT_URL);
        String ApiTokenid = config.getConfig().get(CONF_API_TOKENID);
        String ApiToken = config.getConfig().get(CONF_API_TOKEN);
        String ApiMfaSendOtp = config.getConfig().get(CONF_API_MFA_SEND_OTP);
        Api api = new Api(ApiRootUrl, ApiTokenid, ApiToken);
        
        // Code Generation
        String code = secureCode.generateCode(6);

        // Save code and timestamp
        context.getAuthenticationSession().setAuthNote(NOTE_GENERATED_CODE, code);
        context.getAuthenticationSession().setAuthNote(NOTE_TIMESTAMP,Long.toString(System.currentTimeMillis()));
        
        // Get mobileNote and uid from context
        String mobileNote = context.getAuthenticationSession().getAuthNote(NOTE_USER_MOBILE);
        String emailNote = context.getAuthenticationSession().getAuthNote(NOTE_USER_EMAIL);

        StringEntity data = new StringEntity("{\"uid\":\"" + uid + "\",\"code\":\"" + code + "\",\"num\":\"" + mobileNote + "\",\"email\":\"" + emailNote + "\"}","utf-8");

        return api.post(ApiMfaSendOtp, data);
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
        String KeycloakUserEmailAttribute = config.getConfig().get(CONF_KEYCLOAK_USER_EMAIL_ATTRIBUTE);
        String LdapUserRsaotpownerAttribute = config.getConfig().get(CONF_LDAP_USER_RSAOTPOWNER_ATTRIBUTE);
        String LdapUserRsaotpownerValue = config.getConfig().get(CONF_LDAP_USER_RSAOTPOWNER_VALUE);
        String LdapFunctionalAccountBranch = config.getConfig().get(CONF_LDAP_FUNCTIONAL_ACCOUNT_BRANCH);
        
        // Get RSAOtp Owner Attribute
        List<Object> rsaOtpOwner = user.getAttributeStream(LdapUserRsaotpownerAttribute).collect(Collectors.toList());
        
        // Get User Ldap Branch
        String userDN = user.getFirstAttribute(LDAPConstants.LDAP_ENTRY_DN);

        if (rsaOtpOwner.contains(LdapUserRsaotpownerValue)) { ServicesLogger.LOGGER.info(uid+": OTP User"); }

        // IF (OTP Owner)               AND (no SMS note)   => Form otp-router
        if (rsaOtpOwner.contains(LdapUserRsaotpownerValue) && secondFactorNote == null) {
            redirect = FTL_OTP_ROUTER;
        // IF (account is functional) => no enrollfactor1 => Form enter-uid
        } else if (userDN.contains(LdapFunctionalAccountBranch)) {
            ServicesLogger.LOGGER.info(uid+" est un compte fonctionnel");
            redirect = FTL_ENTER_UID;
        } else {
            // Get kcUserMobile
            String kcUserMobile = user.getFirstAttribute(KeycloakUserMobileAttribute);
            String kcUserEmail = user.getFirstAttribute(KeycloakUserEmailAttribute);
            
            //IF (kcUserMobile)      OR (kcUserEmail)
            if (kcUserMobile != null || kcUserEmail != null) {
                String sendMsg = generateAndSendCode(context,uid);
                context.form().setAttribute("sendMsg", sendMsg);
                ServicesLogger.LOGGER.info(uid+": retour send code "+sendMsg);
                redirect = FTL_ENTER_CODE;
            } else {
                // no kcUserMobile and no otpViaMail
                redirect = FTL_SELF_ENROLL;
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