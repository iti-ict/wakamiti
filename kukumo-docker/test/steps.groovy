package iti.kukumo.custom;

import iti.kukumo.core.Kukumo
import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.annotations.SetUp;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.rest.RestStepContributor;
import groovy.util.logging.Slf4j;


@Slf4j
@I18nResource("customs")
class CustomSteps implements StepContributor {

	def stepContributor
	
	@SetUp
	def getContributor() {
		stepContributor = Kukumo.contributors().getContributor(RestStepContributor.class);
		log.info("{} found!", stepContributor);
	}

    @Step(value = "number.addition", args = ["x:int", "y:int"])
    def whatever(Integer x, Integer y) {
        def result = x + y
        log.info("{} + {} = {}", x, y, result)
    }
	
	@Step(value = "something", args = ["name:word"])
	def something(String name) {
		log.info("Hello {}!", name)
		stepContributor.setTimeoutInMillis(10);
	}

}