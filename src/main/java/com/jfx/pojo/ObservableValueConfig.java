package com.jfx.pojo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Data
@Component
public class ObservableValueConfig {

    private ObjectProperty<String> acceptMethod = new SimpleObjectProperty<>();
    private ObjectProperty<String> acceptIP = new SimpleObjectProperty<>();
    private ObjectProperty<String> acceptBindIP = new SimpleObjectProperty<>();
    private ObjectProperty<Integer> acceptPort = new SimpleObjectProperty<>();

    private ObjectProperty<String> forwardingMethod = new SimpleObjectProperty<>();
    private ObjectProperty<String> forwardingIP = new SimpleObjectProperty<>();
    private ObjectProperty<String> forwardingBindIP = new SimpleObjectProperty<>();
    private ObjectProperty<Integer> forwardingPort = new SimpleObjectProperty<>();

    private ObservableList<TableDataPojo> dataConvertRules = FXCollections.observableList(new ArrayList<>());


    public void loadFromJson(JsonConfig jsonConfig) {
        acceptMethod.set(jsonConfig.getAcceptMethod());
        acceptIP.set(jsonConfig.getAcceptIP());
        acceptBindIP.set(jsonConfig.getAcceptBindIP());
        acceptPort.set(jsonConfig.getAcceptPort());
        forwardingMethod.set(jsonConfig.getForwardingMethod());
        forwardingIP.set(jsonConfig.getForwardingIP());
        forwardingBindIP.set(jsonConfig.getForwardingBindIP());
        forwardingPort.set(jsonConfig.getForwardingPort());
        dataConvertRules.setAll(jsonConfig.getDataConvertRules().stream().map(TableDataPojo.TableDataPojoUnWrap::wrap).toList());
    }

    public JsonConfig toJsonConfig() {
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setAcceptMethod(acceptMethod.getValue());
        jsonConfig.setAcceptIP(acceptIP.getValue());
        jsonConfig.setAcceptBindIP(acceptBindIP.getValue());
        jsonConfig.setAcceptPort(acceptPort.getValue());
        jsonConfig.setForwardingMethod(forwardingMethod.getValue());
        jsonConfig.setForwardingIP(forwardingIP.getValue());
        jsonConfig.setForwardingBindIP(forwardingBindIP.getValue());
        jsonConfig.setForwardingPort(forwardingPort.getValue());
        jsonConfig.setDataConvertRules(dataConvertRules.stream().map(TableDataPojo::unWrap).toList());
        return jsonConfig;
    }
}
