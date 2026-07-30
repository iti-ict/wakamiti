/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.appium;


import java.net.MalformedURLException;
import java.net.URI;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.slf4j.Logger;

import es.iti.wakamiti.api.WakamitiException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;


/**
 * Creates and configures Driver Helper instances.
 */
public class DriverHelperFactory {

    private final Logger logger;

    /**
     * Creates a driver-helper factory.
     *
     * @param logger logger passed to created driver adapters
     */
    public DriverHelperFactory(
            Logger logger
    ) {
        this.logger = logger;
    }

    /**
     * Opens an Appium session and chooses an adapter for its platform.
     * <p>
     * Android capabilities produce an {@link AndroidDriverHelper}; unknown
     * platforms fall back to the platform-neutral {@link DriverHelper}.
     *
     * @param capabilities desired platform, device and application capabilities
     * @param appiumURL absolute Appium server endpoint
     * @return helper wrapping the newly created session
     * @throws WakamitiException if the URL is missing or malformed
     */
    public DriverHelper create(
            Capabilities capabilities,
            String appiumURL
    ) {
        logger.debug("Desired Appium Capabilities:\n{}", capabilities);
        try {
            if (Platform.ANDROID.is(capabilities.getPlatformName())) {
                return new AndroidDriverHelper(new AndroidDriver(URI.create(appiumURL).toURL(), capabilities), logger);
            } else {
                logger.warn("Unknown Appium platform: {} , using basic driver", capabilities.getPlatformName());
                return new DriverHelper(new AppiumDriver(URI.create(appiumURL).toURL(), capabilities), logger);
            }
        } catch (NullPointerException | MalformedURLException e) {
            throw new WakamitiException("Invalid Appium URL: {}", e.getMessage(), e);
        }
    }

}
