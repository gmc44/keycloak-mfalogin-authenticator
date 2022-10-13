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
        color: #990000;
      }
      div.invisible {
        display: none;
      }
      div.visible {
        display: inline;
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
        //email
        const emailInput=document.getElementById("emailInput");
        const emailError=document.getElementById("emailError");
        emailInput.focus();
        emailInput.addEventListener("keyup", () => {
          const valid = emailInput.checkValidity();
          if (valid) {
            emailError.className = "valid";
          } else {
            emailError.className = "invalid";
          }
        })
        //enrollfactor1
        const enrollfactor1Input=document.getElementById("enrollfactor1Input");
        const enrollfactor1Error=document.getElementById("enrollfactor1Error");
        enrollfactor1Input.addEventListener("blur", () => {
          const valid = enrollfactor1Input.checkValidity();
          if (valid) {
            enrollfactor1Error.className = "valid";
          } else {
            enrollfactor1Error.className = "invalid";
          }
        })
        //enrollfactor2
        const enrollfactor2Input=document.getElementById("enrollfactor2Input");
        const enrollfactor2Error=document.getElementById("enrollfactor2Error");
        enrollfactor2Input.addEventListener("blur", () => {
          const valid = enrollfactor2Input.checkValidity();
          if (valid) {
            enrollfactor2Error.className = "valid";
          } else {
            enrollfactor2Error.className = "invalid";
          }
        })
      }
      function toggleForm() {
        const emailForm=document.getElementById("emailForm");
        const mobileForm=document.getElementById("mobileForm");
        if (mobileForm.className == "invisible") {
          mobileForm.className = "visible";
          emailForm.className = "invisible";
        } else {
          emailForm.className = "visible";
          mobileForm.className = "invisible";
        }
      }
    </script>
    <br /><br />
    <h2>Pour des raisons de s&eacute;curit&eacute;, l'acc&egrave;s à cette application n&eacute;cessite une authentification à deux facteurs :</h1>
    <h2>Aucun second facteur n'est actuellement associ&eacute; &agrave; votre compte.</h2>
    <h2>Pour finaliser votre connexion, veuillez saisir :</h2>
    <br />
    <div id="mobileForm">
      <div class="contour_top">
        <div class="contour_bottom">
          <form id="kc-form-login" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <div id="bloc_logon">
              <div id="bloc_user">
                <p>Votre num&eacute;ro de mobile <input tabindex="1" id="mobileInput" class="${properties.kcInputClass!}" name="mobileInput"  type="tel" pattern="0{1}[6-7]{1}\d{8}" placeholder="06789101112" size="10" autofocus autocomplete="on" required title="au format fran&ccedil;ais 0712345678 ou 0612345678"/>
                <i>&nbsp;&nbsp;&nbsp;&nbsp;<a href=# onclick="toggleForm()">Je préfère recevoir mon code par email</a></i></p>
                <p id="mobileError" class="valid">au format fran&ccedil;ais 0<b>7</b>12345678 ou 0<b>6</b>12345678</p>
                <p>Votre date de naissance <input tabindex="2" id="enrollfactor1Input" class="${properties.kcInputClass!}" name="enrollfactor1Input"  type="date" size="10" autofocus autocomplete="off" required/></p>
                <p id="enrollfactor1Error" class="valid">format date invalide</p>
              </div>
            </div>
            <div id="bloc_submit">
              <input tabindex="3" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="sendCode" id="sendCode" type="submit" value="Recevoir mon code"/>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div id="emailForm" class="invisible">
      <div class="contour_top">
        <div class="contour_bottom">
          <form id="kc-form-login" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <div id="bloc_logon">
              <div id="bloc_user">
                <p>Votre adresse email d'authentification <input tabindex="1" id="emailInput" class="${properties.kcInputClass!}" name="emailInput" type="email" placeholder="monadresseemail@domaine.ext" pattern="(?!.+@ac-nantes\.fr$).*" size="20" autofocus="" autocomplete="on" required title="Votre adresse email d'authentification hors ac-nantes.fr">
                <i>&nbsp;&nbsp;&nbsp;&nbsp;<a href=# onclick="toggleForm()">Je préfère recevoir mon code par SMS</a></i></p>
                <p id="emailError" class="valid">Votre adresse email d'authentification en dehors du domaine <b>ac-nantes.fr</b></p>
                <p>Votre date de naissance <input tabindex="2" id="enrollfactor1Input" class="${properties.kcInputClass!}" name="enrollfactor1Input"  type="date" size="10" autofocus autocomplete="off" required/></p>
                <p id="enrollfactor1Error" class="valid">format date invalide</p>
                <p>Votre NUMEN <input tabindex="3" id="enrollfactor2Input" class="${properties.kcInputClass!}" name="enrollfactor2Input" type="text" maxlength="13" minlength="13" size="13" placeholder="17E9501234ABC" pattern="[0-9]{2}[A-Z]{1,2}[0-9]{6,7}[A-Z]{3}" autofocus="" autocomplete="off" required title="NUMéro d'identification Éducation Nationale"></p>
                <p id="enrollfactor2Error" class="valid">au format 17E9501234ABC</p>
              </div>
            </div>
            <div id="bloc_submit">
              <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="sendCode" id="sendCode" type="submit" value="Recevoir mon code"/>
            </div>
          </form>
        </div>
      </div>
    </div>
  </#if>
</@layout.registrationLayout>