package ipave.mask4j.core;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JsonProvider;

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
            JsonPath jsonPath = maskFunctionByPath.getFirst();
            MaskFunction maskFunction = maskFunctionByPath.getSecond();
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
                    traverseTree(foundNode, null, null, foundNode, maskFunction, 0);
                    documentContext.set(jsonPath, foundNode);
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
     * @param parentNode     parent node of 'jsonPath'
     * @param key            parent key if parent is object
     * @param index          parent index if parent is array
     * @param foundNode      node which was found at path 'jsonPath'
     * @param maskFunction   function to be applied to all found nodes
     * @param recursionLevel current recursion level
     */
    private void traverseTree(Object parentNode, String key, Integer index, Object foundNode, MaskFunction maskFunction, int recursionLevel) {
        if (recursionLevel > jsonMaskerConfiguration.getRecursionDepth()) {
            throw new IllegalStateException("Too deep recursion for path " + "jsonPath");
        }
        if (foundNode == null) return;
        JsonProvider jsonProvider = jsonMaskerConfiguration.getJsonWayConfiguration().jsonProvider();
        if (isPrimitive(foundNode)) {
            Object unwrappedValue = jsonProvider.unwrap(foundNode);
            if (key != null) {
                jsonProvider.setProperty(parentNode, key, maskFunction.mask(unwrappedValue.toString()));
            } else {
                jsonProvider.setArrayIndex(parentNode, index, maskFunction.mask(unwrappedValue.toString()));
            }
        } else {
            if (jsonProvider.isArray(foundNode)) {
                Iterator<?> iterator = jsonProvider.toIterable(foundNode).iterator();
                int i = 0;
                while (iterator.hasNext()) {
                    traverseTree(foundNode, null, i, iterator.next(), maskFunction, recursionLevel + 1);
                    i++;
                }
            } else if (jsonProvider.isMap(foundNode)) {
                Collection<String> propertiesKeys = jsonProvider.getPropertyKeys(foundNode);
                for (String propertyKey : propertiesKeys) {
                    Object value = jsonProvider.getMapValue(foundNode, propertyKey);
                    traverseTree(foundNode, propertyKey, null, value, maskFunction, recursionLevel + 1);
                }
            } else {
                throw new IllegalStateException("Unsupported type of node" + foundNode.getClass());
            }
        }
    }
}
