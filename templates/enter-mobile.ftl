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
    <script>
      window.onload = function(){
        //mobile
        const mobileInput=document.getElementById("mobileInput");
        const mobileError=document.getElementById("mobileError");
        mobileInput.focus();
        mobileInput.addEventListener("keyup", () => {
          const valid = mobileInput.checkValidity();
          if (valid) {
            mobileError.className = "valid";
          } else {
            mobileError.className = "invalid";
          }
        })
        //birthdate
        const birthdateInput=document.getElementById("birthdateInput");
        const birthdateError=document.getElementById("birthdateError");
        birthdateInput.addEventListener("blur", () => {
          const valid = birthdateInput.checkValidity();
          if (valid) {
            birthdateError.className = "valid";
          } else {
            birthdateError.className = "invalid";
          }
        })
      }
    </script>
    <br /><br />
    <h2>Pour des raisons de s&eacute;curit&eacute;, l'acc&egrave;s à cette application n&eacute;cessite une authentification à deux facteurs :</h2>
    <h2>Aucun num&eacute;ro de mobile n'est actuellement associ&eacute; &agrave; votre compte.</h2>
    <h2>Pour finaliser votre connexion, veuillez saisir :</h2>
    <br />
    <div class="contour_top">
      <div class="contour_bottom">
        <form id="kc-form-login" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
          <div id="bloc_logon">
            <div id="bloc_user">
              <p>Votre num&eacute;ro de mobile <input tabindex="1" id="mobileInput" class="${properties.kcInputClass!}" name="mobileInput"  type="tel" pattern="0{1}[6-7]{1}\d{8}" placeholder="06789101112" size="10" autofocus autocomplete="on" required/></p>
              <p id="mobileError" class="valid">au format fran&ccedil;ais 0<b>7</b>12345678 ou 0<b>6</b>12345678</p>
              <p>Votre date de naissance <input tabindex="2" id="birthdateInput" class="${properties.kcInputClass!}" name="birthdateInput"  type="date" size="10" autofocus autocomplete="on" required/></p>
              <p id="birthdateError" class="valid">format date invalide</p>
            </div>
          </div>
          <div id="bloc_submit">
            <input tabindex="3" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="sendCode" id="sendCode" type="submit" value="Recevoir mon code"/>
          </div>
        </form>
      </div>
    </div>
  </#if>
</@layout.registrationLayout>