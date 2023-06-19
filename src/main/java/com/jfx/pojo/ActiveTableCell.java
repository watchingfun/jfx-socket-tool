package com.jfx.pojo;

import javafx.scene.control.TableCell;

public class ActiveTableCell extends TableCell<TableDataPojo, Boolean> {
    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            setText(item.toString());
        }
    }
}

