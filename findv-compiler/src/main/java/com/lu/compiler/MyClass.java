package com.lu.compiler;

import com.squareup.javapoet.ClassName;

/**
 * Created by Administrator on 2016/8/4.
 */
public class MyClass {

    public static void main(String[] args){

        String str = "com.android.view.View.Holder";
        ClassName className = ClassName.bestGuess(str);
        System.out.print(className.simpleName());

    }
}
