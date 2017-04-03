package noactivity.compiler;


import android.support.annotation.NonNull;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static com.google.auto.common.MoreElements.getPackage;

final class ActivityGenerator {

    private final TypeName targetTypeName;
    private final ClassName sourceClassName;
    private final boolean isFinal;
    private final QualifiedId layout;

    private static final ClassName BUNDLE = ClassName.get("android.os", "Bundle");
    private static final ClassName ADAPTER = ClassName.get("noactivity", "ActivityAdapter");

    ActivityGenerator(TypeName targetTypeName, ClassName sourceClassName, boolean isFinal,
                      QualifiedId layout) {
        this.targetTypeName = targetTypeName;
        this.sourceClassName = sourceClassName;
        this.isFinal = isFinal;
        this.layout = layout;
    }

    JavaFile brewJava() {
        return JavaFile.builder(sourceClassName.packageName(), createType())
                .addFileComment("Generated code from No Activity. Do not modify!")
                .build();
    }

    private TypeSpec createType() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(sourceClassName.simpleName())
                .addModifiers(Modifier.PUBLIC);
        if (isFinal) {
            classBuilder.addModifiers(Modifier.FINAL);
        }

        classBuilder.addField(createAdapterField());

        classBuilder.addMethod(createOnCreateMethod());
        classBuilder.addMethod(createOnBackPressedMethod());
        classBuilder.addMethod(createOnDestroyMethod());


        return classBuilder.build();
    }

    @NonNull
    private FieldSpec createAdapterField() {
        return FieldSpec.builder(ADAPTER, "mAdapter", Modifier.PRIVATE, Modifier.FINAL).build();
    }

    private MethodSpec createOnBackPressedMethod() {
        return MethodSpec.methodBuilder("onBackPressed")
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .build();
    }

    private MethodSpec createOnDestroyMethod() {
        return MethodSpec.methodBuilder("onDestroy")
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .build();
    }

    @NonNull
    private MethodSpec createOnCreateMethod() {
        return MethodSpec.methodBuilder("onCreate")
                .addAnnotation(AnnotationSpec.builder(Override.class).build())
                .addParameter(ParameterSpec.builder(BUNDLE, "savedInstanceState").build())
                .build();
    }

    static Builder newBuilder(TypeElement enclosingElement) {
        TypeMirror typeMirror = enclosingElement.asType();
        TypeName targetType = TypeName.get(typeMirror);
        if (targetType instanceof ParameterizedTypeName) {
            targetType = ((ParameterizedTypeName) targetType).rawType;
        }

        String packageName = getPackage(enclosingElement).getQualifiedName().toString();
        String className = enclosingElement.getQualifiedName().toString().substring(
                packageName.length() + 1).replace('.', '$');
        ClassName bindingClassName = ClassName.get(packageName, className + "_ViewBinding");

        boolean isFinal = enclosingElement.getModifiers().contains(Modifier.FINAL);
        return new Builder(targetType, bindingClassName, isFinal);
    }

    static class Builder {
        private final TypeName targetType;
        private final ClassName bindingClassName;
        private final boolean isFinal;
        private QualifiedId layout;

        Builder(TypeName targetType, ClassName bindingClassName, boolean isFinal) {
            this.targetType = targetType;
            this.bindingClassName = bindingClassName;
            this.isFinal = isFinal;
        }

        Builder setLayout(QualifiedId layout) {
            this.layout = layout;
            return this;
        }

        ActivityGenerator build() {

            return new ActivityGenerator(targetType, bindingClassName, isFinal, layout);
        }
    }

}
