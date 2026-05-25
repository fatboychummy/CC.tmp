package games.fatboychummy.cc_tmp.client.scanner.docs;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AnnDoc {
    /**
     * Explicitly specify the name or names of the method as exposed to Lua.
     * @return This method's name(s)
     */
    String[] names() default {};

    /**
     * The long description of this method.
     * @return This method's description.
     */
    String description() default "No description available.";

    /**
     * The argument types the method expects. Use `|` to split multiple argument types for the same argument.
     * This should be in Lua types, as strings. For example, `string`, `table`, `number`, etc.
     * @return The list of argument types, in order.
     */
    String[] luaArgumentTypes() default {};

    /**
     * The descriptions of what each argument is, in order.
     * @return The descriptions of each argument, in order.
     */
    String[] luaArgumentDescriptions() default {};
}
