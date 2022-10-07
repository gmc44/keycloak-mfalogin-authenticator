<title>Authentification renforc&eacute;e</title>
<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "form">
    <style>
      #kc-username {
        display: none;
      }
      .bouton1 {
        color: #fff;
        font-weight: bold;
        text-decoration: none;
        height: auto;
        display: flex;
        align-items: center;
        vertical-align: middle;
        flex-wrap: wrap;
        width: auto;
        padding: 10px;
        border-radius: 5px;
        cursor: pointer;
        word-break: keep-all;
        white-space: normal;
        background-color: #06B1CB;
        border: 2px solid transparent;
      }

      .bouton1:hover {
        border: 2px solid black;
      }
      .b1text {
        width: 100%;
        text-align: center;
      }
      .b1logo {
        /* margin-left: auto;
        margin-right: 0; */
        width: 100%;
        height: 61px;
        /* width: 207px; */
        background:  url('data:image/png;base64,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');
        background-repeat: no-repeat;
        background-position-x: center;
      }
      .bouton2 {
        color: rgb(63, 63, 63);
        font-weight:lighter;
        background-color: none;
        /* text-decoration: none; */
        border: none;
        /* display: flex; */
        /* flex-wrap: wrap; */
        width: 100%;
        padding: 10px;
        border-radius: 5px;
        cursor: pointer;
        word-break: keep-all;
        white-space: normal;
        text-align: center;
        border: 2px solid transparent;
      }
      .bouton2:hover {
        border: 2px solid black;
      }
      a { text-decoration: none; }
      </style>
      <script>
        window.onload = function() {
          const url = new URL(window.location.href);
          const urlClientOidc = new URL(url.searchParams.get("redirect_uri"));
          let urlClientOTP = "";
          urlClientOTP = url.origin + "/login/ct_logon.jsp?CTAuthMode=SECURID&CT_ORIG_URL=" + encodeURIComponent(url);

          /*        
          console.log("urlClientOidc="+urlClientOidc);
          console.log("urlClientOTP="+urlClientOTP);
          */

          document.getElementById("buttonOtp").href = urlClientOTP;
        }
      </script>
      <br /><br />  
      <h2>Pour des raisons de s&eacute;curit&eacute;, l'acc&egrave;s à cette application n&eacute;cessite une authentification à deux facteurs :</h2>
      <br />
      <div class="contour_top">
        <div class="contour_bottom">
            <div id="bloc_logon">
                <div id="bloc_submit">
                    <a tabindex="1" id="buttonOtp">
                        <div class="bouton1">
                          <div class="b1logo"></div>
                          <div class="b1text">Continuer avec ma cl&eacute; OTP physique ou logicielle</div>
                        </div>
                    </a>
                    <br />
                    <form id="kc-form-login" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
                        <input id="secondFactor" name="secondFactor" type="hidden" value="sms"/>
                        <input tabindex="2" type="submit" class="bouton2" name="continueSms" id="continueSms" value="Exceptionnellement, je n'ai pas ma cl&eacute;, j'utilise une autre m&eacute;thode"/>
                    </form>
                </div>
            </div>
        </div>
    </div>
  </#if>
</@layout.registrationLayout>