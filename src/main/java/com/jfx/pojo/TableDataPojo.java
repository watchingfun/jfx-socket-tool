package com.jfx.pojo;

import cn.hutool.core.bean.DynaBean;
import cn.hutool.core.clone.CloneSupport;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import lombok.Data;

@Data
public class TableDataPojo extends CloneSupport<TableDataPojo> {
    private ObservableValue<String> name;
    private ObservableValue<Integer> start;
    private ObservableValue<Integer> end;
    private ObservableValue<Boolean> disabled;

    public TableDataPojo() {
        this.name = new SimpleStringProperty();
        this.start = new SimpleObjectProperty<>();
        this.end = new SimpleObjectProperty<>();
        this.disabled = new SimpleBooleanProperty();
    }

    public TableDataPojo(String name, int start, int end, boolean disabled) {
        this.name = new SimpleStringProperty(name);
        this.start = new SimpleObjectProperty<>(start);
        this.end = new SimpleObjectProperty<>(end);
        this.disabled = new SimpleBooleanProperty(disabled);
    }


    public TableDataPojoUnWrap unWrap() {
        return new TableDataPojoUnWrap(name.getValue(), start.getValue(), end.getValue(), disabled.getValue());
    }

    @Data
    public static class TableDataPojoUnWrap {
        private String name;
        private Integer start;
        private Integer end;
        private Boolean disabled;

        public TableDataPojoUnWrap(String name, Integer start, Integer end, Boolean disabled) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.disabled = disabled;
        }

        public TableDataPojo wrap() {
            return new TableDataPojo(name, start, end, disabled);
        }

    }
}
