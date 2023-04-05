package iti.kukumo.appium;

import io.appium.java_client.AppiumDriver;
import iti.kukumo.api.KukumoException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;


import java.util.List;
import java.util.Map;

public class DriverHelper {

    protected final AppiumDriver driver;
    protected final Logger logger;


    public DriverHelper(AppiumDriver driver, Logger logger) {
        this.driver = driver;
        this.logger = logger;
    }


    public void close() {
        driver.quit();
    }



    public WebElement findElement(By elementBy) {
        return driver.findElement(elementBy);
    }


    public void tap(WebElement element) {
        Actions actions = new Actions(driver);
        actions.click(element);
        actions.perform();
    }

    public void doubleTap(WebElement element) {
        Actions actions = new Actions(driver);
        actions.doubleClick(element);
        actions.perform();
    }


    public void executeShellCommand(String command, String...args) {
        logger.debug("executing shell command '{} {}' ... ", command, List.of(args));
        Object output = driver.executeScript("mobile: shell", Map.of(
        "command", command,
        "args", List.of(args)
        ));
        logger.debug("command response: {} ", output);
    }


    public void cancelCall(String number) {
        operationNotAvailable();
    }


    public void receiveCall(String number) {
        operationNotAvailable();
    }


    public void acceptIncomingCall(String number) {
        operationNotAvailable();
    }


    public void rejectIncomingCall(String number) {
        operationNotAvailable();
    }


    public boolean isIncomingCall() {
        operationNotAvailable();
        return false;
    }


    private void operationNotAvailable() {
        throw new KukumoException("Operation not available for current driver {}", driver.getClass().getSimpleName());
    }



}
