package org.apache.camel.model.rest;

import org.apache.camel.spi.Metadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by seb on 5/13/15.
 */
@Metadata(label = "rest")
@XmlRootElement(name = "param")
@XmlAccessorType(XmlAccessType.FIELD)
public class Param {
    @XmlAttribute
    String type;
    @XmlAttribute
    String annotatedCls;

    public Param() {
    
    }

    public Param(String header, String anotatedClass) {
        this.type = header;
        this.annotatedCls = anotatedClass;
    }

    public String getAnnotatedCls() {
        return annotatedCls;
    }

    public void setAnnotatedCls(String annotatedCls) {
        this.annotatedCls = annotatedCls;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
