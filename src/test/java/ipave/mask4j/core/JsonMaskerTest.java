package ipave.mask4j.core;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonMaskerTest {

    @ParameterizedTest
    @ValueSource(classes = {
            GsonJsonProvider.class, JacksonJsonNodeJsonProvider.class, JacksonJsonProvider.class, JettisonProvider.class,
            JsonOrgJsonProvider.class, JsonSmartJsonProvider.class, TapestryJsonProvider.class
    })
    public void shouldMaskDefinitePath(Class<JsonProvider> jsonProviderClass) throws InstantiationException, IllegalAccessException {
        Configuration jsonWayConfiguration = Configuration.builder().jsonProvider(jsonProviderClass.newInstance()).build();
        JsonMaskerConfiguration jsonMaskerConfiguration = new JsonMaskerConfiguration(true, 20, true, jsonWayConfiguration);
        JsonMasker jsonMasker = new JsonMasker(jsonMaskerConfiguration);
        String json = "{\n" +
                "    \"store\": {\n" +
                "        \"book\": [\n" +
                "            {\n" +
                "                \"category\": \"reference\",\n" +
                "                \"author\": \"Nigel Rees\",\n" +
                "                \"title\": \"Sayings of the Century\",\n" +
                "                \"price\": 8.95\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"Evelyn Waugh\",\n" +
                "                \"title\": \"Sword of Honour\",\n" +
                "                \"price\": 12.99\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"Herman Melville\",\n" +
                "                \"title\": \"Moby Dick\",\n" +
                "                \"isbn\": \"0-553-21311-3\",\n" +
                "                \"price\": 8.99\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"J. R. R. Tolkien\",\n" +
                "                \"title\": \"The Lord of the Rings\",\n" +
                "                \"isbn\": \"0-395-19395-8\",\n" +
                "                \"price\": 22.99\n" +
                "            }\n" +
                "        ],\n" +
                "        \"bicycle\": {\n" +
                "            \"color\": \"red\",\n" +
                "            \"price\": 19.95\n" +
                "        }\n" +
                "    },\n" +
                "    \"expensive\": 10\n" +
                "}";

        String maskedJson = jsonMasker.mask(
                json,
                JsonPath.compile("$.store.book[*].category"),
                target -> target.replaceAll(".", "*")
        );
        System.out.println(maskedJson);

        JsonPath path = JsonPath.compile("$.store.book[0].category");
        String category = path.read(maskedJson);
        assertEquals("*********", category);
    }


    @ParameterizedTest
    @ValueSource(classes = {
            GsonJsonProvider.class, JacksonJsonNodeJsonProvider.class, JacksonJsonProvider.class, JettisonProvider.class,
            JsonOrgJsonProvider.class, JsonSmartJsonProvider.class, TapestryJsonProvider.class
    })
    public void shouldMaskNotDefinitePath(Class<JsonProvider> jsonProviderClass) throws InstantiationException, IllegalAccessException {
        Configuration jsonWayConfiguration = Configuration.builder().jsonProvider(jsonProviderClass.newInstance()).build();
        JsonMaskerConfiguration jsonMaskerConfiguration = new JsonMaskerConfiguration(true, 20, true, jsonWayConfiguration);
        JsonMasker jsonMasker = new JsonMasker(jsonMaskerConfiguration);
        String json = "{\n" +
                "    \"store\": {\n" +
                "        \"book\": [\n" +
                "            {\n" +
                "                \"category\": \"reference\",\n" +
                "                \"author\": \"Nigel Rees\",\n" +
                "                \"title\": \"Sayings of the Century\",\n" +
                "                \"price\": 8.95\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"Evelyn Waugh\",\n" +
                "                \"title\": \"Sword of Honour\",\n" +
                "                \"price\": 12.99\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"Herman Melville\",\n" +
                "                \"title\": \"Moby Dick\",\n" +
                "                \"isbn\": \"0-553-21311-3\",\n" +
                "                \"price\": 8.99\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"J. R. R. Tolkien\",\n" +
                "                \"title\": \"The Lord of the Rings\",\n" +
                "                \"isbn\": \"0-395-19395-8\",\n" +
                "                \"price\": 22.99\n" +
                "            }\n" +
                "        ],\n" +
                "        \"bicycle\": {\n" +
                "            \"color\": \"red\",\n" +
                "            \"price\": 19.95\n" +
                "        }\n" +
                "    },\n" +
                "    \"expensive\": 10\n" +
                "}";

        String maskedJson = jsonMasker.mask(
                json,
                JsonPath.compile("$.store.book[*].category"),
                target -> target.replaceAll(".", "*")
        );

        JsonPath pathToFirstCategory = JsonPath.compile("$.store.book[0].category");
        String firstCategory = pathToFirstCategory.read(maskedJson);
        assertEquals("*********", firstCategory);

        JsonPath pathToSecondCategory = JsonPath.compile("$.store.book[1].category");
        String secondCategory = pathToSecondCategory.read(maskedJson);
        assertEquals("*******", secondCategory);

        JsonPath pathToThirdCategory = JsonPath.compile("$.store.book[2].category");
        String thirdCategory = pathToThirdCategory.read(maskedJson);
        assertEquals("*******", thirdCategory);

        JsonPath pathToFourthCategory = JsonPath.compile("$.store.book[3].category");
        String fourthCategory = pathToFourthCategory.read(maskedJson);
        assertEquals("*******", fourthCategory);
    }

    @ParameterizedTest
    @ValueSource(classes = {
            GsonJsonProvider.class, JacksonJsonNodeJsonProvider.class, JacksonJsonProvider.class, JettisonProvider.class,
            JsonOrgJsonProvider.class, JsonSmartJsonProvider.class, TapestryJsonProvider.class
    })
    public void shouldMaskNotDefinitePathWithFilter(Class<JsonProvider> jsonProviderClass) throws InstantiationException, IllegalAccessException {
        Configuration jsonWayConfiguration = Configuration.builder().jsonProvider(jsonProviderClass.newInstance()).build();
        JsonMaskerConfiguration jsonMaskerConfiguration = new JsonMaskerConfiguration(true, 20, true, jsonWayConfiguration);
        JsonMasker jsonMasker = new JsonMasker(jsonMaskerConfiguration);
        String json = "{\n" +
                "    \"store\": {\n" +
                "        \"book\": [\n" +
                "            {\n" +
                "                \"category\": \"reference\",\n" +
                "                \"author\": \"Nigel Rees\",\n" +
                "                \"title\": \"Sayings of the Century\",\n" +
                "                \"price\": 8.95\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"Evelyn Waugh\",\n" +
                "                \"title\": \"Sword of Honour\",\n" +
                "                \"price\": 12.99\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"Herman Melville\",\n" +
                "                \"title\": \"Moby Dick\",\n" +
                "                \"isbn\": \"0-553-21311-3\",\n" +
                "                \"price\": 8.99\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"J. R. R. Tolkien\",\n" +
                "                \"title\": \"The Lord of the Rings\",\n" +
                "                \"isbn\": \"0-395-19395-8\",\n" +
                "                \"price\": 22.99\n" +
                "            }\n" +
                "        ],\n" +
                "        \"bicycle\": {\n" +
                "            \"color\": \"red\",\n" +
                "            \"price\": 19.95\n" +
                "        }\n" +
                "    },\n" +
                "    \"expensive\": 10\n" +
                "}";

        String maskedJson = jsonMasker.mask(
                json,
                JsonPath.compile("$.store.book[?(@.price < 10)].price"),
                target -> target.replaceAll(".", "*")
        );

        JsonPath pathToFirstPrice = JsonPath.compile("$.store.book[0].price");
        String firstPrice = pathToFirstPrice.read(maskedJson);
        assertEquals("****", firstPrice);

        JsonPath pathToSecondPrice = JsonPath.compile("$.store.book[1].price");
        Double secondPrice = pathToSecondPrice.read(maskedJson);
        assertEquals("12.99", secondPrice.toString());

        JsonPath pathToThirdPrice = JsonPath.compile("$.store.book[2].price");
        String thirdPrice = pathToThirdPrice.read(maskedJson);
        assertEquals("****", thirdPrice);

        JsonPath pathToFourthPrice = JsonPath.compile("$.store.book[3].price");
        Double fourthPrice = pathToFourthPrice.read(maskedJson);
        assertEquals("22.99", fourthPrice.toString());
    }

    @ParameterizedTest
    @ValueSource(classes = {
            GsonJsonProvider.class, JacksonJsonNodeJsonProvider.class, JacksonJsonProvider.class, JettisonProvider.class,
            JsonOrgJsonProvider.class, JsonSmartJsonProvider.class, TapestryJsonProvider.class
    })
    public void shouldMaskRecursivelyWithDefinitePath(Class<JsonProvider> jsonProviderClass) throws InstantiationException, IllegalAccessException {
        Configuration jsonWayConfiguration = Configuration.builder().jsonProvider(jsonProviderClass.newInstance()).build();
        JsonMaskerConfiguration jsonMaskerConfiguration = new JsonMaskerConfiguration(true, 20, true, jsonWayConfiguration);
        JsonMasker jsonMasker = new JsonMasker(jsonMaskerConfiguration);
        String json = "{\n" +
                "    \"store\": {\n" +
                "        \"book\": [\n" +
                "            {\n" +
                "                \"category\": \"reference\",\n" +
                "                \"author\": \"Nigel Rees\",\n" +
                "                \"title\": \"Sayings of the Century\",\n" +
                "                \"price\": 8.95\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"J. R. R. Tolkien\",\n" +
                "                \"title\": \"The Lord of the Rings\",\n" +
                "                \"isbn\": \"0-395-19395-8\",\n" +
                "                \"price\": 22.99\n" +
                "            }\n" +
                "        ],\n" +
                "        \"bicycle\": {\n" +
                "            \"color\": \"red\",\n" +
                "            \"price\": 19.95\n" +
                "        }\n" +
                "    },\n" +
                "    \"expensive\": 10\n" +
                "}";

        String maskedJson = jsonMasker.mask(
                json,
                JsonPath.compile("$.store"),
                target -> target.replaceAll(".", "*")
        );

        System.out.println(maskedJson);
        assertEquals("*********", JsonPath.compile("$.store.book[0].category").read(maskedJson));
        assertEquals("**********", JsonPath.compile("$.store.book[0].author").read(maskedJson));
        assertEquals("**********************", JsonPath.compile("$.store.book[0].title").read(maskedJson));
        assertEquals("****", JsonPath.compile("$.store.book[0].price").read(maskedJson));

        assertEquals("*******", JsonPath.compile("$.store.book[1].category").read(maskedJson));
        assertEquals("****************", JsonPath.compile("$.store.book[1].author").read(maskedJson));
        assertEquals("*********************", JsonPath.compile("$.store.book[1].title").read(maskedJson));
        assertEquals("*****", JsonPath.compile("$.store.book[1].price").read(maskedJson));

        assertEquals("***", JsonPath.compile("$.store.bicycle.color").read(maskedJson));
        assertEquals("*****", JsonPath.compile("$.store.bicycle.price").read(maskedJson));

        assertEquals(10, (int) JsonPath.compile("$.expensive").read(maskedJson));
    }


    @ParameterizedTest
    @ValueSource(classes = {
            GsonJsonProvider.class,
            JacksonJsonProvider.class
    })
    public void shouldMaskObject(Class<JsonProvider> jsonProviderClass) throws InstantiationException, IllegalAccessException {
        Configuration jsonWayConfiguration = Configuration.builder().jsonProvider(jsonProviderClass.newInstance()).build();
        JsonMaskerConfiguration jsonMaskerConfiguration = new JsonMaskerConfiguration(true, 20, true, jsonWayConfiguration);
        JsonMasker jsonMasker = new JsonMasker(jsonMaskerConfiguration);

        SampleObject sampleObject = new SampleObject(
                new SampleObject.Store(
                        Arrays.asList(
                                new SampleObject.Book("reference", "Nigel Rees", "Sayings of the Century", null, 8.95F),
                                new SampleObject.Book("fiction", "J. R. R. Tolkien", "The Lord of the Rings", "0-395-19395-8", 22.99F)
                        ),
                        new SampleObject.Bicycle("red", 20.22F)
                ),
                10
        );

        String maskedJson = jsonMasker.mask(
                sampleObject,
                JsonPath.compile("$.store"),
                target -> target.replaceAll(".", "*")
        );

        assertEquals("*********", JsonPath.compile("$.store.books[0].category").read(maskedJson));
        assertEquals("**********", JsonPath.compile("$.store.books[0].author").read(maskedJson));
        assertEquals("**********************", JsonPath.compile("$.store.books[0].title").read(maskedJson));
        assertEquals("****", JsonPath.compile("$.store.books[0].price").read(maskedJson));

        assertEquals("*******", JsonPath.compile("$.store.books[1].category").read(maskedJson));
        assertEquals("****************", JsonPath.compile("$.store.books[1].author").read(maskedJson));
        assertEquals("*********************", JsonPath.compile("$.store.books[1].title").read(maskedJson));
        assertEquals("*************", JsonPath.compile("$.store.books[1].isbn").read(maskedJson));
        assertEquals("*****", JsonPath.compile("$.store.books[1].price").read(maskedJson));

        assertEquals("***", JsonPath.compile("$.store.bicycle.color").read(maskedJson));
        assertEquals("*****", JsonPath.compile("$.store.bicycle.price").read(maskedJson));

        assertEquals(10, (int) JsonPath.compile("$.expensive").read(maskedJson));
    }


}
