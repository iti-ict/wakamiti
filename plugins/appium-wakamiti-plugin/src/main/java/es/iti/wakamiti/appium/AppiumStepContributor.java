package es.iti.wakamiti.appium;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.SetUp;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Extension(provider =  "es.iti.wakamiti", name = "appium-steps", version = "2.6")
@I18nResource("iti_wakamiti_wakamiti-appium")
public class AppiumStepContributor implements StepContributor {

    private static final Logger LOGGER = WakamitiLogger.of(LoggerFactory.getLogger( "es.iti.wakamiti.appium"));

    private String appiumURL;
    private DesiredCapabilities capabilities;
    private DriverHelper driver;
    private WebElement element;
    private By elementBy;
    private String currentCall;


    public AppiumStepContributor() {
        //
    }

    @Override
    public String info() {
        return "Appium";
    }

    public void setCapabilities(DesiredCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public void setAppiumURL(String appiumURL) {
        this.appiumURL = appiumURL;
    }


    @SetUp
    public void createClient() {
        this.driver = new DriverHelperFactory(LOGGER).create(capabilities, appiumURL);
    }


    @TearDown
    public void destroyClient() {
        if (currentCall != null) {
            driver.cancelCall(currentCall);
        }
        driver.close();
        driver = null;
    }



    @Step(value = "define.element.by.id", args = {"text"})
    public void defineElementByID(String id) {
        try {
            this.elementBy = By.id(id);
            this.element = driver.findElement(elementBy);
        } catch (RuntimeException e) {
            throw new WakamitiException("Cannot locate element by id {}",id);
        }
    }


    @Step(value = "define.element.by.type", args = {"text"})
    public void defineElementByType(String type) {
        try {
            this.elementBy = By.className(type);
            this.element = driver.findElement(elementBy);
        } catch (RuntimeException e) {
            throw new WakamitiException("Cannot locate element by type {}",type);
        }
    }


    @Step(value = "define.element.by.path", args = {"text"})
    public void defineElementByPath(String path) {
        try {
            this.elementBy = By.xpath(path);
            this.element = driver.findElement(elementBy);
        } catch (RuntimeException e) {
            throw new WakamitiException("Cannot locate element by path {}",path);
        }
    }



    @Step(value = "action.type.text", args = {"text"})
    public void typeText(String text) {
        this.element.clear();
        this.element.sendKeys(text);
    }


    @Step(value = "action.tap", args = {"text"})
    public void tap(String text) {
        driver.tap(element);
    }


    @Step(value = "action.double.tap", args = {"text"})
    public void doubleTap(String text) {
        driver.doubleTap(element);
    }


    @Step(value = "action.incoming.call", args = {"text"})
    public void incomingCall(String number) {
        this.currentCall = number;
        driver.receiveCall(number);
    }


    @Step(value = "action.accept.incoming.call")
    public void acceptIncomingCall() {
        assertCurrentCallExists();
        driver.acceptIncomingCall(currentCall);
    }


    @Step(value = "action.reject.incoming.call")
    public void rejectIncomingCall() {
        assertCurrentCallExists();
        driver.rejectIncomingCall(currentCall);
    }


    @Step(value = "action.cancel.call")
    public void cancelCall() {
        assertCurrentCallExists();
        driver.cancelCall(currentCall);
    }


    @Step(value = "assert.element.value", args = {"text"})
    public void assertElementValue(String value) {
        if (!value.equals(element.getText())) {
            throw new AssertionError(
                String.format("Element %s expected to have value '%s' but it was '%s'",elementBy,value,element.getText()
            ));
        }
    }


    @Step(value = "assert.element.enabled")
    public void assertElementEnabled(){
        if (!element.isEnabled()) {
            throw new AssertionError(
                String.format("Element %s expected to be enabled but it was not",elementBy
            ));
        }
    }


    @Step(value = "assert.element.disabled")
    public void assertElementDisabled() {
        if (element.isEnabled()) {
            throw new AssertionError(
                String.format("Element %s expected to be disabled but it was not",elementBy
            ));
        }
    }


    @Step(value = "assert.element.displayed")
    public void assertElementDisplayed(){
        if (!element.isDisplayed()) {
            throw new AssertionError(
                String.format("Element %s expected to be displayed but it was not",elementBy
            ));
        }
    }


    @Step(value = "assert.element.not.displayed")
    public void assertElementNotDisplayed() {
        if (element.isDisplayed()) {
            throw new AssertionError(
                String.format("Element %s expected not to be displayed but it was",elementBy
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
