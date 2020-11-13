/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.shellfire.vpn.gui.controller;

import de.shellfire.vpn.Util;
import de.shellfire.vpn.gui.LoginForms;
import de.shellfire.vpn.gui.model.AttributeContainer;
import de.shellfire.vpn.gui.model.AttributeElement;
import de.shellfire.vpn.gui.model.VpnComparisonFXHelper;
import de.shellfire.vpn.gui.model.VpnComparisonFXTableModel;
import de.shellfire.vpn.i18n.VpnI18N;
import de.shellfire.vpn.webservice.WebService;
import de.shellfire.vpn.webservice.model.VpnEntry;
import de.shellfire.vpn.webservice.model.VpnStar;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.xnap.commons.i18n.I18n;

/**
 *
 * @author Tcheutchoua Steve
 */
public class PremiumScreenController implements Initializable {

  @FXML
  private Button cancelButton;
  @FXML
  private TableView<VpnComparisonFXTableModel> comparisonTableView;
  @FXML
  private TableColumn<VpnComparisonFXTableModel, String> connectionColumn;
  @FXML
  private TableColumn<VpnComparisonFXTableModel, VpnEntry> freeColumn;
  @FXML
  private TableColumn<VpnComparisonFXTableModel, VpnEntry> premiumColumn;
  @FXML
  private TableColumn<VpnComparisonFXTableModel, VpnEntry> premiumPlusColumn;
  @FXML
  private ImageView shellfireImageView;
  @FXML
  private Label upgradeLabel;
  @FXML
  private ScrollPane navigationPane;
  @FXML
  private Button buyPremiumButton;
  @FXML
  private Label remainingTimeLabel;
  @FXML
  private Label remainingTimeValue;

  private static final I18n i18N = VpnI18N.getI18n();
  private static Logger log = Util.getLogger(PremiumScreenController.class.getCanonicalName());
  private LoginForms application;
  private WebService shellfireService = null;
  private ObservableList<VpnComparisonFXTableModel> comparisonListData = FXCollections.observableArrayList();

  public PremiumScreenController() {
    // initComparisonTable();
  }

  void setDelay(int i) {
    String text = i + "s";
    this.remainingTimeValue.setText(text);
  }

  @FXML
  private void handleBuyPremiumNow(ActionEvent event) {
    WebService service = WebService.getInstance();
    Util.openUrl(service.getUrlPremiumInfo());
  }

  @FXML
  private void handleCalcelButton(ActionEvent event) {
    // get a handle to the stage
    Stage stage = (Stage) this.cancelButton.getScene().getWindow();
    // do what you have to do
    stage.hide();
    setNormalCursor();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.connectionColumn.setText(i18N.tr("Connection "));
    this.freeColumn.setText(i18N.tr("Free "));
    this.premiumColumn.setText(i18N.tr("Premium "));
    this.premiumPlusColumn.setText(i18N.tr("Premium Plus "));
    this.upgradeLabel.setText(i18N.tr("Upgrage to Shellfire VPN now"));
    this.buyPremiumButton.setText(i18N.tr("Buy premium now"));
    this.remainingTimeLabel.setText(i18N.tr("Remaining waiting time"));
    this.cancelButton.setText(i18N.tr("Cancel"));

    connectionColumn.setCellValueFactory(cellData -> cellData.getValue().connectionProperty());
    freeColumn.setCellValueFactory(cellData -> cellData.getValue().freeProperty());
    premiumColumn.setCellValueFactory(cellData -> cellData.getValue().premiumProperty());
    premiumPlusColumn.setCellValueFactory(cellData -> cellData.getValue().premiumPlusProperty());

    HashMap<Integer, Image> icons = new HashMap<Integer, Image>();
    HashMap<Integer, Image> iconsSelected = new HashMap<Integer, Image>();
    HashMap<Integer, Image> iconsDisabled = new HashMap<Integer, Image>();

    String baseUrl = "src/main/resources";

    Image iconTrue = Util.getImageIconFX(baseUrl + "/icons/yes.png");
    Image iconFalse = Util.getImageIconFX(baseUrl + "/icons/no.png");

    icons.put(1, Util.getImageIconFX(baseUrl + "/icons/stars/1star.png"));
    icons.put(2, Util.getImageIconFX(baseUrl + "/icons/stars/2star.png"));
    icons.put(3, Util.getImageIconFX(baseUrl + "/icons/stars/3star.png"));
    icons.put(4, Util.getImageIconFX(baseUrl + "/icons/stars/4star.png"));
    icons.put(5, Util.getImageIconFX(baseUrl + "/icons/stars/5star.png"));

    iconsSelected.put(1, Util.getImageIconFX(baseUrl + "/icons/stars/1star_selected.png"));
    iconsSelected.put(2, Util.getImageIconFX(baseUrl + "/icons/stars/2star_selected.png"));
    iconsSelected.put(3, Util.getImageIconFX(baseUrl + "/icons/stars/3star_selected.png"));
    iconsSelected.put(4, Util.getImageIconFX(baseUrl + "/icons/stars/4star_selected.png"));
    iconsSelected.put(5, Util.getImageIconFX(baseUrl + "/icons/stars/5star_selected.png"));

    iconsDisabled.put(1, Util.getImageIconFX(baseUrl + "/icons/stars/1star_disabled.png"));
    iconsDisabled.put(2, Util.getImageIconFX(baseUrl + "/icons/stars/2star_disabled.png"));
    iconsDisabled.put(3, Util.getImageIconFX(baseUrl + "/icons/stars/3star_disabled.png"));
    iconsDisabled.put(4, Util.getImageIconFX(baseUrl + "/icons/stars/4star_disabled.png"));
    iconsDisabled.put(5, Util.getImageIconFX(baseUrl + "/icons/stars/5star_disabled.png"));

    connectionColumn.setCellFactory(column -> {
      // Set up the Table
      return new TableCell<VpnComparisonFXTableModel, String>() {

        @Override
        protected void updateItem(String item, boolean empty) {
          super.updateItem(item, empty); // To change body of generated methods, choose Tools | Templates.
          if (item == null) {

            log.debug("PremiumScreenController: Connection View could not be rendered");
            setText("Empty");
          } else {
            Label lbl = new Label();
            log.debug("PremiumScreenController: Connection View rendered properly");
            VpnComparisonFXTableModel modelObject = getTableView().getItems().get(getIndex());
            if (modelObject.getIsContainer()) {
              setText(item);
              setTextFill(Color.WHITE);
              setStyle("-fx-background-color: blue");
            } else if (!modelObject.getIsContainer()) {
              setText(item);
            } else {
              setText("Null " + item);
            }

          }
        }

        public Image getIcon(VpnStar star, boolean isSelected) {
          if (isSelected) {
            return iconsSelected.get(star.getNum());
          } // else if (!isDisabled())
          else if (isDisabled() == false) {
            return icons.get(star.getNum());
          } else {
            return iconsDisabled.get(star.getNum());
          }
        }
      };
    });

    freeColumn.setCellFactory(column -> {
      return new TableCell<VpnComparisonFXTableModel, VpnEntry>() {
        public Image getIconBool(Boolean bool) {
          if (bool) {
            return iconTrue;
          } else {
            return iconFalse;
          }
        }

        public Image getIconStar(VpnStar star, boolean isSelected) {
          if (isSelected) {
            return iconsSelected.get(star.getNum());
          } // else if (!isDisabled())
          else if (!isDisabled() == false) {
            return icons.get(star.getNum());
          } else {
            return iconsDisabled.get(star.getNum());
          }
        }

        @Override
        protected void updateItem(VpnEntry item, boolean empty) {
          super.updateItem(item, empty); // To change body of generated methods, choose Tools | Templates.
          if (item == null) {

            log.debug("PremiumScreenController: free View could not be rendered");
            setText("Empty");
          } else {
            VpnComparisonFXTableModel modelObject = getTableView().getItems().get(getIndex());
            if (!modelObject.getIsContainer()) {
              if (item.isBoolEntry()) {
                log.debug("\n Boolean Entry in freecolumn\n");
                Boolean b = item.isBool();
                Image icon = this.getIconBool(b);

                setGraphic(new ImageView(icon));
                getGraphic().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                setTextAlignment(TextAlignment.CENTER);
              } else if (item.isStarEntry()) {
                log.debug("\n Star Entry in freecolumn\n");
                VpnStar star = new VpnStar(item.getStar());
                setText(star.getText());
                Image img = this.getIconStar(star, isSelected());
                setGraphic(new ImageView(img));
                getGraphic().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

                log.debug("StarImageRendererFX: " + star.getNum());
                setText(item.getText());
                setTextAlignment(TextAlignment.CENTER);
              } else {
                log.debug("\n String Entry in freecolumn\n");
                setText(item.getText());
              }
            } else if (modelObject.getIsContainer()) {
              setText(item.getText());
            } else {
              setText(" NULL" + item.getText());
            }
          }
        }
      };

    });

    premiumColumn.setCellFactory(column -> {
      return new TableCell<VpnComparisonFXTableModel, VpnEntry>() {
        public Image getIconBool(Boolean bool) {
          if (bool) {
            return iconTrue;
          } else {
            return iconFalse;
          }
        }

        public Image getIconStar(VpnStar star, boolean isSelected) {
          if (isSelected) {
            return iconsSelected.get(star.getNum());
          } // else if (!isDisabled())
          else if (isDisabled() == false) {
            return icons.get(star.getNum());
          } else {
            return iconsDisabled.get(star.getNum());
          }
        }

        @Override
        protected void updateItem(VpnEntry item, boolean empty) {
          super.updateItem(item, empty); // To change body of generated methods, choose Tools | Templates.
          if (item == null) {

            log.debug("PremiumScreenController: free View could not be rendered");
            setText("Empty");
          } else {

            if (item.isBoolEntry()) {
              log.debug("\n Boolean Entry in freecolumn\n");
              Boolean b = item.isBool();
              Image icon = this.getIconBool(b);

              setGraphic(new ImageView(icon));
              setTextAlignment(TextAlignment.CENTER);
            } else if (item.isStarEntry()) {
              log.debug("\n Star Entry in freecolumn\n");
              VpnStar star = new VpnStar(item.getStar());
              setText(star.getText());
              Image img = this.getIconStar(star, isSelected());
              setGraphic(new ImageView(img));
              getGraphic().setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
              log.debug("StarImageRendererFX: " + star.getNum());
              setText(item.getText());
            } else {
              log.debug("\n String Entry in freecolumn\n");
              setText(item.getText());
            }
          }

        }
      };

    });

  }

  public void disableTimer() {
    this.remainingTimeLabel.setText("");
    this.remainingTimeValue.setText("");
  }

  private void setNormalCursor() {
    this.application.getStage().getScene().setCursor(Cursor.DEFAULT);
    disableTimer();
  }

  public void initComparisonTable() {
    LinkedList<VpnComparisonFXTableModel> comparisonData = new LinkedList<>();
    VpnComparisonFXHelper comparisonHelper = new VpnComparisonFXHelper();
    comparisonHelper.setService(shellfireService);
    comparisonHelper.initData();
    LinkedList<AttributeContainer> containers = comparisonHelper.getAttributeContainers();
    for (int i = 0; i < containers.size(); i++) {
      // Add the headerAttribute Element to the comparison data
      comparisonData.add(new VpnComparisonFXTableModel(containers.get(i).getContainerName().toString(), null, null, null, true));
      LinkedList<AttributeElement> elements = containers.get(i).getElements();
      for (int j = 0; j < elements.size(); j++) {
        VpnComparisonFXTableModel newModel = new VpnComparisonFXTableModel(elements.get(j).getName(), elements.get(j).getFree(),
            elements.get(j).getPremium(), elements.get(j).getPremiumPlus(), false);
        comparisonData.add(newModel);
        log.debug("Elements of " + containers.get(i).getContainerName() + " are " + elements.get(j).getName() + ", "
            + elements.get(j).getFree() + "elements size is " + elements.size());
      }

    }
    this.comparisonListData.addAll(comparisonData);
    this.comparisonTableView.setItems(comparisonListData);
  }

  public void setApp(LoginForms applic) {
    this.application = applic;
  }

  public void setService(WebService service) {
    this.shellfireService = service;
  }
}
