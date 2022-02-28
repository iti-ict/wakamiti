package iti.kukumo.custom

import iti.kukumo.api.annotations.I18nResource
import iti.kukumo.api.annotations.Step
import iti.kukumo.api.extensions.StepContributor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@I18nResource("customs")
class CustomSteps implements StepContributor {

    public static final Logger LOGGER = LoggerFactory.getLogger("iti.kukumo.custom")

    @Step(value = "number.addion", args = ["x:int", "y:int"])
    def whatever(int x, int y) {
        def result = x + y
        LOGGER.info("{} + {} = {}", x, y, result)
    }

}