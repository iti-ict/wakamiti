/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.appium;


import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;

import es.iti.wakamiti.api.WakamitiException;
import io.appium.java_client.AppiumDriver;


/**
 * Provides the Driver Helper functionality used by Wakamiti.
 */
public class DriverHelper {

    protected final AppiumDriver driver;
    protected final Logger logger;

    /**
     * Creates the platform-neutral driver adapter.
     *
     * @param driver active Appium session
     * @param logger logger receiving driver command details
     */
    public DriverHelper(
            AppiumDriver driver,
            Logger logger
    ) {
        this.driver = driver;
        this.logger = logger;
    }

    /**
     * Terminates the remote Appium session and closes its application.
     */
    public void close() {
        driver.quit();
    }

    /**
     * Finds the first element matching a Selenium locator.
     *
     * @param elementBy locator evaluated by the underlying driver
     * @return matching remote element
     * @throws org.openqa.selenium.NoSuchElementException when no element matches
     */
    public WebElement findElement(
            By elementBy
    ) {
        return driver.findElement(elementBy);
    }

    /**
     * Performs a single pointer click on an element.
     *
     * @param element remote element receiving the gesture
     */
    public void tap(
            WebElement element
    ) {
        Actions actions = new Actions(driver);
        actions.click(element);
        actions.perform();
    }

    /**
     * Performs a double pointer click on an element.
     *
     * @param element remote element receiving the gesture
     */
    public void doubleTap(
            WebElement element
    ) {
        Actions actions = new Actions(driver);
        actions.doubleClick(element);
        actions.perform();
    }

    /**
     * Executes an Appium {@code mobile: shell} command and logs its result.
     *
     * @param command executable or Android shell command name
     * @param args ordered command arguments
     */
    public void executeShellCommand(
            String command,
            String... args
    ) {
        logger.debug("executing shell command '{} {}' ... ", command, List.of(args));
        Object output = driver.executeScript("mobile: shell", Map.of(
                "command", command,
                "args", List.of(args)
        ));
        logger.debug("command response: {} ", output);
    }

    /**
     * Cancels an active simulated call.
     *
     * @param number number associated with the call
     * @throws WakamitiException when the current platform has no call support
     */
    public void cancelCall(
            String number
    ) {
        operationNotAvailable();
    }

    /**
     * Starts a simulated incoming call.
     *
     * @param number number presented by the caller
     * @throws WakamitiException when the current platform has no call support
     */
    public void receiveCall(
            String number
    ) {
        operationNotAvailable();
    }

    /**
     * Accepts a simulated incoming call.
     *
     * @param number number associated with the call
     * @throws WakamitiException when the current platform has no call support
     */
    public void acceptIncomingCall(
            String number
    ) {
        operationNotAvailable();
    }

    /**
     * Rejects a simulated incoming call.
     *
     * @param number number associated with the call
     * @throws WakamitiException when the current platform has no call support
     */
    public void rejectIncomingCall(
            String number
    ) {
        operationNotAvailable();
    }

    /**
     * Determines whether the device currently reports an incoming call.
     *
     * @return {@code true} when an incoming call is detected
     * @throws WakamitiException when the current platform has no call support
     */
    public boolean isIncomingCall() {
        operationNotAvailable();
        return false;
    }

    private void operationNotAvailable() {
        throw new WakamitiException("Operation not available for current driver {}", driver.getClass().getSimpleName());
    }

}
