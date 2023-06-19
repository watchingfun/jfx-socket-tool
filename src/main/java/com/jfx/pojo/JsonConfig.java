package com.jfx.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JsonConfig {

    private String acceptMethod;
    private String acceptIP;
    private String acceptBindIP;
    private Integer acceptPort;

    private String forwardingMethod;
    private String forwardingIP;
    private String forwardingBindIP;
    private Integer forwardingPort;

    private List<TableDataPojo.TableDataPojoUnWrap> dataConvertRules = new ArrayList<>();
}
