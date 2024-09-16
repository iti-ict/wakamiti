package es.iti.wakamiti.test.generator.feature;

import es.iti.wakamiti.core.generator.features.FeatureGenerator;
import es.iti.wakamiti.core.generator.features.OpenAIService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FeatureGeneratorTest {

    private static final String API_DOCS_JSON = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Notification Service Api\",\"description\":\"Notification Service Api for Dataspace\",\"termsOfService\":\"https://www.iti.es/nota-legal/\",\"contact\":{\"name\":\"ITI\",\"url\":\"https://www.iti.es\",\"email\":\"dataspace@iti.es\"},\"license\":{\"name\":\"LICENSE Apache 2.0\",\"url\":\"https://www.apache.org/licenses/LICENSE-2.0\"},\"version\":\"0.0.1\"},\"servers\":[{\"url\":\"https://pre-backend.dataspace.iti.es/notification-service\",\"description\":\"Docker local\"}],\"paths\":{\"/notifications/{id}\":{\"get\":{\"tags\":[\"Notifications\"],\"summary\":\"Get notification details\",\"description\":\"Get notification including the status history\",\"operationId\":\"getNotificationDetails\",\"parameters\":[{\"name\":\"id\",\"in\":\"path\",\"required\":true,\"schema\":{\"type\":\"string\",\"format\":\"uuid\"}}],\"responses\":{\"200\":{\"description\":\"OK\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/NotificationDetailsDto\"}}}}},\"security\":[{\"bearerAuth\":[]}]},\"put\":{\"tags\":[\"Notifications\"],\"summary\":\"Update notification\",\"description\":\"Update a notification\",\"operationId\":\"updateNotification\",\"parameters\":[{\"name\":\"id\",\"in\":\"path\",\"required\":true,\"schema\":{\"type\":\"string\",\"format\":\"uuid\"}}],\"requestBody\":{\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/UpdateNotificationDto\"}}},\"required\":true},\"responses\":{\"200\":{\"description\":\"The notification just updated\",\"content\":{\"application/json\":{\"schema\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/components/schemas/NotificationDto\"}}}}}},\"security\":[{\"bearerAuth\":[]}]}},\"/notifications\":{\"post\":{\"tags\":[\"Notifications\"],\"summary\":\"Create notification\",\"description\":\"Create multiple types of notifications\",\"operationId\":\"createNotification\",\"requestBody\":{\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/CreateNotificationDto\"}}},\"required\":true},\"responses\":{\"201\":{\"description\":\"The notification just created\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/NotificationDto\"}}}}},\"security\":[{\"bearerAuth\":[]}]}},\"/notifications/reasons\":{\"get\":{\"tags\":[\"Notifications\"],\"summary\":\"Get all notification reasons\",\"description\":\"Get all notification reasons\",\"operationId\":\"getAllNotificationReasons\",\"responses\":{\"200\":{\"description\":\"List containing all reasons available for the notifications\",\"content\":{\"application/json\":{\"schema\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/components/schemas/NotificationReasonDto\"}}}}}},\"security\":[{\"bearerAuth\":[]}]}},\"/notifications/personal\":{\"get\":{\"tags\":[\"Notifications\"],\"summary\":\"Get personal notifications\",\"description\":\"Get the notifications assigned to the currently authenticated user\",\"operationId\":\"getPersonalNotifications\",\"responses\":{\"200\":{\"description\":\"List of notifications\",\"content\":{\"application/json\":{\"schema\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/components/schemas/NotificationDto\"}}}}}},\"security\":[{\"bearerAuth\":[]}]}},\"/notifications/kinds\":{\"get\":{\"tags\":[\"Notifications\"],\"summary\":\"Get all notification kinds\",\"description\":\"Get all notification kinds\",\"operationId\":\"getAllNotificationKinds\",\"responses\":{\"200\":{\"description\":\"List containing all kinds of notifications available\",\"content\":{\"application/json\":{\"schema\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/components/schemas/NotificationKindDto\"}}}}}},\"security\":[{\"bearerAuth\":[]}]}},\"/health\":{\"get\":{\"tags\":[\"Health\"],\"summary\":\"Check for service healthiness\",\"description\":\"Endpoint to check for API healthiness\",\"operationId\":\"getHealth\",\"responses\":{\"200\":{\"description\":\"Returns ok if server is in a healthy state\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/HealthDto\"}}}}}}}},\"components\":{\"schemas\":{\"NotificationDto\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\",\"format\":\"uuid\"},\"fromUser\":{\"type\":\"string\",\"format\":\"uuid\"},\"toUser\":{\"type\":\"string\"},\"message\":{\"type\":\"string\"},\"kind\":{\"type\":\"string\",\"enum\":[\"LOG\",\"MSG\"]},\"reason\":{\"type\":\"string\",\"enum\":[\"ORG_CREATED\",\"INVITATION_SEND\",\"USER_REGISTERED\",\"USER_ASSIGNED\",\"DATASET_CREATED\",\"ARTIFACT_UPLOADED\",\"ARTIFACT_PROFILED\",\"ARTIFACT_PROFILING_ERROR\"]},\"generationDate\":{\"type\":\"string\",\"format\":\"date-time\"},\"favorite\":{\"type\":\"boolean\"}}},\"UpdateNotificationDto\":{\"type\":\"object\",\"properties\":{\"read\":{\"type\":\"string\",\"enum\":[\"READ\",\"UNREAD\"]},\"favorite\":{\"type\":\"boolean\"}}},\"CreateNotificationDto\":{\"required\":[\"fromUser\",\"kind\",\"message\",\"reason\",\"toUser\"],\"type\":\"object\",\"properties\":{\"kind\":{\"type\":\"string\",\"enum\":[\"LOG\",\"MSG\"]},\"reason\":{\"type\":\"string\",\"enum\":[\"ORG_CREATED\",\"INVITATION_SEND\",\"USER_REGISTERED\",\"USER_ASSIGNED\",\"DATASET_CREATED\",\"ARTIFACT_UPLOADED\",\"ARTIFACT_PROFILED\",\"ARTIFACT_PROFILING_ERROR\"]},\"fromUser\":{\"type\":\"string\",\"format\":\"uuid\"},\"toUser\":{\"type\":\"string\"},\"message\":{\"type\":\"string\"},\"generationDate\":{\"type\":\"string\",\"format\":\"date-time\"}}},\"NotificationDetailsDto\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\",\"format\":\"uuid\"},\"fromUser\":{\"type\":\"string\",\"format\":\"uuid\"},\"toUser\":{\"type\":\"string\"},\"message\":{\"type\":\"string\"},\"kind\":{\"type\":\"string\",\"enum\":[\"LOG\",\"MSG\"]},\"reason\":{\"type\":\"string\",\"enum\":[\"ORG_CREATED\",\"INVITATION_SEND\",\"USER_REGISTERED\",\"USER_ASSIGNED\",\"DATASET_CREATED\",\"ARTIFACT_UPLOADED\",\"ARTIFACT_PROFILED\",\"ARTIFACT_PROFILING_ERROR\"]},\"generationDate\":{\"type\":\"string\",\"format\":\"date-time\"},\"favorite\":{\"type\":\"boolean\"},\"notificationStatusHistory\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/components/schemas/NotificationStatusDto\"}}}},\"NotificationStatusDto\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\",\"format\":\"uuid\"},\"status\":{\"type\":\"string\",\"enum\":[\"READ\",\"UNREAD\",\"SENT\",\"ERROR\",\"RETRIED\"]},\"message\":{\"type\":\"string\"},\"timestamp\":{\"type\":\"string\",\"format\":\"date-time\"}}},\"NotificationReasonDto\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\",\"format\":\"int32\"},\"name\":{\"type\":\"string\"}}},\"NotificationKindDto\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\",\"format\":\"int32\"},\"name\":{\"type\":\"string\"}}},\"HealthDto\":{\"type\":\"object\",\"properties\":{\"healthy\":{\"type\":\"boolean\"}}}},\"securitySchemes\":{\"bearerAuth\":{\"type\":\"http\",\"scheme\":\"bearer\",\"bearerFormat\":\"JWT\"}}}}\n";
    private static final String TEMP_PATH = "/var/tmp";
    private static final String GENERATED_FILE = "/var/tmp/get_health.feature";

    @Mock
    private OpenAIService openAIService;

    @Test
    public void test() throws URISyntaxException, IOException {

        FeatureGenerator featureGenerator = new FeatureGenerator(openAIService, "", API_DOCS_JSON);

        when(openAIService.runPrompt(anyString(), anyString())).thenReturn("");

        featureGenerator.generate(TEMP_PATH);

        File file = new File(GENERATED_FILE);
        assertTrue(file.exists());

    }

}
