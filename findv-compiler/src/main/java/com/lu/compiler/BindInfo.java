package com.lu.compiler;

import javax.lang.model.type.TypeMirror;


final class BindInfo {
    public final String name;
    public final TypeMirror type;
    public final int id;

    public BindInfo(String name, TypeMirror type, int id) {
        this.name = name;
        this.type = type;
        this.id = id;
    }
}
