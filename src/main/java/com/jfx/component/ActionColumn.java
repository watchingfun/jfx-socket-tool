package com.jfx.component;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ActionColumn<S> {

    private final List<Action<S>> actions = new ArrayList<>();
    private String title ;

    private ActionColumn() {}

    public static <S> ActionColumn<S> forType(Class<S> type) {
        return new ActionColumn<>();
    }

    public ActionColumn<S> withTitle(String title) {
        this.title = title ;
        return this;
    }

    public ActionColumn<S> withAction(String name, Consumer<S> action) {
        actions.add(new Action<>(name, action));
        return this;
    }


    public TableColumn<S, ?> build() {
        TableColumn<S, S> column = new TableColumn<>(title);
        column.setMaxWidth(Double.MAX_VALUE);
        column.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        column.setCellFactory(tc -> new ActionColumnCell<>(actions));
        return column;
    }

    private record Action<S>(String name, Consumer<S> action) {}

    private static class ActionColumnCell<S> extends TableCell<S, S> {
        private final HBox buttons ;
        private ActionColumnCell(List<Action<S>> actions) {
            buttons = new HBox(2);
            actions.stream()
                    .map(this::createButton)
                    .forEach(buttons.getChildren()::add);
        }

        private Button createButton(Action<S> action) {
            Button button = new Button(action.name());
            button.setOnAction(e -> action.action().accept(getItem()));
            return button;
        }

        @Override
        protected void updateItem(S item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : buttons);
        }
    }
}
