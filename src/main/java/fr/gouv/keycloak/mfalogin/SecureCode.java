package fr.gouv.keycloak.mfalogin;

import java.security.SecureRandom;
import java.util.Random;

public class SecureCode
{

    Random random = new SecureRandom();

    /**
     * Version 6 chiffres
     */
    public String generateCode(int codeLength)
    {
        int number = random.nextInt(999999);
        return String.format("%06d", number);
    }


    /**
     * Splits the passed string using "-" to make it more readable
     * split conditions:
     * - don't split if length <= 5
     * - split in 4s if divisible by 4 or length > 20
     * - split in 3s if divisible by 3
     */
    public String makeCodeUserFriendly(String code)
    {
        String userFriendlyCode = code;
        int codeLength = code.length();
        int codeSnippetLength;

        if (codeLength <= 5) {
            codeSnippetLength = 6;

        }
        else if (codeLength > 20 || codeLength % 4 == 0) {
            codeSnippetLength = 4;

        }
        else if (codeLength % 3 == 0) {
            codeSnippetLength = 3;

        }
        else {
            codeSnippetLength = 4;
        }

        for (int i = (codeLength - 1) / codeSnippetLength * codeSnippetLength; i > 0; i -= codeSnippetLength) {
            // userFriendlyCode = userFriendlyCode.substring(0, i) + "-" + userFriendlyCode.substring(i);
            userFriendlyCode = userFriendlyCode.substring(0, i) + " " + userFriendlyCode.substring(i);
            // userFriendlyCode = userFriendlyCode.substring(0, i) + userFriendlyCode.substring(i);
        }

        return userFriendlyCode;
    }

    /**
     * Validates the code input by the user
     * returns true if entered code met following conditions:
     * - code is equal to the code send out by mobile (ignoring "-")
     * - code got entered before timeoutInMinutes have passed since code got sent
     * - code got entered after codeActivationDelayInSeconds have passed since code got sent
     */
    public boolean isValid(String codeInput, String mobileedCode, String timeStamp, int timeoutInMinutes,
                           int codeActivationDelayInSeconds)
    {
        // codeInput = codeInput.replace("-", "");
        codeInput = codeInput.replace(" ", "");
        long timePassedSinceRequest = System.currentTimeMillis()
                - Long.parseLong(timeStamp);
        boolean codeActive = timePassedSinceRequest < 1000 * 60 * timeoutInMinutes
                && timePassedSinceRequest > 1000 * codeActivationDelayInSeconds;
        return (codeInput.equalsIgnoreCase(mobileedCode) && codeActive);

    }

}