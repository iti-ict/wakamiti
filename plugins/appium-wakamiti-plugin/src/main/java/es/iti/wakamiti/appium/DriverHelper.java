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


public class DriverHelper {

    protected final AppiumDriver driver;
    protected final Logger logger;

    public DriverHelper(
            AppiumDriver driver,
            Logger logger
    ) {
        this.driver = driver;
        this.logger = logger;
    }

    public void close() {
        driver.quit();
    }

    public WebElement findElement(
            By elementBy
    ) {
        return driver.findElement(elementBy);
    }

    public void tap(
            WebElement element
    ) {
        Actions actions = new Actions(driver);
        actions.click(element);
        actions.perform();
    }

    public void doubleTap(
            WebElement element
    ) {
        Actions actions = new Actions(driver);
        actions.doubleClick(element);
        actions.perform();
    }

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

    public void cancelCall(
            String number
    ) {
        operationNotAvailable();
    }

    public void receiveCall(
            String number
    ) {
        operationNotAvailable();
    }

    public void acceptIncomingCall(
            String number
    ) {
        operationNotAvailable();
    }

    public void rejectIncomingCall(
            String number
    ) {
        operationNotAvailable();
    }

    public boolean isIncomingCall() {
        operationNotAvailable();
        return false;
    }

    private void operationNotAvailable() {
        throw new WakamitiException("Operation not available for current driver {}", driver.getClass().getSimpleName());
    }

}
