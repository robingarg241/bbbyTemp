package com.bbby.aem.core.models.data;

/**
 * The Class UploadHistoryDetailsItem.
 *
 * @author karthik.koduru
 */
public class UploadHistoryDetailsItem {

    /**
     * The file extension.
     */
    private String fileExtension;

    /**
     * The file name.
     */
    private String fileName;

    /**
     * The width.
     */
    private String width;

    /**
     * The height.
     */
    private String height;

    /**
     * The size.
     */
    private String size;

    private String colorSpace;

    /**
     * Gets the file extension.
     *
     * @return the file extension
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Sets the file extension.
     *
     * @param fileExtension the new file extension
     */
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName the new file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the width.
     *
     * @param width the new width
     */
    public void setWidth(String width) {
        this.width = width;
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    public String getHeight() {
        return height;
    }

    /**
     * Sets the height.
     *
     * @param height the new height
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public String getSize() {

        return size;
    }

    /**
     * Sets the size.
     *
     * @param size the new size
     */
    public void setSize(String size) {
        this.size = size;
    }

    public String getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(String colorSpace) {
        this.colorSpace = colorSpace;
    }
}
