package es.iti.wakamiti.custom

import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.SetUp;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.rest.RestStepContributor;
import groovy.util.logging.Slf4j

import java.time.ZoneId
import java.time.ZoneOffset;


@Slf4j
@I18nResource("customs")
class CustomSteps implements StepContributor {

	WakamitiAPI wakamiti = WakamitiAPI.instance()
	
	@SetUp
	def getContributor() {
		RestStepContributor restStepContributor = wakamiti.contributors().getContributor(RestStepContributor.class)
		log.info("{} found!", restStepContributor.info())
	}

    @Step(value = "number.addition", args = ["x:int", "y:int"])
    def whatever(Integer x, Integer y) {
        def result = x + y
        log.info("{} + {} = {}", x, y, result)
    }
	
	@Step(value = "something", args = ["name:word"])
	def something(String name) {
		log.info("Hello {}!", name)
		log.info("TZ: {}", ZoneId.systemDefault())

		assert ZoneOffset.systemDefault() == ZoneOffset.of(ZoneId.of("Europe/Madrid").offset.id)
	}

}