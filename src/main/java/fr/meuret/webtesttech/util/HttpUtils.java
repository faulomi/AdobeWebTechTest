package fr.meuret.webtesttech.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

/**
 * Created by Jérôme on 18/09/2014.
 */
public class HttpUtils {

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");


    public static String sanitizeRequestPath(String requestPath) {


        // Decode the requestPath.
        try {
            requestPath = URLDecoder.decode(requestPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            throw new Error(e);
        }

        if (!requestPath.startsWith("/")) {
            return null;
        }

        // Remove first /
        requestPath = requestPath.replaceFirst("/", "");
        //Convert the next / to File separator
        requestPath = requestPath.replace("/", File.separator);


        if (requestPath.contains(File.separator + '.') ||
                requestPath.contains('.' + File.separator) ||
                requestPath.startsWith(".") || requestPath.endsWith(".") ||
                INSECURE_URI.matcher(requestPath).matches()) {
            return null;
        }

        //returns relative requestPath
        return requestPath;

    }


    public static boolean isAllowedFilename(String filename) {

        return ALLOWED_FILE_NAME.matcher(filename).matches();
    }
}
