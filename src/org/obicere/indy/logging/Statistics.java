package org.obicere.indy.logging;

public class Statistics {

    private final long start;

    private int classesProcessed;

    private int classesCopied;

    private int methodsVisited;

    private int constructorCallssVisited;

    private int invokeSpecialsVisited;

    private int invokeVirtualsVisited;

    private int invokeInterfacesVisited;

    private int invokeStaticsVisited;

    private int staticGettersVisited;

    private int staticSettersVisited;

    private int fieldGettersVisited;

    private int fieldSettersVisited;

    private String prolificClass;

    private int prolificCalls;

    public Statistics() {
        start = System.currentTimeMillis();
    }

    public long getDuration() {
        return System.currentTimeMillis() - start;
    }

    public void classProcessed() {
        classesProcessed++;
    }

    public int getClassesProcessed() {
        return classesProcessed;
    }

    public void methodVisited() {
        methodsVisited++;
    }

    public int getMethodsVisited() {
        return methodsVisited;
    }

    public int getCallsVisited() {
        return getMethodCallsVisited() + getFieldCallsVisited();
    }

    public int getMethodCallsVisited() {
        return constructorCallssVisited +
                invokeSpecialsVisited +
                invokeVirtualsVisited +
                invokeInterfacesVisited +
                invokeStaticsVisited;
    }

    public void constructorCallVisited(){
        constructorCallssVisited++;
    }

    public int getConstructorCallsVisited() {
        return constructorCallssVisited;
    }

    public void invokeSpecialVisited() {
        invokeSpecialsVisited++;
    }

    public int getInvokeSpecialsVisited() {
        return invokeSpecialsVisited;
    }

    public void invokeVirtualVisited() {
        invokeVirtualsVisited++;
    }

    public int getInvokeVirtualsVisited() {
        return invokeVirtualsVisited;
    }

    public void invokeInterfaceVisited() {
        invokeInterfacesVisited++;
    }

    public int getInvokeInterfacesVisited() {
        return invokeInterfacesVisited;
    }

    public void invokeStaticVisited() {
        invokeStaticsVisited++;
    }

    public int getInvokeStaticsVisited() {
        return invokeStaticsVisited;
    }

    public int getFieldCallsVisited() {
        return staticGettersVisited +
                staticSettersVisited +
                fieldGettersVisited +
                fieldSettersVisited;
    }

    public void staticGetterVisited() {
        staticGettersVisited++;
    }

    public int getStaticGettersVisited() {
        return staticGettersVisited;
    }

    public void staticSetterVisited() {
        staticSettersVisited++;
    }

    public int getStaticSettersVisited() {
        return staticSettersVisited;
    }

    public void fieldGetterVisited() {
        fieldGettersVisited++;
    }

    public int getFieldGettersVisited() {
        return fieldGettersVisited;
    }

    public void fieldSetterVisited() {
        fieldSettersVisited++;
    }

    public int getFieldSettersVisited() {
        return fieldSettersVisited;
    }

    public void record(final int calls, final String processedClass) {
        if(calls > prolificCalls) {
            prolificCalls = calls;
            prolificClass = processedClass;
        }
    }

    public int getProlificCalls() {
        return prolificCalls;
    }

    public String getProlificClass() {
        return prolificClass;
    }
}
