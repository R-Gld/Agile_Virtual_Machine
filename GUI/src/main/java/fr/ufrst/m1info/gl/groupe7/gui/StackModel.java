package fr.ufrst.m1info.gl.groupe7.gui;

import fr.ufrst.m1info.gl.groupe7.memoire.Stacks;
import javafx.beans.property.*;

public class StackModel {

    public StackModel(Stacks.Quad quad, int address) {
        setIdent(quad.ident);
        setType(quad.type.toString());
        setAddress(address);
        setValue(quad.value);
        setObject(quad.object);
    }

    // Address
    private IntegerProperty address;
    public void setAddress(int address) {addressProperty().set(address);}
    public int getAddress() {return addressProperty().get();}
    public IntegerProperty addressProperty() {
        if (address == null) {
            address = new SimpleIntegerProperty(this, "address", 0);
        }
        return address;
    }

    // Ident
    private StringProperty ident;
    public void setIdent(String ident) {this.identProperty().set(ident);}
    public String getIdent() {return identProperty().get();}
    public StringProperty identProperty() {
        if (ident == null) {
            ident = new SimpleStringProperty(this, "ident");
        }
        return ident;
    }

    // Value
    private ObjectProperty<Object> value;
    public void setValue(Object value) {this.valueProperty().set(value);}
    public Object getValue() {return valueProperty().get();}
    public ObjectProperty<Object> valueProperty() {
        if (value == null) {
            value = new SimpleObjectProperty<Object>(this, "value");
        }
        return value;
    }

    // Object
    private StringProperty object;
    public void setObject(String object) {this.objectProperty().set(object);}
    public String getObject() {return objectProperty().get();}
    public StringProperty  objectProperty() {
        if (object == null) {
            object = new SimpleStringProperty(this, "object");
        }
        return object;
    }

    // Type
    private  StringProperty type;
    public void setType(String type) {this.typeProperty().set(type);}
    public String getType() {return typeProperty().get();}
    public StringProperty typeProperty() {
        if (type == null) {
            type = new SimpleStringProperty(this, "type");
        }
        return type;
    }
}
