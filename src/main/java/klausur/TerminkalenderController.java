package klausur;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * eine Terminkalender-Anwendung
 * @author Doro
 *
 */
public class TerminkalenderController extends Application implements PropertyChangeListener {
	@FXML private VBox lstTermine;
	@FXML private ChoiceBox<Kategorie> chKategorie;
	@FXML private TextField txtAktuell;
	@FXML private Label lblMeldung;
	@FXML private TextField txtStundeVon;
	@FXML private TextField txtMinuteVon;
	private Stage stage;
	private Terminkalender kalender;
	
	/**
	 * erstellt einen neuen TerminkalenderController f�r
	 * einen neuen Terminkalender
	 */
	public TerminkalenderController()
	{
		kalender = einlesen();
		kalender.subscribe(this);
	}

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerminUebersicht.fxml"));
		loader.setController(this);
		Parent parent = loader.load();
		Scene scene = new Scene(parent, 500, 500);
		stage.setScene(scene);
		stage.show();
	}

	@FXML
	private void initialize()
	{
		kategorieFuellen(new Kategorie[]{Kategorie.PRIVAT, Kategorie.ARBEIT, Kategorie.LEHRE});
	    txtAktuell.textProperty().bind(kalender.aktuellerTerminProperty().asString());
		stage.setOnCloseRequest((e) -> {
		    	try{
		    		kalender.speichern("kalender.dat");
		    	}catch(Exception ex) {}
		    	System.exit(0);
	    	}
	    	);	
	}
	
	/**
	 * erstellt ein Objekt mit dem heutigen Datum und der angegebenen Uhrzeit
	 * 	stunde:minute:00
	 * @param stunde
	 * @param minute
	 * @return
	 * @throws NumberFormatException, wenn stunde oder minute keine
	 * 								  ganzen Zahlen darstellen
	 * @throws DateTimeException, wenn stunde oder minute aus�erhalb
	 * 							  des f�r Stunden bzw. Minuten g�ltigen
	 * 							  Bereichs liegen
	 */
	private LocalDateTime umwandeln(String stunde, String minute)
	{

		LocalTime zeit = LocalTime.of(Integer.parseInt(stunde), Integer.parseInt(minute));
		return LocalDateTime.of(LocalDate.now(), zeit);
	}
	
	/**
	 * l�scht alle Elemente aus lstTermine
	 */
	private void alleLoeschen()
	{
		Platform.runLater(() ->
		{
			lstTermine.getChildren().retainAll();
		});
	}
	
	/**
	 * f�gt ein neues Label zu lstTermine hinzu, das den �bergebenen Termin t darstellt
	 * @param t
	 */
	private void terminEinfuegen(Termin t)
	{
		Platform.runLater(() ->
		{
			Label l = new Label(t.toString());
			lstTermine.getChildren().add(l);
			
			//Farben bestimmen:
			Color vordergrundfarbe = t.getKategorie().getColor();
			Color hintergrundfarbe = t.getKategorie().getInverseFarbe();
			
			l.setTextFill(vordergrundfarbe);
			BackgroundFill bf = new BackgroundFill(hintergrundfarbe,
			CornerRadii.EMPTY , Insets.EMPTY);
			Background bg = new Background(bf);
			l.setBackground(bg);
		});
	}
	
	/**
	 * f�llt chKategorie mit allen �bergebenen Kategorien
	 * @param alle Liste der Kategorien, die in chKategorie angezeigt werden sollen
	 */
	private void kategorieFuellen(Kategorie[] alle)
	{
		ObservableList<Kategorie> obs;
		obs = FXCollections.observableArrayList(alle);
		chKategorie.setItems(obs);
		chKategorie.getSelectionModel().selectFirst();
	}
	
	/**
	 * wird beim Klick auf den Button "neuer Termin" aufgerufen
	 */
	@FXML
	private void neuerTermin()
	{
		try {
			LocalDateTime beginn = umwandeln(txtStundeVon.getText(), txtMinuteVon.getText());
			if(!kalender.hinzufuegen(new Termin(beginn, chKategorie.getValue())))
				throw new RuntimeException();
		} catch (Exception e){
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setContentText("Beim Erstellen des Termins ist ein Fehler aufgetreten :(. Nicht meine schuld...");
			alert.show();

		}
	}
	
	/**
	 * versucht einen in der Datei kalender.dat gespeicherten Terminkalender 
	 * einzulesen. Gelingt dies nicht, wird ein neue Terminkalender angelegt
	 * @return aus der Datei gelesener oder neue Terminkalender
	 */
	private Terminkalender einlesen()
	{
		try (FileInputStream datei = new FileInputStream("kalender.dat");
		ObjectInputStream oIn = new ObjectInputStream(datei);)
		{

			return (Terminkalender) oIn.readObject();
		}
		catch (Exception e)
		{
			return new Terminkalender();
		}
	}

	/**
	 * Wir benachrichtigt wenn sich die Terminliste ändert...
	 * @param propertyChangeEvent
	 */
	@Override
	public void propertyChange(PropertyChangeEvent propertyChangeEvent) {;
			alleLoeschen();
			kalender.getTerminliste().forEach(this::terminEinfuegen);
	}
}
