package es.iti.wakamiti.custom;

import es.iti.wakamiti.core.Wakamiti
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.SetUp;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.rest.RestStepContributor;
import groovy.util.logging.Slf4j;


@Slf4j
@I18nResource("customs")
class CustomSteps implements StepContributor {

	def stepContributor
	
	@SetUp
	def getContributor() {
		stepContributor = Wakamiti.contributors().getContributor(RestStepContributor.class);
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