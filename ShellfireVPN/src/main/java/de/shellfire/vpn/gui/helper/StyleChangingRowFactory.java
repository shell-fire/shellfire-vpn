package de.shellfire.vpn.gui.helper;

import java.util.Collections;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;


public class StyleChangingRowFactory<T> implements
        Callback<TableView<T>, TableRow<T>> {

    private final String styleClass ;
    private final ObservableList<Integer> styledRowIndices ;
    private final Callback<TableView<T>, TableRow<T>> baseFactory ;

    public StyleChangingRowFactory(String styleClass, Callback<TableView<T>, TableRow<T>> baseFactory) {
        this.styleClass = styleClass ;
        this.baseFactory = baseFactory ;
        this.styledRowIndices = FXCollections.observableArrayList();
    }

    public StyleChangingRowFactory(String styleClass) {
        this(styleClass, null);
    }

    @Override
    public TableRow<T> call(TableView<T> tableView) {

        final TableRow<T> row ;
        if (baseFactory == null) {
            row = new TableRow<>();
        } else {
            row = baseFactory.call(tableView);
        }

        row.indexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> obs,
                    Number oldValue, Number newValue) {
                updateStyleClass(row);
            }
        });

        styledRowIndices.addListener(new ListChangeListener<Integer>() {

            @Override
            public void onChanged(Change<? extends Integer> change) {
                updateStyleClass(row);
            }
        });

        return row;
    }

    public ObservableList<Integer> getStyledRowIndices() {
        return styledRowIndices ;
    }

    private void updateStyleClass(TableRow<T> row) {
        final ObservableList<String> rowStyleClasses = row.getStyleClass();
        if (styledRowIndices.contains(row.getIndex()) ) {
            if (! rowStyleClasses.contains(styleClass)) {
                rowStyleClasses.add(styleClass);
            }
        } else {
            // remove all occurrences of styleClass:
            rowStyleClasses.removeAll(Collections.singleton(styleClass));
        }
    }

}