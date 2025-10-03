module de.derfilli.photography.inverso {
  requires javafx.controls;
  requires javafx.fxml;
  requires java.management;
  requires org.controlsfx.controls;
  requires java.xml;
  requires java.desktop;
  requires java.prefs;
  requires org.jetbrains.annotations;

  opens de.derfilli.photography.inverso to javafx.fxml;
  exports de.derfilli.photography.inverso;
  exports de.derfilli.photography.inverso.settings;
  opens de.derfilli.photography.inverso.settings to javafx.fxml;
}
