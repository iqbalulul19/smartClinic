package controller;

/*import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Pasien;
import service.PasienService;
import util.AlertUtil;
import util.ValidationUtil;

public class FormPasienController {

    @FXML
    private TextField txtId,
            txtNama,
            txtUmur,
            txtAlamat,
            txtHP,
            txtTekanan,
            txtGula;

    @FXML
    private ComboBox<String> cbGender;

    private PasienService pasienService =new PasienService();
    private boolean modeEdit = false;

    @FXML
    public void initialize(){
        cbGender.getItems().addAll("Laki-laki","Perempuan");
    }

    public void setModeTambah(){
        modeEdit = false;
    }

    public void setModeEdit(Pasien p){
        modeEdit = true;
        txtId.setText(String.valueOf(p.getIdPasien()));
        txtNama.setText(p.getNama());
        txtUmur.setText(String.valueOf(p.getUmur()));
        cbGender.setValue(p.getGender());
        txtAlamat.setText(p.getAlamat());
        txtHP.setText(p.getNoHp());
        txtTekanan.setText( String.valueOf( p.getTekananDarah()));
        txtGula.setText( String.valueOf( p.getGulaDarah()));
    }
    @FXML
    public void handleSimpan(){
        try{
                // =====================
                // VALIDASI
                // =====================
                if(ValidationUtil.isEmpty(txtNama,"Nama wajib diisi")){
                   return;
                }
                if(ValidationUtil.isEmpty(txtUmur,"Umur wajib diisi")){
                   return;
                }
                if(ValidationUtil.isEmpty(cbGender,"Pilih gender")){
                  return;
                }

                // =====================
                // OBJECT
                // =====================

                Pasien p =new Pasien(modeEdit? Integer.parseInt(txtId.getText()): 0,
                        txtNama.getText(),
                        Integer.parseInt(txtUmur.getText()),
                        cbGender.getValue(),
                        txtAlamat.getText(),
                        txtHP.getText(),
                        Double.parseDouble(txtTekanan.getText()),
                        Double.parseDouble(txtGula.getText())
                );

                // =====================
                // SERVICE
                // =====================
                pasienService.simpan(p,modeEdit);
                AlertUtil.success(modeEdit? "Data berhasil diupdate": "Data berhasil disimpan");
                closeWindow();

        }catch(Exception e){
                AlertUtil.error(e.getMessage());
                e.printStackTrace();
        }
    }

    @FXML
    public void handleBatal(){

        closeWindow();
    }

    private void closeWindow(){

        Stage stage =
                (Stage) txtNama
                .getScene()
                .getWindow();

        stage.close();
    }
}*/