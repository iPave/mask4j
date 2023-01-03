package ipave.mask4j.core;

import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JsonProvider;
import javafx.util.Pair;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class JsonMasker {

    private final JsonMaskerConfiguration jsonMaskerConfiguration;

    JsonMasker() {
        this(JsonMaskerConfiguration.defaultConfiguration());
    }

    JsonMasker(JsonMaskerConfiguration jsonMaskerConfiguration) {
        this.jsonMaskerConfiguration = jsonMaskerConfiguration;
    }

    public String mask(Object object, JsonPath jsonPath, MaskFunction maskFunction) {
        String jsonString = jsonMaskerConfiguration.getJsonWayConfiguration().jsonProvider().toJson(object);
        return mask(jsonString, jsonPath, maskFunction);
    }

    public String mask(Object object, List<Pair<JsonPath, MaskFunction>> maskFunctionsByPath) {
        String jsonString = jsonMaskerConfiguration.getJsonWayConfiguration().jsonProvider().toJson(object);
        return mask(jsonString, maskFunctionsByPath);
    }

    public String mask(String json, JsonPath jsonPath, MaskFunction maskFunction) {
        JsonProvider jsonProvider = jsonMaskerConfiguration.getJsonWayConfiguration().jsonProvider();
        DocumentContext documentContext = JsonPath.using(jsonProvider).parse(json);
        processMasking(documentContext, jsonProvider, jsonPath, maskFunction);
        return documentContext.jsonString();
    }

    public String mask(String json, List<Pair<JsonPath, MaskFunction>> maskFunctionsByPath) {
        JsonProvider jsonProvider = jsonMaskerConfiguration.getJsonWayConfiguration().jsonProvider();
        DocumentContext documentContext = JsonPath.using(jsonProvider).parse(json);
        for (Pair<JsonPath, MaskFunction> maskFunctionByPath : maskFunctionsByPath) {
            JsonPath jsonPath = maskFunctionByPath.getKey();
            MaskFunction maskFunction = maskFunctionByPath.getValue();
            processMasking(documentContext, jsonProvider, jsonPath, maskFunction);
        }
        return documentContext.jsonString();
    }


    private void processMasking(DocumentContext documentContext, JsonProvider jsonProvider, JsonPath jsonPath, MaskFunction maskFunction) {
        boolean maskRecursively = jsonMaskerConfiguration.maskRecursively();
        boolean throwWhenCantFindTarget = jsonMaskerConfiguration.throwWhenCantFindTarget();
        if (jsonPath.isDefinite()) {
            try {
                Object foundNode = documentContext.read(jsonPath);
                if (isPrimitive(foundNode)) {
                    Object unwrappedValue = jsonProvider.unwrap(foundNode);
                    documentContext.set(jsonPath, maskFunction.mask(unwrappedValue.toString()));
                } else if (maskRecursively) {
                    traverseTree(documentContext, foundNode, jsonPath, maskFunction, 0);
                }
            } catch (PathNotFoundException e) {
                if (throwWhenCantFindTarget) {
                    throw e;
                }
            }
        } else {
            documentContext.map(jsonPath, (currentValue, configuration) -> {
                if (isPrimitive(currentValue)) {
                    Object unwrappedValue = jsonProvider.unwrap(currentValue);
                    return maskFunction.mask(unwrappedValue.toString());
                }
                if (throwWhenCantFindTarget) {
                    throw new IllegalStateException(
                            "Can't apply mask to not primitive value at path: " + jsonPath.getPath()
                    );
                }
                return currentValue;
            });
        }
    }

    /**
     * Checks if found node is primitive type of json
     *
     * @param foundNode object to be checked
     */
    private boolean isPrimitive(Object foundNode) {
        JsonProvider jsonProvider = jsonMaskerConfiguration.getJsonWayConfiguration().jsonProvider();
        return !jsonProvider.isArray(foundNode) && !jsonProvider.isMap(foundNode);
    }

    /**
     * Recursive traverse of json tree, limited recursion depth with JsonMaskerConfiguration.recursionDepth.
     * Makes dfs and masks only primitive nodes with provided mask function.
     *
     * @param documentContext JsonWay document context with specific json provider
     * @param foundNode       node which was found at path 'jsonPath'
     * @param jsonPath        current jsonPath
     * @param maskFunction    function to be applied to all found nodes
     * @param recursionLevel  current recursion level
     */
    private void traverseTree(DocumentContext documentContext, Object foundNode, JsonPath jsonPath, MaskFunction maskFunction, int recursionLevel) {
        if (recursionLevel > jsonMaskerConfiguration.getRecursionDepth()) {
            throw new IllegalStateException("Too deep recursion for path " + jsonPath);
        }
        if (foundNode == null) return;
        JsonProvider jsonProvider = jsonMaskerConfiguration.getJsonWayConfiguration().jsonProvider();
        if (isPrimitive(foundNode)) {
            Object unwrappedValue = jsonProvider.unwrap(foundNode);
            documentContext.set(jsonPath, maskFunction.mask(unwrappedValue.toString()));
        } else {
            if (jsonProvider.isArray(foundNode)) {
                Iterator<?> iterator = jsonProvider.toIterable(foundNode).iterator();
                int index = 0;
                while (iterator.hasNext()) {
                    traverseTree(documentContext, iterator.next(), createArrayJsonPath(jsonPath, index), maskFunction, recursionLevel + 1);
                    index++;
                }
            } else if (jsonProvider.isMap(foundNode)) {
                Collection<String> propertiesKeys = jsonProvider.getPropertyKeys(foundNode);
                for (String propertyKey : propertiesKeys) {
                    Object value = jsonProvider.getMapValue(foundNode, propertyKey);
                    traverseTree(documentContext, value, createMapJsonPath(jsonPath, propertyKey), maskFunction, recursionLevel + 1);
                }
            } else {
                throw new IllegalStateException("Unsupported type of node" + foundNode.getClass());
            }
        }
    }

    private JsonPath createMapJsonPath(JsonPath jsonPath, String propertyKey) {
        return JsonPath.compile(jsonPath.getPath() + "['" + propertyKey + "']");
    }

    private JsonPath createArrayJsonPath(JsonPath jsonPath, int index) {
        return JsonPath.compile(jsonPath.getPath() + "[" + index + "]");
    }
}
