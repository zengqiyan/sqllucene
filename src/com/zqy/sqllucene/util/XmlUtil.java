package com.zqy.sqllucene.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class XmlUtil {
	/* 
     * 获得X属性结果是X值的整个标签
     
    public static Element parse(Element node , String type , String val) {
        for (Iterator iter = node.elementIterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            Attribute name = element.attribute(type);
            if (name != null) {
                String value = name.getValue();
                if (value != null && val.equals(value))
                    return element;
            }
        }
        return null;
    }*/
    public static Element parse(Element parent,String elementName,String attributeName , String attributeValue) {
    	List<Element> parentElements = parent.elements(elementName);
    	if(parentElements!=null && parentElements.size()>0){
    	for(Element element:parentElements){
    		Attribute name = element.attribute(attributeName);
            if (name != null) {
                String value = name.getValue();
                if (value != null && attributeValue.equals(value))
                    return element;
            }
    	}
    	}
        return null;
    }
    public static List<Element> parse(Element parent,String elementName,String attributeName, List<String> attributeValues) {
    	List<Element> parentElements = parent.elements(elementName);
    	List<Element> elements = new ArrayList<Element>();
    	if(parentElements!=null && parentElements.size()>0){
    	for(int i=0;i<parentElements.size();i++){
    		for(int j=0;j<attributeValues.size();j++){
    			Element element = parentElements.get(i);
    			Attribute name = element.attribute(attributeName);
                if (name != null) {
                    String value = name.getValue();
                    if (value != null && attributeValues.get(j).equals(value))
                    	elements.add(element);
                        attributeValues.remove(j);
                        break;
                }
    		}
    		
    	}
    	}
        return null;
    }
}
