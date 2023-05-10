package iti.kukumo.appium;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import iti.kukumo.api.KukumoException;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URI;

public class DriverHelperFactory {

    private final Logger logger;

    public DriverHelperFactory(Logger logger) {
        this.logger = logger;
    }


    public DriverHelper create(Capabilities capabilities, String appiumURL) {
        logger.debug("Desired Appium Capabilities:\n{}", capabilities);
        try {
            if (Platform.ANDROID.is(capabilities.getPlatformName())) {
                return new AndroidDriverHelper(new AndroidDriver(URI.create(appiumURL).toURL(),capabilities),logger);
            } else {
                logger.warn("Unknown Appium platform: {} , using basic driver", capabilities.getPlatformName());
                return new DriverHelper(new AppiumDriver(URI.create(appiumURL).toURL(),capabilities),logger);
            }
        } catch (NullPointerException | MalformedURLException e) {
            throw new KukumoException("Invalid Appium URL: {}", e.getMessage(), e);
        }
    }

}
