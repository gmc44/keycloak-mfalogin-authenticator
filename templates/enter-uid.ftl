<title>Authentification renforc&eacute;e</title>
<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "form">
    <style>
      #kc-username {
        display: none;
      }
      input:invalid {
      border-color: firebrick;
      }
      p.valid {
      display: none;
      }
      p.invalid {
      display: inline;
      margin-top: 1rem;
      margin-bottom: 0.25rem;
      font-style: italic;
      }
    </style>
    <script type="text/javascript">
      window.onload = function(){
      //mobile
      const uidInput=document.getElementById("uidInput");
      const uidError=document.getElementById("uidError");
      uidInput.focus();
      uidInput.addEventListener("keyup", () => {
          const valid = uidInput.checkValidity();
          if (valid) {
            uidError.className = "valid";
          } else {
            uidError.className = "invalid";
          }
      })
      }
    </script>
    <br /><br />
    <h2>Pour des raisons de s&eacute;curit&eacute;, l'acc&egrave;s à cette application n&eacute;cessite une authentification à deux facteurs :</h2>
    <h2>Attention : vous essayez de vous connecter à un compte fonctionnel, un code sera envoyé sur le numéro de mobile associé à votre compte personnel</h2>
    <br />
    <div class="contour_top">
      <div class="contour_bottom">
        <form id="kc-form-login" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
          <div id="bloc_logon">
            <div id="bloc_user">
              <p>Votre identifiant de messagerie personnel <input tabindex="1" id="uidInput" class="${properties.kcInputClass!}" name="uidInput"  type="text" pattern="^[a-zA-Z\-]+\d*" size="10" autofocus autocomplete="on" required/></p>
              <p id="uidError" class="valid">vérifiez votre saisie</p>
              <div id="bloc_submit">
                <input tabindex="3" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="sendCode" id="sendCode" type="submit" value="Recevoir mon code"/>
              </div>
            </div>
          </div>
          </form>
            </div>
      </div>
    </#if>
</@layout.registrationLayout>