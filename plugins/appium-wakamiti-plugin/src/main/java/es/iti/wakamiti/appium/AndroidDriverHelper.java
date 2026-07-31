/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.appium;


import org.slf4j.Logger;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.GsmCallActions;


/**
 * Provides the Android Driver Helper functionality used by Wakamiti.
 */
public class AndroidDriverHelper extends DriverHelper {

    private final AndroidDriver androidDriver;

    /**
     * Creates an Android-specific helper around an active Appium session.
     *
     * @param driver Android driver used for elements, gestures and GSM
     *               emulation
     * @param logger logger receiving shell-command diagnostics
     */
    public AndroidDriverHelper(
            AndroidDriver driver,
            Logger logger
    ) {
        super(driver, logger);
        this.androidDriver = driver;
    }

    @Override
    public void cancelCall(
            String currentCall
    ) {
        androidDriver.makeGsmCall(currentCall, GsmCallActions.CANCEL);
    }

    @Override
    public void receiveCall(
            String number
    ) {
        androidDriver.makeGsmCall(number, GsmCallActions.CALL);
    }

    @Override
    public void acceptIncomingCall(
            String number
    ) {
        androidDriver.makeGsmCall(number, GsmCallActions.ACCEPT);
    }

    @Override
    public void rejectIncomingCall(
            String number
    ) {
        androidDriver.makeGsmCall(number, GsmCallActions.CANCEL);
    }

    @Override
    public boolean isIncomingCall() {
        executeShellCommand("dumpsys telephony.registry");
        return false;
    }

}
