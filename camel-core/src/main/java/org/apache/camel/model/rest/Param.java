package org.apache.camel.model.rest;

/**
 * Created by seb on 5/13/15.
 */
public class Param {
    String type;
    Class annotatedCls;

    public Param(String header, Class anotatedClass) {
        this.type=header;
        this.annotatedCls=anotatedClass;
    }

    public Class getAnnotatedCls() {
        return annotatedCls;
    }

    public void setAnnotatedCls(Class annotatedCls) {
        this.annotatedCls = annotatedCls;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
