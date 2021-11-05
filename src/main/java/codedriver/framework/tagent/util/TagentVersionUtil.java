package codedriver.framework.tagent.util;

import java.util.List;

public class TagentVersionUtil {

    public static int compareVersion(String version1, String version2) {
        String[] versionArray1 = version1.split("\\.");
        String[] versionArray2 = version2.split("\\.");
        int idx = 0;
        int minLength = Math.min(versionArray1.length, versionArray2.length);
        int diff = 0;
        while (idx < minLength && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0 && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {
            ++idx;
        }
        diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
        return diff;
    }

    /**
     * 寻找最高版本的安装包
     *
     * @param nowVersion
     * @param versionList
     * @return
     */
    public static String findHighestVersion(String nowVersion, List<String> versionList) {
        versionList.add(nowVersion);
        String hightVersion = nowVersion;
        for (int i = 0; i < versionList.size(); i++) {
            String tryVersion = versionList.get(i);
            if (TagentVersionUtil.compareVersion(tryVersion, nowVersion) > 0) {
                hightVersion = tryVersion;
            }

        }
        return hightVersion;
    }

}
