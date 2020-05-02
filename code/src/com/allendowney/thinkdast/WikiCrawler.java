package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;


public class WikiCrawler {
	// keeps track of where we started
	@SuppressWarnings("unused")
	private final String source;

	// the index where the results go
	private JedisIndex index;

	// queue of URLs to be indexed
	private Queue<String> queue = new LinkedList<String>();

	// fetcher used to get pages from Wikipedia
	final static WikiFetcher wf = new WikiFetcher();

	/**
	 * Constructor.
	 *
	 * @param source
	 * @param index
	 */
	public WikiCrawler(String source, JedisIndex index) {
		this.source = source;
		this.index = index;
		queue.offer(source);
	}

	/**
	 * Returns the number of URLs in the queue.
	 *
	 * @return
	 */
	public int queueSize() {
		return queue.size();
	}

	/**
	 * Gets a URL from the queue and indexes it.
	 * @param testing
	 *
	 * @return URL of page indexed.
	 * @throws IOException
	 */
	public String crawl(boolean testing) throws IOException {
		if (queue.isEmpty()) {
			return null;
		}
		String url = queue.poll();

		Elements paragraphs;
		if (testing) {
			paragraphs = wf.readWikipedia(url);
		} else {
			// If already indexed, return null
			if (index.isIndexed(url)) {
				return null;
			}

			paragraphs = wf.fetchWikipedia(url);
		}

		index.indexPage(url, paragraphs);
		queueInternalLinks(paragraphs);

		return url;
	}

	/**
	 * Parses paragraphs and adds internal links to the queue.
	 * 
	 * @param paragraphs
	 */
	// NOTE: absence of access level modifier means package-level
	void queueInternalLinks(Elements paragraphs) {
		Deque<Node> stack = new ArrayDeque<>(paragraphs);

		int parenthesisBalance = 0;
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
				if (!element.attr("href").startsWith("/") || element.attr("href").startsWith("//")) {
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

				queue.add("https://en.wikipedia.org" + element.attr("href"));
			} else if (node instanceof TextNode) {
				TextNode textNode = (TextNode) node;
				for (Character c: textNode.text().toCharArray()) {
					if (c == '(') {
						parenthesisBalance++;
					} else if (c == ')') {
						parenthesisBalance--;
					}
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		// make a WikiCrawler
		Jedis jedis = JedisMaker.make();
		jedis.flushAll();
		JedisIndex index = new JedisIndex(jedis);
		String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		WikiCrawler wc = new WikiCrawler(source, index);
		
		// for testing purposes, load up the queue
		Elements paragraphs = wf.fetchWikipedia(source);
		wc.queueInternalLinks(paragraphs);

		// loop until we index a new page
		String res;
		int iterations = 3;
		do {
			res = wc.crawl(false);
		} while (res == null || --iterations > 0);

		Map<String, Integer> map = index.getCounts("the");
		for (Entry<String, Integer> entry: map.entrySet()) {
			System.out.println(entry);
		}
	}
}
