package com.jfx.controller;

import atlantafx.base.controls.CustomTextField;
import atlantafx.base.theme.Styles;
import cn.hutool.core.clone.Cloneable;
import com.jfx.config.ConfigHelper;
import com.jfx.fxweaver.core.FxControllerAndView;
import com.jfx.fxweaver.core.FxmlView;
import com.jfx.pojo.ObservableValueConfig;
import com.jfx.pojo.TableDataPojo;
import com.jfx.utils.DebounceTask;
import com.jfx.utils.StageManageUtil;
import com.jfx.utils.ThemeUtil;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@FxmlView("/fxml/main-view.fxml")
@RequiredArgsConstructor
public class MainController implements Initializable {

    @Getter
    private Stage stage;
    @FXML
    private AnchorPane root;
    @FXML
    private Button themeSwitchButton;

    private final ResourceBundle resourceBundle;

    private final ObservableValueConfig observableValueConfig;

    @FXML
    private ComboBox<String> acceptMethod;
    @FXML
    private CustomTextField acceptIP;
    @FXML
    private ComboBox<String> acceptBindIP;
    @FXML
    private CustomTextField acceptPort;
    @FXML
    private TextArea acceptTextArea;
    @FXML
    private Button acceptBtn;

    @FXML
    private ComboBox<String> forwardingMethod;
    @FXML
    private CustomTextField forwardingIP;
    @FXML
    private ComboBox<String> forwardingBindIP;
    @FXML
    private CustomTextField forwardingPort;
    @FXML
    private TextArea forwardingTextArea;
    @FXML
    private Button forwardingBtn;
    private ObservableList<TableDataPojo> dataConvertRules;

    @FXML
    private Button dataConvertConfigButton;

    private final FxControllerAndView<ConfigController, Node> configControllerAndView;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.stage = StageManageUtil.register(this, root);
        this.stage.setTitle("socket tool");
        this.stage.setMinWidth(root.getMinWidth());
        this.stage.setMinHeight(root.getMinHeight());
        initComponentEvent();
        initComponent();
        initDataBind();
    }

    private void initDataBind() {
        observableValueConfig.loadFromJson(ConfigHelper.read());
        acceptMethod.valueProperty().bindBidirectional(observableValueConfig.getAcceptMethod());
        acceptIP.textProperty().bindBidirectional(observableValueConfig.getAcceptIP());
        acceptBindIP.valueProperty().bindBidirectional(observableValueConfig.getAcceptBindIP());
        acceptPort.textProperty().bindBidirectional(observableValueConfig.getAcceptPort(), new IntegerStringConverter());
        forwardingMethod.valueProperty().bindBidirectional(observableValueConfig.getForwardingMethod());
        forwardingIP.textProperty().bindBidirectional(observableValueConfig.getForwardingIP());
        forwardingBindIP.valueProperty().bindBidirectional(observableValueConfig.getForwardingBindIP());
        forwardingPort.textProperty().bindBidirectional(observableValueConfig.getForwardingPort(), new IntegerStringConverter());
        ConfigHelper.startSyncToFile(observableValueConfig);
    }

    private void initComponent() {

    }

    private void initComponentEvent() {
        //选项关联显示隐藏
        changeBind(forwardingMethod, forwardingIP, forwardingBindIP);
        changeBind(acceptMethod, acceptIP, acceptBindIP);
        //打开配置弹窗事件绑定
        dataConvertConfigButton.setOnMouseClicked(event -> {
            Optional<ObservableList<TableDataPojo>> optional = configControllerAndView.getController().showDialog(
                    FXCollections.observableList(
                            observableValueConfig.getDataConvertRules().stream().map(Cloneable::clone).collect(Collectors.toList())
                    )
            );
            optional.ifPresent(observableValueConfig::setDataConvertRules);
        });
        //主题切换
        themeSwitchButton.setOnMouseClicked(event -> {
            ThemeUtil.switchTheme(stage);
        });
        acceptIP.textProperty().addListener(createDebounceValidate(acceptIP, this::validateIP));
        acceptBindIP.valueProperty().addListener(createDebounceValidate(acceptBindIP, this::validateIP));
        acceptPort.textProperty().addListener(createDebounceValidate(acceptPort, this::validatePort));
        forwardingIP.textProperty().addListener(createDebounceValidate(forwardingIP, this::validateIP));
        forwardingBindIP.valueProperty().addListener(createDebounceValidate(forwardingBindIP, this::validateIP));
        forwardingPort.textProperty().addListener(createDebounceValidate(forwardingPort, this::validatePort));
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

    private Pattern ip_pattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$");
    private Pattern port_pattern = Pattern.compile("^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$");

    private boolean validateIP(String val) {
        Matcher matcher = ip_pattern.matcher(val);
        return matcher.matches();
    }

    private boolean validatePort(String val) {
        Matcher matcher = port_pattern.matcher(val);
        return matcher.matches();
    }

    private void changeBind(ComboBox<String> acceptMethod, CustomTextField ipField, ComboBox<String> bindIpField) {
        acceptMethod.valueProperty().addListener((observable, oldValue, newValue) -> {
            if ("TCP CLIENT".equals(newValue)) {
                ipField.setVisible(true);
                ipField.setManaged(true);
                bindIpField.setVisible(false);
                bindIpField.setManaged(false);
            } else {
                ipField.setVisible(false);
                ipField.setManaged(false);
                bindIpField.setVisible(true);
                bindIpField.setManaged(true);
            }
        });
    }

    private final Function<Integer, UnaryOperator<TextFormatter.Change>> createFilter = (Integer max) -> {
        NumberFormat format = NumberFormat.getIntegerInstance();
        return c -> {
            if (c.isContentChange()) {
                ParsePosition parsePosition = new ParsePosition(0);
                // NumberFormat evaluates the beginning of the text
                Number number = format.parse(c.getControlNewText(), parsePosition);
                if (parsePosition.getIndex() == 0 ||
                        parsePosition.getIndex() < c.getControlNewText().length()) {
                    // reject parsing the complete text failed
                    return null;
                }
                if (number.longValue() > max) {
                    return null;
                }
            }
            return c;
        };
    };


//    @SneakyThrows
//    private void openBrowserDir(File file) {
//        final String command = "explorer.exe /SELECT,\"" + file.getAbsolutePath() + "\"";
//        Runtime.getRuntime().exec(command);
//    }

}