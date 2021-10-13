package codedriver.framework.tagent.util;

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
}
