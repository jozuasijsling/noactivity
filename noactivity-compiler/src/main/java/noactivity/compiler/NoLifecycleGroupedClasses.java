package noactivity.compiler;


import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;

public class NoLifecycleGroupedClasses {

    private String qualifiedClassName;

    private Map<Integer, NoLifecycleAnnotatedClass> itemsMap =
            new LinkedHashMap<Integer, NoLifecycleAnnotatedClass>();

    public NoLifecycleGroupedClasses(String qualifiedClassName) {
        this.qualifiedClassName = qualifiedClassName;
    }

    public void add(NoLifecycleAnnotatedClass toInsert) {
        itemsMap.put(toInsert.getId(), toInsert);
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException {

    }

}
