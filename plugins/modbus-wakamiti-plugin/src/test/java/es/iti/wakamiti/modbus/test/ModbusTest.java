package es.iti.wakamiti.modbus.test;


import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;


@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
})
@RunWith(WakamitiJUnitRunner.class)
public class ModbusTest {

    public static final GenericContainer<?> CONTAINER = new GenericContainer<>("oitc/modbus-server:latest")
            .withCommand("-f /server_config.json")
            .withCreateContainerCmdModifier(cmd ->
                    cmd.getHostConfig().withPortBindings(
                            new PortBinding(Ports.Binding.bindPort(5020), ExposedPort.tcp(5020)))
            )
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("server.json"),
                    "/server_config.json"
            )
            .waitingFor(Wait.forLogMessage(".*Starting Modbus TCP server on 0.0.0.0:.*", 1));

    @BeforeClass
    public static void setUp() {
        System.out.println("Creating container. Please, be patient... ");
        CONTAINER.start();
        System.out.println("\rContainer [oitc/modbus-server] started");
    }

    @AfterClass
    public static void shutdown() {
        CONTAINER.stop();
        CONTAINER.close();
    }

}