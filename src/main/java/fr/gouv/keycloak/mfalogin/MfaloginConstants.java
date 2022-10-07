package fr.gouv.keycloak.mfalogin;

public class MfaloginConstants {
    //Extension Parameters
     //MOBILE
    public static final String CONF_KEYCLOAK_USER_MOBILE_ATTRIBUTE = "mfalogin.keycloak.user.mobile.attribute";
    public static final String CONF_KEYCLOAK_USER_MOBILE_MODIFYTIMESTAMP_ATTRIBUTE = "mfalogin.keycloak.user.mobile.modifytimestamp.attribute";
    public static final String CONF_KEYCLOAK_USER_EMAIL_ATTRIBUTE = "mfalogin.keycloak.user.email.attribute";
    public static final String CONF_KEYCLOAK_USER_EMAIL_MODIFYTIMESTAMP_ATTRIBUTE = "mfalogin.keycloak.user.email.modifytimestamp.attribute";
    public static final String CONF_KEYCLOAK_USER_ENROLLFACTOR1_ATTRIBUTE = "mfalogin.keycloak.user.enrollfactor1.attribute";
    public static final String CONF_KEYCLOAK_USER_ENROLLFACTOR2_ATTRIBUTE = "mfalogin.keycloak.user.enrollfactor2.attribute";
     //API
    public static final String CONF_API_ROOT_URL =                  "mfalogin.api.root.url";
    public static final String CONF_API_TOKENID =                   "mfalogin.api.tokenid";
    public static final String CONF_API_TOKEN =                     "mfalogin.api.token";
    public static final String CONF_API_MFA_SEND_OTP =              "mfalogin.api.mfa.send.otp";
    public static final String CONF_API_MFA_SEND_USER_NOTIF =       "mfalogin.api.mfa.send.user.notif";
    public static final String CONF_API_EMAIL_SEND =                "mfalogin.api.email.send";
    public static final String CONF_API_LDAP_ADDATTR =              "mfalogin.api.ldap.addattr";
    public static final String CONF_API_LDAP_MODATTR =              "mfalogin.api.ldap.modattr";
     //ADMIN EMAIL
    public static final String CONF_ADMIN_EMAIL =                   "mfalogin.admin.email";
    public static final String CONF_ALERT_SUBJECT =                 "mfalogin.alert.subject";
    public static final String CONF_ALERT_MESSAGE =                 "mfalogin.alert.message";
     //LDAP
    public static final String CONF_LDAP_MASTER_DN =                "mfalogin.ldapmaster.dn";
    public static final String CONF_LDAP_MASTER_PWD =               "mfalogin.ldapmaster.pwd";
    public static final String CONF_LDAP_USER_MOBILE_ATTRIBUTE =                    "mfalogin.ldap.user.mobile.attribute";
    public static final String CONF_LDAP_USER_MOBILE_MODIFYTIMESTAMP_ATTRIBUTE =    "mfalogin.ldap.user.mobile.modifytimestamp.attribute";
    public static final String CONF_LDAP_USER_EMAIL_ATTRIBUTE =                    "mfalogin.ldap.user.email.attribute";
    public static final String CONF_LDAP_USER_EMAIL_MODIFYTIMESTAMP_ATTRIBUTE =    "mfalogin.ldap.user.email.modifytimestamp.attribute";
    public static final String CONF_LDAP_USER_PREFERED_MFA_ATTRIBUTE =             "mfalogin.ldap.user.prefered.mfa.attribute";
    public static final String CONF_LDAP_USER_RSAOTPOWNER_ATTRIBUTE =               "mfalogin.ldap.user.rsaowner.attribute";
    public static final String CONF_LDAP_USER_RSAOTPOWNER_VALUE =               "mfalogin.ldap.user.rsaowner.value";
    public static final String CONF_LDAP_FUNCTIONAL_ACCOUNT_BRANCH =            "mfalogin.ldap.functional.account.branch";

    //Other Constants
    // FTL
    public static final String FTL_SELF_ENROLL         = "self-enroll.ftl";
    public static final String FTL_ENTER_CODE           = "enter-code.ftl";
    public static final String FTL_ENTER_UID            = "enter-uid.ftl";
    public static final String FTL_OTP_ROUTER           = "otp-router.ftl";

    // NOTES
    public static final String NOTE_USER_MOBILE         = "user-mobile";
    public static final String NOTE_USER_EMAIL          = "user-email";
    public static final String NOTE_GENERATED_CODE      = "mobile-code";
    public static final String NOTE_SECOND_FACTOR       = "second-factor";
    public static final String NOTE_TIMESTAMP           = "timestamp";
}
