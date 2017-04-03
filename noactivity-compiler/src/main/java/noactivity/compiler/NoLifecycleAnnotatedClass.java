package noactivity.compiler;


import android.support.annotation.LayoutRes;

import javax.lang.model.element.TypeElement;

import noactivity.annotations.NoLifecycle;

public class NoLifecycleAnnotatedClass {

    private final TypeElement annotatedClassElement;
    @LayoutRes private final int id;

    public NoLifecycleAnnotatedClass(TypeElement classElement) {
        this.annotatedClassElement = classElement;

        NoLifecycle annotation = classElement.getAnnotation(NoLifecycle.class);
        this.id = annotation.value();

        // TODO lookup R.layout.[id}
    }

    public int getId() {
        return id;
    }

    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }
}
