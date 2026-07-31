/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.appium;


import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.SetUp;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.util.WakamitiLogger;


/**
 * Provides the Appium Step Contributor functionality used by Wakamiti.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "appium-steps",
        version = "2.13"
)
@I18nResource("iti_wakamiti_wakamiti-appium")
public class AppiumStepContributor implements StepContributor {

    /** Field value. */
    private static final Logger LOGGER = WakamitiLogger.of(LoggerFactory.getLogger("es.iti.wakamiti.appium"));

    private String appiumURL;
    private DesiredCapabilities capabilities;
    private DriverHelper driver;
    private WebElement element;
    private By elementBy;
    private String currentCall;

    /**
     * Creates an unconfigured Appium contributor.
     * <p>
     * Capabilities and the server URL must be supplied before
     * {@link #createClient()} starts the scenario session.
     */
    public AppiumStepContributor() {
        //
    }

    /**
     * Returns the contributor name shown in Wakamiti diagnostics.
     *
     * @return {@code "Appium"}
     */
    @Override
    public String info() {
        return "Appium";
    }

    /**
     * Sets the desired capabilities used to create the next Appium session.
     *
     * @param capabilities platform, device, automation and application
     *                     capabilities
     */
    public void setCapabilities(
            DesiredCapabilities capabilities
    ) {
        this.capabilities = capabilities;
    }

    /**
     * Sets the Appium server endpoint used to create the next session.
     *
     * @param appiumURL absolute Appium server URL
     */
    public void setAppiumURL(
            String appiumURL
    ) {
        this.appiumURL = appiumURL;
    }

    /**
     * Creates the platform-specific driver before scenario execution.
     *
     * @throws WakamitiException if the endpoint is invalid or the session
     *                           cannot be created
     */
    @SetUp
    public void createClient() {
        this.driver = new DriverHelperFactory(LOGGER).create(capabilities, appiumURL);
    }

    /**
     * Cancels any tracked call and terminates the Appium session.
     * <p>
     * The stored driver reference is cleared after the remote session is
     * closed.
     */
    @TearDown
    public void destroyClient() {
        if (currentCall != null) {
            driver.cancelCall(currentCall);
        }
        driver.close();
        driver = null;
    }

    /**
     * Selects the element whose platform identifier exactly matches a value.
     * The located element becomes the target of subsequent actions and
     * assertions.
     *
     * @param id platform element identifier
     * @throws WakamitiException if no element can be located
     */
    @Step(value = "define.element.by.id", args = {"text"})
    public void defineElementByID(
            String id
    ) {
        try {
            this.elementBy = By.id(id);
            this.element = driver.findElement(elementBy);
        } catch (RuntimeException e) {
            throw new WakamitiException("Cannot locate element by id {}", id);
        }
    }

    /**
     * Selects an element by its platform class name.
     *
     * @param type fully qualified or platform-specific element class
     * @throws WakamitiException if no element can be located
     */
    @Step(value = "define.element.by.type", args = {"text"})
    public void defineElementByType(
            String type
    ) {
        try {
            this.elementBy = By.className(type);
            this.element = driver.findElement(elementBy);
        } catch (RuntimeException e) {
            throw new WakamitiException("Cannot locate element by type {}", type);
        }
    }

    /**
     * Selects an element with an XPath expression.
     *
     * @param path XPath evaluated by the Appium driver
     * @throws WakamitiException if the expression is invalid or no element can
     *                           be located
     */
    @Step(value = "define.element.by.path", args = {"text"})
    public void defineElementByPath(
            String path
    ) {
        try {
            this.elementBy = By.xpath(path);
            this.element = driver.findElement(elementBy);
        } catch (RuntimeException e) {
            throw new WakamitiException("Cannot locate element by path {}", path);
        }
    }

    /**
     * Replaces the selected element's current value with text.
     *
     * @param text character sequence sent after clearing the element
     */
    @Step(value = "action.type.text", args = {"text"})
    public void typeText(
            String text
    ) {
        this.element.clear();
        this.element.sendKeys(text);
    }

    /**
     * Performs a single tap on the selected element.
     *
     * @param text descriptive step argument; the previously selected element
     *             determines the actual gesture target
     */
    @Step(value = "action.tap", args = {"text"})
    public void tap(
            String text
    ) {
        driver.tap(element);
    }

    /**
     * Performs a double tap on the selected element.
     *
     * @param text descriptive step argument; the previously selected element
     *             determines the actual gesture target
     */
    @Step(value = "action.double.tap", args = {"text"})
    public void doubleTap(
            String text
    ) {
        driver.doubleTap(element);
    }

    /**
     * Simulates an incoming call and remembers its number for later call
     * actions and teardown cleanup.
     *
     * @param number phone number presented by the simulated caller
     */
    @Step(value = "action.incoming.call", args = {"text"})
    public void incomingCall(
            String number
    ) {
        this.currentCall = number;
        driver.receiveCall(number);
    }

    /**
     * Accepts the currently tracked incoming call.
     *
     * @throws WakamitiException if no incoming call has been created
     */
    @Step(value = "action.accept.incoming.call")
    public void acceptIncomingCall() {
        assertCurrentCallExists();
        driver.acceptIncomingCall(currentCall);
    }

    /**
     * Rejects the currently tracked incoming call.
     *
     * @throws WakamitiException if no incoming call has been created
     */
    @Step(value = "action.reject.incoming.call")
    public void rejectIncomingCall() {
        assertCurrentCallExists();
        driver.rejectIncomingCall(currentCall);
    }

    /**
     * Cancels the currently tracked call.
     *
     * @throws WakamitiException if no call has been created
     */
    @Step(value = "action.cancel.call")
    public void cancelCall() {
        assertCurrentCallExists();
        driver.cancelCall(currentCall);
    }

    /**
     * Requires the selected element's visible text to equal a value.
     *
     * @param value exact expected text
     * @throws AssertionError when the text differs
     */
    @Step(value = "assert.element.value", args = {"text"})
    public void assertElementValue(
            String value
    ) {
        if (!value.equals(element.getText())) {
            throw new AssertionError(
                    String.format("Element %s expected to have value '%s' but it was '%s'", elementBy, value, element.getText()
                    ));
        }
    }

    /**
     * Requires the selected element to accept interaction.
     *
     * @throws AssertionError when the element is disabled
     */
    @Step(value = "assert.element.enabled")
    public void assertElementEnabled() {
        if (!element.isEnabled()) {
            throw new AssertionError(
                    String.format("Element %s expected to be enabled but it was not", elementBy
                    ));
        }
    }

    /**
     * Requires the selected element not to accept interaction.
     *
     * @throws AssertionError when the element is enabled
     */
    @Step(value = "assert.element.disabled")
    public void assertElementDisabled() {
        if (element.isEnabled()) {
            throw new AssertionError(
                    String.format("Element %s expected to be disabled but it was not", elementBy
                    ));
        }
    }

    /**
     * Requires the selected element to be displayed in the current view.
     *
     * @throws AssertionError when the element is not displayed
     */
    @Step(value = "assert.element.displayed")
    public void assertElementDisplayed() {
        if (!element.isDisplayed()) {
            throw new AssertionError(
                    String.format("Element %s expected to be displayed but it was not", elementBy
                    ));
        }
    }

    /**
     * Requires the selected element not to be displayed in the current view.
     *
     * @throws AssertionError when the element is displayed
     */
    @Step(value = "assert.element.not.displayed")
    public void assertElementNotDisplayed() {
        if (element.isDisplayed()) {
            throw new AssertionError(
                    String.format("Element %s expected not to be displayed but it was", elementBy
                    ));
        }
    }

    private void assertCurrentCallExists() {
        driver.isIncomingCall();
        if (currentCall == null) {
            throw new WakamitiException("There is no incoming call");
        }
    }

}
