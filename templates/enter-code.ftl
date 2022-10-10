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
      #bloc_submit.invalid {
        display: none;
      }
      #bloc_submit.valid {
        display: inline;
      }
      summary {
        color: #069;
        cursor: pointer;
        text-decoration: none;
      }
      summary:hover {
        text-decoration: underline;
      }
      #resendCode:disabled { cursor: progress; }
      input[type=submit]:hover { cursor: pointer; }

    </style>
    <script>
      window.onload = function(){
        const codeInput=document.getElementById("codeInput");
        const codeError=document.getElementById("codeError");
        const blocSubmit=document.getElementById("bloc_submit");
        const resendCode=document.getElementById("resendCode");
        const new2fa=document.getElementById("new2fa");

        codeInput.focus();

        // desactivation du bouton "renvoyer" pendant les 5 premieres secondes
        resendCode.disabled = true;
        setTimeout(function(){resendCode.disabled = false;},5000);
        
        // Controle de la saisie du code
        codeInput.addEventListener("keyup", () => {
          const valid = codeInput.checkValidity();
          if (valid) {
            codeError.className = "valid";
            blocSubmit.className = "valid";
          } else {
            codeError.className = "invalid";
            blocSubmit.className = "invalid";
          }
        })

        // Descatication du check si click sur Renvoyer ou J'ai changé de numéro
        resendCode.addEventListener("click", () => { codeInput.required = false; })
        new2fa.addEventListener("click", () => { codeInput.required = false; })

      }
    </script>
    <br /><br />
    <h2>Pour des raisons de s&eacute;curit&eacute;, l'acc&egrave;s à cette application n&eacute;cessite une authentification à deux facteurs :</h2>
    <h2>Entrez votre code re&ccedil;u par ${sendMsg}</h2>
    <br />
    <div class="contour_top">
      <div class="contour_bottom">
        <form id="kc-form-login" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
          <div id="bloc_logon">
            <div id="bloc_user">
              <label for="code">Votre code de v&eacute;rification</label>
              <input tabindex="1" id="codeInput" name="codeInput"  type="text" type="number" pattern="\d{6}" placeholder="" size="6" required autofocus autocomplete="off" />
              <p id="codeError" class="valid">entrez les 6 chiffres</p>
              <div id="bloc_submit" class="invalid">
                <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="sendCode" id="sendCode" type="submit" value="${msg("doLogIn")}"}"/>
              </div>
            </div>
          </div>
          <details>
            <summary>Je n'ai pas reçu le code</summary>
            <div id="formfailed" class="collapse">
              <input tabindex="6" class="${properties.kcButtonClass!}" name="resendCode" id="resendCode" type="submit" value="Renvoyer"/>
              <input tabindex="7" class="${properties.kcButtonClass!}" name="new2fa" id="new2fa" type="submit" value="Je veux changer de second facteur"/>
            </div>
          </details>
        </form>
      </div>
    </div>
  </#if>
</@layout.registrationLayout>