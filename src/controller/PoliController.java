package controller;

import dao.PoliDAO;
import model.Poli;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

public class PoliController {
    @FXML private TableView<Poli> tablePoli;
    @FXML private TableColumn<Poli, Integer> colId;
    @FXML private TableColumn<Poli, String> colNama, colKeterangan;
    @FXML private TextField txtNamaPoli;
    @FXML private TextArea txtKeterangan;

    private PoliDAO poliDAO = new PoliDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPoli"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaPoli"));
        colKeterangan.setCellValueFactory(new PropertyValueFactory<>("keterangan"));
        loadData();
    }

    private void loadData() {
        ObservableList<Poli> list = poliDAO.getAllPoli();
        tablePoli.setItems(list);
    }

    @FXML
    private void handleSimpan() {
        try {
            Poli p = new Poli();
            p.setNamaPoli(txtNamaPoli.getText());
            p.setKeterangan(txtKeterangan.getText());
            
            poliDAO.insert(p);
            loadData(); // Refresh tabel
            
            // Bersihkan form
            txtNamaPoli.clear();
            txtKeterangan.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKembali(javafx.event.ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }
}