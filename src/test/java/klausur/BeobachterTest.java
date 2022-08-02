package klausur;

import klausur.Kategorie;
import klausur.Termin;
import klausur.Terminkalender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BeobachterTest {
    Terminkalender terminkalender;
    PropertyChangeListener listener;
    @BeforeEach
    public void init(){
        terminkalender = new Terminkalender();
        listener = Mockito.mock(PropertyChangeListener.class);
        terminkalender.subscribe(listener);
    }
    @Test
    public void beobachterTest(){
        LocalTime time = LocalTime.of(22, 40);
        terminkalender.hinzufuegen(new Termin(LocalDateTime.of(LocalDate.now(), time), Kategorie.ARBEIT));
        Mockito.verify(listener).propertyChange(ArgumentMatchers.any());
    }
}
