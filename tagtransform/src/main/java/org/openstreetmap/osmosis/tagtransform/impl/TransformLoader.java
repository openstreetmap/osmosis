// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.tagtransform.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openstreetmap.osmosis.tagtransform.Matcher;
import org.openstreetmap.osmosis.tagtransform.Output;
import org.openstreetmap.osmosis.tagtransform.TTEntityType;
import org.openstreetmap.osmosis.tagtransform.Translation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class TransformLoader {
	private static final Logger LOG = Logger.getLogger(TransformLoader.class.getName());


	public List<Translation> load(String configFile) {
		List<Translation> translations = new ArrayList<Translation>();
		File file = new File(configFile);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);

			NodeList translationElements = doc.getDocumentElement().getElementsByTagName("translation");
			for (int i = 0; i < translationElements.getLength(); i++) {
				Translation t = parseTranslation((Element) translationElements.item(i));
				if (t != null) {
					translations.add(t);
				}
			}
		} catch (Exception e) {
			throw new TransformLoadException("Failed to load transform", e);
		}
		return translations;
	}


	private Translation parseTranslation(Element element) {
		String name = "";
		String description = "";
		Matcher matcher = null;
		Matcher finder = null;
		List<Output> output = new ArrayList<Output>();

		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (!(children.item(i) instanceof Element)) {
				continue;
			}
			Element child = (Element) children.item(i);
			String nodeName = child.getNodeName();
			if (nodeName.equals("name")) {
				name = child.getTextContent();
			} else if (nodeName.equals("description")) {
				description = child.getTextContent();
			} else if (nodeName.equals("match")) {
				matcher = parseMatcher(child);
			} else if (nodeName.equals("find")) {
				finder = parseMatcher(child);
			} else if (nodeName.equals("output")) {
				NodeList outputs = child.getChildNodes();
				for (int j = 0; j < outputs.getLength(); j++) {
					if (!(outputs.item(j) instanceof Element)) {
						continue;
					}
					Output o = parseOutput((Element) outputs.item(j));
					if (o != null) {
						output.add(o);
					}
				}
			}
		}

		if (matcher != null) {
			LOG.info("New translation: " + name);
			return new TranslationImpl(name, description, matcher, finder, output);
		} else {
			return null;
		}
	}


	private Output parseOutput(Element child) {
		String name = child.getNodeName();
		if (name.equals("copy-all")) {
			return new CopyAll();
		} else if (name.equals("copy-unmatched")) {
			return new CopyUnmatched();
		} else if (name.equals("copy-matched")) {
			return new CopyMatched();
		} else if (name.equals("tag")) {
			String k = child.getAttribute("k");
			String v = child.getAttribute("v");
			String m = child.getAttribute("from_match");
			return new TagOutput(k, v, m);
		}
		return null;
	}


	private Matcher parseMatcher(Element matcher) {
		String name = matcher.getNodeName();
		if (name.equals("match") || name.equals("find")) {
			NodeList children = matcher.getChildNodes();
			List<Matcher> matchers = new ArrayList<Matcher>();
			String uname = null;
			int uid = 0;

			for (int i = 0; i < children.getLength(); i++) {
				if (!(children.item(i) instanceof Element)) {
					continue;
				}
				Element child = (Element) children.item(i);
				Matcher m = parseMatcher(child);
				if (m != null) {
					matchers.add(m);
				}
			}

			TTEntityType type = getType(matcher.getAttribute("type"));
			if (matcher.getAttribute("user") != "") {
				uname = matcher.getAttribute("user");
			}
			if (matcher.getAttribute("uid") != "") {
				uid = Integer.parseInt(matcher.getAttribute("uid"));
			}
			String mode;
			if (name.equals("find")) {
				mode = "or";
			} else {
				mode = matcher.getAttribute("mode");
			}
			if (mode == null || mode.equals("") || mode.equals("and")) {
				return new AndMatcher(matchers, type, uname, uid);
			} else if (mode.equals("or")) {
				return new OrMatcher(matchers, type, uname, uid);
			}

		} else if (name.equals("tag")) {
			String k = matcher.getAttribute("k");
			String v = matcher.getAttribute("v");
			String id = matcher.getAttribute("match_id");
			return new TagMatcher(id, k, v);
		} else if (name.equals("notag")) {
			String k = matcher.getAttribute("k");
			String v = matcher.getAttribute("v");
			return new NoTagMatcher(k, v);
		}
		return null;
	}


	private TTEntityType getType(String type) {
		if (type == null || type.isEmpty() || type.equals("all")) {
			return null;
		}
		if (type.equals("node")) {
			return TTEntityType.NODE;
		}
		if (type.equals("way")) {
			return TTEntityType.WAY;
		}
		if (type.equals("relation")) {
			return TTEntityType.RELATION;
		}
		if (type.equals("bound")) {
			return TTEntityType.BOUND;
		}
		return null;
	}

}
