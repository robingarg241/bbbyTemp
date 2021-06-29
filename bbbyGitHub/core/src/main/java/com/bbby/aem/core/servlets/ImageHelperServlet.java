package com.bbby.aem.core.servlets;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.ServerException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    service = Servlet.class,
    immediate = true,
    name = "Retrieve image Info",
    property = {
        ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/bedbath/imageHelper",
        ServletResolverConstants.SLING_SERVLET_METHODS + "=POST"
    })
public class ImageHelperServlet extends SlingAllMethodsServlet {

	private static final long serialVersionUID = 2598426539166189512L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageHelperServlet.class);

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServerException,
        IOException {

        String imageStr = request.getParameter("base64").toString();
        imageStr = imageStr.substring(imageStr.indexOf("base64,")+7);
        BufferedImage image = null;

        try {

            InputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(imageStr.getBytes()));
            image = ImageIO.read(stream);

            ColorSpace colorSpace = image.getColorModel().getColorSpace();
            String colorType = getColorSpaceName(colorSpace.getType());
            response.getWriter().write(colorType);

        } catch (Exception e) {
            LOGGER.error("Error trying to get color space",e);
        }
    }

    private String getColorSpaceName(int type) {
        String colorName = "";
        switch(type) {
            case 0 :
                colorName = "XYZ";
                break;
            case 1 :
                colorName = "lAB";
                break;
            case 2 :
                colorName = "LUV";
                break;
            case 3 :
                colorName = "YCbCr";
                break;
            case 4 :
                colorName = "Yxy";
                break;
            case 5 :
                colorName = "RGB";
                break;
            case 6 :
                colorName = "GRAY";
                break;
            case 7 :
                colorName = "HSV";
                break;
            case 8 :
                colorName = "HLS";
                break;
            case 9 :
                colorName = "CMYK";
                break;
            case 11 :
                colorName = "CMY";
                break;
            case 12 :
                colorName = "2CLR";
                break;
            case 13 :
                colorName = "3CLR";
                break;
            case 14 :
                colorName = "4CLR";
                break;
            case 15 :
                colorName = "5CLR";
                break;
            case 16 :
                colorName = "6CLR";
                break;
            case 18 :
                colorName = "8CLR";
                break;
            case 19 :
                colorName = "9CLR";
                break;
            case 20 :
                colorName = "ACLR";
                break;
            case 21 :
                colorName = "BCLR";
                break;
            case 22 :
                colorName = "CCLR";
                break;
            case 23 :
                colorName = "DCLR";
                break;
            case 24 :
                colorName = "ECLR";
                break;
            case 25 :
                colorName = "FCLR";
                break;
            default :
                colorName = "";
                break;
        }

        return colorName;
    }
}
