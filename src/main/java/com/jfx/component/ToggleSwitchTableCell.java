package com.jfx.component;

import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.util.Callback;

public class ToggleSwitchTableCell<S, T> extends TableCell<S, T> {

    private final ToggleSwitch toggleSwitch;

    private ObservableValue<Boolean> booleanProperty;

    public ToggleSwitchTableCell() {
        this(null);
    }

    public ToggleSwitchTableCell(final Callback<Integer, ObservableValue<Boolean>> getSelectedProperty) {
        this.toggleSwitch = new ToggleSwitch();
        this.toggleSwitch.selectedProperty().addListener((observable, oldV, newV) -> {
            this.toggleSwitch.pseudoClassStateChanged(Styles.STATE_DANGER, newV);
        });
        // by default the graphic is null until the cell stops being empty
        setGraphic(null);
        setSelectedStateCallback(getSelectedProperty);
    }

    private ObservableValue<?> getSelectedProperty() {
        return getSelectedStateCallback() != null ?
                getSelectedStateCallback().call(getIndex()) :
                getTableColumn().getCellObservableValue(getIndex());
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {

            setGraphic(toggleSwitch);

            if (booleanProperty instanceof BooleanProperty) {
                toggleSwitch.selectedProperty().unbindBidirectional((BooleanProperty) booleanProperty);
            }
            ObservableValue<?> obsValue = getSelectedProperty();
            if (obsValue instanceof BooleanProperty) {
                booleanProperty = (ObservableValue<Boolean>) obsValue;
                toggleSwitch.selectedProperty().bindBidirectional((BooleanProperty) booleanProperty);
            }

            toggleSwitch.disableProperty().bind(Bindings.not(
                    getTableView().editableProperty().and(
                            getTableColumn().editableProperty()).and(
                            editableProperty())
            ));
        }
    }


    // --- selected state callback property
    private ObjectProperty<Callback<Integer, ObservableValue<Boolean>>>
            selectedStateCallback =
            new SimpleObjectProperty<Callback<Integer, ObservableValue<Boolean>>>(
                    this, "selectedStateCallback");

    public final ObjectProperty<Callback<Integer, ObservableValue<Boolean>>> selectedStateCallbackProperty() {
        return selectedStateCallback;
    }


    public final void setSelectedStateCallback(Callback<Integer, ObservableValue<Boolean>> value) {
        selectedStateCallbackProperty().set(value);
    }

    public final Callback<Integer, ObservableValue<Boolean>> getSelectedStateCallback() {
        return selectedStateCallbackProperty().get();
    }
}
