package es.iti.wakamiti.steps;


import es.iti.wakamiti.api.annotations.I18nResource
import es.iti.wakamiti.api.annotations.TearDown
import es.iti.wakamiti.api.extensions.StepContributor
import es.iti.wakamiti.api.imconfig.Configurable
import es.iti.wakamiti.api.imconfig.Configuration
import es.iti.wakamiti.api.util.WakamitiLogger
import es.iti.wakamiti.api.WakamitiStepRunContext
import org.slf4j.Logger


@I18nResource("customs")
class CustomSteps implements StepContributor, Configurable {

    private static final transient Logger log = WakamitiLogger.forName("es.iti.wakamiti.jacoco");

    private String host
    private String port
    private String output
    private int retries

    @Override
    void configure(Configuration configuration) {
        host = configuration.get("jacoco.host", String.class).orElse(null)
        port = configuration.get("jacoco.port", String.class).orElse(null)
        output = configuration.get("jacoco.output", String.class).orElse(null)
        retries = configuration.get("jacoco.retries", Integer.class).orElse(null)
    }

    @TearDown(order = 1)
    void callJacoco() {
        log.debug("Calling to Jacoco...")
        def id = WakamitiStepRunContext.current().backend().getExtraProperties().id

        executeCommand([
                "jacoco", "dump",
                "--reset",
                "--address", host,
                "--port", port,
                "--destfile=${output}/${id}.exec",
                "--retry", retries
        ] as String[])
        
        executeCommand([
                "jacoco", "report", "${output}/${id}.exec",
                "--classfiles", "/app/classes",
                "--xml", "${output}/${id}.xml",
                "--html", "${output}/html"
        ] as String[])
    }

    void executeCommand(String[] command) {
        ProcessBuilder builder = new ProcessBuilder( command )
        builder.redirectErrorStream(true)

        Process process = builder.start()

        InputStream stdout = process.getInputStream()
        BufferedReader reader = new BufferedReader (new InputStreamReader(stdout))

        def line;
        while ((line = reader.readLine ()) != null) {
            log.debug(line)
        }
    }

}
