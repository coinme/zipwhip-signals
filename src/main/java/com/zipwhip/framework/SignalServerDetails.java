package com.zipwhip.framework;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;

/**
 * Date: 5/19/13
 * Time: 12:02 AM
 *
 * @author Michael
 * @version 1
 */
@JsonRootName("details")
public class SignalServerDetails implements Serializable {

    private static final long serialVersionUID = 6537153829283396815L;

    private String description;

    public SignalServerDetails() {

    }

    public SignalServerDetails(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
