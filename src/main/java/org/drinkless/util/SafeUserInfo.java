package org.drinkless.util;

public class SafeUserInfo {

    private String machineCode;

    private long endTime;

    public String getMachineCode() {
        return machineCode;
    }

    public void setMachineCode(String machineCode) {
        this.machineCode = machineCode;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String toKey() {
        return machineCode + "{#}" + endTime;
    }

    public static SafeUserInfo fromKey(String key) {
        String[] strArr = key.split("\\{#}");
        SafeUserInfo info = new SafeUserInfo();
        info.setMachineCode(strArr[0]);
        info.setEndTime(Long.parseLong(strArr[1]));
        return info;
    }
}
