
package com.shifang.demo.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * ClassName:CloseUtils <br/>
 * Function: 流的工具类. <br/>
 *
 */
public class CloseUtils {

    public CloseUtils() {
    }

    /**
     * 关闭 IO
     * @param closeables
     */
    public static void close(Closeable... closeables) {
        if (closeables != null && closeables.length != 0) {
            Closeable[] var1 = closeables;
            int var2 = closeables.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                Closeable closeable = var1[var3];
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException var6) {
                        var6.printStackTrace();
                    }
                }
            }

        }
    }
}
