package com.bbby.aem.core.util;

import com.day.cq.commons.jcr.JcrUtil;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AssetHasherUtils {

    public static long gcd(long height, long width) {

        if (width == 0) {
            return height;
        }

        return gcd(width, height % width);

    }

    public static String getColorSpaceName(int type) {
        String colorName = "";
        switch (type) {
            case 0:
                colorName = "XYZ";
                break;
            case 1:
                colorName = "lAB";
                break;
            case 2:
                colorName = "LUV";
                break;
            case 3:
                colorName = "YCbCr";
                break;
            case 4:
                colorName = "Yxy";
                break;
            case 5:
                colorName = "RGB";
                break;
            case 6:
                colorName = "GRAY";
                break;
            case 7:
                colorName = "HSV";
                break;
            case 8:
                colorName = "HLS";
                break;
            case 9:
                colorName = "CMYK";
                break;
            case 11:
                colorName = "CMY";
                break;
            case 12:
                colorName = "2CLR";
                break;
            case 13:
                colorName = "3CLR";
                break;
            case 14:
                colorName = "4CLR";
                break;
            case 15:
                colorName = "5CLR";
                break;
            case 16:
                colorName = "6CLR";
                break;
            case 18:
                colorName = "8CLR";
                break;
            case 19:
                colorName = "9CLR";
                break;
            case 20:
                colorName = "ACLR";
                break;
            case 21:
                colorName = "BCLR";
                break;
            case 22:
                colorName = "CCLR";
                break;
            case 23:
                colorName = "DCLR";
                break;
            case 24:
                colorName = "ECLR";
                break;
            case 25:
                colorName = "FCLR";
                break;
            default:
                colorName = "";
                break;
        }

        return colorName;
    }

    public static boolean checkForDups(Session session, Node node, String assetHash) throws Exception {

        //We need to query the dam to see if any other asset has this hash. if it does we move the current asset to the dups folder

        String q = "SELECT * FROM [dam:Asset] AS s WHERE (ISDESCENDANTNODE([/content/dam/bbby]) " +
            "and NOT ISDESCENDANTNODE([/content/dam/bbby/asset_transitions_folder/vendor/duplicate_vendor_assets])) " +
            "and [jcr:content/metadata/bbby:assetHash] = \"" + assetHash + "\"";

        Query myQuery;

        long numberOfMatches = 0;


        myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
        QueryResult result = myQuery.execute();
        NodeIterator ni = result.getNodes();

        numberOfMatches = ni.getSize();


        if (numberOfMatches > 0) {

            String path = node.getPath();

            Path p = Paths.get(path);
            String fileName = p.getFileName().toString();

            String datePath = getOrCreateDatePath(session, node, "/content/dam/bbby/asset_transitions_folder/vendor/duplicate_vendor_assets");
            String destination = datePath + "/" + new Date().getTime() + "-" + fileName;

            session.move(path, destination);

            return true;
        }


        return false;

    }
    
    public static boolean checkForDupsForInternal(Session session, Node node) throws Exception {
    	
    	String path = node.getPath();

        Path p = Paths.get(path);
        String fileName = p.getFileName().toString();

        //We need to query the dam to see if any other asset has this filename. if it does we move the current asset to the dups folder
    	String q = "SELECT * FROM [dam:Asset] AS s WHERE ISDESCENDANTNODE([/content/dam]) " +
    			   "and NAME() = \"" + fileName + "\"";

        Query myQuery;

        long numberOfMatches = 0;


        myQuery = session.getWorkspace().getQueryManager().createQuery(q, Query.JCR_SQL2);
        QueryResult result = myQuery.execute();
        NodeIterator ni = result.getNodes();

        numberOfMatches = ni.getSize();


        if (numberOfMatches > 1) {

            String datePath = getOrCreateDatePath(session, node, "/content/dam/bbby/asset_transitions_folder/vendor/duplicate_vendor_assets");
            String destination = datePath + "/" + new Date().getTime() + "-" + fileName;

            session.move(path, destination);

            return true;
        }


        return false;

    }

    /**
     * @param session
     * @param node
     * @param rootPath
     * @return
     */
    public static String getOrCreateDatePath(Session session, Node node, String rootPath) throws Exception {

        String targetPath = null;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        Calendar createdDate = node.getProperty(CommonConstants.JCR_CREATED).getDate();

        String dayFormatted = dateFormatter.format(createdDate.getTime());

        String datePath = rootPath + "/" + dayFormatted;
        if (!session.nodeExists(datePath)) {
            JcrUtil.createPath(datePath, "sling:Folder", session);
//				session.save();
        }

        targetPath = datePath;


        return targetPath;
    }
}
