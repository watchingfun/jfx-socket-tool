package com.jfx.controller;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.theme.Styles;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.jfx.component.ActionColumn;
import com.jfx.component.ToggleSwitchTableCell;
import com.jfx.fxweaver.core.FxmlView;
import com.jfx.pojo.TableDataPojo;
import com.jfx.utils.DebounceTask;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;

@Slf4j
@Component
@FxmlView("/fxml/table-config-view.fxml")
@RequiredArgsConstructor
public class ConfigController implements Initializable {

    private Dialog<ObservableList<TableDataPojo>> dialog;

    @FXML
    private AnchorPane root;
    @FXML
    private TableView<TableDataPojo> tableView;

    @FXML
    private TableColumn<TableDataPojo, String> tableCol_name;
    @FXML
    private TableColumn<TableDataPojo, Integer> tableCol_start;
    @FXML
    private TableColumn<TableDataPojo, Integer> tableCol_end;
    @FXML
    private TableColumn<TableDataPojo, Boolean> tableCol_disabled;

    @FXML
    private CustomTextField nameField;
    @FXML
    private CustomTextField startField;
    @FXML
    private CustomTextField endField;
    @FXML
    private Button addBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dialog = new Dialog<>();
        dialog.setTitle("配置");
        //dialog.setResizable(true);
        //dialog.getDialogPane().setMinSize(root.getMinWidth(), root.getMinHeight());
        //dialog.getDialogPane().setPrefSize(root.getMinWidth(), root.getMinHeight());
        root.setPadding(new Insets(10, 10, 10, 10));
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("确认", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE));
        dialog.setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonBar.ButtonData.OK_DONE ? tableView.getItems() : null;
        });
        initTable();
    }

    private void initTable() {
        tableCol_name.setCellValueFactory(c -> c.getValue().getName());
        tableCol_name.setCellFactory(tc -> new TextFieldTableCell<>(new DefaultStringConverter()));

        tableCol_start.setCellValueFactory(c -> c.getValue().getStart());
        tableCol_start.setCellFactory(tc -> new TextFieldTableCell<>(new IntegerStringConverter()));

        tableCol_end.setCellValueFactory(c -> c.getValue().getEnd());
        tableCol_end.setCellFactory(tc -> new TextFieldTableCell<>(new IntegerStringConverter()));

        tableCol_disabled.setCellValueFactory(c -> c.getValue().getDisabled());
        tableCol_disabled.setCellFactory(tc -> new ToggleSwitchTableCell<>());
        tableView.getColumns().add(
                ActionColumn.forType(TableDataPojo.class).withTitle("操作")
                        .withAction("删除",
                                item -> {
                                    log.debug("item {}", item);
                                    tableView.getItems().remove(item);
                                })
                        .build()
        );

        // 启用拖拽
        tableView.setRowFactory(tv -> {
            TableRow<TableDataPojo> row = new TableRow<>();
            // 设置可拖拽
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    Map<String, Object> data = MapUtil.of("index", row.getIndex());
                    data.put("data", row.getItem().unWrap());
                    content.putString(JSONUtil.toJsonStr(data));
                    db.setContent(content);
                }
                event.consume();
            });

            // 设置放置目标
            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                JSON json = JSONUtil.parse(db.getString());
                if (db.hasString() && row.getIndex() != json.getByPath("index", Integer.class)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            // 设置放置操作
            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    JSON json = JSONUtil.parse(db.getString());
                    int draggedIndex = json.getByPath("index", Integer.class);
                    TableDataPojo draggedData = tableView.getItems().remove(draggedIndex);

                    int dropIndex;
                    if (row.isEmpty()) {
                        dropIndex = tableView.getItems().size();
                    } else {
                        dropIndex = row.getIndex();
                    }

                    tableView.getItems().add(dropIndex, draggedData);

                    event.setDropCompleted(true);
                    tableView.getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });

            return row;
        });

        nameField.textProperty().addListener(createDebounceValidate(nameField, StringUtils::isNoneBlank));
        startField.textProperty().addListener(createDebounceValidate(startField, NumberUtil::isInteger));
        endField.textProperty().addListener(createDebounceValidate(endField, NumberUtil::isInteger));
        addBtn.setOnMouseClicked(event -> {
            if (StringUtils.isBlank(nameField.getText()) || !NumberUtil.isInteger(startField.getText()) || !NumberUtil.isInteger(endField.getText())) {
                return;
            }
            tableView.getItems().add(new TableDataPojo(nameField.getText(), Integer.parseInt(startField.getText()), Integer.parseInt(endField.getText()), false));
        });
    }

    //创建带防抖的校验监听
    private ChangeListener<String> createDebounceValidate(Control textField, Function<String, Boolean> validate) {
        DebounceTask<String> task = DebounceTask.build((v) -> {
            textField.pseudoClassStateChanged(Styles.STATE_DANGER, v != null && !validate.apply(v));
        }, 1000L);
        return (observable, oldValue, newValue) -> {
            task.run(newValue);
        };
    }

    public Optional<ObservableList<TableDataPojo>> showDialog(ObservableList<TableDataPojo> list) {
        tableView.setItems(list);
        return dialog.showAndWait();
    }
}
