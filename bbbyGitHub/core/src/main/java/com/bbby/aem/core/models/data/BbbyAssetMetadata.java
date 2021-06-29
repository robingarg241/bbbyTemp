
package com.bbby.aem.core.models.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class BbbyAssetMetadata {

    @SerializedName("parentDepartmentName")
    @Expose
    private String parentDepartmentName;
    @SerializedName("parentDepartmentNumber")
    @Expose
    private String parentDepartmentNumber;

    public String getParentDepartmentName() {
        return parentDepartmentName;
    }

    public void setParentDepartmentName(String parentDepartmentName) {
        this.parentDepartmentName = parentDepartmentName;
    }

    public String getParentDepartmentNumber() {
        return parentDepartmentNumber;
    }

    public void setParentDepartmentNumber(String parentDepartmentNumber) {
        this.parentDepartmentNumber = parentDepartmentNumber;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("parentDepartmentName", parentDepartmentName).append("parentDepartmentNumber", parentDepartmentNumber).toString();
    }

}
