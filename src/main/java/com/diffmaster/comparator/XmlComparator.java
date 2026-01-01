package com.diffmaster.comparator;

import com.diffmaster.core.*;
import com.diffmaster.exception.ComparisonException;
import com.diffmaster.rules.ComparisonRules;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;

/**
 * XML comparison with namespace support.
 */
public class XmlComparator extends AbstractComparator {

    @Override
    public String getFormat() {
        return "xml";
    }

    @Override
    public boolean supports(String format) {
        return "xml".equalsIgnoreCase(format);
    }

    @Override
    public ComparisonResult compare(String expected, String actual, ComparisonRules rules) {
        reset();
        long startTime = System.currentTimeMillis();

        try {
            Document expectedDoc = parseXml(expected);
            Document actualDoc = parseXml(actual);

            compareNodes(expectedDoc.getDocumentElement(), actualDoc.getDocumentElement(),
                    FieldPath.root(), rules);

            long duration = System.currentTimeMillis() - startTime;
            return buildResult("expected.xml", "actual.xml", duration);
        } catch (Exception e) {
            throw new ComparisonException("Invalid XML: " + e.getMessage(), e);
        }
    }

    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private void compareNodes(Node expected, Node actual, FieldPath path, ComparisonRules rules) {
        if (expected == null && actual == null) {
            return;
        }

        if (expected == null) {
            recordAdded(path, nodeToString(actual));
            return;
        }

        if (actual == null) {
            recordRemoved(path, nodeToString(expected));
            return;
        }

        // Compare node names
        String expName = getNodeName(expected);
        String actName = getNodeName(actual);
        FieldPath nodePath = path.child(expName);

        if (rules.shouldIgnore(nodePath, null, null)) {
            return;
        }

        if (!expName.equals(actName)) {
            recordModified(nodePath, expName, actName);
            return;
        }

        // Compare attributes
        compareAttributes(expected, actual, nodePath, rules);

        // Compare text content for leaf nodes
        if (isLeafNode(expected) && isLeafNode(actual)) {
            String expText = getTextContent(expected);
            String actText = getTextContent(actual);
            if (!rules.areValuesEqual(expText, actText)) {
                recordModified(nodePath, expText, actText);
            } else {
                recordMatch(nodePath, expText);
            }
            return;
        }

        // Compare child elements
        List<Node> expChildren = getElementChildren(expected);
        List<Node> actChildren = getElementChildren(actual);

        if (rules.isIgnoreArrayOrder()) {
            compareChildrenUnordered(expChildren, actChildren, nodePath, rules);
        } else {
            compareChildrenOrdered(expChildren, actChildren, nodePath, rules);
        }
    }

    private void compareAttributes(Node expected, Node actual, FieldPath path, ComparisonRules rules) {
        NamedNodeMap expAttrs = expected.getAttributes();
        NamedNodeMap actAttrs = actual.getAttributes();

        Set<String> allAttrs = new HashSet<>();
        if (expAttrs != null) {
            for (int i = 0; i < expAttrs.getLength(); i++) {
                allAttrs.add(expAttrs.item(i).getNodeName());
            }
        }
        if (actAttrs != null) {
            for (int i = 0; i < actAttrs.getLength(); i++) {
                allAttrs.add(actAttrs.item(i).getNodeName());
            }
        }

        for (String attr : allAttrs) {
            // Skip namespace declarations
            if (attr.startsWith("xmlns"))
                continue;

            FieldPath attrPath = path.child("@" + attr);
            if (rules.shouldIgnore(attrPath, null, null))
                continue;

            Node expAttr = expAttrs != null ? expAttrs.getNamedItem(attr) : null;
            Node actAttr = actAttrs != null ? actAttrs.getNamedItem(attr) : null;

            if (expAttr == null) {
                recordAdded(attrPath, actAttr.getNodeValue());
            } else if (actAttr == null) {
                recordRemoved(attrPath, expAttr.getNodeValue());
            } else if (!rules.areValuesEqual(expAttr.getNodeValue(), actAttr.getNodeValue())) {
                recordModified(attrPath, expAttr.getNodeValue(), actAttr.getNodeValue());
            } else {
                recordMatch(attrPath, expAttr.getNodeValue());
            }
        }
    }

    private void compareChildrenOrdered(List<Node> expected, List<Node> actual, FieldPath path, ComparisonRules rules) {
        int maxLen = Math.max(expected.size(), actual.size());
        for (int i = 0; i < maxLen; i++) {
            if (i >= expected.size()) {
                FieldPath childPath = path.child(getNodeName(actual.get(i))).index(i);
                recordAdded(childPath, nodeToString(actual.get(i)));
            } else if (i >= actual.size()) {
                FieldPath childPath = path.child(getNodeName(expected.get(i))).index(i);
                recordRemoved(childPath, nodeToString(expected.get(i)));
            } else {
                compareNodes(expected.get(i), actual.get(i), path, rules);
            }
        }
    }

    private void compareChildrenUnordered(List<Node> expected, List<Node> actual, FieldPath path,
            ComparisonRules rules) {
        boolean[] matchedActual = new boolean[actual.size()];

        for (int i = 0; i < expected.size(); i++) {
            Node expChild = expected.get(i);
            boolean found = false;

            for (int j = 0; j < actual.size(); j++) {
                if (!matchedActual[j] && nodesMatch(expChild, actual.get(j), rules)) {
                    matchedActual[j] = true;
                    found = true;
                    compareNodes(expChild, actual.get(j), path, rules);
                    break;
                }
            }

            if (!found) {
                FieldPath childPath = path.child(getNodeName(expChild));
                recordRemoved(childPath, nodeToString(expChild));
            }
        }

        for (int j = 0; j < actual.size(); j++) {
            if (!matchedActual[j]) {
                FieldPath childPath = path.child(getNodeName(actual.get(j)));
                recordAdded(childPath, nodeToString(actual.get(j)));
            }
        }
    }

    private boolean nodesMatch(Node a, Node b, ComparisonRules rules) {
        if (!getNodeName(a).equals(getNodeName(b)))
            return false;
        if (isLeafNode(a) && isLeafNode(b)) {
            return rules.areValuesEqual(getTextContent(a), getTextContent(b));
        }
        return nodeToString(a).equals(nodeToString(b));
    }

    private String getNodeName(Node node) {
        String name = node.getLocalName();
        return name != null ? name : node.getNodeName();
    }

    private boolean isLeafNode(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return false;
            }
        }
        return true;
    }

    private String getTextContent(Node node) {
        return node.getTextContent().trim();
    }

    private List<Node> getElementChildren(Node node) {
        List<Node> children = new ArrayList<>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                children.add(childNodes.item(i));
            }
        }
        return children;
    }

    private String nodeToString(Node node) {
        return node.getTextContent().trim();
    }
}
