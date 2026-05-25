package games.fatboychummy.cc_tmp.client.scanner.docs;

import games.fatboychummy.cc_tmp.exceptions.JsonStructureException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PeripheralDoc {
    public static int loadedMethods = 0;
    private int loadedThisTime = 0;
    private final String modID;
    private final String modVersion;
    private final String peripheralType;
    private final ArrayList<PeripheralDocMethod> methods;
    private final HashMap<String, Integer> methodLookup;

    public final String getUniqueIdentifier() {
        return modID + ":" + modVersion + ":" + peripheralType;
    }

    public final String getModVersion() {
        return modVersion;
    }

    public final String getPeripheralType() {
        return peripheralType;
    }

    public final String getModID() {
        return modID;
    }

    public PeripheralDoc(String modID, String modVersion, String peripheralType) {
        this.modID = modID;
        this.modVersion = modVersion;
        this.peripheralType = peripheralType;
        this.methods = new ArrayList<>();
        this.methodLookup = new HashMap<>();
    }

    /**
     * Debug: Increment loaded methods counter.
     */
    private void loadOne() {
        loadedThisTime++;
        loadedMethods++;
    }

    /**
     * Debug: Remove this object's loaded methods from the counter. Used on failure.
     */
    private void unload() {
        loadedMethods -= loadedThisTime;
    }

    private static PeripheralDoc _fromJson(PeripheralDoc doc, JsonObject json) throws JsonStructureException {
        validateBase(json);

        JsonArray methods = json.getAsJsonArray("methods");
        validateMethods(methods);

        // For recalling alias methods, once we've collected all methods.
        // "AliasMethodName" -> "MethodName"
        HashMap<String, String> recall = new HashMap<>();

        for (JsonElement method : methods) {
            JsonObject methodObject = method.getAsJsonObject();

            String name = methodObject.get("name").getAsString();
            if (methodObject.has("alias")) {
                recall.put(name, methodObject.get("alias").getAsString());
                continue; // Handle this later.
            }

            String description = getStringFromArray(methodObject, "description");
            String shortDescription = methodObject.get("shortDescription").getAsString();
            boolean mainThread = methodObject.get("mainThread").getAsBoolean();
            ArrayList<PeripheralDocParameter> arguments = new ArrayList<>();
            ArrayList<PeripheralDocParameter> returnValues = new ArrayList<>();

            JsonArray argumentsArray = methodObject.getAsJsonArray("arguments");
            JsonArray returnValuesArray = methodObject.getAsJsonArray("returnValues");
            getParameterList(argumentsArray, arguments);
            getParameterList(returnValuesArray, returnValues);

            doc.methods.add(new PeripheralDocMethod(
                    name,
                    description,
                    shortDescription,
                    mainThread,
                    arguments,
                    returnValues
            ));
            doc.loadOne();
        }

        for (HashMap.Entry<String, String> entry : recall.entrySet()) {
            String alias = entry.getKey();
            String actual =  entry.getValue();

            Integer i = doc.methodLookup.get(actual);
            PeripheralDocMethod method = null;
            if (i != null) {
                method = doc.methods.get(i);
            }

            if (i == null || method == null) {
                doc.unload();
                throw new JsonStructureException("Method " + actual + " not found for alias " + alias);
            }

            doc.methods.add(new PeripheralDocMethod(
                    alias,
                    method.description(),
                    method.shortDescription(),
                    method.mainThread(),
                    method.arguments(),
                    method.returnValues()
            ));
            doc.loadOne();
        }

        return doc;
    }

    public static PeripheralDoc fromJson(JsonObject json) throws JsonStructureException {
        validateBase(json);
        PeripheralDoc doc = new PeripheralDoc(
                json.get("modID").getAsString(),
                json.get("modVersion").getAsString(),
                json.get("peripheralType").getAsString()
        );
        try {
            return _fromJson(doc, json);
        } catch (JsonStructureException e) {
            doc.unload();
            throw e;
        }
    }

    private static String getStringFromArray(JsonObject json, String key) {
        try {
            return json.get(key).getAsString();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder();
            for (JsonElement element : json.getAsJsonArray(key)) {
                sb.append(element.getAsString());
            }
            return sb.toString();
        }
    }

    private static void getParameterList(JsonArray array, ArrayList<PeripheralDocParameter> parameters) {
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            String name = object.get("name").getAsString();
            String description = getStringFromArray(object, "description");
            String shortDescription = object.get("shortDescription").getAsString();
            String luaType = object.get("luaType").getAsString();
            parameters.add(new PeripheralDocParameter(name, description, shortDescription, luaType));
        }
    }

    private static void existOrExcept(JsonObject json, String key) throws JsonStructureException {
        if (json.has(key)) {
            return;
        }

        throw new JsonStructureException("The key " + key + " does not exist");
    }

    private static void stringOrExcept(JsonObject json, String key) {
        existOrExcept(json, key);
        try {
            json.get(key).getAsString();
        } catch (Exception e) {
            throw new JsonStructureException("The key " + key + " is not a string");
        }
    }

    private static void stringOrStringArrayOrExcept(JsonObject json, String key) {
        existOrExcept(json, key);
        try {
            stringOrExcept(json, key);
        } catch (Exception e) {
            try {
                arrayOrExcept(json, key);
                JsonArray array = json.getAsJsonArray(key);
                for (JsonElement element : array) {
                    element.getAsString();
                }
            } catch (Exception ex) {
                throw new JsonStructureException("The key " + key + " is not a string or string array");
            }
        }
    }

    private static void arrayOrExcept(JsonObject json, String key) {
        existOrExcept(json, key);
        if (json.get(key).isJsonArray()) {
            return;
        }

        throw new JsonStructureException("The key " + key + " is not an array");
    }

    private static void booleanOrExcept(JsonObject json, String key) {
        existOrExcept(json, key);
        try {
            json.get(key).getAsBoolean();
        } catch (Exception e) {
            throw new JsonStructureException("The key " + key + " is not a boolean");
        }
    }

    private static void arrayObjectOrExcept(JsonArray array, int key) {
        if (array.get(key).isJsonPrimitive() || array.get(key).isJsonArray() || !array.get(key).isJsonObject()) {
            throw new JsonStructureException("The element " + key + " is not an object");
        }
    }

    /**
     * Validates that the base object contains all required values, of the correct types.
     * @param json The root JSON object.
     * @throws JsonStructureException On validation failure.
     */
    private static void validateBase(JsonObject json) throws JsonStructureException {
        stringOrExcept(json, "modID");
        stringOrExcept(json, "modVersion");
        stringOrExcept(json, "peripheralType");

        arrayOrExcept(json, "methods");
    }

    private static void validateMethods(JsonArray array) throws JsonStructureException {
        for (int i = 0; i < array.size(); i++) {
            arrayObjectOrExcept(array, i);
            validateMethod(array.get(i).getAsJsonObject());
        }
    }

    /**
     * Validates a single method entry.
     * @param json The JSON object containing the method.
     */
    private static void validateMethod(JsonObject json) throws JsonStructureException {
        stringOrExcept(json, "name");
        if (json.has("alias")) {
            stringOrExcept(json, "alias");
            return; // This is an alias of another peripheral method. Continue.
        }

        // Not an alias, so we should expect all other information.
        stringOrExcept(json, "description");
        stringOrExcept(json, "shortDescription");
        booleanOrExcept(json, "mainThread");
        arrayOrExcept(json, "arguments");
        arrayOrExcept(json, "returnValues");

        validateArguments(json.get("arguments").getAsJsonArray());
        validateReturnValues(json.get("returnValues").getAsJsonArray());
    }

    private static void validateArguments(JsonArray array) throws JsonStructureException {
        for (int i = 0; i < array.size(); i++) {
            arrayObjectOrExcept(array, i);
            validateArgumentOrReturnValue(array.get(i).getAsJsonObject());
        }
    }

    private static void validateReturnValues(JsonArray array) throws JsonStructureException {
        for (int i = 0; i < array.size(); i++) {
            arrayObjectOrExcept(array, i);
            validateArgumentOrReturnValue(array.get(i).getAsJsonObject());
        }
    }

    private static void validateArgumentOrReturnValue(JsonObject json) throws JsonStructureException {
        stringOrExcept(json, "name");
        stringOrStringArrayOrExcept(json, "description");
        stringOrExcept(json, "shortDescription");
        stringOrExcept(json, "luaType");
        booleanOrExcept(json, "vararg");
        booleanOrExcept(json, "required");
    }
}
