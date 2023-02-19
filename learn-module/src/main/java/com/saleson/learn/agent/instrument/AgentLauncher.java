package com.saleson.learn.agent.instrument;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.bytecode.CodeAttribute;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Objects;

public class AgentLauncher {

    // 启动时接入
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain.agentArgs: " + agentArgs);
        handle(agentArgs, inst);
    }

    // 运行时接入(VirtualMachine attach)
    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        System.out.println("agentmain.agentArgs: " + agentArgs);
        handle(agentArgs, inst);

        System.out.println("isRetransformClassesSupported:" + inst.isRetransformClassesSupported());
        System.out.println("isRedefineClassesSupported:" + inst.isRedefineClassesSupported());

        // 重新transform已经加载的类
        inst.retransformClasses(
                Class.forName("com.saleson.learn.java.agent.Handle")
        );
    }

    private static void handle(String agentArgs, Instrumentation inst) {
        System.out.println("agentArgs: " + agentArgs);
        if (StringUtils.isBlank(agentArgs)) {
            throw new NullPointerException("agentArgs is null.");
        }
        String[] args = agentArgs.split(",");
        if (args.length > 3) {
            throw new IllegalArgumentException("agentArgs is illegal.");
        }
        String mode, className = null, methodName = null;

        if (args.length == 1) {
            mode = args[0];
        } else if (args.length == 2) {
            mode = args[0];
            className = args[1];
        } else {
            mode = args[0];
            className = args[1];
            methodName = args[2];
        }
        javassistTransform(inst, className, methodName);
    }

    private static void javassistTransform(Instrumentation instrumentation, String className, String methodName) {
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader,
                                    String cn, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws IllegalClassFormatException {
                if (Objects.equals(className.replaceAll("\\.", "/"), cn)) {
                    try {
                        return javassistTransform(loader, classfileBuffer, methodName);
                    } catch (Throwable t) {
                        throw new IllegalClassFormatException(t.getClass().getName() + " -> " + t.getMessage());
                    }
                }
                return classfileBuffer;
            }
        }, true);
    }

    private static byte[] javassistTransform(ClassLoader loader, byte[] classfileBuffer, String methodName) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        pool.appendClassPath(new LoaderClassPath(loader));
        CtClass ctClass = pool.makeClass(new ByteArrayInputStream(classfileBuffer));
        if ("*".equals(methodName) || StringUtils.isBlank(methodName)) {
            CtMethod[] ctmethods = ctClass.getMethods();
            for (CtMethod ctMethod : ctmethods) {
                CodeAttribute ca = ctMethod.getMethodInfo2().getCodeAttribute();
                if (ca == null) {
                    continue;
                }
                if (!ctMethod.isEmpty()) {
                    ctMethod.insertBefore("System.out.println(\"hello Im agent : " + ctMethod.getName() + "\");");
                }
            }
            return ctClass.toBytecode();
        }

        CtMethod ctMethod = ctClass.getDeclaredMethod(methodName);
        ctMethod.insertBefore("System.out.println(\"hello Im agent : " + ctMethod.getName() + "\");");
        return ctClass.toBytecode();
    }
}
