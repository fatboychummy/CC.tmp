package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing;

import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style.MDStyle;

import java.util.Stack;

public class MDStyleState {
    private static final Stack<MDFinalizedStyleFrame> frameStack = new Stack<>();

    static <T> T inherit(T delta, T current) {
        return delta != null ? delta : current;
    }

    /**
     * Not to be used by you, nerd.
     * @param frame The finalized frame.
     */
    public static void pushFrame(MDFinalizedStyleFrame frame) {
        frameStack.push(frame);
    }

    public static void pushFrame(MDStyleFrame delta) {
        MDFinalizedStyleFrame currentFrame = frameStack.peek();
        pushFrame(new MDFinalizedStyleFrame(
                inherit(delta.color(), currentFrame.color()),
                inherit(delta.size(), currentFrame.size()),
                inherit(delta.bold(), currentFrame.bold()),
                inherit(delta.italic(), currentFrame.italic()),
                inherit(delta.underline(), currentFrame.underline()),
                inherit(delta.strikethrough(), currentFrame.strikethrough()),
                inherit(delta.monospace(), currentFrame.monospace()),
                inherit(delta.hover(), currentFrame.hover()),
                inherit(delta.click(), currentFrame.click())
        ));
    }

    public static void popFrame() {
        frameStack.pop();
    }

    public static void reset() {
        pushFrame(MDFinalizedStyleFrame.defaultFrame());
    }

    public static MDStyle current() {
        MDFinalizedStyleFrame currentFrame = frameStack.peek();
        return new MDStyle(
                currentFrame.color(),
                currentFrame.size(),
                currentFrame.bold(),
                currentFrame.italic(),
                currentFrame.underline(),
                currentFrame.strikethrough(),
                currentFrame.monospace(),
                currentFrame.hover(),
                currentFrame.click()
        );
    }
}
