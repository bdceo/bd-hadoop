package com.bdsoft.hot.bean;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class XMNamespaceContext implements NamespaceContext {
	private String ns;
	
	public XMNamespaceContext(String ns){
		this.ns = ns;
	}
	@Override
	public String getNamespaceURI(String prefix) {
		// TODO Auto-generated method stub
		if(prefix == null){
			throw new NullPointerException("Null prefix");
		}else if(prefix.equals("pre")){
			return this.ns;
		}else if(prefix.equals("xml")){
			return XMLConstants.XML_NS_URI;
		}
		return XMLConstants.XML_NS_URI;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator getPrefixes(String namespaceURI) {
		// TODO Auto-generated method stub
		return null;
	}

}
