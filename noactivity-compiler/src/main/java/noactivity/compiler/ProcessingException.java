package noactivity.compiler;


import javax.lang.model.element.Element;

class ProcessingException extends Exception {

    private final Element element;

    ProcessingException(Element element, String message) {
        super(message);
        this.element = element;
    }

    Element getElement() {
        return element;
    }
}
