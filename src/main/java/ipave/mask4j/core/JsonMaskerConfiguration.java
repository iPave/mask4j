package ipave.mask4j.core;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

public class JsonMaskerConfiguration {

    private static final int DEFAULT_RECURSION_DEPTH = 20;

    private final boolean maskRecursively;

    private final int recursionDepth;

    private final boolean throwWhenCantFindTarget;

    private final Configuration jsonWayConfiguration;

    public JsonMaskerConfiguration() {
        this(true, DEFAULT_RECURSION_DEPTH, true);
    }

    public JsonMaskerConfiguration(boolean maskRecursively, int recursionDepth, boolean throwWhenCantFindTarget, Configuration jsonWayConfiguration) {
        this.maskRecursively = maskRecursively;
        this.throwWhenCantFindTarget = throwWhenCantFindTarget;
        this.recursionDepth = recursionDepth;
        this.jsonWayConfiguration = jsonWayConfiguration;
    }

    public JsonMaskerConfiguration(boolean maskRecursively, boolean throwWhenCantFindTarget) {
        this(maskRecursively, DEFAULT_RECURSION_DEPTH, throwWhenCantFindTarget, Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider()).build());
    }

    public JsonMaskerConfiguration(boolean maskRecursively, int recursionDepth, boolean throwWhenCantFindTarget) {
        this(maskRecursively, recursionDepth, throwWhenCantFindTarget, Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider()).build());
    }

    static JsonMaskerConfiguration defaultConfiguration() {
        return new JsonMaskerConfiguration(true, true);
    }

    public boolean maskRecursively() {
        return maskRecursively;
    }

    public int getRecursionDepth() {
        return recursionDepth;
    }

    public boolean throwWhenCantFindTarget() {
        return throwWhenCantFindTarget;
    }

    public Configuration getJsonWayConfiguration() {
        return jsonWayConfiguration;
    }
}
