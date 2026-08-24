package games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import games.fatboychummy.cc_tmp.Cc_tmp;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.color.ColorCloseNode;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.color.ColorOpenNode;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.hover.HoverCloseNode;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.parsing.nodes.hover.HoverOpenNode;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style.HoverData;
import games.fatboychummy.cc_tmp.client.scanner.docs.render.markdown.style.MarkdownSpan;

import java.util.*;

public abstract class MarkdownVisitor extends NodeVisitor {
    private static final List<MarkdownSpan> spans = new ArrayList<>();
    private static final NodeVisitor visitor = new NodeVisitor(
            new VisitHandler<>(Text.class, MarkdownVisitor::visit),
            new VisitHandler<>(Heading.class, MarkdownVisitor::visit),
            new VisitHandler<>(StrongEmphasis.class, MarkdownVisitor::visit),
            new VisitHandler<>(Emphasis.class, MarkdownVisitor::visit),
            new VisitHandler<>(HardLineBreak.class, MarkdownVisitor::visit),
            new VisitHandler<>(SoftLineBreak.class, MarkdownVisitor::visit),
            new VisitHandler<>(Paragraph.class, MarkdownVisitor::visit),
            new VisitHandler<>(Code.class, MarkdownVisitor::visit),
            new VisitHandler<>(ColorOpenNode.class, MarkdownVisitor::visit),
            new VisitHandler<>(ColorCloseNode.class, MarkdownVisitor::visit),
            new VisitHandler<>(HoverOpenNode.class, MarkdownVisitor::visit),
            new VisitHandler<>(HoverCloseNode.class, MarkdownVisitor::visit)
    );

    public static void reset() {
        MDStyleState.reset();
        spans.clear();
    }

    public static NodeVisitor getVisitor() {
        return visitor;
    }

    public static void dump(Node node) {
        dump(node, 0);
    }
    public static void dump(Node node, int depth) {
        String indent = " ".repeat(depth);
        String info = node.getClass().getSimpleName();

        if (node instanceof Text) {
            info += " \"" + ((Text) node).getChars() + "\"";
        } else if (node.getChars() != null && !node.getChars().isEmpty()) {
            info += " [" + node.getChars() + "]";
        }
        Cc_tmp.LOGGER.info("[AST]: {}", indent + info);

        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            dump(child, depth + 1);
        }
    }

    public static void visit(Text text) {
        if (text.getChars().isEmpty()) {
            Cc_tmp.LOGGER.info("(MDVisitor) Empty Text");
            return;
        }

        Cc_tmp.LOGGER.info("(MDVisitor) Text {}", text);
        spans.add(new MarkdownSpan(
                text.getChars().toString(),
                MDStyleState.current()
        ));
    }

    public static void visit(StrongEmphasis strongEmphasis) {
        Cc_tmp.LOGGER.info("(MDVisitor) Bold Enter");
        MDStyleState.pushFrame(MDStyleFrame.boldFrame(true));

        visitor.visitChildren(strongEmphasis);

        MDStyleState.popFrame();
        Cc_tmp.LOGGER.info("(MDVisitor) Bold Exit");
    }

    public static void visit(Emphasis emphasis) {
        Cc_tmp.LOGGER.info("(MDVisitor) Italic Enter");
        MDStyleState.pushFrame(MDStyleFrame.italicFrame(true));

        visitor.visitChildren(emphasis);

        MDStyleState.popFrame();
        Cc_tmp.LOGGER.info("(MDVisitor) Italic Exit");
    }

    public static void visit(Heading heading) {
        Cc_tmp.LOGGER.info("(MDVisitor) Heading {} Enter: {}", heading.getLevel(), heading.getText());
        spans.add(newlineSpan());
        MDStyleFrame headingFrame = getHeadingStyle(heading.getLevel());
        MDStyleState.pushFrame(headingFrame);
        if (heading.getLevel() > 1) {
            spans.add(new MarkdownSpan(" ".repeat(heading.getLevel() - 1), MDStyleState.current()));
        }

        visitor.visitChildren(heading);

        spans.add(newlineSpan()); // Headings create their own paragraphs!
        MDStyleState.reset(); // Lazy handling: We just reset the state after a heading. Headings should always be "main" blocks anyway.
        Cc_tmp.LOGGER.info("(MDVisitor) Heading {} Exit", heading.getLevel());
    }

    public static void visit(HardLineBreak hardLineBreak) {
        Cc_tmp.LOGGER.info("(MDVisitor) Line Break");
        spans.add(newlineSpan());
    }

    public static void visit(SoftLineBreak softLineBreak) {
        Cc_tmp.LOGGER.info("(MDVisitor) Soft Break");
        spans.add(charSpan(' '));
    }

    public static void visit(ColorOpenNode colorOpenNode) {
        Cc_tmp.LOGGER.info("(MDVisitor) Color {}", colorOpenNode.getColor());
        MDStyleState.pushFrame(MDStyleFrame.colorFrame(colorOpenNode.getColor()));
    }

    public static void visit(ColorCloseNode colorCloseNode) {
        Cc_tmp.LOGGER.info("(MDVisitor) Color close");
        MDStyleState.popFrame();
    }

    public static void visit(Paragraph paragraph) {
        Cc_tmp.LOGGER.info("(MDVisitor) Paragraph");

        visitor.visitChildren(paragraph);

        spans.add(newlineSpan());
    }

    public static void visit(Code inlineCode) {
        Cc_tmp.LOGGER.info("(MDVisitor) Inline Code");
        MDStyleState.pushFrame(getInlineCodeStyle());

        visitor.visitChildren(inlineCode);

        MDStyleState.popFrame();
    }

    public static void visit(HoverOpenNode hoverOpenNode) {
        Cc_tmp.LOGGER.info("(MDVisitor) Hover Open");
        MDStyleState.pushFrame(hoverData(hoverOpenNode.getHoverText()));
    }

    public static void visit(HoverCloseNode hoverCloseNode) {
        Cc_tmp.LOGGER.info("(MDVisitor) Hover Close");
        MDStyleState.popFrame();
    }


    private static MarkdownSpan newlineSpan() {
        return charSpan('\n');
    }

    private static MarkdownSpan charSpan(char ch) {
        return new MarkdownSpan(String.valueOf(ch), MDStyleState.current());
    }

    public static List<MarkdownSpan> getSpans() {
        return spans;
    }

    private static MDStyleFrame hoverData(String text) {
        return new MDStyleFrame(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new HoverData(text),
                null
        );
    }

    private static MDStyleFrame getInlineCodeStyle() {
        return new MDStyleFrame(
                0xababab,
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                null
        );
    }

    private static MDStyleFrame getHeadingStyle(int headingLevel) {
        MDStyleFrame base = MDStyleFrame.emptyFrame();
        return switch (headingLevel) {
            case 1 -> base
                    .boldThis(true)
                    .underlineThis(true)
                    .sizeThis(2.5F);

            case 2 -> base
                    .boldThis(true)
                    .sizeThis(2.0F);

            case 3 -> base
                    .boldThis(true)
                    .sizeThis(1.5F);

            case 4 -> base
                    .sizeThis(1.5F);

            case 5 -> base
                    .italicThis(true)
                    .colorThis(0xCBCBCB);

            case 6 -> base
                    .italicThis(true)
                    .colorThis(0xBBBBBB);

            default -> throw new IllegalStateException("Unexpected value: " + headingLevel);
        };
    }
}
