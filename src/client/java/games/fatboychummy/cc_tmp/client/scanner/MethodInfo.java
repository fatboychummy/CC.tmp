package games.fatboychummy.cc_tmp.client.scanner;

import com.demonwav.mcdev.annotations.Translatable;
import games.fatboychummy.cc_tmp.client.scanner.docs.*;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MethodInfo {
    private final Method method;
    private final String modName;
    private final String modVersion;
    private final String blockID;
    private final Language lang;

    // Collected data
    private String methodName;
    private boolean mainThread;
    private boolean unsafe;
    //private @Nullable AnnDoc annDoc;
    private String methodDescription;
    private String[] methodArguments;
    private String[] methodArgumentsDescriptions;
    private String[] methodReturns;
    private String[] methodReturnsTypes;
    private String[] methodReturnsDescriptions;

    public MethodInfo(Method m, String modName, String modVersion, String blockID) {
        this.method = m;
        this.modName = modName;
        this.modVersion = modVersion;
        this.blockID = blockID;
        this.lang = Language.getInstance();
        collectMethodDetails();
    }

    public void collectMethodDetails() {
        //ResourceLocation base = new ResourceLocation(
        //        "cc_tmp",
        //        "peripheral_docs"
        //);
        String base = "docs.cc_tmp." + modName + "." + modVersion + "." + blockID + ".";

        methodDescription = hasOrDefault(
                base + "description",
                "docs.cc_tmp.no_description"
        );

        int argN = method.getParameterCount();
        methodArguments = new String[argN];
        methodArgumentsDescriptions = new String[argN];
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < argN; i++) {
            methodArguments[i] = parameters[i].getName();

            methodArgumentsDescriptions[i] = hasOrDefault(
                    base + "argument." + i,
                    "docs.cc_tmp.no_argument_description"
            );
        }

        {

        }
    }

    private String hasOrDefault(
            @Translatable(foldMethod = true) String value,
            @Translatable(foldMethod = true) String defaultValue
    ) {
        return lang.has(value) ? value : defaultValue;
    }
}
