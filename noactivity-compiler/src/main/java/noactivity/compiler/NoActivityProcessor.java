/*
 *    Copyright 2017 Jozua Catrinus Sijsling
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package noactivity.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

import noactivity.annotations.NoLifecycle;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;


@AutoService(Processor.class)
public class NoActivityProcessor extends AbstractProcessor {

    private static final List<String> SUPPORTED_TYPES = Collections.singletonList(
            "layout"
    );

    private Elements elementUtils;
    private Types typeUtils;
    private Filer filer;
    private Messager messager;
    private Trees trees;

    private final Map<QualifiedId, Id> symbols = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();

        try {
            trees = Trees.instance(processingEnv);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        try {
            Map<TypeElement, ActivityGenerator> generatorMap = findAndParseTargets(roundEnvironment);

            for (Map.Entry<TypeElement, ActivityGenerator> entry : generatorMap.entrySet()) {
                TypeElement typeElement = entry.getKey();
                ActivityGenerator generator = entry.getValue();

                JavaFile javaFile = generator.brewJava();
                try {
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    error(typeElement, "Unable to write binding for type %s: %s", typeElement, e.getMessage());
                }
            }


        } catch (ProcessingException e) {
            messager.printMessage(Kind.ERROR, e.getMessage(), e.getElement());
        }


        return false;
    }


    private Map<TypeElement, ActivityGenerator> findAndParseTargets(RoundEnvironment env) throws ProcessingException {
        Map<TypeElement, ActivityGenerator.Builder> builderMap = new LinkedHashMap<>();
        Set<TypeElement> erasedTargetNames = new LinkedHashSet<>();

        scanForRClasses(env);

        Set<? extends Element> elements = env.getElementsAnnotatedWith(NoLifecycle.class);
        for (Element element : elements) {
            if (element.getKind() != CLASS) {
                throw new ProcessingException(element, "Only classes can be annotated with " + NoLifecycle.class.getSimpleName());
            } else {
                parseNoLifecycleElement(element, builderMap, erasedTargetNames);
            }
        }

        Map<TypeElement, ActivityGenerator> generatorMap = new LinkedHashMap<>();
        for (Map.Entry<TypeElement, ActivityGenerator.Builder> entry : builderMap.entrySet()) {
            generatorMap.put(entry.getKey(), entry.getValue().build());
        }

        return generatorMap;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }


    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();

        annotations.add(NoLifecycle.class);

        return annotations;
    }

    private static AnnotationMirror getMirror(Element element,
                                              Class<? extends Annotation> annotation) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(annotation.getCanonicalName())) {
                return annotationMirror;
            }
        }
        return null;
    }

    private void scanForRClasses(RoundEnvironment env) {
        if (trees == null) return;

        RClassScanner scanner = new RClassScanner();

        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            for (Element element : env.getElementsAnnotatedWith(annotation)) {
                JCTree tree = (JCTree) trees.getTree(element, getMirror(element, annotation));
                if (tree != null) { // tree can be null if the references are compiled types and not source
                    String respectivePackageName =
                            elementUtils.getPackageOf(element).getQualifiedName().toString();
                    scanner.setCurrentPackageName(respectivePackageName);
                    tree.accept(scanner);
                }
            }
        }

        for (Map.Entry<String, Set<String>> packageNameToRClassSet : scanner.getRClasses().entrySet()) {
            String respectivePackageName = packageNameToRClassSet.getKey();
            for (String rClass : packageNameToRClassSet.getValue()) {
                parseRClass(respectivePackageName, rClass);
            }
        }
    }

    private void parseRClass(String respectivePackageName, String rClass) {
        Element element;

        try {
            element = elementUtils.getTypeElement(rClass);
        } catch (MirroredTypeException mte) {
            element = typeUtils.asElement(mte.getTypeMirror());
        }

        JCTree tree = (JCTree) trees.getTree(element);
        if (tree != null) { // tree can be null if the references are compiled types and not source
            IdScanner idScanner = new IdScanner(symbols, elementUtils.getPackageOf(element)
                    .getQualifiedName().toString(), respectivePackageName);
            tree.accept(idScanner);
        } else {
            parseCompiledR(respectivePackageName, (TypeElement) element);
        }
    }

    private void parseNoLifecycleElement(Element element,
                                         Map<TypeElement, ActivityGenerator.Builder> builderMap,
                                         Set<TypeElement> erasedTargetNames) {
        boolean hasError = false;

        // Verify that the target type is a class.
        if (element.getKind() != CLASS) {
            error(element, "@%s must annotate a class.", NoLifecycle.class.getSimpleName());
            hasError = true;
        }

        TypeElement typeElement = (TypeElement) element;

        // Verify common generated code restrictions.
        hasError |= isInaccessibleViaGeneratedCode(NoLifecycle.class, "class", element);
        hasError |= isSourceInWrongPackage(NoLifecycle.class, element);

        if (hasError) {
            return;
        }

        // Assemble information on the class.
        int id = element.getAnnotation(NoLifecycle.class).value();
        QualifiedId qualifiedId = elementToQualifiedId(element, id);
        ActivityGenerator.Builder generatorBuilder = getOrCreateBindingBuilder(builderMap, typeElement);
        generatorBuilder.setLayout(qualifiedId);

        erasedTargetNames.add(typeElement);
    }

    private ActivityGenerator.Builder getOrCreateBindingBuilder(
            Map<TypeElement, ActivityGenerator.Builder> builderMap, TypeElement enclosingElement) {
        ActivityGenerator.Builder builder = builderMap.get(enclosingElement);
        if (builder == null) {
            builder = ActivityGenerator.newBuilder(enclosingElement);
            builderMap.put(enclosingElement, builder);
        }
        return builder;
    }

    private QualifiedId elementToQualifiedId(Element element, int id) {
        return new QualifiedId(elementUtils.getPackageOf(element).getQualifiedName().toString(), id);
    }

    private boolean isInaccessibleViaGeneratedCode(Class<? extends Annotation> annotationClass,
                                                   String targetThing, Element element) {
        boolean hasError = false;

        // Verify method modifiers.
        Set<Modifier> modifiers = element.getModifiers();
        if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
            error(element, "@%s %s must not be private or static.", annotationClass.getSimpleName(), targetThing);
            hasError = true;
        }

        return hasError;
    }

    private boolean isSourceInWrongPackage(Class<? extends Annotation> annotationClass,
                                           Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();

        if (qualifiedName.startsWith("android.")) {
            error(element, "@%s-annotated class incorrectly in Android framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }
        if (qualifiedName.startsWith("java.")) {
            error(element, "@%s-annotated class incorrectly in Java framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }

        return false;
    }


    private void error(Element element, String message, Object... args) {
        printMessage(Kind.ERROR, element, message, args);
    }

    private void printMessage(Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        processingEnv.getMessager().printMessage(kind, message, element);
    }

    private void parseCompiledR(String respectivePackageName, TypeElement rClass) {
        for (Element element : rClass.getEnclosedElements()) {
            String innerClassName = element.getSimpleName().toString();
            if (SUPPORTED_TYPES.contains(innerClassName)) {
                for (Element enclosedElement : element.getEnclosedElements()) {
                    if (enclosedElement instanceof VariableElement) {
                        VariableElement variableElement = (VariableElement) enclosedElement;
                        Object value = variableElement.getConstantValue();

                        if (value instanceof Integer) {
                            int id = (Integer) value;
                            ClassName rClassName =
                                    ClassName.get(elementUtils.getPackageOf(variableElement).toString(), "R",
                                            innerClassName);
                            String resourceName = variableElement.getSimpleName().toString();
                            QualifiedId qualifiedId = new QualifiedId(respectivePackageName, id);
                            symbols.put(qualifiedId, new Id(id, rClassName, resourceName));
                        }
                    }
                }
            }
        }
    }


    private static class RClassScanner extends TreeScanner {
        // Maps the currently evaulated rPackageName to R Classes
        private final Map<String, Set<String>> rClasses = new LinkedHashMap<>();
        private String currentPackageName;

        @Override
        public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
            Symbol symbol = jcFieldAccess.sym;
            if (symbol != null
                    && symbol.getEnclosingElement() != null
                    && symbol.getEnclosingElement().getEnclosingElement() != null
                    && symbol.getEnclosingElement().getEnclosingElement().enclClass() != null) {
                Set<String> rClassSet = rClasses.get(currentPackageName);
                if (rClassSet == null) {
                    rClassSet = new HashSet<>();
                    rClasses.put(currentPackageName, rClassSet);
                }
                rClassSet.add(symbol.getEnclosingElement().getEnclosingElement().enclClass().className());
            }
        }

        Map<String, Set<String>> getRClasses() {
            return rClasses;
        }

        void setCurrentPackageName(String respectivePackageName) {
            this.currentPackageName = respectivePackageName;
        }
    }

    private static class IdScanner extends TreeScanner {
        private final Map<QualifiedId, Id> ids;
        private final String rPackageName;
        private final String respectivePackageName;

        IdScanner(Map<QualifiedId, Id> ids, String rPackageName, String respectivePackageName) {
            this.ids = ids;
            this.rPackageName = rPackageName;
            this.respectivePackageName = respectivePackageName;
        }

        @Override
        public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
            for (JCTree tree : jcClassDecl.defs) {
                if (tree instanceof ClassTree) {
                    ClassTree classTree = (ClassTree) tree;
                    String className = classTree.getSimpleName().toString();
                    if (SUPPORTED_TYPES.contains(className)) {
                        ClassName rClassName = ClassName.get(rPackageName, "R", className);
                        VarScanner scanner = new VarScanner(ids, rClassName, respectivePackageName);
                        ((JCTree) classTree).accept(scanner);
                    }
                }
            }
        }
    }

    private static class VarScanner extends TreeScanner {
        private final Map<QualifiedId, Id> ids;
        private final ClassName className;
        private final String respectivePackageName;

        private VarScanner(Map<QualifiedId, Id> ids, ClassName className,
                           String respectivePackageName) {
            this.ids = ids;
            this.className = className;
            this.respectivePackageName = respectivePackageName;
        }

        @Override
        public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
            if ("int".equals(jcVariableDecl.getType().toString())) {
                int id = Integer.valueOf(jcVariableDecl.getInitializer().toString());
                String resourceName = jcVariableDecl.getName().toString();
                QualifiedId qualifiedId = new QualifiedId(respectivePackageName, id);
                ids.put(qualifiedId, new Id(id, className, resourceName));
            }
        }
    }
}
