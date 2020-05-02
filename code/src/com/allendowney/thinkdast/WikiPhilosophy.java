package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.*;

import com.sun.istack.internal.Nullable;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class WikiPhilosophy {

    final static List<String> visited = new ArrayList<String>();
    final static WikiFetcher wf = new WikiFetcher();
    public static final String HOST = "https://en.wikipedia.org";

    /**
     * Tests a conjecture about Wikipedia and Philosophy.
     *
     * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
     *
     * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        testConjecture(destination, source, 10);
    }

    /**
     * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
     *
     * @param destination
     * @param source
     * @throws IOException
     */
    public static void testConjecture(String destination, String source, int limit) throws IOException {
        WikiFetcher wf = new WikiFetcher();

        List<String> visited = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            Elements paragraphs = wf.fetchWikipedia(source);
            visited.add(source);
            source = extractFirstURL(paragraphs, source);

            if (source == null) {
                finish("Fail: no links found on iteration", visited);

            }

            if (visited.contains(source)) {
                finish("Fail: link already visited", visited);
            }

            if (source.equals(destination)) {
                finish("Success", visited);
            }
        }

        if (source.equals(destination)) {
            finish("Fail: limit exceeded", visited);
        }
    }

    @Nullable
    private static String extractFirstURL(Elements paragraphs, String currentLink) throws IOException {
        Deque<Node> stack = new ArrayDeque<>(paragraphs);

        Integer parenthesisBalance = 0;
        while (!stack.isEmpty()) {
            Node node = stack.pop();

            if (node instanceof Element) {
                Element element = (Element) node;
                stack.addAll(element.childNodes());

                // Skip not link
                if (!element.tagName().equals("a")) {
                    continue;
                }

                // Skip image related
                if (element.hasClass("image")) {
                    continue;
                }

                // Skip external
                if (!element.attr("href").startsWith("/")) {
                    continue;
                }

                // Skip current
                if ((HOST + element.attr("href")).equals(currentLink)) {
                    continue;
                }

                // Skip in parenthesis
                if (parenthesisBalance > 0) {
                    continue;
                }

                // Skip italic
                Element parent = element.parent();
                boolean isItalic = false;
                while (parent != null) {
                    if (parent.tagName().equals("i") || (parent.tagName().equals("em"))) {
                        isItalic = true;
                        break;
                    }
                    parent = parent.parent();
                }
                if (isItalic) {
                    continue;
                }

                return HOST + element.attr("href");
            } else if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                for (Character c: textNode.text().toCharArray()) {
                    if (c == '(') {
                        System.out.println("(");
                        parenthesisBalance++;
                    } else if (c == ')') {
                        parenthesisBalance--;
                        System.out.println(")");
                    }
                }
            }
        }

        return null;
    }

    private static void finish(String message, List<String> visited) {
        System.out.println(message);
        System.out.println(visited);
        System.exit(0);
    }
}
