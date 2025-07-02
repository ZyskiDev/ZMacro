package net.zyski.zmacro.client.util;

import java.util.function.BooleanSupplier;

public class SleepUtil {

    private static BooleanSupplier breakSleepCondition = null;
    private static boolean sleeping = false;
    private static long sleepTimeout = 0;


    public static BooleanSupplier getBreakSleepCondition() {
        return breakSleepCondition;
    }

    public static void setBreakSleepCondition(BooleanSupplier breakSleepCondition) {
        SleepUtil.breakSleepCondition = breakSleepCondition;
    }

    public static long getSleepTimeout() {
        return sleepTimeout;
    }

    public static void setSleepTimeout(long sleepTimeout) {
        SleepUtil.sleepTimeout = sleepTimeout;
    }

    public static boolean isSleeping() {
        return sleeping;
    }

    public static void setSleeping(boolean sleeping) {
        SleepUtil.sleeping = sleeping;
    }


}
