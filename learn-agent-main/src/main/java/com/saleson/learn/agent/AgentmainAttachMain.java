package com.saleson.learn.agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;

public class AgentmainAttachMain {
    public static void main(String[] args) {
        // check args
        if (args.length == 2  ) {
            unsafeExec(() -> attachAgent(args[0], args[1]));
            return;
        } else if (args.length != 3  ) {
            throw new IllegalArgumentException("illegal args");
        }
        unsafeExec(() -> attachAgent(args[0], args[1], args[2]));
    }

    // 从表列表查找运行HandleMain的jvm pid
    private static void attachAgent(String agentJarPath, String cfg) throws Exception {
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        for (VirtualMachineDescriptor descriptor : list) {
            if (descriptor.displayName().endsWith("HandleMain")) {
                VirtualMachine virtualMachine = VirtualMachine.attach(descriptor.id());
                virtualMachine.loadAgent(agentJarPath, cfg);
                virtualMachine.detach();
            }
        }
    }

    // 指定jvm pid 加载Agent
    private static void attachAgent(String targetJvmPid, String agentJarPath, String cfg) throws Exception {
        VirtualMachine vmObj = VirtualMachine.attach(targetJvmPid);
        if (vmObj != null) {
            vmObj.loadAgent(agentJarPath, cfg);
            vmObj.detach();
        }
    }

    /**
     * 获取异常的原因描述
     *
     * @param t 异常
     * @return 异常原因
     */
    public static String getCauseMessage(Throwable t) {
        if (null != t.getCause()) {
            return getCauseMessage(t.getCause());
        }
        return t.getMessage();
    }

    interface Exec {
        void exec() throws Throwable;
    }

    private static void unsafeExec(Exec exec) {
        try {
            exec.exec();
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("load jvm failed : " + getCauseMessage(t));
            System.exit(-1);
        }
    }
}
