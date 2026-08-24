package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing;

import com.vladsch.flexmark.util.ast.Node;
import games.fatboychummy.cc_tmp.client.scanner.docs.PeripheralDoc;
import games.fatboychummy.cc_tmp.client.scanner.docs.PeripheralDocMethod;
import games.fatboychummy.cc_tmp.client.scanner.docs.PeripheralDocParameter;


/**
 * This class' job is to:
 * 1. Take an entire PeripheralDoc
 * 2. Combine the body strings of the doc into a single markdown string.
 * 3. Call MarkdownParser.parse(...) to get the actual parsed object.
 */
public abstract class DocParser {

    /**
     * Parses a *single* peripheral document.
     * @param document The document to parse
     * @return The AST of the document's built markdown.
     */
    public static Node parse(PeripheralDoc document) {
        StringBuilder sb = new StringBuilder();

        // Header: The peripheral's type.
        sb.append("## <color=#ff0000>**");
        sb.append(document.getPeripheralType());
        sb.append("**</color>\n\n");

        // Documentation link
        if (document.getDocSite() != null) {
            sb.append("[<color=#1b75d0>Online Documentation</color>](");
            sb.append(document.getDocSite());
            sb.append(")\n\n");
        }

        // Additional blank space between header and body.
        sb.append("\n\n");

        String[] methodNames = document.getMethodNames();
        for (String methodName : methodNames) {
            PeripheralDocMethod method = document.getMethod(methodName); assert method != null;
            String[] aliases = document.getAliases(methodName);
            PeripheralDocParameter[] params = method.arguments();
            PeripheralDocParameter[] returns = method.returnValues();
            boolean isMainThread = method.mainThread();
            String description = method.description();
            String shortDescription = method.shortDescription();

            sb.append("### `");
            sb.append(methodName);
            sb.append("`(");

            for (int i = 0; i < params.length; i++) {
                PeripheralDocParameter param = params[i];
                sb.append("<hover=\"");
                sb.append(param.shortDescription());
                sb.append("\">`");
                sb.append(param.name());
                sb.append(": ");
                sb.append(param.luaType());
                sb.append("`</hover>");
                if (i < params.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")\n\n");
            sb.append(shortDescription);
            sb.append("\n\n\n\n");
        }


        return MarkdownParser.parse(sb.toString());
    }
}
