package com.lu.compiler;

import com.google.auto.service.AutoService;
import com.lu.findv.FindV;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;


@AutoService(Processor.class)
public class FindProcessor extends AbstractProcessor {

    private final ClassName VIEW_BIND = ClassName.get("lu.inject", "ViewBinder");
    private static final String BINDING_CLASS_SUFFIX = "$$ViewBinder";//生成类的后缀 以后会用反射去取

    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        mElementUtils = env.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //该map用于存储每一个类对象和它内部被注解的变量信息
        Map<TypeElement, List<BindInfo>> targetClassMap = new LinkedHashMap<>();
        Set<? extends Element> elementSet = roundEnv.getElementsAnnotatedWith(FindV.class);

        for (Element element : elementSet) {
            //元素所在的类信息
            TypeElement type = (TypeElement) element.getEnclosingElement();

            if (isClassLegal(element, type)) {
                continue;
            }

            if (!isFieldLegal(element.asType())) {
                continue;
            }

            List<BindInfo> bindInfoList = targetClassMap.get(type);

            if (bindInfoList == null) {
                bindInfoList = new ArrayList<>();
                targetClassMap.put(type, bindInfoList);
            }

            /**
             * R.id.xxx
             */
            int id = element.getAnnotation(FindV.class).value();
            /**
             * 变量名
             */
            String fieldName = element.getSimpleName().toString();
            /**
             * 对应的变量类型 例如  TextView
             */
            TypeMirror fieldType = element.asType();

            BindInfo bindInfo = new BindInfo(fieldName, fieldType, id);
            bindInfoList.add(bindInfo);
        }


        for (Map.Entry<TypeElement, List<BindInfo>> item : targetClassMap.entrySet()) {
            List<BindInfo> list = item.getValue();
            if (list == null || list.size() == 0)
                continue;
            //类类型
            TypeElement classType = item.getKey();
            //类包名
            String packageName = getPackageName(classType);
            //类名称
            String classNameStr = getClassName(classType, packageName);
            ClassName className = ClassName.bestGuess(classNameStr);


            TypeSpec typeSpec = TypeSpec.classBuilder(classNameStr.replace(".", "$") + BINDING_CLASS_SUFFIX)

                    .addModifiers(Modifier.PUBLIC)
//                    .addTypeVariable(TypeVariableName.get("T", className))
                    .addSuperinterface(ParameterizedTypeName.get(VIEW_BIND, className))
                    .addMethod(createBindMethod(list, className))
                    .build();

            JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                    .addFileComment(" This codes are generated automatically. Do not modify!")
                    .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        return true;
    }

    /**
     * 复写bind方法
     *
     * @param list
     * @param className
     * @return 这里分两种情况，当source为空的时候 ，是通过target本身的findViewById()，如果source部位空，是通过View.findViewById();
     */

    private MethodSpec createBindMethod(List<BindInfo> list, ClassName className) {

        ClassName viewC = ClassName.get("android.view", "View");

        MethodSpec.Builder builder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.VOID)
                .addParameter(className, "target", Modifier.FINAL)
                .addParameter(TypeName.OBJECT, "source", Modifier.FINAL);

        //用于判断是否为内部类
        if (className.simpleNames().size() == 1) {

            builder.beginControlFlow("if (source==null)");
            for (int i = 0; i < list.size(); i++) {
                BindInfo bind = list.get(i);

                String packageStr = bind.type.toString();
                ClassName viewClass = ClassName.bestGuess(packageStr);

                builder.addStatement("target.$L = ($T)target.findViewById($L)", bind.name, viewClass, bind.id);

            }
            builder.endControlFlow().beginControlFlow("else");
        }
        for (int i = 0; i < list.size(); i++) {
            BindInfo bind = list.get(i);

            String packageStr = bind.type.toString();
            ClassName viewClass = ClassName.bestGuess(packageStr);

            builder.addStatement("target.$L = ($T)(($T)source).findViewById($L)", bind.name, viewClass, viewC, bind.id);
        }
        if (className.simpleNames().size() == 1)
            builder.endControlFlow();
        return builder.build();

    }

    /**
     * 判断注解类型是否合法  1.必须是在变量上注解  2.注解变量必须是在一个类中
     *
     * @param element
     * @return
     */

    private boolean isClassLegal(Element element, TypeElement type) {
        if (element.getKind() != ElementKind.FIELD) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "only support for fields");
            return false;
        }

        if (type.getKind() != ElementKind.CLASS) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "field should be in a class");
            return false;
        }

        if (!(element instanceof TypeElement)) {
            return false;
        }

        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "only support for public field");
            return false;
        }


        return true;
    }

    /**
     * 判断注解对象类型是否合法
     *
     * @param typeMirror
     * @return
     */
    private boolean isFieldLegal(TypeMirror typeMirror) {


        String ViewTypeString = "android.view.View";
        //元素的类型信息

        if (typeMirror.getKind() == TypeKind.TYPEVAR) {
            TypeVariable typeVariable = (TypeVariable) typeMirror;
            typeMirror = typeVariable.getUpperBound();
        }

        if (typeMirror.toString().equals(ViewTypeString)) {
            return true;
        }

        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }

        //表示这是一个类或接口类型
        DeclaredType declaredType = (DeclaredType) typeMirror;
        Element element = declaredType.asElement();
        TypeElement typeElement = (TypeElement) element;

        //得到该元素的直接超类
        TypeMirror superType = typeElement.getSuperclass();

        if (isFieldLegal(superType))
            return true;

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(FindV.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private String getPackageName(TypeElement type) {
        return mElementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        String className = type.getQualifiedName().toString().substring(packageLen);
        return className;
    }
}
